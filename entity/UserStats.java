package com.example.learning.learning_habit_plan_backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_stats")
public class UserStats {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;
    
    @Column(name = "study_hours_total", precision = 10, scale = 2)
    private BigDecimal studyHoursTotal = BigDecimal.ZERO;
    
    @Column(name = "study_hours_weekly", precision = 10, scale = 2)
    private BigDecimal studyHoursWeekly = BigDecimal.ZERO;
    
    @Column(name = "study_hours_monthly", precision = 10, scale = 2)
    private BigDecimal studyHoursMonthly = BigDecimal.ZERO;
    
    private Integer points = 0;
    
    private Integer experience = 0;
    
    private Integer level = 1;
    
    @Column(name = "streak_days")
    private Integer streakDays = 0;
    
    @Column(name = "tasks_completed")
    private Integer tasksCompleted = 0;
    
    @Column(name = "best_answers_count")
    private Integer bestAnswersCount = 0;
    
    @Column(name = "shares_count")
    private Integer sharesCount = 0;
    
    @Column(name = "likes_received")
    private Integer likesReceived = 0;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // 构造函数
    public UserStats() {
        this.updatedAt = LocalDateTime.now();
    }
    
    public UserStats(Long userId) {
        this.userId = userId;
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
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
    
    public BigDecimal getStudyHoursTotal() {
        return studyHoursTotal;
    }
    
    public void setStudyHoursTotal(BigDecimal studyHoursTotal) {
        this.studyHoursTotal = studyHoursTotal;
    }
    
    public BigDecimal getStudyHoursWeekly() {
        return studyHoursWeekly;
    }
    
    public void setStudyHoursWeekly(BigDecimal studyHoursWeekly) {
        this.studyHoursWeekly = studyHoursWeekly;
    }
    
    public BigDecimal getStudyHoursMonthly() {
        return studyHoursMonthly;
    }
    
    public void setStudyHoursMonthly(BigDecimal studyHoursMonthly) {
        this.studyHoursMonthly = studyHoursMonthly;
    }
    
    public Integer getPoints() {
        return points;
    }
    
    public void setPoints(Integer points) {
        this.points = points;
    }
    
    public Integer getExperience() {
        return experience;
    }
    
    public void setExperience(Integer experience) {
        this.experience = experience;
    }
    
    public Integer getLevel() {
        return level;
    }
    
    public void setLevel(Integer level) {
        this.level = level;
    }
    
    public Integer getStreakDays() {
        return streakDays;
    }
    
    public void setStreakDays(Integer streakDays) {
        this.streakDays = streakDays;
    }
    
    public Integer getTasksCompleted() {
        return tasksCompleted;
    }
    
    public void setTasksCompleted(Integer tasksCompleted) {
        this.tasksCompleted = tasksCompleted;
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
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // 业务方法
    public void addStudyHours(BigDecimal hours) {
        this.studyHoursTotal = this.studyHoursTotal.add(hours);
        this.studyHoursWeekly = this.studyHoursWeekly.add(hours);
        this.studyHoursMonthly = this.studyHoursMonthly.add(hours);
    }
    
    public void addPoints(Integer points) {
        this.points += points;
    }
    
    public void addExperience(Integer exp) {
        this.experience += exp;
        // 简单的升级逻辑：每1000经验升一级
        int newLevel = (this.experience / 1000) + 1;
        if (newLevel > this.level) {
            this.level = newLevel;
        }
    }
    
    public void incrementTasksCompleted() {
        this.tasksCompleted++;
    }
    
    public void incrementBestAnswers() {
        this.bestAnswersCount++;
    }
    
    public void incrementShares() {
        this.sharesCount++;
    }
    
    public void incrementLikesReceived() {
        this.likesReceived++;
    }
}