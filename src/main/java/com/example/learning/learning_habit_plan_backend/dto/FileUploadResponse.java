package com.example.learning.learning_habit_plan_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResponse {
    private Long id;
    private String fileName;
    private String fileDownloadUri;
    private String fileType;
    private long size;
    private String subject;
    private String contentType;
    private String uploadTime;
}
