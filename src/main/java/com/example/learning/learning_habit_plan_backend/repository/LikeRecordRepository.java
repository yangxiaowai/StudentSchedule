package com.example.learning.learning_habit_plan_backend.repository;

import com.example.learning.learning_habit_plan_backend.entity.LikeRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikeRecordRepository extends JpaRepository<LikeRecord, Long> {
    
    // 查找用户对特定目标的点赞记录
    Optional<LikeRecord> findByUserIdAndTargetTypeAndTargetId(Long userId, String targetType, Long targetId);
    
    // 检查用户是否已点赞
    boolean existsByUserIdAndTargetTypeAndTargetId(Long userId, String targetType, Long targetId);
    
    // 删除用户对特定目标的点赞记录
    void deleteByUserIdAndTargetTypeAndTargetId(Long userId, String targetType, Long targetId);
}