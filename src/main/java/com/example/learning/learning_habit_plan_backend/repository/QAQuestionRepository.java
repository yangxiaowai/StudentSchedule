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
    
    // 查找所有问题（按创建时间降序）
    Page<QAQuestion> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    // 查找用户提出的问题
    Page<QAQuestion> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    // 根据状态查找问题
    Page<QAQuestion> findByStatusOrderByCreatedAtDesc(QAQuestion.QuestionStatus status, Pageable pageable);
    
    // 根据用户ID和状态查找问题
    Page<QAQuestion> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, QAQuestion.QuestionStatus status, Pageable pageable);
    
    // 根据学科查找问题
    Page<QAQuestion> findBySubjectOrderByCreatedAtDesc(String subject, Pageable pageable);
    
    // 根据学科查找问题（排除已关闭的问题）
    @Query("SELECT q FROM QAQuestion q WHERE q.subject = :subject AND q.status IN :statuses ORDER BY q.createdAt DESC")
    Page<QAQuestion> findBySubjectAndStatusIn(@Param("subject") String subject, @Param("statuses") List<QAQuestion.QuestionStatus> statuses, Pageable pageable);
    
    // 根据难度等级查找问题
    Page<QAQuestion> findByDifficultyLevelOrderByCreatedAtDesc(QAQuestion.DifficultyLevel difficultyLevel, Pageable pageable);
    
    // 根据难度等级查找问题（排除已关闭的问题）
    @Query("SELECT q FROM QAQuestion q WHERE q.difficultyLevel = :difficultyLevel AND q.status IN :statuses ORDER BY q.createdAt DESC")
    Page<QAQuestion> findByDifficultyLevelAndStatusIn(@Param("difficultyLevel") QAQuestion.DifficultyLevel difficultyLevel, @Param("statuses") List<QAQuestion.QuestionStatus> statuses, Pageable pageable);
    
    // 查找小组内的问题
    Page<QAQuestion> findByGroupIdOrderByCreatedAtDesc(Long groupId, Pageable pageable);
    
    // 查找小组内的问题（排除已关闭的问题）
    @Query("SELECT q FROM QAQuestion q WHERE q.groupId = :groupId AND q.status IN :statuses ORDER BY q.createdAt DESC")
    Page<QAQuestion> findByGroupIdAndStatusIn(@Param("groupId") Long groupId, @Param("statuses") List<QAQuestion.QuestionStatus> statuses, Pageable pageable);
    
    // 根据标题模糊查询
    @Query("SELECT q FROM QAQuestion q WHERE q.title LIKE CONCAT('%', :keyword, '%') ORDER BY q.createdAt DESC")
    Page<QAQuestion> findByTitleContaining(@Param("keyword") String keyword, Pageable pageable);
    
    // 根据标题模糊查询并过滤状态
    @Query("SELECT q FROM QAQuestion q WHERE q.title LIKE CONCAT('%', :keyword, '%') AND q.status IN :statuses ORDER BY q.createdAt DESC")
    Page<QAQuestion> findByTitleContainingAndStatusIn(@Param("keyword") String keyword, @Param("statuses") List<QAQuestion.QuestionStatus> statuses, Pageable pageable);
    
    // 根据状态列表查找问题（按创建时间降序）
    Page<QAQuestion> findByStatusInOrderByCreatedAtDesc(List<QAQuestion.QuestionStatus> statuses, Pageable pageable);
    
    // 查找热门问题（按点赞数和回答数排序）
    @Query("SELECT q FROM QAQuestion q ORDER BY (q.likeCount + q.answerCount * 2) DESC, q.createdAt DESC")
    Page<QAQuestion> findPopularQuestions(Pageable pageable);
    
    // 查找热门问题（排除已关闭的问题）
    @Query("SELECT q FROM QAQuestion q WHERE q.status IN :statuses ORDER BY (q.likeCount + q.answerCount * 2) DESC, q.createdAt DESC")
    Page<QAQuestion> findPopularQuestionsByStatus(@Param("statuses") List<QAQuestion.QuestionStatus> statuses, Pageable pageable);
    
    // 查找没有最佳答案的问题
    Page<QAQuestion> findByBestAnswerIdIsNullOrderByCreatedAtDesc(Pageable pageable);
    
    // 查找有悬赏的问题
    @Query("SELECT q FROM QAQuestion q WHERE q.rewardPoints > 0 ORDER BY q.rewardPoints DESC, q.createdAt DESC")
    Page<QAQuestion> findQuestionsWithReward(Pageable pageable);
    
    // 查找有悬赏的问题（排除已关闭的问题）
    @Query("SELECT q FROM QAQuestion q WHERE q.rewardPoints > 0 AND q.status IN :statuses ORDER BY q.rewardPoints DESC, q.createdAt DESC")
    Page<QAQuestion> findQuestionsWithRewardAndStatus(@Param("statuses") List<QAQuestion.QuestionStatus> statuses, Pageable pageable);
    
    // 统计用户提问数量
    long countByUserId(Long userId);
    
    // 根据标签查找问题
    @Query("SELECT q FROM QAQuestion q WHERE q.tags LIKE CONCAT('%', :tag, '%') ORDER BY q.createdAt DESC")
    Page<QAQuestion> findByTagsContaining(@Param("tag") String tag, Pageable pageable);
    
    // 根据标签查找问题（排除已关闭的问题）
    @Query("SELECT q FROM QAQuestion q WHERE q.tags LIKE CONCAT('%', :tag, '%') AND q.status IN :statuses ORDER BY q.createdAt DESC")
    Page<QAQuestion> findByTagsContainingAndStatusIn(@Param("tag") String tag, @Param("statuses") List<QAQuestion.QuestionStatus> statuses, Pageable pageable);
    
    // 查找最近的问题（最近7天）
    @Query("SELECT q FROM QAQuestion q WHERE q.createdAt >= :startDate ORDER BY q.createdAt DESC")
    Page<QAQuestion> findRecentQuestions(@Param("startDate") LocalDateTime startDate, Pageable pageable);
    
    // 查找最近的问题（最近7天，排除已关闭的问题）
    @Query("SELECT q FROM QAQuestion q WHERE q.createdAt >= :startDate AND q.status IN :statuses ORDER BY q.createdAt DESC")
    Page<QAQuestion> findRecentQuestionsByStatus(@Param("startDate") LocalDateTime startDate, @Param("statuses") List<QAQuestion.QuestionStatus> statuses, Pageable pageable);
    
    // 查找推荐问题（基于用户学科偏好）
    @Query("SELECT q FROM QAQuestion q WHERE q.subject IN :subjects AND q.status = 'OPEN' ORDER BY q.rewardPoints DESC, q.createdAt DESC")
    Page<QAQuestion> findRecommendedQuestions(@Param("subjects") List<String> subjects, Pageable pageable);
    
    // 查找推荐问题（基于用户学科偏好，排除已关闭的问题）
    @Query("SELECT q FROM QAQuestion q WHERE q.subject IN :subjects AND q.status IN :statuses ORDER BY q.rewardPoints DESC, q.createdAt DESC")
    Page<QAQuestion> findRecommendedQuestionsBySubjectsAndStatus(@Param("subjects") List<String> subjects, @Param("statuses") List<QAQuestion.QuestionStatus> statuses, Pageable pageable);
    
    // 查找推荐问题（按点赞数排序）
    @Query("SELECT q FROM QAQuestion q ORDER BY q.likeCount DESC, q.createdAt DESC")
    Page<QAQuestion> findRecommendedQuestionsByLikes(Pageable pageable);
    
    // 查找推荐问题（按点赞数排序，排除已关闭的问题）
    @Query("SELECT q FROM QAQuestion q WHERE q.status IN :statuses ORDER BY q.likeCount DESC, q.createdAt DESC")
    Page<QAQuestion> findRecommendedQuestionsByLikesAndStatus(@Param("statuses") List<QAQuestion.QuestionStatus> statuses, Pageable pageable);
    
    // 统计各状态问题数量
    @Query("SELECT q.status, COUNT(q) FROM QAQuestion q GROUP BY q.status")
    List<Object[]> countByStatus();
}