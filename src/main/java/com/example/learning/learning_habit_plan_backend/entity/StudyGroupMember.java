package com.example.learning.learning_habit_plan_backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
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
    private MemberRole role = MemberRole.MEMBER;
    
    @Column(name = "contribution_score")
    private Integer contributionScore = 0;
    
    @Column(name = "study_hours")
    private Double studyHours = 0.0;
    
    @Column(name = "tasks_completed")
    private Integer tasksCompleted = 0;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @CreationTimestamp
    @Column(name = "joined_at")
    private LocalDateTime joinedAt;
    
    public enum MemberRole {
        CREATOR, ADMIN, MEMBER
    }
    
    // 构造函数
    public StudyGroupMember() {}
    
    public StudyGroupMember(Long groupId, Long userId, MemberRole role) {
        this.groupId = groupId;
        this.userId = userId;
        this.role = role;
    }
}