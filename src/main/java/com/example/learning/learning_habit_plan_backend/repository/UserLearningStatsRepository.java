package com.example.learning.learning_habit_plan_backend.repository;

import com.example.learning.learning_habit_plan_backend.entity.UserLearningStats;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserLearningStatsRepository extends JpaRepository<UserLearningStats, Long> {
    
    // 根据用户ID查找统计信息
    Optional<UserLearningStats> findByUserId(Long userId);
    
    // 学习时长排行榜
    @Query("SELECT uls FROM UserLearningStats uls ORDER BY uls.totalStudyHours DESC")
    Page<UserLearningStats> findStudyHoursLeaderboard(Pageable pageable);
    
    // 积分排行榜
    @Query("SELECT uls FROM UserLearningStats uls ORDER BY uls.totalPoints DESC")
    Page<UserLearningStats> findPointsLeaderboard(Pageable pageable);
    
    // 经验值排行榜
    @Query("SELECT uls FROM UserLearningStats uls ORDER BY uls.experiencePoints DESC")
    Page<UserLearningStats> findExperienceLeaderboard(Pageable pageable);
    
    // 连续学习天数排行榜
    @Query("SELECT uls FROM UserLearningStats uls ORDER BY uls.currentStreak DESC")
    Page<UserLearningStats> findStreakLeaderboard(Pageable pageable);
    
    // 任务完成数排行榜
    @Query("SELECT uls FROM UserLearningStats uls ORDER BY uls.totalTasksCompleted DESC")
    Page<UserLearningStats> findTasksCompletedLeaderboard(Pageable pageable);
    
    // 周学习时长排行榜
    @Query("SELECT uls FROM UserLearningStats uls ORDER BY uls.weeklyStudyHours DESC")
    Page<UserLearningStats> findWeeklyStudyHoursLeaderboard(Pageable pageable);
    
    // 月学习时长排行榜
    @Query("SELECT uls FROM UserLearningStats uls ORDER BY uls.monthlyStudyHours DESC")
    Page<UserLearningStats> findMonthlyStudyHoursLeaderboard(Pageable pageable);
    
    // 根据等级查找用户
    List<UserLearningStats> findByLevel(UserLearningStats.UserLevel level);
    
    // 统计各等级用户数量
    @Query("SELECT uls.level, COUNT(uls) FROM UserLearningStats uls GROUP BY uls.level")
    List<Object[]> countByLevel();
    
    // 查找最佳答案数排行榜
    @Query("SELECT uls FROM UserLearningStats uls ORDER BY uls.bestAnswersCount DESC")
    Page<UserLearningStats> findBestAnswersLeaderboard(Pageable pageable);
    
    // 查找分享数排行榜
    @Query("SELECT uls FROM UserLearningStats uls ORDER BY uls.sharesCount DESC")
    Page<UserLearningStats> findSharesLeaderboard(Pageable pageable);
    
    // 查找获赞数排行榜
    @Query("SELECT uls FROM UserLearningStats uls ORDER BY uls.likesReceived DESC")
    Page<UserLearningStats> findLikesReceivedLeaderboard(Pageable pageable);
    
    // 查找用户排名（根据总积分）
    @Query("SELECT COUNT(uls) + 1 FROM UserLearningStats uls WHERE uls.totalPoints > (SELECT u.totalPoints FROM UserLearningStats u WHERE u.userId = :userId)")
    Long findUserRankByPoints(@Param("userId") Long userId);
    
    // 查找用户排名（根据学习时长）
    @Query("SELECT COUNT(uls) + 1 FROM UserLearningStats uls WHERE uls.totalStudyHours > (SELECT u.totalStudyHours FROM UserLearningStats u WHERE u.userId = :userId)")
    Long findUserRankByStudyHours(@Param("userId") Long userId);
    
    // 查找活跃用户（本周有学习记录）
    @Query("SELECT uls FROM UserLearningStats uls WHERE uls.weeklyStudyHours > 0 ORDER BY uls.weeklyStudyHours DESC")
    Page<UserLearningStats> findActiveUsers(Pageable pageable);
    
    // 查找新手用户（经验值较低）
    @Query("SELECT uls FROM UserLearningStats uls WHERE uls.experiencePoints < 1000 ORDER BY uls.experiencePoints DESC")
    Page<UserLearningStats> findNewbieUsers(Pageable pageable);
}