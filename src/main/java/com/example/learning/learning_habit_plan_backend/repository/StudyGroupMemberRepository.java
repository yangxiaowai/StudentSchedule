package com.example.learning.learning_habit_plan_backend.repository;

import com.example.learning.learning_habit_plan_backend.entity.StudyGroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudyGroupMemberRepository extends JpaRepository<StudyGroupMember, Long> {
    
    // 查找用户在某个小组的成员信息（只查找活跃成员）
    Optional<StudyGroupMember> findByGroupIdAndUserIdAndIsActiveTrue(Long groupId, Long userId);
    
    // 查找小组的所有成员
    List<StudyGroupMember> findByGroupIdAndIsActiveTrue(Long groupId);
    
    // 查找用户加入的所有小组
    List<StudyGroupMember> findByUserIdAndIsActiveTrue(Long userId);
    
    // 统计小组成员数量
    long countByGroupIdAndIsActiveTrue(Long groupId);
    
    // 统计用户加入的小组数量
    long countByUserIdAndIsActiveTrue(Long userId);
    
    // 查找小组管理员
    List<StudyGroupMember> findByGroupIdAndRoleInAndIsActiveTrue(Long groupId, List<StudyGroupMember.MemberRole> roles);
    
    // 移除小组排行榜相关查询方法
    // @Query("SELECT sgm FROM StudyGroupMember sgm WHERE sgm.groupId = :groupId AND sgm.isActive = true ORDER BY sgm.contributionScore DESC")
    // List<StudyGroupMember> findGroupLeaderboard(@Param("groupId") Long groupId);
    
    // @Query("SELECT sgm FROM StudyGroupMember sgm WHERE sgm.groupId = :groupId AND sgm.isActive = true ORDER BY sgm.studyHours DESC")
    // List<StudyGroupMember> findGroupStudyHoursLeaderboard(@Param("groupId") Long groupId);
    
    // 检查用户是否为小组成员
    boolean existsByGroupIdAndUserIdAndIsActiveTrue(Long groupId, Long userId);
    
    // 删除小组成员（软删除）
    @Modifying
    @Query("UPDATE StudyGroupMember sgm SET sgm.isActive = false WHERE sgm.groupId = :groupId AND sgm.userId = :userId")
    void softDeleteMember(@Param("groupId") Long groupId, @Param("userId") Long userId);
}