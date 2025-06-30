package com.example.learning.learning_habit_plan_backend.controller;

import com.example.learning.learning_habit_plan_backend.dto.FileUploadResponse;
import com.example.learning.learning_habit_plan_backend.service.FileStorageService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileStorageService fileStorageService;

    public FileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PostMapping("/upload")
    public FileUploadResponse uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("subject") String subject,
            @RequestParam("type") String type) {
        return fileStorageService.storeFile(file, subject, type);
    }

    @GetMapping("/list")
    public List<FileUploadResponse> getFiles() {
        return fileStorageService.getAllFiles();
    }

    @DeleteMapping("/delete/{fileName}")
    public void deleteFile(@PathVariable String fileName) {
        fileStorageService.deleteFile(fileName);
    }

    @GetMapping("/download/{fileName:.+}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable String fileName) {
        byte[] fileContent = fileStorageService.loadFileAsResource(fileName);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(fileContent);
    }
}
