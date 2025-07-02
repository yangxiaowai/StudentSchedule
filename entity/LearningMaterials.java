package com.example.learning.learning_habit_plan_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "learning_materials")
public class LearningMaterials {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "content_type", nullable = false)
    private String contentType;
    
    @Column(name = "file_name", nullable = false)
    private String fileName;
    
    @Column(name = "file_path", nullable = false)
    private String filePath;
    
    @Column(name = "file_size", nullable = false)
    private Long fileSize;
    
    @Column(name = "file_type", nullable = false)
    private String fileType;
    
    @Column(name = "subject", nullable = false)
    private String subject;
    
    @Column(name = "upload_time")
    private LocalDateTime uploadTime;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    // 默认构造函数
    public LearningMaterials() {}
    
    // 带参构造函数
    public LearningMaterials(String contentType, String fileName, String filePath, 
                           Long fileSize, String fileType, String subject, 
                           LocalDateTime uploadTime, Long userId) {
        this.contentType = contentType;
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.fileType = fileType;
        this.subject = subject;
        this.uploadTime = uploadTime;
        this.userId = userId;
    }
    
    // Getter 和 Setter 方法
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getContentType() {
        return contentType;
    }
    
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    public String getFilePath() {
        return filePath;
    }
    
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    
    public Long getFileSize() {
        return fileSize;
    }
    
    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }
    
    public String getFileType() {
        return fileType;
    }
    
    public void setFileType(String fileType) {
        this.fileType = fileType;
    }
    
    public String getSubject() {
        return subject;
    }
    
    public void setSubject(String subject) {
        this.subject = subject;
    }
    
    public LocalDateTime getUploadTime() {
        return uploadTime;
    }
    
    public void setUploadTime(LocalDateTime uploadTime) {
        this.uploadTime = uploadTime;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    @Override
    public String toString() {
        return "LearningMaterials{" +
                "id=" + id +
                ", contentType='" + contentType + '\'' +
                ", fileName='" + fileName + '\'' +
                ", filePath='" + filePath + '\'' +
                ", fileSize=" + fileSize +
                ", fileType='" + fileType + '\'' +
                ", subject='" + subject + '\'' +
                ", uploadTime=" + uploadTime +
                ", userId=" + userId +
                '}';
    }
}