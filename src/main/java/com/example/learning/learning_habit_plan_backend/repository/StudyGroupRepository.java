package com.example.learning.learning_habit_plan_backend.repository;

import com.example.learning.learning_habit_plan_backend.entity.StudyGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudyGroupRepository extends JpaRepository<StudyGroup, Long> {
    
    // 根据创建者ID查找小组
    List<StudyGroup> findByCreatorId(Long creatorId);
    
    // 根据邀请码查找小组
    Optional<StudyGroup> findByInviteCode(String inviteCode);
    
    // 查找公开的小组
    Page<StudyGroup> findByIsPublicTrueAndStatus(StudyGroup.GroupStatus status, Pageable pageable);
    
    // 根据学科查找小组
    Page<StudyGroup> findBySubjectAndIsPublicTrueAndStatus(String subject, StudyGroup.GroupStatus status, Pageable pageable);
    
    // 根据名称模糊查询
    @Query("SELECT sg FROM StudyGroup sg WHERE sg.name LIKE %:keyword% AND sg.isPublic = true AND sg.status = :status")
    Page<StudyGroup> findByNameContainingAndIsPublicTrueAndStatus(@Param("keyword") String keyword, @Param("status") StudyGroup.GroupStatus status, Pageable pageable);
    
    // 查找热门小组（按成员数量排序）
    @Query("SELECT sg FROM StudyGroup sg WHERE sg.isPublic = true AND sg.status = :status ORDER BY sg.currentMembers DESC")
    Page<StudyGroup> findPopularGroups(@Param("status") StudyGroup.GroupStatus status, Pageable pageable);
    
    // 统计用户创建的小组数量
    long countByCreatorId(Long creatorId);
    
    // 查找活跃的小组
    List<StudyGroup> findByStatus(StudyGroup.GroupStatus status);
}