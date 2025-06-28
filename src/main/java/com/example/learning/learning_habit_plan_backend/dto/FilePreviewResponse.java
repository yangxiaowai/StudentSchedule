package com.example.learning.learning_habit_plan_backend.dto;

import lombok.Data;

@Data
public class FilePreviewResponse {
    private String fileName;
    private String fileType;  // txt/pdf/doc/ppt等
    private String content;   // 文本内容或Base64编码的文件内容
    private String error;     // 错误信息（如有）

    public static FilePreviewResponse success(String fileName, String fileType, String content) {
        FilePreviewResponse response = new FilePreviewResponse();
        response.setFileName(fileName);
        response.setFileType(fileType);
        response.setContent(content);
        return response;
    }

    public static FilePreviewResponse error(String fileName, String errorMessage) {
        FilePreviewResponse response = new FilePreviewResponse();
        response.setFileName(fileName);
        response.setError(errorMessage);
        return response;
    }
}
