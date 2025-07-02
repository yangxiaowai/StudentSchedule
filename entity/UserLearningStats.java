package com.example.learning.learning_habit_plan_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_learning_stats")
public class UserLearningStats {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;
    
    @Column(name = "total_study_hours")
    private Double totalStudyHours;
    
    @Column(name = "weekly_study_hours")
    private Double weeklyStudyHours;
    
    @Column(name = "monthly_study_hours")
    private Double monthlyStudyHours;
    
    @Column(name = "total_points")
    private Integer totalPoints;
    
    @Column(name = "current_level")
    private Integer currentLevel;
    
    @Column(name = "current_streak")
    private Integer currentStreak;
    
    @Column(name = "longest_streak")
    private Integer longestStreak;
    
    @Column(name = "total_tasks_completed")
    private Integer totalTasksCompleted;
    
    @Column(name = "best_answers_count")
    private Integer bestAnswersCount;
    
    @Column(name = "shares_count")
    private Integer sharesCount;
    
    @Column(name = "likes_received")
    private Integer likesReceived;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // 默认构造函数
    public UserLearningStats() {}
    
    // 带参构造函数
    public UserLearningStats(Long userId, Double totalStudyHours, Double weeklyStudyHours, 
                           Double monthlyStudyHours, Integer totalPoints, Integer currentLevel, 
                           Integer currentStreak, Integer longestStreak, Integer totalTasksCompleted, 
                           Integer bestAnswersCount, Integer sharesCount, Integer likesReceived, 
                           LocalDateTime updatedAt) {
        this.userId = userId;
        this.totalStudyHours = totalStudyHours;
        this.weeklyStudyHours = weeklyStudyHours;
        this.monthlyStudyHours = monthlyStudyHours;
        this.totalPoints = totalPoints;
        this.currentLevel = currentLevel;
        this.currentStreak = currentStreak;
        this.longestStreak = longestStreak;
        this.totalTasksCompleted = totalTasksCompleted;
        this.bestAnswersCount = bestAnswersCount;
        this.sharesCount = sharesCount;
        this.likesReceived = likesReceived;
        this.updatedAt = updatedAt;
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
    
    public Double getTotalStudyHours() {
        return totalStudyHours;
    }
    
    public void setTotalStudyHours(Double totalStudyHours) {
        this.totalStudyHours = totalStudyHours;
    }
    
    public Double getWeeklyStudyHours() {
        return weeklyStudyHours;
    }
    
    public void setWeeklyStudyHours(Double weeklyStudyHours) {
        this.weeklyStudyHours = weeklyStudyHours;
    }
    
    public Double getMonthlyStudyHours() {
        return monthlyStudyHours;
    }
    
    public void setMonthlyStudyHours(Double monthlyStudyHours) {
        this.monthlyStudyHours = monthlyStudyHours;
    }
    
    public Integer getTotalPoints() {
        return totalPoints;
    }
    
    public void setTotalPoints(Integer totalPoints) {
        this.totalPoints = totalPoints;
    }
    
    public Integer getCurrentLevel() {
        return currentLevel;
    }
    
    public void setCurrentLevel(Integer currentLevel) {
        this.currentLevel = currentLevel;
    }
    
    public Integer getCurrentStreak() {
        return currentStreak;
    }
    
    public void setCurrentStreak(Integer currentStreak) {
        this.currentStreak = currentStreak;
    }
    
    public Integer getLongestStreak() {
        return longestStreak;
    }
    
    public void setLongestStreak(Integer longestStreak) {
        this.longestStreak = longestStreak;
    }
    
    public Integer getTotalTasksCompleted() {
        return totalTasksCompleted;
    }
    
    public void setTotalTasksCompleted(Integer totalTasksCompleted) {
        this.totalTasksCompleted = totalTasksCompleted;
    }
    
    public Integer getBestAnswersCount() {
        return bestAnswersCount;
    }
    
    public void setBestAnswersCount(Integer bestAnswersCount) {
        this.bestAnswersCount = bestAnswersCount;
    }
    
    public Integer getSharesCount() {
        return sharesCount;
    }
    
    public void setSharesCount(Integer sharesCount) {
        this.sharesCount = sharesCount;
    }
    
    public Integer getLikesReceived() {
        return likesReceived;
    }
    
    public void setLikesReceived(Integer likesReceived) {
        this.likesReceived = likesReceived;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @Override
    public String toString() {
        return "UserLearningStats{" +
                "id=" + id +
                ", userId=" + userId +
                ", totalStudyHours=" + totalStudyHours +
                ", weeklyStudyHours=" + weeklyStudyHours +
                ", monthlyStudyHours=" + monthlyStudyHours +
                ", totalPoints=" + totalPoints +
                ", currentLevel=" + currentLevel +
                ", currentStreak=" + currentStreak +
                ", longestStreak=" + longestStreak +
                ", totalTasksCompleted=" + totalTasksCompleted +
                ", bestAnswersCount=" + bestAnswersCount +
                ", sharesCount=" + sharesCount +
                ", likesReceived=" + likesReceived +
                ", updatedAt=" + updatedAt +
                '}';
    }
}