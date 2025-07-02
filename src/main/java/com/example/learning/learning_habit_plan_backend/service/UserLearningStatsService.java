package com.example.learning.learning_habit_plan_backend.service;

import com.example.learning.learning_habit_plan_backend.entity.UserLearningStats;
import com.example.learning.learning_habit_plan_backend.entity.UserLearningStats.UserLevel;
import com.example.learning.learning_habit_plan_backend.repository.UserLearningStatsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
@Transactional
public class UserLearningStatsService {
    
    @Autowired
    private UserLearningStatsRepository statsRepository;
    
    /**
     * 获取或创建用户统计信息
     */
    public UserLearningStats getOrCreateUserStats(Long userId) {
        Optional<UserLearningStats> statsOpt = statsRepository.findByUserId(userId);
        if (statsOpt.isPresent()) {
            return statsOpt.get();
        }
        
        UserLearningStats stats = new UserLearningStats(userId);
        return statsRepository.save(stats);
    }
    
    /**
     * 更新学习时长
     */
    public void updateStudyHours(Long userId, double hours) {
        UserLearningStats stats = getOrCreateUserStats(userId);
        
        stats.setTotalStudyHours(stats.getTotalStudyHours() + hours);
        stats.setWeeklyStudyHours(stats.getWeeklyStudyHours() + hours);
        stats.setMonthlyStudyHours(stats.getMonthlyStudyHours() + hours);
        
        // 更新连续学习天数
        updateStreak(stats);
        
        // 根据学习时长增加经验值
        int experienceGain = (int) (hours * 10); // 每小时10经验值
        addExperiencePoints(userId, experienceGain);
        
        statsRepository.save(stats);
    }
    
    /**
     * 增加完成任务数
     */
    public void incrementTasksCompleted(Long userId) {
        UserLearningStats stats = getOrCreateUserStats(userId);
        stats.setTotalTasksCompleted(stats.getTotalTasksCompleted() + 1);
        
        // 完成任务获得经验值
        addExperiencePoints(userId, 15);
        
        statsRepository.save(stats);
    }
    
    /**
     * 增加积分
     */
    public void addPoints(Long userId, int points) {
        UserLearningStats stats = getOrCreateUserStats(userId);
        stats.setTotalPoints(stats.getTotalPoints() + points);
        statsRepository.save(stats);
    }
    
    /**
     * 增加经验值
     */
    public void addExperiencePoints(Long userId, int experience) {
        UserLearningStats stats = getOrCreateUserStats(userId);
        stats.setExperiencePoints(stats.getExperiencePoints() + experience);
        
        // 更新等级
        stats.updateLevel();
        
        statsRepository.save(stats);
    }
    
    /**
     * 增加提问数
     */
    public void incrementQuestionsAsked(Long userId) {
        UserLearningStats stats = getOrCreateUserStats(userId);
        stats.setQuestionsAsked(stats.getQuestionsAsked() + 1);
        statsRepository.save(stats);
    }
    
    /**
     * 增加回答数
     */
    public void incrementQuestionsAnswered(Long userId) {
        UserLearningStats stats = getOrCreateUserStats(userId);
        stats.setQuestionsAnswered(stats.getQuestionsAnswered() + 1);
        statsRepository.save(stats);
    }
    
    /**
     * 增加最佳答案数
     */
    public void incrementBestAnswersCount(Long userId) {
        UserLearningStats stats = getOrCreateUserStats(userId);
        stats.setBestAnswersCount(stats.getBestAnswersCount() + 1);
        statsRepository.save(stats);
    }
    
    /**
     * 减少最佳答案数
     */
    public void decrementBestAnswersCount(Long userId) {
        UserLearningStats stats = getOrCreateUserStats(userId);
        if (stats.getBestAnswersCount() > 0) {
            stats.setBestAnswersCount(stats.getBestAnswersCount() - 1);
            statsRepository.save(stats);
        }
    }
    
    /**
     * 增加分享数
     */
    public void incrementSharesCount(Long userId) {
        UserLearningStats stats = getOrCreateUserStats(userId);
        stats.setSharesCount(stats.getSharesCount() + 1);
        statsRepository.save(stats);
    }
    
    /**
     * 减少分享数
     */
    public void decrementSharesCount(Long userId) {
        UserLearningStats stats = getOrCreateUserStats(userId);
        if (stats.getSharesCount() > 0) {
            stats.setSharesCount(stats.getSharesCount() - 1);
            statsRepository.save(stats);
        }
    }
    
    /**
     * 增加获赞数
     */
    public void incrementLikesReceived(Long userId) {
        UserLearningStats stats = getOrCreateUserStats(userId);
        stats.setLikesReceived(stats.getLikesReceived() + 1);
        statsRepository.save(stats);
    }
    
    /**
     * 减少获赞数
     */
    public void decrementLikesReceived(Long userId) {
        UserLearningStats stats = getOrCreateUserStats(userId);
        if (stats.getLikesReceived() > 0) {
            stats.setLikesReceived(stats.getLikesReceived() - 1);
            statsRepository.save(stats);
        }
    }
    
    /**
     * 增加加入小组数
     */
    public void incrementGroupsJoined(Long userId) {
        UserLearningStats stats = getOrCreateUserStats(userId);
        stats.setGroupsJoined(stats.getGroupsJoined() + 1);
        statsRepository.save(stats);
    }
    
    /**
     * 增加创建小组数
     */
    public void incrementGroupsCreated(Long userId) {
        UserLearningStats stats = getOrCreateUserStats(userId);
        stats.setGroupsCreated(stats.getGroupsCreated() + 1);
        addExperiencePoints(userId, 25); // 创建小组获得25经验值
        statsRepository.save(stats);
    }
    
    /**
     * 更新连续学习天数
     */
    private void updateStreak(UserLearningStats stats) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastStudy = stats.getLastStudyDate();
        
        if (lastStudy == null) {
            // 第一次学习
            stats.setCurrentStreak(1);
            stats.setMaxStreak(1);
        } else {
            long daysBetween = ChronoUnit.DAYS.between(lastStudy.toLocalDate(), now.toLocalDate());
            
            if (daysBetween == 0) {
                // 同一天，不更新连续天数
                return;
            } else if (daysBetween == 1) {
                // 连续学习
                stats.setCurrentStreak(stats.getCurrentStreak() + 1);
                if (stats.getCurrentStreak() > stats.getMaxStreak()) {
                    stats.setMaxStreak(stats.getCurrentStreak());
                }
            } else {
                // 中断了连续学习
                stats.setCurrentStreak(1);
            }
        }
        
        stats.setLastStudyDate(now);
    }
    
    /**
     * 重置周统计
     */
    public void resetWeeklyStats() {
        // 这个方法应该由定时任务调用
        statsRepository.findAll().forEach(stats -> {
            stats.setWeeklyStudyHours(0.0);
            statsRepository.save(stats);
        });
    }
    
    /**
     * 重置月统计
     */
    public void resetMonthlyStats() {
        // 这个方法应该由定时任务调用
        statsRepository.findAll().forEach(stats -> {
            stats.setMonthlyStudyHours(0.0);
            statsRepository.save(stats);
        });
    }
    
    // ==================== 排行榜相关方法 ====================
    
    /**
     * 获取学习时长排行榜
     */
    public Page<UserLearningStats> getStudyHoursLeaderboard(Pageable pageable) {
        return statsRepository.findStudyHoursLeaderboard(pageable);
    }
    
    /**
     * 获取积分排行榜
     */
    public Page<UserLearningStats> getPointsLeaderboard(Pageable pageable) {
        return statsRepository.findPointsLeaderboard(pageable);
    }
    
    /**
     * 获取经验值排行榜
     */
    public Page<UserLearningStats> getExperienceLeaderboard(Pageable pageable) {
        return statsRepository.findExperienceLeaderboard(pageable);
    }
    
    /**
     * 获取连续学习天数排行榜
     */
    public Page<UserLearningStats> getStreakLeaderboard(Pageable pageable) {
        return statsRepository.findStreakLeaderboard(pageable);
    }
    
    /**
     * 获取任务完成数排行榜
     */
    public Page<UserLearningStats> getTasksCompletedLeaderboard(Pageable pageable) {
        return statsRepository.findTasksCompletedLeaderboard(pageable);
    }
    
    /**
     * 获取周学习时长排行榜
     */
    public Page<UserLearningStats> getWeeklyStudyHoursLeaderboard(Pageable pageable) {
        return statsRepository.findWeeklyStudyHoursLeaderboard(pageable);
    }
    
    /**
     * 获取月学习时长排行榜
     */
    public Page<UserLearningStats> getMonthlyStudyHoursLeaderboard(Pageable pageable) {
        return statsRepository.findMonthlyStudyHoursLeaderboard(pageable);
    }
    
    /**
     * 获取最佳答案数排行榜
     */
    public Page<UserLearningStats> getBestAnswersLeaderboard(Pageable pageable) {
        return statsRepository.findBestAnswersLeaderboard(pageable);
    }
    
    /**
     * 获取分享数排行榜
     */
    public Page<UserLearningStats> getSharesLeaderboard(Pageable pageable) {
        return statsRepository.findSharesLeaderboard(pageable);
    }
    
    /**
     * 获取获赞数排行榜
     */
    public Page<UserLearningStats> getLikesReceivedLeaderboard(Pageable pageable) {
        return statsRepository.findLikesReceivedLeaderboard(pageable);
    }
    
    /**
     * 获取用户排名（根据积分）
     */
    public Long getUserRankByPoints(Long userId) {
        return statsRepository.findUserRankByPoints(userId);
    }
    
    /**
     * 获取用户排名（根据学习时长）
     */
    public Long getUserRankByStudyHours(Long userId) {
        return statsRepository.findUserRankByStudyHours(userId);
    }
    
    /**
     * 获取用户统计信息
     */
    public Optional<UserLearningStats> getUserStats(Long userId) {
        return statsRepository.findByUserId(userId);
    }
    
    /**
     * 获取用户积分排名
     */
    public Long getUserPointsRank(Long userId) {
        return statsRepository.findUserRankByPoints(userId);
    }
    
    /**
     * 获取用户学习时长排名
     */
    public Long getUserStudyHoursRank(Long userId) {
        return statsRepository.findUserRankByStudyHours(userId);
    }
    
    /**
     * 增加经验值
     */
    public void addExperience(Long userId, Integer experience) {
        UserLearningStats stats = getOrCreateUserStats(userId);
        stats.setExperiencePoints(stats.getExperiencePoints() + experience);
        
        // 检查是否升级
        checkLevelUp(stats);
        
        statsRepository.save(stats);
    }
    
    /**
     * 检查用户是否升级
     */
    private void checkLevelUp(UserLearningStats stats) {
        int currentExp = stats.getExperiencePoints();
        UserLevel newLevel = calculateLevel(currentExp);
        
        if (newLevel != stats.getLevel()) {
            stats.setLevel(newLevel);
            // 可以在这里添加升级奖励逻辑
        }
    }
    
    /**
     * 根据经验值计算等级
     */
    private UserLevel calculateLevel(int experience) {
        if (experience < 100) {
            return UserLevel.BEGINNER;
        } else if (experience < 500) {
            return UserLevel.INTERMEDIATE;
        } else if (experience < 1000) {
            return UserLevel.ADVANCED;
        } else if (experience < 2000) {
            return UserLevel.EXPERT;
        } else {
            return UserLevel.MASTER;
        }
    }
}