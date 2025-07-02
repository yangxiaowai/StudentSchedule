package com.example.learning.learning_habit_plan_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "study_record")
public class StudyRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "subject", nullable = false, length = 100)
    private String subject;
    
    @Column(name = "study_duration", nullable = false)
    private Integer studyDuration;
    
    @Column(name = "study_date", nullable = false)
    private LocalDateTime studyDate;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    // 默认构造函数
    public StudyRecord() {}
    
    // 带参构造函数
    public StudyRecord(Long userId, String subject, Integer studyDuration, 
                      LocalDateTime studyDate, String notes, LocalDateTime createdAt) {
        this.userId = userId;
        this.subject = subject;
        this.studyDuration = studyDuration;
        this.studyDate = studyDate;
        this.notes = notes;
        this.createdAt = createdAt;
    }
    
    // Getter 和 Setter 方法
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getSubject() {
        return subject;
    }
    
    public void setSubject(String subject) {
        this.subject = subject;
    }
    
    public Integer getStudyDuration() {
        return studyDuration;
    }
    
    public void setStudyDuration(Integer studyDuration) {
        this.studyDuration = studyDuration;
    }
    
    public LocalDateTime getStudyDate() {
        return studyDate;
    }
    
    public void setStudyDate(LocalDateTime studyDate) {
        this.studyDate = studyDate;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    @Override
    public String toString() {
        return "StudyRecord{" +
                "id=" + id +
                ", userId=" + userId +
                ", subject='" + subject + '\'' +
                ", studyDuration=" + studyDuration +
                ", studyDate=" + studyDate +
                ", notes='" + notes + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}