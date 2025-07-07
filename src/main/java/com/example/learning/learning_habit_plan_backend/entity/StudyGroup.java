package com.example.learning.learning_habit_plan_backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "study_group")
public class StudyGroup {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(length = 500)
    private String description;
    
    @Column(name = "creator_id", nullable = false)
    private Long creatorId;
    
    @Column(name = "max_members")
    private Integer maxMembers = 50;
    
    @Column(name = "current_members")
    private Integer currentMembers = 1;
    
    @Column(name = "invite_code", unique = true, length = 20)
    private String inviteCode;
    
    // 所有小组都是公开的，移除私密小组功能
    
    @Column(name = "subject", length = 50)
    private String subject;
    
    @Column(name = "study_goal", length = 200)
    private String studyGoal;
    
    @Column(name = "task_sharing_enabled")
    private Boolean taskSharingEnabled = false;
    
    @Column(name = "resource_sharing_enabled")
    private Boolean resourceSharingEnabled = false;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private GroupStatus status = GroupStatus.ACTIVE;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum GroupStatus {
        ACTIVE, INACTIVE, DISBANDED
    }
    
    // 构造函数
    public StudyGroup() {}
    
    public StudyGroup(String name, String description, Long creatorId, String subject) {
        this.name = name;
        this.description = description;
        this.creatorId = creatorId;
        this.subject = subject;
    }
}