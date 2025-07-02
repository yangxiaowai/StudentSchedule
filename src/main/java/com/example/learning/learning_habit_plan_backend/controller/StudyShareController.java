package com.example.learning.learning_habit_plan_backend.controller;

import com.example.learning.learning_habit_plan_backend.entity.StudyShare;
import com.example.learning.learning_habit_plan_backend.service.StudyShareService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/study-shares")
@Tag(name = "学习分享管理", description = "学习分享相关API")

public class StudyShareController {
    
    @Autowired
    private StudyShareService studyShareService;
    
    @Operation(summary = "创建学习分享")
    @PostMapping
    public ResponseEntity<Map<String, Object>> createShare(@RequestBody StudyShare share) {
        Map<String, Object> response = new HashMap<>();
        try {
            StudyShare createdShare = studyShareService.createShare(share);
            response.put("success", true);
            response.put("message", "学习分享创建成功");
            response.put("data", createdShare);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "创建失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @Operation(summary = "获取公开分享列表")
    @GetMapping("/public")
    public ResponseEntity<Page<StudyShare>> getPublicShares(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<StudyShare> shares = studyShareService.getPublicShares(pageable);
            
            System.out.println("Found " + shares.getTotalElements() + " shares");
            System.out.println("Content size: " + shares.getContent().size());
            
            return ResponseEntity.ok(shares);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
    
    @Operation(summary = "根据分享类型获取分享")
    @GetMapping("/type/{shareType}")
    public ResponseEntity<Map<String, Object>> getSharesByType(
            @PathVariable StudyShare.ShareType shareType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<StudyShare> shares = studyShareService.getSharesByType(shareType, pageable);
            
            response.put("success", true);
            response.put("data", shares.getContent());
            response.put("totalElements", shares.getTotalElements());
            response.put("totalPages", shares.getTotalPages());
            response.put("currentPage", page);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @Operation(summary = "根据学科获取分享")
    @GetMapping("/subject/{subject}")
    public ResponseEntity<Map<String, Object>> getSharesBySubject(
            @PathVariable String subject,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<StudyShare> shares = studyShareService.getSharesBySubject(subject, pageable);
            
            response.put("success", true);
            response.put("data", shares.getContent());
            response.put("totalElements", shares.getTotalElements());
            response.put("totalPages", shares.getTotalPages());
            response.put("currentPage", page);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @Operation(summary = "获取小组内的分享")
    @GetMapping("/group/{groupId}")
    public ResponseEntity<Map<String, Object>> getGroupShares(
            @PathVariable Long groupId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<StudyShare> shares = studyShareService.getGroupShares(groupId, pageable);
            
            response.put("success", true);
            response.put("data", shares.getContent());
            response.put("totalElements", shares.getTotalElements());
            response.put("totalPages", shares.getTotalPages());
            response.put("currentPage", page);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @Operation(summary = "获取用户的分享")
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getUserShares(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<StudyShare> shares = studyShareService.getUserShares(userId, pageable);
            
            response.put("success", true);
            response.put("data", shares.getContent());
            response.put("totalElements", shares.getTotalElements());
            response.put("totalPages", shares.getTotalPages());
            response.put("currentPage", page);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @Operation(summary = "搜索分享")
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchShares(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<StudyShare> shares = studyShareService.searchShares(keyword, pageable);
            
            response.put("success", true);
            response.put("data", shares.getContent());
            response.put("totalElements", shares.getTotalElements());
            response.put("totalPages", shares.getTotalPages());
            response.put("currentPage", page);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "搜索失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @Operation(summary = "获取热门分享")
    @GetMapping("/popular")
    public ResponseEntity<Map<String, Object>> getPopularShares(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<StudyShare> shares = studyShareService.getPopularShares(pageable);
            
            response.put("success", true);
            response.put("data", shares.getContent());
            response.put("totalElements", shares.getTotalElements());
            response.put("totalPages", shares.getTotalPages());
            response.put("currentPage", page);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @Operation(summary = "获取最近热门分享")
    @GetMapping("/recent-popular")
    public ResponseEntity<Map<String, Object>> getRecentPopularShares(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<StudyShare> shares = studyShareService.getRecentPopularShares(pageable);
            
            response.put("success", true);
            response.put("data", shares.getContent());
            response.put("totalElements", shares.getTotalElements());
            response.put("totalPages", shares.getTotalPages());
            response.put("currentPage", page);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @Operation(summary = "根据标签搜索分享")
    @GetMapping("/search/tag")
    public ResponseEntity<Map<String, Object>> searchSharesByTag(
            @RequestParam String tag,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<StudyShare> shares = studyShareService.searchSharesByTag(tag, pageable);
            
            response.put("success", true);
            response.put("data", shares.getContent());
            response.put("totalElements", shares.getTotalElements());
            response.put("totalPages", shares.getTotalPages());
            response.put("currentPage", page);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "搜索失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @Operation(summary = "获取推荐分享")
    @GetMapping("/recommended")
    public ResponseEntity<Map<String, Object>> getRecommendedShares(
            @RequestParam(required = false) String subjects,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            Pageable pageable = PageRequest.of(page, size);
            List<String> subjectList = subjects != null ? Arrays.asList(subjects.split(",")) : null;
            Page<StudyShare> shares = studyShareService.getRecommendedShares(subjectList, pageable);
            
            response.put("success", true);
            response.put("data", shares.getContent());
            response.put("totalElements", shares.getTotalElements());
            response.put("totalPages", shares.getTotalPages());
            response.put("currentPage", page);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @Operation(summary = "点赞分享")
    @PostMapping("/{shareId}/like")
    public ResponseEntity<Map<String, Object>> likeShare(
            @PathVariable Long shareId,
            @RequestParam Long userId) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            boolean success = studyShareService.likeShare(shareId, userId);
            if (success) {
                response.put("success", true);
                response.put("message", "点赞成功");
            } else {
                response.put("success", false);
                response.put("message", "点赞失败，分享不存在");
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "点赞失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @Operation(summary = "取消点赞")
    @PostMapping("/{shareId}/unlike")
    public ResponseEntity<Map<String, Object>> unlikeShare(
            @PathVariable Long shareId,
            @RequestParam Long userId) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            boolean success = studyShareService.unlikeShare(shareId, userId);
            if (success) {
                response.put("success", true);
                response.put("message", "取消点赞成功");
            } else {
                response.put("success", false);
                response.put("message", "取消点赞失败");
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "取消点赞失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @Operation(summary = "增加浏览量")
    @PostMapping("/{shareId}/view")
    public ResponseEntity<Map<String, Object>> incrementViewCount(@PathVariable Long shareId) {
        Map<String, Object> response = new HashMap<>();
        try {
            studyShareService.incrementViewCount(shareId);
            response.put("success", true);
            response.put("message", "浏览量已更新");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "更新失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @Operation(summary = "获取分享详情")
    @GetMapping("/{shareId}")
    public ResponseEntity<Map<String, Object>> getShareById(@PathVariable Long shareId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Optional<StudyShare> share = studyShareService.getShareById(shareId);
            if (share.isPresent()) {
                response.put("success", true);
                response.put("data", share.get());
            } else {
                response.put("success", false);
                response.put("message", "分享不存在");
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @Operation(summary = "更新分享")
    @PutMapping("/{shareId}")
    public ResponseEntity<Map<String, Object>> updateShare(
            @PathVariable Long shareId,
            @RequestBody StudyShare share) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            share.setId(shareId);
            StudyShare updatedShare = studyShareService.updateShare(share);
            response.put("success", true);
            response.put("message", "分享更新成功");
            response.put("data", updatedShare);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "更新失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @Operation(summary = "删除分享")
    @DeleteMapping("/{shareId}")
    public ResponseEntity<Map<String, Object>> deleteShare(
            @PathVariable Long shareId,
            @RequestParam Long userId) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            boolean success = studyShareService.deleteShare(shareId, userId);
            if (success) {
                response.put("success", true);
                response.put("message", "分享删除成功");
            } else {
                response.put("success", false);
                response.put("message", "删除失败，只有分享者可以删除");
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "删除失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}