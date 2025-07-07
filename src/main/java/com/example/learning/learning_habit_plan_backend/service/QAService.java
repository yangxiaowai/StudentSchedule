package com.example.learning.learning_habit_plan_backend.service;

import com.example.learning.learning_habit_plan_backend.entity.QAAnswer;
import com.example.learning.learning_habit_plan_backend.entity.QAQuestion;
import com.example.learning.learning_habit_plan_backend.entity.LikeRecord;
import com.example.learning.learning_habit_plan_backend.repository.QAAnswerRepository;
import com.example.learning.learning_habit_plan_backend.repository.QAQuestionRepository;
import com.example.learning.learning_habit_plan_backend.repository.LikeRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class QAService {
    
    @Autowired
    private QAQuestionRepository questionRepository;
    
    @Autowired
    private QAAnswerRepository answerRepository;
    
    // @Autowired
    // private UserLearningStatsService userLearningStatsService; // 移除排行榜相关服务
    
    @Autowired
    private LikeRecordRepository likeRecordRepository;
    
    // ==================== 问题相关方法 ====================
    
    /**
     * 创建问题
     */
    public QAQuestion createQuestion(QAQuestion question) {
        QAQuestion savedQuestion = questionRepository.save(question);
        
        // 移除用户统计信息更新
        // userLearningStatsService.incrementQuestionsAsked(question.getUserId());
        // userLearningStatsService.addExperiencePoints(question.getUserId(), 5); // 提问获得5经验值
        
        return savedQuestion;
    }
    
    /**
     * 获取所有问题（按时间排序）
     */
    public Page<QAQuestion> getAllQuestions(Pageable pageable) {
        return questionRepository.findAllByOrderByCreatedAtDesc(pageable);
    }
    
    /**
     * 根据状态获取问题
     */
    public Page<QAQuestion> getQuestionsByStatus(QAQuestion.QuestionStatus status, Pageable pageable) {
        return questionRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
    }
    
    /**
     * 根据学科获取问题
     */
    public Page<QAQuestion> getQuestionsBySubject(String subject, Pageable pageable) {
        return questionRepository.findBySubjectOrderByCreatedAtDesc(subject, pageable);
    }
    
    /**
     * 根据难度获取问题
     */
    public Page<QAQuestion> getQuestionsByDifficulty(QAQuestion.DifficultyLevel difficulty, Pageable pageable) {
        return questionRepository.findByDifficultyLevelOrderByCreatedAtDesc(difficulty, pageable);
    }
    
    /**
     * 获取小组内的问题
     */
    public Page<QAQuestion> getGroupQuestions(Long groupId, Pageable pageable) {
        return questionRepository.findByGroupIdOrderByCreatedAtDesc(groupId, pageable);
    }
    
    /**
     * 获取用户的问题
     */
    public Page<QAQuestion> getUserQuestions(Long userId, Pageable pageable) {
        return questionRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }
    
    /**
     * 搜索问题
     */
    public Page<QAQuestion> searchQuestions(String keyword, Pageable pageable) {
        return questionRepository.findByTitleContaining(keyword, pageable);
    }
    
    /**
     * 获取热门问题
     */
    public Page<QAQuestion> getPopularQuestions(Pageable pageable) {
        return questionRepository.findPopularQuestions(pageable);
    }
    
    /**
     * 获取未解决的问题
     */
    public Page<QAQuestion> getUnsolvedQuestions(Pageable pageable) {
        List<QAQuestion.QuestionStatus> statuses = Arrays.asList(
            QAQuestion.QuestionStatus.OPEN
        );
        return questionRepository.findByStatusInOrderByCreatedAtDesc(statuses, pageable);
    }
    
    /**
     * 获取有悬赏的问题
     */
    public Page<QAQuestion> getQuestionsWithReward(Pageable pageable) {
        return questionRepository.findQuestionsWithReward(pageable);
    }
    
    /**
     * 根据标签搜索问题
     */
    public Page<QAQuestion> searchQuestionsByTag(String tag, Pageable pageable) {
        return questionRepository.findByTagsContaining(tag, pageable);
    }
    
    /**
     * 获取推荐问题
     */
    public Page<QAQuestion> getRecommendedQuestions(List<String> userSubjects, Pageable pageable) {
        if (userSubjects == null || userSubjects.isEmpty()) {
            return getPopularQuestions(pageable);
        }
        return questionRepository.findRecommendedQuestions(userSubjects, pageable);
    }
    
    /**
     * 获取推荐问题（按点赞数排序）
     */
    public Page<QAQuestion> getRecommendedQuestionsByLikes(Pageable pageable) {
        return questionRepository.findRecommendedQuestionsByLikes(pageable);
    }
    
    /**
     * 获取最近问题
     */
    public Page<QAQuestion> getRecentQuestions(Pageable pageable) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        return questionRepository.findRecentQuestions(startDate, pageable);
    }
    
    /**
     * 关闭问题
     */
    public boolean closeQuestion(Long questionId, Long userId) {
        Optional<QAQuestion> questionOpt = questionRepository.findById(questionId);
        if (!questionOpt.isPresent()) {
            return false;
        }
        
        QAQuestion question = questionOpt.get();
        // 只有问题发布者可以关闭问题
        if (!question.getUserId().equals(userId)) {
            return false;
        }
        
        question.setStatus(QAQuestion.QuestionStatus.CLOSED);
        questionRepository.save(question);
        return true;
    }
    
    /**
     * 获取已关闭的问题
     */
    public Page<QAQuestion> getClosedQuestions(Pageable pageable) {
        return questionRepository.findByStatusOrderByCreatedAtDesc(QAQuestion.QuestionStatus.CLOSED, pageable);
    }
    
    /**
     * 获取用户的已关闭问题（只有发布者可见）
     */
    public Page<QAQuestion> getClosedQuestionsByUser(Long userId, Pageable pageable) {
        return questionRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, QAQuestion.QuestionStatus.CLOSED, pageable);
    }
    
    /**
     * 重新开启问题
     */
    public boolean reopenQuestion(Long questionId, Long userId) {
        Optional<QAQuestion> questionOpt = questionRepository.findById(questionId);
        if (!questionOpt.isPresent()) {
            return false;
        }
        
        QAQuestion question = questionOpt.get();
        // 只有问题发布者可以重新开启问题
        if (!question.getUserId().equals(userId)) {
            return false;
        }
        
        // 只有已关闭的问题才能重新开启
        if (question.getStatus() != QAQuestion.QuestionStatus.CLOSED) {
            return false;
        }
        
        // 根据是否有回答来设置状态
        if (question.getAnswerCount() != null && question.getAnswerCount() > 0) {
            question.setStatus(QAQuestion.QuestionStatus.ANSWERED);
        } else {
            question.setStatus(QAQuestion.QuestionStatus.OPEN);
        }
        
        questionRepository.save(question);
        return true;
    }
    
    /**
     * 点赞问题
     */
    public boolean likeQuestion(Long questionId, Long userId) {
        Optional<QAQuestion> questionOpt = questionRepository.findById(questionId);
        if (!questionOpt.isPresent()) {
            return false;
        }
        
        // 检查用户是否已经点赞过
        if (likeRecordRepository.existsByUserIdAndTargetTypeAndTargetId(userId, "QUESTION", questionId)) {
            return false; // 已经点赞过，不能重复点赞
        }
        
        QAQuestion question = questionOpt.get();
        // 确保likeCount不为null
        if (question.getLikeCount() == null) {
            question.setLikeCount(0);
        }
        question.setLikeCount(question.getLikeCount() + 1);
        questionRepository.save(question);
        
        // 创建点赞记录
        LikeRecord likeRecord = new LikeRecord(userId, "QUESTION", questionId, LocalDateTime.now());
        likeRecordRepository.save(likeRecord);
        
        // 移除提问者的获赞统计更新
        // userLearningStatsService.incrementLikesReceived(question.getUserId());
        // userLearningStatsService.addExperiencePoints(question.getUserId(), 1);
        
        return true;
    }
    
    /**
     * 取消点赞问题
     */
    public boolean unlikeQuestion(Long questionId, Long userId) {
        Optional<QAQuestion> questionOpt = questionRepository.findById(questionId);
        if (!questionOpt.isPresent()) {
            return false;
        }
        
        // 检查用户是否已经点赞过
        if (!likeRecordRepository.existsByUserIdAndTargetTypeAndTargetId(userId, "QUESTION", questionId)) {
            return false; // 没有点赞过，不能取消点赞
        }
        
        QAQuestion question = questionOpt.get();
        // 确保likeCount不为null
        if (question.getLikeCount() == null) {
            question.setLikeCount(0);
        }
        if (question.getLikeCount() > 0) {
            question.setLikeCount(question.getLikeCount() - 1);
            questionRepository.save(question);
            
            // 删除点赞记录
            likeRecordRepository.deleteByUserIdAndTargetTypeAndTargetId(userId, "QUESTION", questionId);
            
            // 移除提问者的获赞统计更新
            // userLearningStatsService.decrementLikesReceived(question.getUserId());
        }
        
        return true;
    }
    
    /**
     * 增加问题浏览量
     */
    public void incrementQuestionViewCount(Long questionId) {
        Optional<QAQuestion> questionOpt = questionRepository.findById(questionId);
        if (questionOpt.isPresent()) {
            QAQuestion question = questionOpt.get();
            // 确保viewCount不为null
        if (question.getViewCount() == null) {
            question.setViewCount(0);
        }
        question.setViewCount(question.getViewCount() + 1);
            questionRepository.save(question);
        }
    }
    
    // ==================== 回答相关方法 ====================
    
    /**
     * 创建回答
     */
    public QAAnswer createAnswer(QAAnswer answer) {
        // 确保answererId字段被设置
        if (answer.getAnswererId() == null && answer.getUserId() != null) {
            answer.setAnswererId(answer.getUserId());
        }
        
        QAAnswer savedAnswer = answerRepository.save(answer);
        
        // 更新问题的回答数量
        Optional<QAQuestion> questionOpt = questionRepository.findById(answer.getQuestionId());
        if (questionOpt.isPresent()) {
            QAQuestion question = questionOpt.get();
            // 确保answerCount不为null
            if (question.getAnswerCount() == null) {
                question.setAnswerCount(0);
            }
            question.setAnswerCount(question.getAnswerCount() + 1);
            
            // 如果是第一个回答，更新问题状态
            if (question.getAnswerCount() == 1) {
                question.setStatus(QAQuestion.QuestionStatus.ANSWERED);
            }
            
            questionRepository.save(question);
        }
        
        // 移除用户统计信息更新
        // userLearningStatsService.incrementQuestionsAnswered(answer.getUserId());
        // userLearningStatsService.addExperiencePoints(answer.getUserId(), 8); // 回答问题获得8经验值
        
        return savedAnswer;
    }
    
    /**
     * 获取问题的回答
     */
    public Page<QAAnswer> getQuestionAnswers(Long questionId, Pageable pageable) {
        return answerRepository.findByQuestionIdOrderByLikeCountDescCreatedAtDesc(questionId, pageable);
    }
    
    /**
     * 获取用户的回答
     */
    public Page<QAAnswer> getUserAnswers(Long userId, Pageable pageable) {
        return answerRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }
    
    /**
     * 设置最佳答案
     */
    public boolean setBestAnswer(Long questionId, Long answerId, Long userId) {
        Optional<QAQuestion> questionOpt = questionRepository.findById(questionId);
        Optional<QAAnswer> answerOpt = answerRepository.findById(answerId);
        
        if (!questionOpt.isPresent() || !answerOpt.isPresent()) {
            return false;
        }
        
        QAQuestion question = questionOpt.get();
        QAAnswer answer = answerOpt.get();
        
        // 只有提问者可以设置最佳答案
        if (!question.getUserId().equals(userId)) {
            return false;
        }
        
        // 检查答案是否属于该问题
        if (!answer.getQuestionId().equals(questionId)) {
            return false;
        }
        
        // 清除之前的最佳答案
        Optional<QAAnswer> previousBestAnswer = answerRepository.findByQuestionIdAndIsBestAnswerTrue(questionId);
        if (previousBestAnswer.isPresent()) {
            QAAnswer prevAnswer = previousBestAnswer.get();
            prevAnswer.setIsBestAnswer(false);
            answerRepository.save(prevAnswer);
            
            // 移除之前最佳答案者的统计更新
            // userLearningStatsService.decrementBestAnswersCount(prevAnswer.getUserId());
        }
        
        // 设置新的最佳答案
        answer.setIsBestAnswer(true);
        answerRepository.save(answer);
        
        // 更新问题的最佳答案ID
        question.setBestAnswerId(answerId);
        questionRepository.save(question);
        
        // 移除回答者的统计信息更新
        // userLearningStatsService.incrementBestAnswersCount(answer.getUserId());
        // userLearningStatsService.addExperiencePoints(answer.getUserId(), 20); // 最佳答案获得20经验值
        
        return true;
    }
    
    /**
     * 点赞回答
     */
    public boolean likeAnswer(Long answerId, Long userId) {
        Optional<QAAnswer> answerOpt = answerRepository.findById(answerId);
        if (!answerOpt.isPresent()) {
            return false;
        }
        
        // 检查用户是否已经点赞过
        if (likeRecordRepository.existsByUserIdAndTargetTypeAndTargetId(userId, "ANSWER", answerId)) {
            return false; // 已经点赞过，不能重复点赞
        }
        
        QAAnswer answer = answerOpt.get();
        // 确保likeCount不为null
        if (answer.getLikeCount() == null) {
            answer.setLikeCount(0);
        }
        answer.setLikeCount(answer.getLikeCount() + 1);
        answerRepository.save(answer);
        
        // 创建点赞记录
        LikeRecord likeRecord = new LikeRecord(userId, "ANSWER", answerId, LocalDateTime.now());
        likeRecordRepository.save(likeRecord);
        
        // 自动更新最佳答案（根据点赞数）
        updateBestAnswerByLikes(answer.getQuestionId());
        
        // 移除回答者的获赞统计更新
        // userLearningStatsService.incrementLikesReceived(answer.getUserId());
        // userLearningStatsService.addExperiencePoints(answer.getUserId(), 2);
        
        return true;
    }
    
    /**
     * 取消点赞回答
     */
    public boolean unlikeAnswer(Long answerId, Long userId) {
        Optional<QAAnswer> answerOpt = answerRepository.findById(answerId);
        if (!answerOpt.isPresent()) {
            return false;
        }
        
        // 检查用户是否已经点赞过
        if (!likeRecordRepository.existsByUserIdAndTargetTypeAndTargetId(userId, "ANSWER", answerId)) {
            return false; // 没有点赞过，不能取消点赞
        }
        
        QAAnswer answer = answerOpt.get();
        // 确保likeCount不为null
        if (answer.getLikeCount() == null) {
            answer.setLikeCount(0);
        }
        if (answer.getLikeCount() > 0) {
            answer.setLikeCount(answer.getLikeCount() - 1);
            answerRepository.save(answer);
            
            // 删除点赞记录
            likeRecordRepository.deleteByUserIdAndTargetTypeAndTargetId(userId, "ANSWER", answerId);
            
            // 自动更新最佳答案（根据点赞数）
            updateBestAnswerByLikes(answer.getQuestionId());
            
            // 移除回答者的获赞统计更新
            // userLearningStatsService.decrementLikesReceived(answer.getUserId());
        }
        
        return true;
    }
    
    /**
     * 获取问题详情
     */
    public Optional<QAQuestion> getQuestionById(Long questionId) {
        return questionRepository.findById(questionId);
    }
    
    /**
     * 获取回答详情
     */
    public Optional<QAAnswer> getAnswerById(Long answerId) {
        return answerRepository.findById(answerId);
    }
    
    /**
     * 检查用户是否已回答问题
     */
    public boolean hasUserAnsweredQuestion(Long questionId, Long userId) {
        return answerRepository.existsByQuestionIdAndUserId(questionId, userId);
    }
    
    /**
     * 更新问题状态
     */
    public boolean updateQuestionStatus(Long questionId, QAQuestion.QuestionStatus status, Long userId) {
        Optional<QAQuestion> questionOpt = questionRepository.findById(questionId);
        if (!questionOpt.isPresent()) {
            return false;
        }
        
        QAQuestion question = questionOpt.get();
        // 只有提问者可以更新问题状态
        if (!question.getUserId().equals(userId)) {
            return false;
        }
        
        question.setStatus(status);
        questionRepository.save(question);
        return true;
    }
    
    /**
     * 解析标签字符串为列表
     */
    public List<String> parseTags(String tags) {
        if (tags == null || tags.trim().isEmpty()) {
            return Arrays.asList();
        }
        return Arrays.asList(tags.split(","));
    }
    
    /**
     * 将标签列表转换为字符串
     */
    public String tagsToString(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return "";
        }
        return String.join(",", tags);
    }
    
    /**
     * 获取用户的最佳答案
     */
    public Page<QAAnswer> getUserBestAnswers(Long userId, Pageable pageable) {
        return answerRepository.findByUserIdAndIsBestAnswerTrueOrderByCreatedAtDesc(userId, pageable);
    }
    
    /**
     * 获取未解决的问题
     */
    public Page<QAQuestion> getUnresolvedQuestions(Pageable pageable) {
        return questionRepository.findByBestAnswerIdIsNullOrderByCreatedAtDesc(pageable);
    }
    
    /**
     * 获取问题的热门回答
     */
    public Page<QAAnswer> getQuestionPopularAnswers(Long questionId, Pageable pageable) {
        return answerRepository.findByQuestionIdOrderByLikeCountDescCreatedAtDesc(questionId, pageable);
    }
    
    /**
     * 检查用户是否已点赞问题
     */
    public boolean isQuestionLikedByUser(Long questionId, Long userId) {
        return likeRecordRepository.existsByUserIdAndTargetTypeAndTargetId(userId, "QUESTION", questionId);
    }
    
    /**
     * 根据点赞数自动更新最佳答案
     */
    public void updateBestAnswerByLikes(Long questionId) {
        // 获取该问题的所有回答，按点赞数降序排列
        Page<QAAnswer> answers = answerRepository.findByQuestionIdOrderByLikeCountDescCreatedAtDesc(
            questionId, PageRequest.of(0, 1));
        
        if (answers.isEmpty()) {
            return; // 没有回答，无需更新
        }
        
        QAAnswer topAnswer = answers.getContent().get(0);
        
        // 检查当前最佳答案
        Optional<QAAnswer> currentBestAnswer = answerRepository.findByQuestionIdAndIsBestAnswerTrue(questionId);
        
        // 如果当前最佳答案就是点赞数最高的答案，无需更新
        if (currentBestAnswer.isPresent() && currentBestAnswer.get().getId().equals(topAnswer.getId())) {
            return;
        }
        
        // 清除之前的最佳答案标记
        if (currentBestAnswer.isPresent()) {
            QAAnswer prevBestAnswer = currentBestAnswer.get();
            prevBestAnswer.setIsBestAnswer(false);
            answerRepository.save(prevBestAnswer);
        }
        
        // 设置新的最佳答案（只有当点赞数大于0时才设置）
        if (topAnswer.getLikeCount() != null && topAnswer.getLikeCount() > 0) {
            topAnswer.setIsBestAnswer(true);
            answerRepository.save(topAnswer);
            
            // 更新问题的最佳答案ID
            Optional<QAQuestion> questionOpt = questionRepository.findById(questionId);
            if (questionOpt.isPresent()) {
                QAQuestion question = questionOpt.get();
                question.setBestAnswerId(topAnswer.getId());
                questionRepository.save(question);
            }
        }
    }
    
    /**
     * 检查用户是否已点赞回答
     */
    public boolean isAnswerLikedByUser(Long answerId, Long userId) {
        return likeRecordRepository.existsByUserIdAndTargetTypeAndTargetId(userId, "ANSWER", answerId);
    }
}