package com.example.learning.learning_habit_plan_backend.util;

import org.springframework.web.multipart.MultipartFile;
import java.util.Arrays;
import java.util.List;

/**
 * 视频文件验证工具类
 */
public class VideoFileValidator {
    
    // 支持的视频文件扩展名
    private static final List<String> SUPPORTED_VIDEO_EXTENSIONS = Arrays.asList(
        ".mp4", ".avi", ".mov", ".wmv", ".flv", ".webm", ".mkv", ".m4v"
    );
    
    // 支持的视频MIME类型
    private static final List<String> SUPPORTED_VIDEO_MIME_TYPES = Arrays.asList(
        "video/mp4", "video/avi", "video/quicktime", "video/x-ms-wmv", 
        "video/x-flv", "video/webm", "video/x-matroska", "video/x-m4v"
    );
    
    // 视频文件最大大小 (200MB)
    private static final long MAX_VIDEO_SIZE = 200 * 1024 * 1024;
    
    /**
     * 验证是否为支持的视频文件
     * @param file 上传的文件
     * @return 验证结果
     */
    public static ValidationResult validateVideoFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return new ValidationResult(false, "文件不能为空");
        }
        
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            return new ValidationResult(false, "文件名不能为空");
        }
        
        // 检查文件扩展名
        String fileExtension = getFileExtension(originalFilename).toLowerCase();
        if (!SUPPORTED_VIDEO_EXTENSIONS.contains(fileExtension)) {
            return new ValidationResult(false, 
                "不支持的视频格式。支持的格式: " + String.join(", ", SUPPORTED_VIDEO_EXTENSIONS));
        }
        
        // 检查MIME类型
        String contentType = file.getContentType();
        if (contentType != null && !SUPPORTED_VIDEO_MIME_TYPES.contains(contentType)) {
            return new ValidationResult(false, "不支持的视频文件类型: " + contentType);
        }
        
        // 检查文件大小
        if (file.getSize() > MAX_VIDEO_SIZE) {
            return new ValidationResult(false, 
                String.format("视频文件过大。最大支持 %.1fMB，当前文件 %.1fMB", 
                    MAX_VIDEO_SIZE / (1024.0 * 1024.0), 
                    file.getSize() / (1024.0 * 1024.0)));
        }
        
        return new ValidationResult(true, "视频文件验证通过");
    }
    
    /**
     * 检查是否为视频文件
     * @param filename 文件名
     * @return 是否为视频文件
     */
    public static boolean isVideoFile(String filename) {
        if (filename == null) return false;
        String extension = getFileExtension(filename).toLowerCase();
        return SUPPORTED_VIDEO_EXTENSIONS.contains(extension);
    }
    
    /**
     * 获取文件扩展名
     * @param filename 文件名
     * @return 扩展名（包含点号）
     */
    private static String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        return lastDotIndex > 0 ? filename.substring(lastDotIndex) : "";
    }
    
    /**
     * 验证结果类
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String message;
        
        public ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public String getMessage() {
            return message;
        }
    }
}