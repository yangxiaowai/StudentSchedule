package com.example.learning.learning_habit_plan_backend.controller;

import com.example.learning.learning_habit_plan_backend.entity.QAAnswer;
import com.example.learning.learning_habit_plan_backend.entity.QAQuestion;
import com.example.learning.learning_habit_plan_backend.service.QAService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/qa")
@Tag(name = "问答管理", description = "互助问答相关API")

public class QAController {
    
    @Autowired
    private QAService qaService;
    
    // ========== 问题相关接口 ==========
    
    @Operation(summary = "提问")
    @PostMapping("/questions")
    public ResponseEntity<Map<String, Object>> createQuestion(@RequestBody QAQuestion question) {
        Map<String, Object> response = new HashMap<>();
        try {
            QAQuestion createdQuestion = qaService.createQuestion(question);
            response.put("success", true);
            response.put("message", "问题提交成功");
            response.put("data", createdQuestion);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "提问失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @Operation(summary = "获取所有问题")
    @GetMapping("/questions")
    public ResponseEntity<Map<String, Object>> getAllQuestions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long userId) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<QAQuestion> questions = qaService.getAllQuestions(pageable);
            
            // 如果提供了userId，添加点赞状态
            List<Map<String, Object>> questionsWithLikeStatus = new ArrayList<>();
            for (QAQuestion question : questions.getContent()) {
                Map<String, Object> questionMap = new HashMap<>();
                questionMap.put("id", question.getId());
                questionMap.put("title", question.getTitle());
                questionMap.put("content", question.getContent());
                questionMap.put("userId", question.getUserId());
                questionMap.put("subject", question.getSubject());
                questionMap.put("difficultyLevel", question.getDifficultyLevel());
                questionMap.put("tags", question.getTags());
                questionMap.put("likeCount", question.getLikeCount());
                questionMap.put("viewCount", question.getViewCount());
                questionMap.put("answerCount", question.getAnswerCount());
                questionMap.put("status", question.getStatus());
                questionMap.put("createdAt", question.getCreatedAt());
                questionMap.put("updatedAt", question.getUpdatedAt());
                questionMap.put("groupId", question.getGroupId());
                questionMap.put("rewardPoints", question.getRewardPoints());
                questionMap.put("bestAnswerId", question.getBestAnswerId());
                
                if (userId != null) {
                    questionMap.put("isLiked", qaService.isQuestionLikedByUser(question.getId(), userId));
                } else {
                    questionMap.put("isLiked", false);
                }
                
                questionsWithLikeStatus.add(questionMap);
            }
            
            response.put("success", true);
            response.put("data", questionsWithLikeStatus);
            response.put("totalElements", questions.getTotalElements());
            response.put("totalPages", questions.getTotalPages());
            response.put("currentPage", page);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @Operation(summary = "获取用户的问题")
    @GetMapping("/questions/user/{userId}")
    public ResponseEntity<Map<String, Object>> getUserQuestions(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<QAQuestion> questions = qaService.getUserQuestions(userId, pageable);
            
            response.put("success", true);
            response.put("data", questions.getContent());
            response.put("totalElements", questions.getTotalElements());
            response.put("totalPages", questions.getTotalPages());
            response.put("currentPage", page);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @Operation(summary = "根据状态获取问题")
    @GetMapping("/questions/status/{status}")
    public ResponseEntity<Map<String, Object>> getQuestionsByStatus(
            @PathVariable QAQuestion.QuestionStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<QAQuestion> questions = qaService.getQuestionsByStatus(status, pageable);
            
            response.put("success", true);
            response.put("data", questions.getContent());
            response.put("totalElements", questions.getTotalElements());
            response.put("totalPages", questions.getTotalPages());
            response.put("currentPage", page);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @Operation(summary = "根据学科获取问题")
    @GetMapping("/questions/subject/{subject}")
    public ResponseEntity<Map<String, Object>> getQuestionsBySubject(
            @PathVariable String subject,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<QAQuestion> questions = qaService.getQuestionsBySubject(subject, pageable);
            
            response.put("success", true);
            response.put("data", questions.getContent());
            response.put("totalElements", questions.getTotalElements());
            response.put("totalPages", questions.getTotalPages());
            response.put("currentPage", page);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @Operation(summary = "根据难度获取问题")
    @GetMapping("/questions/difficulty/{difficulty}")
    public ResponseEntity<Map<String, Object>> getQuestionsByDifficulty(
            @PathVariable QAQuestion.DifficultyLevel difficulty,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<QAQuestion> questions = qaService.getQuestionsByDifficulty(difficulty, pageable);
            
            response.put("success", true);
            response.put("data", questions.getContent());
            response.put("totalElements", questions.getTotalElements());
            response.put("totalPages", questions.getTotalPages());
            response.put("currentPage", page);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @Operation(summary = "获取小组内的问题")
    @GetMapping("/questions/group/{groupId}")
    public ResponseEntity<Map<String, Object>> getGroupQuestions(
            @PathVariable Long groupId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<QAQuestion> questions = qaService.getGroupQuestions(groupId, pageable);
            
            response.put("success", true);
            response.put("data", questions.getContent());
            response.put("totalElements", questions.getTotalElements());
            response.put("totalPages", questions.getTotalPages());
            response.put("currentPage", page);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @Operation(summary = "搜索问题")
    @GetMapping("/questions/search")
    public ResponseEntity<Map<String, Object>> searchQuestions(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<QAQuestion> questions = qaService.searchQuestions(keyword, pageable);
            
            response.put("success", true);
            response.put("data", questions.getContent());
            response.put("totalElements", questions.getTotalElements());
            response.put("totalPages", questions.getTotalPages());
            response.put("currentPage", page);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "搜索失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @Operation(summary = "获取热门问题")
    @GetMapping("/questions/popular")
    public ResponseEntity<Map<String, Object>> getPopularQuestions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<QAQuestion> questions = qaService.getPopularQuestions(pageable);
            
            response.put("success", true);
            response.put("data", questions.getContent());
            response.put("totalElements", questions.getTotalElements());
            response.put("totalPages", questions.getTotalPages());
            response.put("currentPage", page);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @Operation(summary = "获取未解决的问题")
    @GetMapping("/questions/unresolved")
    public ResponseEntity<Map<String, Object>> getUnresolvedQuestions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<QAQuestion> questions = qaService.getUnresolvedQuestions(pageable);
            
            response.put("success", true);
            response.put("data", questions.getContent());
            response.put("totalElements", questions.getTotalElements());
            response.put("totalPages", questions.getTotalPages());
            response.put("currentPage", page);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @Operation(summary = "获取有悬赏的问题")
    @GetMapping("/questions/with-reward")
    public ResponseEntity<Map<String, Object>> getQuestionsWithReward(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<QAQuestion> questions = qaService.getQuestionsWithReward(pageable);
            
            response.put("success", true);
            response.put("data", questions.getContent());
            response.put("totalElements", questions.getTotalElements());
            response.put("totalPages", questions.getTotalPages());
            response.put("currentPage", page);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @Operation(summary = "根据标签搜索问题")
    @GetMapping("/questions/search/tag")
    public ResponseEntity<Map<String, Object>> searchQuestionsByTag(
            @RequestParam String tag,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<QAQuestion> questions = qaService.searchQuestionsByTag(tag, pageable);
            
            response.put("success", true);
            response.put("data", questions.getContent());
            response.put("totalElements", questions.getTotalElements());
            response.put("totalPages", questions.getTotalPages());
            response.put("currentPage", page);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "搜索失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @Operation(summary = "获取推荐问题")
    @GetMapping("/questions/recommended")
    public ResponseEntity<Map<String, Object>> getRecommendedQuestions(
            @RequestParam(required = false) String subjects,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            Pageable pageable = PageRequest.of(page, size);
            List<String> subjectList = subjects != null ? Arrays.asList(subjects.split(",")) : null;
            Page<QAQuestion> questions = qaService.getRecommendedQuestions(subjectList, pageable);
            
            response.put("success", true);
            response.put("data", questions.getContent());
            response.put("totalElements", questions.getTotalElements());
            response.put("totalPages", questions.getTotalPages());
            response.put("currentPage", page);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @Operation(summary = "点赞问题")
    @PostMapping("/questions/{questionId}/like")
    public ResponseEntity<Map<String, Object>> likeQuestion(
            @PathVariable Long questionId,
            @RequestParam Long userId) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            boolean success = qaService.likeQuestion(questionId, userId);
            if (success) {
                response.put("success", true);
                response.put("message", "点赞成功");
            } else {
                response.put("success", false);
                response.put("message", "点赞失败，问题不存在");
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "点赞失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @Operation(summary = "取消点赞问题")
    @DeleteMapping("/questions/{questionId}/like")
    public ResponseEntity<Map<String, Object>> unlikeQuestion(
            @PathVariable Long questionId,
            @RequestParam Long userId) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            boolean success = qaService.unlikeQuestion(questionId, userId);
            if (success) {
                response.put("success", true);
                response.put("message", "取消点赞成功");
            } else {
                response.put("success", false);
                response.put("message", "取消点赞失败，问题不存在");
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "取消点赞失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @Operation(summary = "增加问题浏览量")
    @PostMapping("/questions/{questionId}/view")
    public ResponseEntity<Map<String, Object>> incrementQuestionViewCount(@PathVariable Long questionId) {
        Map<String, Object> response = new HashMap<>();
        try {
            qaService.incrementQuestionViewCount(questionId);
            response.put("success", true);
            response.put("message", "浏览量已更新");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "更新失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @Operation(summary = "获取问题详情")
    @GetMapping("/questions/{questionId}")
    public ResponseEntity<Map<String, Object>> getQuestionById(@PathVariable Long questionId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Optional<QAQuestion> question = qaService.getQuestionById(questionId);
            if (question.isPresent()) {
                response.put("success", true);
                response.put("data", question.get());
            } else {
                response.put("success", false);
                response.put("message", "问题不存在");
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // ========== 回答相关接口 ==========
    
    @Operation(summary = "回答问题")
    @PostMapping("/answers")
    public ResponseEntity<Map<String, Object>> createAnswer(@RequestBody QAAnswer answer) {
        Map<String, Object> response = new HashMap<>();
        try {
            QAAnswer createdAnswer = qaService.createAnswer(answer);
            response.put("success", true);
            response.put("message", "回答提交成功");
            response.put("data", createdAnswer);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "回答失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @Operation(summary = "获取问题的所有回答")
    @GetMapping("/questions/{questionId}/answers")
    public ResponseEntity<Map<String, Object>> getQuestionAnswers(
            @PathVariable Long questionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long userId) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<QAAnswer> answers = qaService.getQuestionAnswers(questionId, pageable);
            
            // 如果提供了userId，添加点赞状态
            List<Map<String, Object>> answersWithLikeStatus = new ArrayList<>();
            for (QAAnswer answer : answers.getContent()) {
                Map<String, Object> answerMap = new HashMap<>();
                answerMap.put("id", answer.getId());
                answerMap.put("questionId", answer.getQuestionId());
                answerMap.put("userId", answer.getUserId());
                answerMap.put("content", answer.getContent());
                answerMap.put("likeCount", answer.getLikeCount());
                answerMap.put("isBestAnswer", answer.getIsBestAnswer());
                answerMap.put("createdAt", answer.getCreatedAt());
                answerMap.put("updatedAt", answer.getUpdatedAt());
                
                if (userId != null) {
                    answerMap.put("isLiked", qaService.isAnswerLikedByUser(answer.getId(), userId));
                } else {
                    answerMap.put("isLiked", false);
                }
                
                answersWithLikeStatus.add(answerMap);
            }
            
            response.put("success", true);
            response.put("data", answersWithLikeStatus);
            response.put("totalElements", answers.getTotalElements());
            response.put("totalPages", answers.getTotalPages());
            response.put("currentPage", page);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @Operation(summary = "获取问题的热门回答")
    @GetMapping("/questions/{questionId}/answers/popular")
    public ResponseEntity<Map<String, Object>> getQuestionPopularAnswers(
            @PathVariable Long questionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<QAAnswer> answers = qaService.getQuestionPopularAnswers(questionId, pageable);
            
            response.put("success", true);
            response.put("data", answers.getContent());
            response.put("totalElements", answers.getTotalElements());
            response.put("totalPages", answers.getTotalPages());
            response.put("currentPage", page);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @Operation(summary = "获取用户的回答")
    @GetMapping("/answers/user/{userId}")
    public ResponseEntity<Map<String, Object>> getUserAnswers(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<QAAnswer> answers = qaService.getUserAnswers(userId, pageable);
            
            response.put("success", true);
            response.put("data", answers.getContent());
            response.put("totalElements", answers.getTotalElements());
            response.put("totalPages", answers.getTotalPages());
            response.put("currentPage", page);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @Operation(summary = "获取用户的最佳回答")
    @GetMapping("/answers/user/{userId}/best")
    public ResponseEntity<Map<String, Object>> getUserBestAnswers(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<QAAnswer> answers = qaService.getUserBestAnswers(userId, pageable);
            
            response.put("success", true);
            response.put("data", answers.getContent());
            response.put("totalElements", answers.getTotalElements());
            response.put("totalPages", answers.getTotalPages());
            response.put("currentPage", page);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @Operation(summary = "点赞回答")
    @PostMapping("/answers/{answerId}/like")
    public ResponseEntity<Map<String, Object>> likeAnswer(
            @PathVariable Long answerId,
            @RequestParam Long userId) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            boolean success = qaService.likeAnswer(answerId, userId);
            if (success) {
                response.put("success", true);
                response.put("message", "点赞成功");
            } else {
                response.put("success", false);
                response.put("message", "点赞失败，回答不存在");
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "点赞失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @Operation(summary = "取消点赞回答")
    @DeleteMapping("/answers/{answerId}/like")
    public ResponseEntity<Map<String, Object>> unlikeAnswer(
            @PathVariable Long answerId,
            @RequestParam Long userId) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            boolean success = qaService.unlikeAnswer(answerId, userId);
            if (success) {
                response.put("success", true);
                response.put("message", "取消点赞成功");
            } else {
                response.put("success", false);
                response.put("message", "取消点赞失败，回答不存在");
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "取消点赞失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @Operation(summary = "设置最佳回答")
    @PostMapping("/answers/{answerId}/set-best")
    public ResponseEntity<Map<String, Object>> setBestAnswer(
            @PathVariable Long answerId,
            @RequestParam Long questionId,
            @RequestParam Long userId) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            boolean success = qaService.setBestAnswer(answerId, questionId, userId);
            if (success) {
                response.put("success", true);
                response.put("message", "最佳回答设置成功");
            } else {
                response.put("success", false);
                response.put("message", "设置失败，只有提问者可以设置最佳回答");
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "设置失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @Operation(summary = "获取问题的所有回答 (兼容路径)")
    @GetMapping("/answers/question/{questionId}")
    public ResponseEntity<Map<String, Object>> getAnswersByQuestionId(
            @PathVariable Long questionId,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<QAAnswer> answers;
            
            // 根据sortBy参数决定排序方式
            if ("popular".equals(sortBy)) {
                answers = qaService.getQuestionPopularAnswers(questionId, pageable);
            } else {
                answers = qaService.getQuestionAnswers(questionId, pageable);
            }
            
            response.put("success", true);
            response.put("data", answers.getContent());
            response.put("totalElements", answers.getTotalElements());
            response.put("totalPages", answers.getTotalPages());
            response.put("currentPage", page);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @Operation(summary = "获取回答详情")
    @GetMapping("/answers/{answerId}")
    public ResponseEntity<Map<String, Object>> getAnswerById(@PathVariable Long answerId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Optional<QAAnswer> answer = qaService.getAnswerById(answerId);
            if (answer.isPresent()) {
                response.put("success", true);
                response.put("data", answer.get());
            } else {
                response.put("success", false);
                response.put("message", "回答不存在");
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}