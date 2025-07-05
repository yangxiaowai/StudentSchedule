package com.example.learning.learning_habit_plan_backend.service;

import com.example.learning.learning_habit_plan_backend.dto.FileUploadResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileStorageService {
    FileUploadResponse storeFile(MultipartFile file, String subject, String type);
    List<FileUploadResponse> getAllFiles();
    void deleteFile(String fileName);
    byte[] loadFileAsResource(String fileName);
}
