package com.example.learning.learning_habit_plan_backend.repository;

import com.example.learning.learning_habit_plan_backend.entity.QAAnswer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QAAnswerRepository extends JpaRepository<QAAnswer, Long> {
    
    // 查找问题的所有回答
    Page<QAAnswer> findByQuestionIdOrderByCreatedAtDesc(Long questionId, Pageable pageable);
    
    // 查找问题的所有回答（按点赞数排序）
    Page<QAAnswer> findByQuestionIdOrderByLikeCountDescCreatedAtDesc(Long questionId, Pageable pageable);
    
    // 查找用户的回答
    Page<QAAnswer> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    // 查找问题的最佳答案
    Optional<QAAnswer> findByQuestionIdAndIsBestAnswerTrue(Long questionId);
    
    // 查找用户的最佳答案
    List<QAAnswer> findByUserIdAndIsBestAnswerTrue(Long userId);
    
    // 查找用户的最佳答案（分页）
    Page<QAAnswer> findByUserIdAndIsBestAnswerTrueOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    // 统计问题的回答数量
    long countByQuestionId(Long questionId);
    
    // 统计用户回答数量
    long countByUserId(Long userId);
    
    // 统计用户最佳答案数量
    long countByUserIdAndIsBestAnswerTrue(Long userId);
    
    // 统计用户获得的总点赞数
    @Query("SELECT COALESCE(SUM(a.likeCount), 0) FROM QAAnswer a WHERE a.userId = :userId")
    Long getTotalLikesByUserId(@Param("userId") Long userId);
    
    // 查找用户最受欢迎的回答
    @Query("SELECT a FROM QAAnswer a WHERE a.userId = :userId ORDER BY a.likeCount DESC")
    Page<QAAnswer> findUserPopularAnswers(@Param("userId") Long userId, Pageable pageable);
    
    // 查找有用的回答
    List<QAAnswer> findByQuestionIdAndIsHelpfulTrueOrderByLikeCountDesc(Long questionId);
    
    // 检查用户是否已回答某个问题
    boolean existsByQuestionIdAndUserId(Long questionId, Long userId);
    
    // 查找最近的回答
    @Query("SELECT a FROM QAAnswer a ORDER BY a.createdAt DESC")
    Page<QAAnswer> findRecentAnswers(Pageable pageable);
    
    // 查找热门回答（按点赞数排序）
    @Query("SELECT a FROM QAAnswer a ORDER BY a.likeCount DESC, a.createdAt DESC")
    Page<QAAnswer> findPopularAnswers(Pageable pageable);
}