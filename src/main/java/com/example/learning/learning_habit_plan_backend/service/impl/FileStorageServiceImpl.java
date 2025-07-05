package com.example.learning.learning_habit_plan_backend.service.impl;

import com.example.learning.learning_habit_plan_backend.dto.FileUploadResponse;
import com.example.learning.learning_habit_plan_backend.entity.LearningMaterial;
import com.example.learning.learning_habit_plan_backend.repository.LearningMaterialRepository;
import com.example.learning.learning_habit_plan_backend.service.FileStorageService;
import com.example.learning.learning_habit_plan_backend.utils.JwtUtil;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import com.example.learning.learning_habit_plan_backend.util.VideoFileValidator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Autowired
    private JwtUtil jwtUtil;

    private final LearningMaterialRepository materialRepository;

    public FileStorageServiceImpl(LearningMaterialRepository materialRepository) {
        this.materialRepository = materialRepository;
    }

    @Override
    // ... existing code ...
    public FileUploadResponse storeFile(MultipartFile file, String subject, String type) {
        try {
            // 基本参数验证
            if (file == null || file.isEmpty()) {
                throw new RuntimeException("上传文件不能为空");
            }

            if (subject == null || subject.trim().isEmpty()) {
                throw new RuntimeException("学科分类不能为空");
            }

            if (type == null || type.trim().isEmpty()) {
                throw new RuntimeException("内容类型不能为空");
            }

            // 添加详细日志输出
            System.out.println("开始处理文件上传请求 - 文件名: " + file.getOriginalFilename() + ", 学科: " + subject + ", 类型: " + type);
            
            // 获取原始文件名并进行基本验证
            String originalFileName = file.getOriginalFilename();
            if (originalFileName == null || originalFileName.isEmpty()) {
                throw new RuntimeException("文件名不能为空");
            }

            // 如果是视频文件，进行特殊验证
            if (VideoFileValidator.isVideoFile(originalFileName)) {
                VideoFileValidator.ValidationResult validationResult = VideoFileValidator.validateVideoFile(file);
                if (!validationResult.isValid()) {
                    System.out.println("视频文件验证失败: " + validationResult.getMessage());
                    throw new RuntimeException("视频文件验证失败: " + validationResult.getMessage());
                }
                System.out.println("视频文件验证通过: " + validationResult.getMessage());
            }

            // 从请求头获取JWT令牌
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            String token = request.getHeader("Authorization");
            
            // 记录令牌获取情况
            if (token == null) {
                System.out.println("警告: 请求头中未找到Authorization令牌");
            } else {
                // 避免打印完整令牌
                String tokenPreview = token.length() > 20 ? token.substring(0, 20) + "..." : token;
                System.out.println("获取到Authorization令牌: " + tokenPreview);
            }
            
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            // 添加令牌有效性检查
            if (token == null || token.isEmpty()) {
                System.out.println("错误: 未提供有效的认证令牌");
                throw new RuntimeException("未提供有效的认证令牌");
            }
            
            Long userId;
            try {
                // 从令牌中获取用户ID
                userId = jwtUtil.getUserIdFromToken(token);
            } catch (ExpiredJwtException e) {
                System.out.println("错误: 令牌已过期 - " + e.getMessage());
                throw new RuntimeException("令牌已过期，请重新登录", e);
            } catch (JwtException | IllegalArgumentException e) {
                System.out.println("错误: 无效的认证令牌 - " + e.getMessage());
                throw new RuntimeException("无效的认证令牌", e);
            }
            
            // 文件大小验证（通用限制：100MB，视频文件在VideoFileValidator中有单独限制）
            long maxFileSize = 100 * 1024 * 1024; // 100MB
            if (file.getSize() > maxFileSize) {
                throw new RuntimeException("文件大小超过限制（最大100MB）");
            }

            // 确保上传目录存在
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 生成唯一文件名 - 安全的文件名处理，移除路径分隔符
            originalFileName = originalFileName.replaceAll("[/\\\\]", "_");

            // 检查文件是否有扩展名
            if (!originalFileName.contains(".")) {
                throw new RuntimeException("文件必须包含扩展名");
            }

            String fileExtension = originalFileName.substring(originalFileName.lastIndexOf(".")).toLowerCase();
            String uniqueFileName = UUID.randomUUID() + fileExtension;

            // 保存文件
            Path filePath = uploadPath.resolve(uniqueFileName);

            // 确保文件路径安全
            if (!filePath.startsWith(uploadPath)) {
                throw new RuntimeException("非法的文件路径");
            }

            Files.copy(file.getInputStream(), filePath);

            // 保存到数据库
            LearningMaterial material = new LearningMaterial();
            material.setFileName(originalFileName);
            material.setFilePath(filePath.toString());
            material.setFileType(file.getContentType());
            material.setFileSize(file.getSize());
            material.setSubject(subject);
            material.setContentType(type);
            material.setUserId(userId);

            LearningMaterial savedMaterial = materialRepository.save(material);

            // 验证保存结果
            if (savedMaterial == null) {
                throw new RuntimeException("数据库保存失败，返回结果为空");
            }


            // 返回响应
            System.out.println("文件上传成功 - ID: " + savedMaterial.getId() + ", 文件名: " + originalFileName);

            // 安全获取上传时间
            String uploadTimeStr = savedMaterial.getUploadTime() != null ?
                savedMaterial.getUploadTime().toString() :
                java.time.LocalDateTime.now().toString();

            return new FileUploadResponse(
                    savedMaterial.getId(),
                    originalFileName,
                    "/api/files/download?fileName=" + uniqueFileName,
                    file.getContentType(),
                    file.getSize(),
                    subject,
                    type,
                    uploadTimeStr
            );
        } catch (IOException e) {
            System.err.println("文件IO操作失败: " + e.getMessage());
            throw new RuntimeException("文件保存失败，请检查磁盘空间和权限: " + e.getMessage(), e);
        } catch (RuntimeException e) {
            System.err.println("运行时异常: " + e.getMessage());
            throw e; // 重新抛出运行时异常，保持原有的错误信息
        } catch (Exception e) {
            System.err.println("未知异常: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("文件存储过程中发生未知错误: " + e.getMessage(), e);
        }
    }
        


    @Override
    public List<FileUploadResponse> getAllFiles() {
        try {
            // 从请求头获取JWT令牌
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            String token = request.getHeader("Authorization");
            
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            if (token == null || token.isEmpty()) {
                throw new RuntimeException("未提供有效的认证令牌");
            }
            
            Long userId;
            try {
                userId = jwtUtil.getUserIdFromToken(token);
            } catch (ExpiredJwtException e) {
                throw new RuntimeException("令牌已过期，请重新登录", e);
            } catch (JwtException | IllegalArgumentException e) {
                throw new RuntimeException("无效的认证令牌", e);
            }
            
            List<LearningMaterial> materials = materialRepository.findByUserId(userId);
            return materials.stream().map(material -> {
                    // 从文件路径中提取实际的文件名（UUID格式）
                    String actualFileName = Paths.get(material.getFilePath()).getFileName().toString();
                    return new FileUploadResponse(
                            material.getId(),
                            material.getFileName(),
                            "/api/files/download?fileName=" + actualFileName,
                            material.getFileType(),
                            material.getFileSize(),
                            material.getSubject(),
                            material.getContentType(),
                            material.getUploadTime().toString()
                    );
            }).toList();
        } catch (Exception e) {
            throw new RuntimeException("获取文件列表失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteFile(String fileName) {
        try {
            // 从请求头获取JWT令牌
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            String token = request.getHeader("Authorization");
            
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            if (token == null || token.isEmpty()) {
                throw new RuntimeException("未提供有效的认证令牌");
            }
            
            Long userId;
            try {
                userId = jwtUtil.getUserIdFromToken(token);
            } catch (ExpiredJwtException e) {
                throw new RuntimeException("令牌已过期，请重新登录", e);
            } catch (JwtException | IllegalArgumentException e) {
                throw new RuntimeException("无效的认证令牌", e);
            }
            
            // 根据文件名查找文件记录（fileName实际上是UUID格式的文件名）
             List<LearningMaterial> materials = materialRepository.findByFilePathContaining(fileName);
             LearningMaterial material = materials.stream()
                     .filter(m -> m.getUserId().equals(userId))
                     .findFirst()
                     .orElseThrow(() -> new RuntimeException("文件不存在或无权限删除"));
            
            // 删除物理文件
            Path filePath = Paths.get(material.getFilePath());
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
            
            // 删除数据库记录
            materialRepository.delete(material);
        } catch (Exception e) {
            throw new RuntimeException("删除文件失败: " + e.getMessage(), e);
        }
    }

    @Override
    public byte[] loadFileAsResource(String fileName) {
        try {
            // 根据文件名查找文件
            List<LearningMaterial> materials = materialRepository.findByFilePathContaining(fileName);
            if (materials.isEmpty()) {
                throw new RuntimeException("文件不存在");
            }
            
            LearningMaterial material = materials.get(0);
            Path filePath = Paths.get(material.getFilePath());
            
            if (!Files.exists(filePath)) {
                throw new RuntimeException("文件不存在");
            }
            
            return Files.readAllBytes(filePath);
        } catch (Exception e) {
            throw new RuntimeException("加载文件失败: " + e.getMessage(), e);
        }
    }

    @Override
    public List<LearningMaterial> getMaterialsByUserIds(List<Long> userIds) {
        return materialRepository.findByUserIdIn(userIds);
    }

    @Override
    public List<LearningMaterial> getMaterialsByUserIdsAndSubject(List<Long> userIds, String subject) {
        return materialRepository.findByUserIdInAndSubject(userIds, subject);
    }

    @Override
    public List<FileUploadResponse> getFilesByUserId(Long userId) {
        try {
            List<LearningMaterial> materials = materialRepository.findByUserId(userId);
            return materials.stream().map(material -> {
                FileUploadResponse response = new FileUploadResponse();
                response.setId(material.getId());
                response.setFileName(material.getFileName());
                // 从文件路径中提取实际的UUID格式文件名
                String actualFileName = Paths.get(material.getFilePath()).getFileName().toString();
                response.setFileDownloadUri("/api/files/download?fileName=" + actualFileName);
                response.setFileType(material.getFileType());
                response.setSize(material.getFileSize());
                response.setSubject(material.getSubject());
                response.setContentType(material.getContentType());
                response.setUploadTime(material.getUploadTime().toString());
                return response;
            }).collect(java.util.stream.Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("获取用户文件失败: " + e.getMessage(), e);
        }
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("用户未认证");
        }

        // 获取JWT令牌
        String token = (String) authentication.getCredentials();
        if (token == null) {
            throw new RuntimeException("无法获取用户令牌");
        }

        // 从令牌中获取用户ID
        return jwtUtil.getUserIdFromToken(token);
    }
}
