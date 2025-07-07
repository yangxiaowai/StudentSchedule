package com.example.learning.learning_habit_plan_backend.service;

import com.example.learning.learning_habit_plan_backend.entity.UserStats;
import com.example.learning.learning_habit_plan_backend.repository.UserStatsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class UserStatsService {
    
    @Autowired
    private UserStatsRepository userStatsRepository;
    
    /**
     * 获取用户统计信息，如果不存在则创建
     */
    public UserStats getUserStats(Long userId) {
        Optional<UserStats> existingStats = userStatsRepository.findByUserId(userId);
        if (existingStats.isPresent()) {
            return existingStats.get();
        }
        
        // 如果不存在，创建新的统计记录并同步实时数据
        UserStats newStats = new UserStats(userId);
        syncRealTimeData(newStats);
        return userStatsRepository.save(newStats);
    }
    
    /**
     * 同步实时数据到统计记录
     */
    private void syncRealTimeData(UserStats stats) {
        Long userId = stats.getUserId();
        
        try {
            // 同步加入小组数量
            Long groupsJoined = userStatsRepository.countGroupsJoinedByUserId(userId);
            stats.setGroupsJoined(groupsJoined.intValue());
            
            // 同步学习分享数量
            Long sharesCount = userStatsRepository.countSharesByUserId(userId);
            stats.setSharesCount(sharesCount.intValue());
            
            // 同步提出问题数量
            Long questionsAsked = userStatsRepository.countQuestionsAskedByUserId(userId);
            stats.setQuestionsAsked(questionsAsked.intValue());
        } catch (Exception e) {
            // 如果查询失败，使用默认值
            stats.setGroupsJoined(0);
            stats.setSharesCount(0);
            stats.setQuestionsAsked(0);
        }
    }
    
    /**
     * 更新用户统计信息
     */
    public UserStats updateUserStats(UserStats stats) {
        return userStatsRepository.save(stats);
    }
    
    /**
     * 刷新用户统计数据（重新从数据库计算）
     */
    public UserStats refreshUserStats(Long userId) {
        UserStats stats = getUserStats(userId);
        syncRealTimeData(stats);
        return userStatsRepository.save(stats);
    }
    
    /**
     * 增加用户加入小组数量
     */
    public void incrementGroupsJoined(Long userId) {
        UserStats stats = getUserStats(userId);
        stats.incrementGroupsJoined();
        userStatsRepository.save(stats);
    }
    
    /**
     * 增加用户分享数量
     */
    public void incrementSharesCount(Long userId) {
        UserStats stats = getUserStats(userId);
        stats.incrementSharesCount();
        userStatsRepository.save(stats);
    }
    
    /**
     * 增加用户提出问题数量
     */
    public void incrementQuestionsAsked(Long userId) {
        UserStats stats = getUserStats(userId);
        stats.incrementQuestionsAsked();
        userStatsRepository.save(stats);
    }
}