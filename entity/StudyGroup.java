package com.example.learning.learning_habit_plan_backend.entity;

import jakarta.persistence.*;
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
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(length = 100)
    private String subject;
    
    @Column(columnDefinition = "TEXT")
    private String studyGoal;
    
    @Column(name = "max_members")
    private Integer maxMembers = 10;
    
    @Column(name = "current_members")
    private Integer currentMembers = 0;
    
    @Column(name = "is_public")
    private Boolean isPublic = true;
    
    @Column(name = "creator_id", nullable = false)
    private Long creatorId;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // 关联关系
    @OneToMany(mappedBy = "groupId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<GroupMember> members;
    
    // 构造函数
    public StudyGroup() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
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
    
    public Long getCreatorId() {
        return creatorId;
    }
    
    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
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
    
    public List<GroupMember> getMembers() {
        return members;
    }
    
    public void setMembers(List<GroupMember> members) {
        this.members = members;
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}