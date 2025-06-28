package com.example.learning.learning_habit_plan_backend.controller;

import com.example.learning.learning_habit_plan_backend.dto.FilePreviewResponse;
import com.example.learning.learning_habit_plan_backend.service.FilePreviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/preview")
public class FilePreviewController {

    private final FilePreviewService filePreviewService;

    public FilePreviewController(FilePreviewService filePreviewService) {
        this.filePreviewService = filePreviewService;
    }

    @GetMapping("/{fileName}")
    public ResponseEntity<FilePreviewResponse> previewFile(@PathVariable String fileName) {
        FilePreviewResponse response = filePreviewService.previewFile(fileName);

        if (response.getError() != null) {
            return ResponseEntity.badRequest().body(response);
        }

        return ResponseEntity.ok(response);
    }

}
