package com.example.learning.learning_habit_plan_backend.service.impl;

import com.example.learning.learning_habit_plan_backend.dto.FilePreviewResponse;
import com.example.learning.learning_habit_plan_backend.service.FilePreviewService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

@Service
public class FilePreviewServiceImpl implements FilePreviewService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    // 支持的文件类型及大小限制（单位：MB）
    private static final int MAX_TEXT_SIZE = 20; // 2MB
    private static final int MAX_PDF_SIZE = 20; // 20MB
    private static final int MAX_OFFICE_SIZE = 20; // 10MB

    @Override
    public FilePreviewResponse previewFile(String fileName) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(fileName).normalize();
            if (!Files.exists(filePath)) {
                return FilePreviewResponse.error(fileName, "文件不存在");
            }

            String fileExtension = getFileExtension(fileName).toLowerCase();
            long fileSize = Files.size(filePath) / (1024 * 1024); // 转换为MB

            // 根据文件类型应用不同限制
            switch (fileExtension) {
                case "txt":
                    if (fileSize > MAX_TEXT_SIZE) {
                        return FilePreviewResponse.error(fileName,
                                String.format("文本文件超过%dMB限制，请下载查看", MAX_TEXT_SIZE));
                    }
                    break;
                case "pdf":
                    if (fileSize > MAX_PDF_SIZE) {
                        return FilePreviewResponse.error(fileName,
                                String.format("PDF文件超过%dMB限制，请下载查看", MAX_PDF_SIZE));
                    }
                    break;
                case "doc":
                case "docx":
                case "ppt":
                case "pptx":
                    if (fileSize > MAX_OFFICE_SIZE) {
                        return FilePreviewResponse.error(fileName,
                                String.format("Office文件超过%dMB限制，请下载查看", MAX_OFFICE_SIZE));
                    }
                    break;
                default:
                    return FilePreviewResponse.error(fileName,
                            "不支持的文件类型: " + fileExtension);
            }

            // 读取文件内容
            byte[] fileContent = Files.readAllBytes(filePath);

            // 特殊处理：对于Office文档，返回下载URL而不是内容
            if (isOfficeFile(fileExtension)) {
                return FilePreviewResponse.success(
                        fileName,
                        fileExtension,
                        "/api/files/download/" + fileName // 返回下载URL
                );
            }

            // 其他文件返回Base64内容
            return FilePreviewResponse.success(
                    fileName,
                    fileExtension,
                    Base64.getEncoder().encodeToString(fileContent)
            );

        } catch (IOException e) {
            return FilePreviewResponse.error(
                    fileName,
                    "文件读取失败: " + e.getMessage()
            );
        }
    }

    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        return lastDotIndex == -1 ? "" : fileName.substring(lastDotIndex + 1);
    }

    private boolean isOfficeFile(String extension) {
        return extension.matches("doc|docx|ppt|pptx");
    }
}
