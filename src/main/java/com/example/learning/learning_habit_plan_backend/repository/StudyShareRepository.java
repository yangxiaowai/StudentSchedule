package com.example.learning.learning_habit_plan_backend.repository;

import com.example.learning.learning_habit_plan_backend.entity.StudyShare;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StudyShareRepository extends JpaRepository<StudyShare, Long> {
    
    // 查找用户的分享
    Page<StudyShare> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    // 查找公开的分享
    Page<StudyShare> findByIsPublicTrueOrderByCreatedAtDesc(Pageable pageable);
    
    // 根据分享类型查找
    Page<StudyShare> findByShareTypeAndIsPublicTrueOrderByCreatedAtDesc(StudyShare.ShareType shareType, Pageable pageable);
    
    // 根据学科查找分享
    Page<StudyShare> findBySubjectAndIsPublicTrueOrderByCreatedAtDesc(String subject, Pageable pageable);
    
    // 查找小组内的分享
    Page<StudyShare> findByGroupIdOrderByCreatedAtDesc(Long groupId, Pageable pageable);
    
    // 根据标题模糊查询
    @Query("SELECT ss FROM StudyShare ss WHERE ss.title LIKE %:keyword% AND ss.isPublic = true ORDER BY ss.createdAt DESC")
    Page<StudyShare> findByTitleContainingAndIsPublicTrue(@Param("keyword") String keyword, Pageable pageable);
    
    // 查找热门分享（按点赞数排序）
    @Query("SELECT ss FROM StudyShare ss WHERE ss.isPublic = true ORDER BY ss.likeCount DESC, ss.createdAt DESC")
    Page<StudyShare> findPopularShares(Pageable pageable);
    
    // 查找最近热门分享（最近7天内按点赞数排序）
    @Query("SELECT ss FROM StudyShare ss WHERE ss.isPublic = true AND ss.createdAt >= :startDate ORDER BY ss.likeCount DESC, ss.createdAt DESC")
    Page<StudyShare> findRecentPopularShares(@Param("startDate") LocalDateTime startDate, Pageable pageable);
    
    // 统计用户分享数量
    long countByUserId(Long userId);
    
    // 统计用户获得的总点赞数
    @Query("SELECT COALESCE(SUM(ss.likeCount), 0) FROM StudyShare ss WHERE ss.userId = :userId")
    Long getTotalLikesByUserId(@Param("userId") Long userId);
    
    // 根据标签查找分享
    @Query("SELECT ss FROM StudyShare ss WHERE ss.tags LIKE %:tag% AND ss.isPublic = true ORDER BY ss.createdAt DESC")
    Page<StudyShare> findByTagsContaining(@Param("tag") String tag, Pageable pageable);
    
    // 查找推荐分享（基于用户学科偏好）
    @Query("SELECT ss FROM StudyShare ss WHERE ss.subject IN :subjects AND ss.isPublic = true ORDER BY ss.likeCount DESC, ss.createdAt DESC")
    Page<StudyShare> findRecommendedShares(@Param("subjects") List<String> subjects, Pageable pageable);
}