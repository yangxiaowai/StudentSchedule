package com.example.learning.learning_habit_plan_backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "qa_question")
public class QAQuestion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "asker_id", nullable = false)
    private Long userId;
    
    @Column(name = "group_id")
    private Long groupId; // 可选，如果是在小组内提问
    
    @Column(nullable = false, length = 200)
    private String title;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;
    
    @Column(name = "subject", length = 50)
    private String subject;
    
    @Column(name = "tags", length = 200)
    private String tags; // 用逗号分隔的标签
    
    @Column(name = "image_url")
    private String imageUrl; // 问题配图
    
    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty_level")
    private DifficultyLevel difficultyLevel = DifficultyLevel.MEDIUM;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private QuestionStatus status = QuestionStatus.OPEN;
    
    @Column(name = "best_answer_id")
    private Long bestAnswerId; // 最佳答案ID
    
    @Column(name = "answer_count")
    private Integer answerCount = 0;
    
    @Column(name = "view_count")
    private Integer viewCount = 0;
    
    @Column(name = "like_count")
    private Integer likeCount = 0;
    
    @Column(name = "reward_points")
    private Integer rewardPoints = 0; // 悬赏积分
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum DifficultyLevel {
        EASY, MEDIUM, HARD
    }
    
    public enum QuestionStatus {
        OPEN,      // 开放中
        ANSWERED,  // 已回答
        RESOLVED,  // 已解决
        CLOSED     // 已关闭
    }
    
    // 构造函数
    public QAQuestion() {}
    
    public QAQuestion(Long userId, String title, String content, String subject) {
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.subject = subject;
    }
}