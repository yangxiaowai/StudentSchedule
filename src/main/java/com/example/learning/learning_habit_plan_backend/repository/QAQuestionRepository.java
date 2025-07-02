package com.example.learning.learning_habit_plan_backend.repository;

import com.example.learning.learning_habit_plan_backend.entity.QAQuestion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface QAQuestionRepository extends JpaRepository<QAQuestion, Long> {
    
    // 查找用户提出的问题
    Page<QAQuestion> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    // 根据状态查找问题
    Page<QAQuestion> findByStatusOrderByCreatedAtDesc(QAQuestion.QuestionStatus status, Pageable pageable);
    
    // 根据学科查找问题
    Page<QAQuestion> findBySubjectOrderByCreatedAtDesc(String subject, Pageable pageable);
    
    // 根据难度等级查找问题
    Page<QAQuestion> findByDifficultyLevelOrderByCreatedAtDesc(QAQuestion.DifficultyLevel difficultyLevel, Pageable pageable);
    
    // 查找小组内的问题
    Page<QAQuestion> findByGroupIdOrderByCreatedAtDesc(Long groupId, Pageable pageable);
    
    // 根据标题模糊查询
    @Query("SELECT q FROM QAQuestion q WHERE q.title LIKE %:keyword% ORDER BY q.createdAt DESC")
    Page<QAQuestion> findByTitleContaining(@Param("keyword") String keyword, Pageable pageable);
    
    // 查找热门问题（按点赞数和回答数排序）
    @Query("SELECT q FROM QAQuestion q ORDER BY (q.likeCount + q.answerCount * 2) DESC, q.createdAt DESC")
    Page<QAQuestion> findPopularQuestions(Pageable pageable);
    
    // 查找未解决的问题
    Page<QAQuestion> findByStatusInOrderByCreatedAtDesc(List<QAQuestion.QuestionStatus> statuses, Pageable pageable);
    
    // 查找没有最佳答案的问题
    Page<QAQuestion> findByBestAnswerIdIsNullOrderByCreatedAtDesc(Pageable pageable);
    
    // 查找有悬赏的问题
    @Query("SELECT q FROM QAQuestion q WHERE q.rewardPoints > 0 ORDER BY q.rewardPoints DESC, q.createdAt DESC")
    Page<QAQuestion> findQuestionsWithReward(Pageable pageable);
    
    // 统计用户提问数量
    long countByUserId(Long userId);
    
    // 根据标签查找问题
    @Query("SELECT q FROM QAQuestion q WHERE q.tags LIKE %:tag% ORDER BY q.createdAt DESC")
    Page<QAQuestion> findByTagsContaining(@Param("tag") String tag, Pageable pageable);
    
    // 查找最近的问题（最近7天）
    @Query("SELECT q FROM QAQuestion q WHERE q.createdAt >= :startDate ORDER BY q.createdAt DESC")
    Page<QAQuestion> findRecentQuestions(@Param("startDate") LocalDateTime startDate, Pageable pageable);
    
    // 查找推荐问题（基于用户学科偏好）
    @Query("SELECT q FROM QAQuestion q WHERE q.subject IN :subjects AND q.status = 'OPEN' ORDER BY q.rewardPoints DESC, q.createdAt DESC")
    Page<QAQuestion> findRecommendedQuestions(@Param("subjects") List<String> subjects, Pageable pageable);
    
    // 统计各状态问题数量
    @Query("SELECT q.status, COUNT(q) FROM QAQuestion q GROUP BY q.status")
    List<Object[]> countByStatus();
}