package com.example.learning.learning_habit_plan_backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "qa_answer")
public class QAAnswer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "question_id", nullable = false)
    private Long questionId;
    
    @Column(name = "answerer_id", nullable = false)
    private Long userId;
    
    @Column(name = "user_id", nullable = false)
    private Long answererId;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;
    
    @Column(name = "image_url")
    private String imageUrl; // 回答配图
    
    @Column(name = "like_count")
    private Integer likeCount = 0;
    
    @Column(name = "is_best_answer")
    private Boolean isBestAnswer = false;
    
    @Column(name = "is_helpful")
    private Boolean isHelpful = false;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // 构造函数
    public QAAnswer() {}
    
    public QAAnswer(Long questionId, Long userId, String content) {
        this.questionId = questionId;
        this.userId = userId;
        this.answererId = userId; // 同时设置user_id字段
        this.content = content;
    }
}