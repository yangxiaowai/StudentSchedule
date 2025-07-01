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

import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

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
            // 添加详细日志输出
            System.out.println("开始处理文件上传请求 - 文件名: " + file.getOriginalFilename() + ", 学科: " + subject + ", 类型: " + type);
            
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
            
            // 确保上传目录存在
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 生成唯一文件名
            String originalFileName = file.getOriginalFilename();
            if (originalFileName == null || originalFileName.isEmpty()) {
                throw new RuntimeException("文件名不能为空");
            }
            
            String fileExtension = "";
            int lastDotIndex = originalFileName.lastIndexOf(".");
            if (lastDotIndex > 0) {
                fileExtension = originalFileName.substring(lastDotIndex);
            }
            String uniqueFileName = UUID.randomUUID() + fileExtension;

            // 保存文件
            Path filePath = uploadPath.resolve(uniqueFileName);
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

            // 返回响应
            System.out.println("文件上传成功 - ID: " + savedMaterial.getId() + ", 文件名: " + originalFileName);
            return new FileUploadResponse(
                    savedMaterial.getId(),
                    originalFileName,
                    "/api/files/download?fileName=" + uniqueFileName,
                    file.getContentType(),
                    file.getSize(),
                    subject,
                    type,
                    savedMaterial.getUploadTime().toString()
            );
        } catch (Exception e) {
            throw new RuntimeException("文件存储失败: " + e.getMessage(), e);
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
            // 使用更精确的查询方法，查找路径以fileName结尾的文件
            List<LearningMaterial> materials = materialRepository.findByFilePathEndingWith(fileName);
            
            // 打印调试信息
            System.out.println("删除文件: " + fileName);
            System.out.println("找到匹配的文件数量: " + materials.size());
            materials.forEach(m -> System.out.println("文件路径: " + m.getFilePath() + ", 用户ID: " + m.getUserId()));
            
            // 过滤出当前用户的文件
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
            
            // 根据文件名查找文件（使用更精确的查询方法）
            List<LearningMaterial> materials = materialRepository.findByFilePathEndingWith(fileName);
            if (materials.isEmpty()) {
                throw new RuntimeException("文件不存在");
            }
            
            // 过滤出当前用户的文件
            LearningMaterial material = materials.stream()
                    .filter(m -> m.getUserId().equals(userId))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("文件不存在或无权限访问"));
            
            Path filePath = Paths.get(material.getFilePath());
            
            if (!Files.exists(filePath)) {
                throw new RuntimeException("文件不存在");
            }
            
            // 打印调试信息
            System.out.println("加载文件: " + fileName);
            System.out.println("文件路径: " + filePath);
            
            return Files.readAllBytes(filePath);
        } catch (Exception e) {
            throw new RuntimeException("加载文件失败: " + e.getMessage(), e);
        }
    }


}
