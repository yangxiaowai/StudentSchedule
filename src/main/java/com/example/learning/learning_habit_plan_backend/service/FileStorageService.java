package com.example.learning.learning_habit_plan_backend.service;

import com.example.learning.learning_habit_plan_backend.dto.FileUploadResponse;
import com.example.learning.learning_habit_plan_backend.entity.LearningMaterial;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileStorageService {
    FileUploadResponse storeFile(MultipartFile file, String subject, String type);
    List<FileUploadResponse> getAllFiles();
    void deleteFile(String fileName);
    byte[] loadFileAsResource(String fileName);
    
    // 根据用户ID列表获取资料（用于小组资料库共享）
    List<LearningMaterial> getMaterialsByUserIds(List<Long> userIds);
    
    // 根据用户ID列表和学科获取资料
    List<LearningMaterial> getMaterialsByUserIdsAndSubject(List<Long> userIds, String subject);
    
    // 根据用户ID获取文件（用于查看其他用户的资料）
    List<FileUploadResponse> getFilesByUserId(Long userId);
}
