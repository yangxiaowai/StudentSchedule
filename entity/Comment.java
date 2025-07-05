package com.example.learning.learning_habit_plan_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "comment")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "target_type", nullable = false, length = 50)
    private String targetType;
    
    @Column(name = "target_id", nullable = false)
    private Long targetId;
    
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;
    
    @Column(name = "commenter_id", nullable = false)
    private Long commenterId;
    
    @Column(name = "parent_id")
    private Long parentId;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    // 默认构造函数
    public Comment() {}
    
    // 带参构造函数
    public Comment(String targetType, Long targetId, String content, Long commenterId, 
                  Long parentId, LocalDateTime createdAt) {
        this.targetType = targetType;
        this.targetId = targetId;
        this.content = content;
        this.commenterId = commenterId;
        this.parentId = parentId;
        this.createdAt = createdAt;
    }
    
    // Getter 和 Setter 方法
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
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
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public Long getCommenterId() {
        return commenterId;
    }
    
    public void setCommenterId(Long commenterId) {
        this.commenterId = commenterId;
    }
    
    public Long getParentId() {
        return parentId;
    }
    
    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    @Override
    public String toString() {
        return "Comment{" +
                "id=" + id +
                ", targetType='" + targetType + '\'' +
                ", targetId=" + targetId +
                ", content='" + content + '\'' +
                ", commenterId=" + commenterId +
                ", parentId=" + parentId +
                ", createdAt=" + createdAt +
                '}';
    }
}