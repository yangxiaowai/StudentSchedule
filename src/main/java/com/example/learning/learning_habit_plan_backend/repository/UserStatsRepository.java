package com.example.learning.learning_habit_plan_backend.repository;

import com.example.learning.learning_habit_plan_backend.entity.UserStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserStatsRepository extends JpaRepository<UserStats, Long> {
    
    /**
     * 根据用户ID查找用户统计信息
     */
    Optional<UserStats> findByUserId(Long userId);
    
    /**
     * 检查用户统计记录是否存在
     */
    boolean existsByUserId(Long userId);
    
    /**
     * 获取用户加入的小组数量
     */
    @Query("SELECT COUNT(sgm) FROM StudyGroupMember sgm WHERE sgm.userId = :userId")
    Long countGroupsJoinedByUserId(@Param("userId") Long userId);
    
    /**
     * 获取用户分享的学习内容数量
     */
    @Query("SELECT COUNT(ss) FROM StudyShare ss WHERE ss.userId = :userId")
    Long countSharesByUserId(@Param("userId") Long userId);
    
    /**
     * 获取用户提出的问题数量
     */
    @Query("SELECT COUNT(q) FROM QAQuestion q WHERE q.userId = :userId")
    Long countQuestionsAskedByUserId(@Param("userId") Long userId);
}