package com.example.learning.learning_habit_plan_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_achievement")
public class UserAchievement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "achievement_id", nullable = false)
    private Long achievementId;
    
    @Column(name = "unlocked_at")
    private LocalDateTime unlockedAt;
    
    // 默认构造函数
    public UserAchievement() {}
    
    // 带参构造函数
    public UserAchievement(Long userId, Long achievementId, LocalDateTime unlockedAt) {
        this.userId = userId;
        this.achievementId = achievementId;
        this.unlockedAt = unlockedAt;
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
    
    public Long getAchievementId() {
        return achievementId;
    }
    
    public void setAchievementId(Long achievementId) {
        this.achievementId = achievementId;
    }
    
    public LocalDateTime getUnlockedAt() {
        return unlockedAt;
    }
    
    public void setUnlockedAt(LocalDateTime unlockedAt) {
        this.unlockedAt = unlockedAt;
    }
    
    @Override
    public String toString() {
        return "UserAchievement{" +
                "id=" + id +
                ", userId=" + userId +
                ", achievementId=" + achievementId +
                ", unlockedAt=" + unlockedAt +
                '}';
    }
}