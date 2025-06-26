package com.example.learning.learning_habit_plan_backend.service.impl;

import com.example.learning.learning_habit_plan_backend.dto.FileUploadResponse;
import com.example.learning.learning_habit_plan_backend.service.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public FileUploadResponse storeFile(MultipartFile file, String subject, String type) {
        try {
            // 创建上传目录
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);

            // 清理文件名
            String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));

            // 防止路径遍历攻击
            if(fileName.contains("..")) {
                throw new RuntimeException("文件名包含非法路径序列: " + fileName);
            }

            // 保存文件
            Path targetLocation = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // 构建响应
            FileUploadResponse response = new FileUploadResponse();
            response.setFileName(fileName);
            response.setFileType(type);
            response.setSubject(subject);
            response.setSize(file.getSize());
            response.setContentType(file.getContentType());
            response.setFileDownloadUri("/api/files/download/" + fileName);

            return response;
        } catch (IOException ex) {
            throw new RuntimeException("无法存储文件 " + file.getOriginalFilename() + ". 请重试!", ex);
        }
    }

    @Override
    public List<FileUploadResponse> getAllFiles() {
        List<FileUploadResponse> files = new ArrayList<>();
        try {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            if(Files.exists(uploadPath)) {
                Files.list(uploadPath).forEach(path -> {
                    FileUploadResponse file = new FileUploadResponse();
                    file.setFileName(path.getFileName().toString());
                    file.setFileDownloadUri("/api/files/download/" + path.getFileName());
                    try {
                        file.setSize(Files.size(path));
                    } catch (IOException e) {
                        throw new RuntimeException("无法获取文件大小", e);
                    }
                    files.add(file);
                });
            }
            return files;
        } catch (IOException ex) {
            throw new RuntimeException("无法读取文件列表", ex);
        }
    }

    @Override
    public void deleteFile(String fileName) {
        try {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path filePath = uploadPath.resolve(fileName).normalize();
            if(!filePath.startsWith(uploadPath)) {
                throw new RuntimeException("无法访问文件: " + fileName);
            }
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            throw new RuntimeException("无法删除文件 " + fileName, ex);
        }
    }

    @Override
    public byte[] loadFileAsResource(String fileName) {
        try {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path filePath = uploadPath.resolve(fileName).normalize();
            if(!filePath.startsWith(uploadPath)) {
                throw new RuntimeException("无法访问文件: " + fileName);
            }
            Resource resource = new UrlResource(filePath.toUri());
            if(resource.exists()) {
                return Files.readAllBytes(filePath);
            } else {
                throw new RuntimeException("文件未找到: " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new RuntimeException("文件未找到: " + fileName, ex);
        } catch (IOException ex) {
            throw new RuntimeException("无法读取文件: " + fileName, ex);
        }
    }
}
