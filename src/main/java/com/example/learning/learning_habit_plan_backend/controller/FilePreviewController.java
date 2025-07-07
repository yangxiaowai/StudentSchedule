package com.example.learning.learning_habit_plan_backend.controller;

import com.example.learning.learning_habit_plan_backend.dto.FilePreviewResponse;
import com.example.learning.learning_habit_plan_backend.entity.LearningMaterial;
import com.example.learning.learning_habit_plan_backend.repository.LearningMaterialRepository;
import com.example.learning.learning_habit_plan_backend.service.FilePreviewService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/preview")
public class FilePreviewController {

    private final FilePreviewService filePreviewService;
    private final LearningMaterialRepository materialRepository;

    public FilePreviewController(FilePreviewService filePreviewService, LearningMaterialRepository materialRepository) {
        this.filePreviewService = filePreviewService;
        this.materialRepository = materialRepository;
    }

    @GetMapping("/{fileName}")
    public ResponseEntity<FilePreviewResponse> previewFile(@PathVariable String fileName) {
        FilePreviewResponse response = filePreviewService.previewFile(fileName);

        if (response.getError() != null) {
            return ResponseEntity.badRequest().body(response);
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/file/{id}")
    public ResponseEntity<FilePreviewResponse> previewFileById(@PathVariable Long id) {
        try {
            // 根据ID查找文件
            LearningMaterial material = materialRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("文件不存在"));
            
            // 从文件路径中提取UUID格式的文件名
            String actualFileName = Paths.get(material.getFilePath()).getFileName().toString();
            
            // 调用预览服务
            FilePreviewResponse response = filePreviewService.previewFile(actualFileName);
            
            // 设置文件类型信息
            if (response.getFileType() == null || response.getFileType().isEmpty()) {
                String fileExtension = getFileExtension(material.getFileName());
                response.setFileType(fileExtension);
            }
            
            if (response.getError() != null) {
                return ResponseEntity.badRequest().body(response);
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            FilePreviewResponse errorResponse = FilePreviewResponse.error("", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        return lastDotIndex == -1 ? "" : fileName.substring(lastDotIndex + 1);
    }

}
