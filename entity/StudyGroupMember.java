package com.example.learning.learning_habit_plan_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "study_group_member")
public class StudyGroupMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "group_id", nullable = false)
    private Long groupId;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Role role;
    
    @Column(name = "joined_at")
    private LocalDateTime joinedAt;
    
    @Column(name = "is_active")
    private Boolean isActive;
    
    @Column(name = "contribution_score")
    private Integer contributionScore;
    
    @Column(name = "study_hours")
    private Double studyHours;
    
    @Column(name = "tasks_completed")
    private Integer tasksCompleted;
    
    // 角色枚举
    public enum Role {
        ADMIN, CREATOR, MEMBER
    }
    
    // 默认构造函数
    public StudyGroupMember() {}
    
    // 带参构造函数
    public StudyGroupMember(Long groupId, Long userId, Role role, LocalDateTime joinedAt, 
                           Boolean isActive, Integer contributionScore, Double studyHours, 
                           Integer tasksCompleted) {
        this.groupId = groupId;
        this.userId = userId;
        this.role = role;
        this.joinedAt = joinedAt;
        this.isActive = isActive;
        this.contributionScore = contributionScore;
        this.studyHours = studyHours;
        this.tasksCompleted = tasksCompleted;
    }
    
    // Getter 和 Setter 方法
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getGroupId() {
        return groupId;
    }
    
    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public Role getRole() {
        return role;
    }
    
    public void setRole(Role role) {
        this.role = role;
    }
    
    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }
    
    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public Integer getContributionScore() {
        return contributionScore;
    }
    
    public void setContributionScore(Integer contributionScore) {
        this.contributionScore = contributionScore;
    }
    
    public Double getStudyHours() {
        return studyHours;
    }
    
    public void setStudyHours(Double studyHours) {
        this.studyHours = studyHours;
    }
    
    public Integer getTasksCompleted() {
        return tasksCompleted;
    }
    
    public void setTasksCompleted(Integer tasksCompleted) {
        this.tasksCompleted = tasksCompleted;
    }
    
    @Override
    public String toString() {
        return "StudyGroupMember{" +
                "id=" + id +
                ", groupId=" + groupId +
                ", userId=" + userId +
                ", role=" + role +
                ", joinedAt=" + joinedAt +
                ", isActive=" + isActive +
                ", contributionScore=" + contributionScore +
                ", studyHours=" + studyHours +
                ", tasksCompleted=" + tasksCompleted +
                '}';
    }
}