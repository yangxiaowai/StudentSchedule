package com.example.learning.learning_habit_plan_backend.service;

import com.example.learning.learning_habit_plan_backend.dto.FilePreviewResponse;

public interface FilePreviewService {
    FilePreviewResponse previewFile(String fileName);
}
