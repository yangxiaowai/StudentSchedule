package com.example.learning.learning_habit_plan_backend.dto;

import lombok.Data;

@Data
public class FileUploadResponse {
    private String fileName;
    private String fileDownloadUri;
    private String fileType;
    private long size;
    private String subject;
    private String contentType;
}
