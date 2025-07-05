package com.example.learning.learning_habit_plan_backend.aitalk.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.learning.learning_habit_plan_backend.aitalk.service.ChatService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.HashMap;
import java.util.Map;

/**
 * 聊天控制器，提供会话管理API接口
 * 注意：实际的AI对话功能由前端直接调用阿里云API实现
 */
@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "http://localhost:5173")
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);
    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * 清除会话历史
     * @param sessionId 会话ID
     * @return 响应
     */
    @DeleteMapping("/history/{sessionId}")
    public ResponseEntity<Map<String, Object>> clearChatHistory(@PathVariable String sessionId) {
        logger.info("清除会话历史请求 - sessionId: {}", sessionId);
        chatService.clearChatHistory(sessionId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Chat history cleared successfully");
        response.put("sessionId", sessionId);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取会话信息
     * @param sessionId 会话ID
     * @return 会话信息
     */
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<Map<String, Object>> getSessionInfo(@PathVariable String sessionId) {
        logger.info("获取会话信息请求 - sessionId: {}", sessionId);
        int messageCount = chatService.getSessionMessageCount(sessionId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("sessionId", sessionId);
        response.put("messageCount", messageCount);
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }

    /**
     * 健康检查接口
     * @return 响应
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Chat Session Management");
        response.put("timestamp", System.currentTimeMillis());
        response.put("note", "AI chat functionality is handled by frontend directly");
        
        return ResponseEntity.ok(response);
    }
}