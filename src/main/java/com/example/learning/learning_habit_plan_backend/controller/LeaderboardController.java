package com.example.learning.learning_habit_plan_backend.controller;

import com.example.learning.learning_habit_plan_backend.entity.UserLearningStats;
import com.example.learning.learning_habit_plan_backend.service.UserLearningStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/leaderboard")
@Tag(name = "学习排行榜", description = "学习统计和排行榜相关API")

public class LeaderboardController {
    
    @Autowired
    private UserLearningStatsService userLearningStatsService;
    
    @Operation(summary = "获取用户学习统计")
    @GetMapping("/stats/{userId}")
    public ResponseEntity<Map<String, Object>> getUserStats(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Optional<UserLearningStats> statsOpt = userLearningStatsService.getUserStats(userId);
            if (statsOpt.isPresent()) {
                response.put("success", true);
                response.put("data", statsOpt.get());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "用户统计数据不存在");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @Operation(summary = "获取用户学习统计（兼容性端点）")
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getUserStatsCompat(@PathVariable Long userId) {
        return getUserStats(userId);
    }
    
    @Operation(summary = "学习时长排行榜")
    @GetMapping("/study-hours")
    public ResponseEntity<Map<String, Object>> getStudyHoursLeaderboard(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<UserLearningStats> leaderboard = userLearningStatsService.getStudyHoursLeaderboard(pageable);
            
            response.put("success", true);
            response.put("data", leaderboard.getContent());
            response.put("totalElements", leaderboard.getTotalElements());
            response.put("totalPages", leaderboard.getTotalPages());
            response.put("currentPage", page);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @Operation(summary = "积分排行榜")
    @GetMapping("/points")
    public ResponseEntity<Map<String, Object>> getPointsLeaderboard(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<UserLearningStats> leaderboard = userLearningStatsService.getPointsLeaderboard(pageable);
            
            response.put("success", true);
            response.put("data", leaderboard.getContent());
            response.put("totalElements", leaderboard.getTotalElements());
            response.put("totalPages", leaderboard.getTotalPages());
            response.put("currentPage", page);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @Operation(summary = "经验值排行榜")
    @GetMapping("/experience")
    public ResponseEntity<Map<String, Object>> getExperienceLeaderboard(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<UserLearningStats> leaderboard = userLearningStatsService.getExperienceLeaderboard(pageable);
            
            response.put("success", true);
            response.put("data", leaderboard.getContent());
            response.put("totalElements", leaderboard.getTotalElements());
            response.put("totalPages", leaderboard.getTotalPages());
            response.put("currentPage", page);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @Operation(summary = "连续学习天数排行榜")
    @GetMapping("/streak")
    public ResponseEntity<Map<String, Object>> getStreakLeaderboard(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<UserLearningStats> leaderboard = userLearningStatsService.getStreakLeaderboard(pageable);
            
            response.put("success", true);
            response.put("data", leaderboard.getContent());
            response.put("totalElements", leaderboard.getTotalElements());
            response.put("totalPages", leaderboard.getTotalPages());
            response.put("currentPage", page);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @Operation(summary = "任务完成数排行榜")
    @GetMapping("/tasks-completed")
    public ResponseEntity<Map<String, Object>> getTasksCompletedLeaderboard(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<UserLearningStats> leaderboard = userLearningStatsService.getTasksCompletedLeaderboard(pageable);
            
            response.put("success", true);
            response.put("data", leaderboard.getContent());
            response.put("totalElements", leaderboard.getTotalElements());
            response.put("totalPages", leaderboard.getTotalPages());
            response.put("currentPage", page);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @Operation(summary = "周学习时长排行榜")
    @GetMapping("/weekly-study-hours")
    public ResponseEntity<Map<String, Object>> getWeeklyStudyHoursLeaderboard(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<UserLearningStats> leaderboard = userLearningStatsService.getWeeklyStudyHoursLeaderboard(pageable);
            
            response.put("success", true);
            response.put("data", leaderboard.getContent());
            response.put("totalElements", leaderboard.getTotalElements());
            response.put("totalPages", leaderboard.getTotalPages());
            response.put("currentPage", page);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @Operation(summary = "月学习时长排行榜")
    @GetMapping("/monthly-study-hours")
    public ResponseEntity<Map<String, Object>> getMonthlyStudyHoursLeaderboard(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<UserLearningStats> leaderboard = userLearningStatsService.getMonthlyStudyHoursLeaderboard(pageable);
            
            response.put("success", true);
            response.put("data", leaderboard.getContent());
            response.put("totalElements", leaderboard.getTotalElements());
            response.put("totalPages", leaderboard.getTotalPages());
            response.put("currentPage", page);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @Operation(summary = "最佳回答数排行榜")
    @GetMapping("/best-answers")
    public ResponseEntity<Map<String, Object>> getBestAnswersLeaderboard(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<UserLearningStats> leaderboard = userLearningStatsService.getBestAnswersLeaderboard(pageable);
            
            response.put("success", true);
            response.put("data", leaderboard.getContent());
            response.put("totalElements", leaderboard.getTotalElements());
            response.put("totalPages", leaderboard.getTotalPages());
            response.put("currentPage", page);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @Operation(summary = "分享数排行榜")
    @GetMapping("/shares")
    public ResponseEntity<Map<String, Object>> getSharesLeaderboard(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<UserLearningStats> leaderboard = userLearningStatsService.getSharesLeaderboard(pageable);
            
            response.put("success", true);
            response.put("data", leaderboard.getContent());
            response.put("totalElements", leaderboard.getTotalElements());
            response.put("totalPages", leaderboard.getTotalPages());
            response.put("currentPage", page);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @Operation(summary = "获赞数排行榜")
    @GetMapping("/likes-received")
    public ResponseEntity<Map<String, Object>> getLikesReceivedLeaderboard(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<UserLearningStats> leaderboard = userLearningStatsService.getLikesReceivedLeaderboard(pageable);
            
            response.put("success", true);
            response.put("data", leaderboard.getContent());
            response.put("totalElements", leaderboard.getTotalElements());
            response.put("totalPages", leaderboard.getTotalPages());
            response.put("currentPage", page);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @Operation(summary = "获取用户在积分排行榜中的排名")
    @GetMapping("/rank/points/{userId}")
    public ResponseEntity<Map<String, Object>> getUserPointsRank(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long rank = userLearningStatsService.getUserPointsRank(userId);
            response.put("success", true);
            response.put("rank", rank);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @Operation(summary = "获取用户在积分排行榜中的排名（兼容性端点）")
    @GetMapping("/rank/points")
    public ResponseEntity<Map<String, Object>> getUserPointsRankCompat(@RequestParam Long userId) {
        return getUserPointsRank(userId);
    }
    
    @Operation(summary = "获取用户在学习时长排行榜中的排名")
    @GetMapping("/rank/study-hours/{userId}")
    public ResponseEntity<Map<String, Object>> getUserStudyHoursRank(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long rank = userLearningStatsService.getUserStudyHoursRank(userId);
            response.put("success", true);
            response.put("rank", rank);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @Operation(summary = "更新用户学习时长")
    @PostMapping("/update-study-hours")
    public ResponseEntity<Map<String, Object>> updateStudyHours(
            @RequestParam Long userId,
            @RequestParam Double hours) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            userLearningStatsService.updateStudyHours(userId, hours);
            response.put("success", true);
            response.put("message", "学习时长更新成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "更新失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @Operation(summary = "增加任务完成数")
    @PostMapping("/increment-tasks")
    public ResponseEntity<Map<String, Object>> incrementTasksCompleted(
            @RequestParam Long userId) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            userLearningStatsService.incrementTasksCompleted(userId);
            response.put("success", true);
            response.put("message", "任务完成数更新成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "更新失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @Operation(summary = "增加积分")
    @PostMapping("/add-points")
    public ResponseEntity<Map<String, Object>> addPoints(
            @RequestParam Long userId,
            @RequestParam Integer points) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            userLearningStatsService.addPoints(userId, points);
            response.put("success", true);
            response.put("message", "积分增加成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "更新失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @Operation(summary = "增加经验值")
    @PostMapping("/add-experience")
    public ResponseEntity<Map<String, Object>> addExperience(
            @RequestParam Long userId,
            @RequestParam Integer experience) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            userLearningStatsService.addExperience(userId, experience);
            response.put("success", true);
            response.put("message", "经验值增加成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "更新失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @Operation(summary = "重置周统计")
    @PostMapping("/reset-weekly")
    public ResponseEntity<Map<String, Object>> resetWeeklyStats() {
        Map<String, Object> response = new HashMap<>();
        try {
            userLearningStatsService.resetWeeklyStats();
            response.put("success", true);
            response.put("message", "周统计重置成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "重置失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @Operation(summary = "重置月统计")
    @PostMapping("/reset-monthly")
    public ResponseEntity<Map<String, Object>> resetMonthlyStats() {
        Map<String, Object> response = new HashMap<>();
        try {
            userLearningStatsService.resetMonthlyStats();
            response.put("success", true);
            response.put("message", "月统计重置成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "重置失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}