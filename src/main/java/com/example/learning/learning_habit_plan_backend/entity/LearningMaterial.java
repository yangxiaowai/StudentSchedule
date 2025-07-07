package com.example.learning.learning_habit_plan_backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "learning_materials")
public class LearningMaterial {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String filePath;

    @Column(nullable = false)
    private String fileType;

    @Column(nullable = false)
    private Long fileSize;

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false)
    private String contentType;

    @CreationTimestamp
    private LocalDateTime uploadTime;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    // 移除与User的关联关系
    // @ManyToOne
    // @JoinColumn(name = "user_id", nullable = false)
    // private User user;
}
