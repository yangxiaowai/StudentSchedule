package com.example.learning.learning_habit_plan_backend.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

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
    
    @Column(name = "is_public")
    private Boolean isPublic = true;
    
    @Column(name = "invite_code", unique = true, length = 10)
    private String inviteCode;
    
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

    // Getter and Setter methods
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

    public Long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }

    public Integer getMaxMembers() {
        return maxMembers;
    }

    public void setMaxMembers(Integer maxMembers) {
        this.maxMembers = maxMembers;
    }

    public Integer getCurrentMembers() {
        return currentMembers;
    }

    public void setCurrentMembers(Integer currentMembers) {
        this.currentMembers = currentMembers;
    }

    public Boolean getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }

    public String getInviteCode() {
        return inviteCode;
    }

    public void setInviteCode(String inviteCode) {
        this.inviteCode = inviteCode;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getStudyGoal() {
        return studyGoal;
    }

    public void setStudyGoal(String studyGoal) {
        this.studyGoal = studyGoal;
    }

    public Boolean getTaskSharingEnabled() {
        return taskSharingEnabled;
    }

    public void setTaskSharingEnabled(Boolean taskSharingEnabled) {
        this.taskSharingEnabled = taskSharingEnabled;
    }

    public Boolean getResourceSharingEnabled() {
        return resourceSharingEnabled;
    }

    public void setResourceSharingEnabled(Boolean resourceSharingEnabled) {
        this.resourceSharingEnabled = resourceSharingEnabled;
    }

    public GroupStatus getStatus() {
        return status;
    }

    public void setStatus(GroupStatus status) {
        this.status = status;
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