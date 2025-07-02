package com.example.learning.learning_habit_plan_backend.dto;

import lombok.Data;

@Data
public class FilePreviewResponse {
    private String fileName;
    private String fileType;  // txt/pdf/doc/ppt等
    private String content;   // 文本内容或Base64编码的文件内容
    private boolean multiPage; // 是否为多页面文档（PDF、PPT等）
    private int pageCount;    // 页面数量
    private String error;     // 错误信息（如有）

    public static FilePreviewResponse success(String fileName, String fileType, String content) {
        FilePreviewResponse response = new FilePreviewResponse();
        response.setFileName(fileName);
        response.setFileType(fileType);
        response.setContent(content);
        
        // 对于PDF和PPT文件，检查内容是否包含多个页面（以逗号分隔的Base64字符串）
        if (("pdf".equals(fileType) || "ppt".equals(fileType) || "pptx".equals(fileType)) 
                && content.contains(",")) {
            response.setMultiPage(true);
            // 计算页数（逗号数量+1）
            response.setPageCount(content.split(",").length);
        } else {
            response.setMultiPage(false);
            response.setPageCount(1);
        }
        
        return response;
    }

    public static FilePreviewResponse error(String fileName, String errorMessage) {
        FilePreviewResponse response = new FilePreviewResponse();
        response.setFileName(fileName);
        response.setError(errorMessage);
        response.setMultiPage(false);
        response.setPageCount(0);
        return response;
    }
}
