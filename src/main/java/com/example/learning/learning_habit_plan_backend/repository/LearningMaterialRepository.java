package com.example.learning.learning_habit_plan_backend.repository;

import com.example.learning.learning_habit_plan_backend.entity.LearningMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LearningMaterialRepository extends JpaRepository<LearningMaterial, Long> {
    List<LearningMaterial> findByUserId(Long userId);
    List<LearningMaterial> findByUserIdAndSubject(Long userId, String subject);
    List<LearningMaterial> findByUserIdAndContentType(Long userId, String contentType);

    // 根据用户ID列表查询资料（用于小组资料库共享）
    List<LearningMaterial> findByUserIdIn(List<Long> userIds);

    // 根据用户ID列表和学科查询资料
    List<LearningMaterial> findByUserIdInAndSubject(List<Long> userIds, String subject);


    @Query("SELECT m FROM LearningMaterial m WHERE m.filePath LIKE %:fileName%")
    List<LearningMaterial> findByFilePathContaining(@Param("fileName") String fileName);
    

    @Query("SELECT m FROM LearningMaterial m WHERE m.filePath LIKE CONCAT('%', :fileName)")
    List<LearningMaterial> findByFilePathEndingWith(@Param("fileName") String fileName);
}
