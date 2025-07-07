package com.example.learning.learning_habit_plan_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_stats")
public class UserStats {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;
    
    @Column(name = "study_hours_total", columnDefinition = "DECIMAL(10,2) DEFAULT 0")
    private Double studyHoursTotal = 0.0;
    
    @Column(name = "study_hours_weekly", columnDefinition = "DECIMAL(10,2) DEFAULT 0")
    private Double studyHoursWeekly = 0.0;
    
    @Column(name = "study_hours_monthly", columnDefinition = "DECIMAL(10,2) DEFAULT 0")
    private Double studyHoursMonthly = 0.0;
    
    @Column(name = "points", columnDefinition = "INT DEFAULT 0")
    private Integer points = 0;
    
    @Column(name = "experience", columnDefinition = "INT DEFAULT 0")
    private Integer experience = 0;
    
    @Column(name = "level", columnDefinition = "INT DEFAULT 1")
    private Integer level = 1;
    
    @Column(name = "streak_days", columnDefinition = "INT DEFAULT 0")
    private Integer streakDays = 0;
    
    @Column(name = "tasks_completed", columnDefinition = "INT DEFAULT 0")
    private Integer tasksCompleted = 0;
    
    @Column(name = "best_answers_count", columnDefinition = "INT DEFAULT 0")
    private Integer bestAnswersCount = 0;
    
    @Column(name = "shares_count", columnDefinition = "INT DEFAULT 0")
    private Integer sharesCount = 0;
    
    @Column(name = "likes_received", columnDefinition = "INT DEFAULT 0")
    private Integer likesReceived = 0;
    
    @Column(name = "groups_joined", columnDefinition = "INT DEFAULT 0")
    private Integer groupsJoined = 0;
    
    @Column(name = "questions_asked", columnDefinition = "INT DEFAULT 0")
    private Integer questionsAsked = 0;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Constructors
    public UserStats() {}
    
    public UserStats(Long userId) {
        this.userId = userId;
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
    
    public Double getStudyHoursTotal() {
        return studyHoursTotal;
    }
    
    public void setStudyHoursTotal(Double studyHoursTotal) {
        this.studyHoursTotal = studyHoursTotal;
    }
    
    public Double getStudyHoursWeekly() {
        return studyHoursWeekly;
    }
    
    public void setStudyHoursWeekly(Double studyHoursWeekly) {
        this.studyHoursWeekly = studyHoursWeekly;
    }
    
    public Double getStudyHoursMonthly() {
        return studyHoursMonthly;
    }
    
    public void setStudyHoursMonthly(Double studyHoursMonthly) {
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
    
    public Integer getGroupsJoined() {
        return groupsJoined;
    }
    
    public void setGroupsJoined(Integer groupsJoined) {
        this.groupsJoined = groupsJoined;
    }
    
    public Integer getQuestionsAsked() {
        return questionsAsked;
    }
    
    public void setQuestionsAsked(Integer questionsAsked) {
        this.questionsAsked = questionsAsked;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    // Business methods
    public void incrementGroupsJoined() {
        this.groupsJoined = (this.groupsJoined == null ? 0 : this.groupsJoined) + 1;
    }
    
    public void incrementQuestionsAsked() {
        this.questionsAsked = (this.questionsAsked == null ? 0 : this.questionsAsked) + 1;
    }
    
    public void incrementSharesCount() {
        this.sharesCount = (this.sharesCount == null ? 0 : this.sharesCount) + 1;
    }
    
    public void incrementBestAnswersCount() {
        this.bestAnswersCount = (this.bestAnswersCount == null ? 0 : this.bestAnswersCount) + 1;
    }
    
    public void incrementLikesReceived() {
        this.likesReceived = (this.likesReceived == null ? 0 : this.likesReceived) + 1;
    }
}