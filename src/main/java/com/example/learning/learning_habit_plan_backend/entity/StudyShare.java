package com.example.learning.learning_habit_plan_backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "study_share")
public class StudyShare {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "group_id")
    private Long groupId; // 可选，如果是在小组内分享
    
    @Column(nullable = false, length = 200)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "share_type")
    private ShareType shareType;
    
    @Column(name = "subject", length = 50)
    private String subject;
    
    @Column(name = "tags", length = 200)
    private String tags; // 用逗号分隔的标签
    
    @Column(name = "file_url")
    private String fileUrl;
    
    @Column(name = "file_name")
    private String fileName;
    
    @Column(name = "like_count")
    private Integer likeCount = 0;
    
    @Column(name = "comment_count")
    private Integer commentCount = 0;
    
    @Column(name = "view_count")
    private Integer viewCount = 0;
    
    @Column(name = "is_public")
    private Boolean isPublic = true;
    
    @CreationTimestamp
    @Column(name = "created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    
    public enum ShareType {
        NOTE,      // 学习笔记
        RESOURCE,  // 学习资源
        EXPERIENCE, // 学习心得
        QUESTION,  // 问题讨论
        SUMMARY    // 总结归纳
    }
    
    // 构造函数
    public StudyShare() {}
    
    public StudyShare(Long userId, String title, String content, ShareType shareType, String subject) {
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.shareType = shareType;
        this.subject = subject;
    }
}