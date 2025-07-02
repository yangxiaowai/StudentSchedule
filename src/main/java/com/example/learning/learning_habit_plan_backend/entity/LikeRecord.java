package com.example.learning.learning_habit_plan_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "like_record")
public class LikeRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "target_type", nullable = false, length = 50)
    private String targetType;
    
    @Column(name = "target_id", nullable = false)
    private Long targetId;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    // 默认构造函数
    public LikeRecord() {}
    
    // 带参构造函数
    public LikeRecord(Long userId, String targetType, Long targetId, LocalDateTime createdAt) {
        this.userId = userId;
        this.targetType = targetType;
        this.targetId = targetId;
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
    
    public String getTargetType() {
        return targetType;
    }
    
    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }
    
    public Long getTargetId() {
        return targetId;
    }
    
    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    @Override
    public String toString() {
        return "LikeRecord{" +
                "id=" + id +
                ", userId=" + userId +
                ", targetType='" + targetType + '\'' +
                ", targetId=" + targetId +
                ", createdAt=" + createdAt +
                '}';
    }
}