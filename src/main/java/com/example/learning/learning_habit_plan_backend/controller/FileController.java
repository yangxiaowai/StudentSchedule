package com.example.learning.learning_habit_plan_backend.controller;

import com.example.learning.learning_habit_plan_backend.dto.FileUploadResponse;
import com.example.learning.learning_habit_plan_backend.model.ErrorResponse;
import com.example.learning.learning_habit_plan_backend.entity.LearningMaterial;
import com.example.learning.learning_habit_plan_backend.entity.User;
import com.example.learning.learning_habit_plan_backend.repository.LearningMaterialRepository;
import com.example.learning.learning_habit_plan_backend.service.FileStorageService;
import com.example.learning.learning_habit_plan_backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;
import java.util.List;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileStorageService fileStorageService;
    private final LearningMaterialRepository materialRepository;

    @Autowired
    private UserService userService;

    public FileController(FileStorageService fileStorageService, LearningMaterialRepository materialRepository) {
        this.fileStorageService = fileStorageService;
        this.materialRepository = materialRepository;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("subject") String subject,
            @RequestParam("type") String type) {
        try {
            FileUploadResponse response = fileStorageService.storeFile(file, subject, type);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }


    @GetMapping("/list")
    public List<FileUploadResponse> getFiles() {
        return fileStorageService.getAllFiles();
    }

    @DeleteMapping("/delete/{fileId}")
    public ResponseEntity<?> deleteFile(@PathVariable Long fileId) {
        try {
            // 根据ID查找文件
            LearningMaterial material = materialRepository.findById(fileId)
                    .orElseThrow(() -> new RuntimeException("文件不存在"));
            
            // 从文件路径中提取UUID格式的文件名
            String actualFileName = Paths.get(material.getFilePath()).getFileName().toString();
            
            // 删除文件（使用UUID格式的文件名）
            fileStorageService.deleteFile(actualFileName);
            return ResponseEntity.ok(Map.of("message", "文件删除成功"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadFile(@RequestParam String fileName) {
        byte[] fileContent = fileStorageService.loadFileAsResource(fileName);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(fileContent);
    }

    @GetMapping("/download/{fileName:.+}")
    public ResponseEntity<byte[]> downloadFileByPath(@PathVariable String fileName) {
        try {
            byte[] fileContent = fileStorageService.loadFileAsResource(fileName);
            
            // 根据文件扩展名设置正确的Content-Type
            String contentType = getContentType(fileName);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                    .body(fileContent);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(null);
        }
    }

    @GetMapping("/preview/{fileName:.+}")
    public ResponseEntity<?> previewFile(@PathVariable String fileName) {
        try {
            // 获取文件扩展名
            String extension = getFileExtension(fileName).toLowerCase();
            
            // 构建预览响应
            Map<String, Object> response = Map.of(
                "fileName", fileName,
                "fileType", extension,
                "downloadUrl", "/api/files/download/" + fileName
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "预览失败: " + e.getMessage()));
        }
    }
    
    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1);
        }
        return "";
    }
    
    private String getContentType(String fileName) {
        String extension = getFileExtension(fileName).toLowerCase();
        switch (extension) {
            case "mp4":
                return "video/mp4";
            case "avi":
                return "video/x-msvideo";
            case "mov":
                return "video/quicktime";
            case "wmv":
                return "video/x-ms-wmv";
            case "flv":
                return "video/x-flv";
            case "webm":
                return "video/webm";
            case "mkv":
                return "video/x-matroska";
            case "pdf":
                return "application/pdf";
            case "txt":
                return "text/plain";
            case "doc":
                return "application/msword";
            case "docx":
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "ppt":
                return "application/vnd.ms-powerpoint";
            case "pptx":
                return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            default:
                return "application/octet-stream";
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserFiles(@PathVariable Long userId) {
        try {
            // 获取当前登录用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User currentUser = userService.findByUsername(username);

            if (currentUser == null) {
                ErrorResponse errorResponse = new ErrorResponse("用户未登录", "请先登录后再查看文件");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }

            // 验证目标用户是否存在
            User targetUser = userService.findById(userId);
            if (targetUser == null) {
                ErrorResponse errorResponse = new ErrorResponse("用户不存在", "找不到ID为" + userId + "的用户");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            // 获取指定用户的文件
            List<FileUploadResponse> files = fileStorageService.getFilesByUserId(userId);
            return ResponseEntity.ok(files);
        } catch (Exception e) {
            e.printStackTrace();
            ErrorResponse errorResponse = new ErrorResponse("获取文件失败", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
