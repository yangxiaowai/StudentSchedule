package com.example.learning.learning_habit_plan_backend.dto;

import com.example.learning.learning_habit_plan_backend.entity.StudyGroupMember;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StudyGroupMemberDTO {
    private Long id;
    private Long groupId;
    private Long userId;
    private String username;  // 用户名而不是用户ID
    private StudyGroupMember.MemberRole role;
    private Integer contributionScore;
    private Double studyHours;
    private Integer tasksCompleted;
    private Boolean isActive;
    private LocalDateTime joinedAt;
    
    // 构造函数
    public StudyGroupMemberDTO() {}
    
    public StudyGroupMemberDTO(StudyGroupMember member, String username) {
        this.id = member.getId();
        this.groupId = member.getGroupId();
        this.userId = member.getUserId();
        this.username = username;
        this.role = member.getRole();
        this.contributionScore = member.getContributionScore();
        this.studyHours = member.getStudyHours();
        this.tasksCompleted = member.getTasksCompleted();
        this.isActive = member.getIsActive();
        this.joinedAt = member.getJoinedAt();
    }
}