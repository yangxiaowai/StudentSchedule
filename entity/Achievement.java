package com.example.learning.learning_habit_plan_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "achievement")
public class Achievement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(length = 100)
    private String icon;
    
    @Column(nullable = false, length = 50)
    private String type; // task_completion, study_streak, share_creation, best_answers, study_hours
    
    @Column(name = "condition_value")
    private Integer conditionValue;
    
    @Column(name = "points_reward")
    private Integer pointsReward = 0;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    // 构造函数
    public Achievement() {
        this.createdAt = LocalDateTime.now();
    }
    
    public Achievement(String name, String description, String type, Integer conditionValue, Integer pointsReward) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.conditionValue = conditionValue;
        this.pointsReward = pointsReward;
        this.createdAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getIcon() {
        return icon;
    }
    
    public void setIcon(String icon) {
        this.icon = icon;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public Integer getConditionValue() {
        return conditionValue;
    }
    
    public void setConditionValue(Integer conditionValue) {
        this.conditionValue = conditionValue;
    }
    
    public Integer getPointsReward() {
        return pointsReward;
    }
    
    public void setPointsReward(Integer pointsReward) {
        this.pointsReward = pointsReward;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}