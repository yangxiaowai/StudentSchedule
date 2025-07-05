package com.example.learning.learning_habit_plan_backend.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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
    private Double totalStudyHours = 0.0;
    
    @Column(name = "total_tasks_completed")
    private Integer totalTasksCompleted = 0;
    
    @Column(name = "total_points")
    private Integer totalPoints = 0;
    
    @Column(name = "current_streak")
    private Integer currentStreak = 0; // 当前连续学习天数
    
    @Column(name = "max_streak")
    private Integer maxStreak = 0; // 最大连续学习天数
    
    @Column(name = "questions_asked")
    private Integer questionsAsked = 0;
    
    @Column(name = "questions_answered")
    private Integer questionsAnswered = 0;
    
    @Column(name = "best_answers_count")
    private Integer bestAnswersCount = 0;
    
    @Column(name = "shares_count")
    private Integer sharesCount = 0;
    
    @Column(name = "likes_received")
    private Integer likesReceived = 0;
    
    @Column(name = "groups_joined")
    private Integer groupsJoined = 0;
    
    @Column(name = "groups_created")
    private Integer groupsCreated = 0;
    
    @Column(name = "weekly_study_hours")
    private Double weeklyStudyHours = 0.0;
    
    @Column(name = "monthly_study_hours")
    private Double monthlyStudyHours = 0.0;
    
    @Column(name = "last_study_date")
    private LocalDateTime lastStudyDate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "level")
    private UserLevel level = UserLevel.BEGINNER;
    
    @Column(name = "experience_points")
    private Integer experiencePoints = 0;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum UserLevel {
        BEGINNER,    // 初学者 (0-999 经验)
        INTERMEDIATE, // 中级者 (1000-4999 经验)
        ADVANCED,    // 高级者 (5000-9999 经验)
        EXPERT,      // 专家 (10000-19999 经验)
        MASTER       // 大师 (20000+ 经验)
    }
    
    // 构造函数
    public UserLearningStats() {}
    
    public UserLearningStats(Long userId) {
        this.userId = userId;
    }
    
    // 更新等级的方法
    public void updateLevel() {
        if (experiencePoints >= 20000) {
            this.level = UserLevel.MASTER;
        } else if (experiencePoints >= 10000) {
            this.level = UserLevel.EXPERT;
        } else if (experiencePoints >= 5000) {
            this.level = UserLevel.ADVANCED;
        } else if (experiencePoints >= 1000) {
            this.level = UserLevel.INTERMEDIATE;
        } else {
            this.level = UserLevel.BEGINNER;
        }
    }

    // Getter and Setter methods
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

    public Integer getTotalTasksCompleted() {
        return totalTasksCompleted;
    }

    public void setTotalTasksCompleted(Integer totalTasksCompleted) {
        this.totalTasksCompleted = totalTasksCompleted;
    }

    public Integer getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(Integer totalPoints) {
        this.totalPoints = totalPoints;
    }

    public Integer getCurrentStreak() {
        return currentStreak;
    }

    public void setCurrentStreak(Integer currentStreak) {
        this.currentStreak = currentStreak;
    }

    public Integer getMaxStreak() {
        return maxStreak;
    }

    public void setMaxStreak(Integer maxStreak) {
        this.maxStreak = maxStreak;
    }

    public Integer getQuestionsAsked() {
        return questionsAsked;
    }

    public void setQuestionsAsked(Integer questionsAsked) {
        this.questionsAsked = questionsAsked;
    }

    public Integer getQuestionsAnswered() {
        return questionsAnswered;
    }

    public void setQuestionsAnswered(Integer questionsAnswered) {
        this.questionsAnswered = questionsAnswered;
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

    public Integer getGroupsCreated() {
        return groupsCreated;
    }

    public void setGroupsCreated(Integer groupsCreated) {
        this.groupsCreated = groupsCreated;
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

    public LocalDateTime getLastStudyDate() {
        return lastStudyDate;
    }

    public void setLastStudyDate(LocalDateTime lastStudyDate) {
        this.lastStudyDate = lastStudyDate;
    }

    public UserLevel getLevel() {
        return level;
    }

    public void setLevel(UserLevel level) {
        this.level = level;
    }

    public Integer getExperiencePoints() {
        return experiencePoints;
    }

    public void setExperiencePoints(Integer experiencePoints) {
        this.experiencePoints = experiencePoints;
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
}