package com.example.learning.learning_habit_plan_backend.dto;

public class FilePreviewResponse {
    private String fileName;
    private String fileType;  // txt/pdf/doc/ppt/mp4等
    private String content;   // 文本内容或Base64编码的文件内容
    private boolean multiPage; // 是否为多页面文档（PDF、PPT等）
    private int pageCount;    // 页面数量
    private String error;     // 错误信息（如有）
    private String previewType; // 预览类型：text/pdf/office/image/video等
    private String contentType; // MIME类型
    private String downloadUrl; // 下载URL
    private long fileSize;    // 文件大小（字节）

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

    // Getter and Setter methods
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isMultiPage() {
        return multiPage;
    }

    public void setMultiPage(boolean multiPage) {
        this.multiPage = multiPage;
    }

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getPreviewType() {
        return previewType;
    }

    public void setPreviewType(String previewType) {
        this.previewType = previewType;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
}
