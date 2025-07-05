package com.example.learning.learning_habit_plan_backend.aitalk.service;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * 聊天服务层，提供会话管理功能
 * 注意：实际的AI对话功能由前端直接调用阿里云API实现
 */
@Service
public class ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);
    private final ChatMemoryService chatMemoryService;

    public ChatService(ChatMemoryService chatMemoryService) {
        this.chatMemoryService = chatMemoryService;
    }

    /**
     * 清除聊天历史
     * @param sessionId 会话ID
     */
    public void clearChatHistory(String sessionId) {
        logger.info("清除会话历史: {}", sessionId);
        chatMemoryService.clearSession(sessionId);
    }

    /**
     * 获取会话历史消息数量
     * @param sessionId 会话ID
     * @return 消息数量
     */
    public int getSessionMessageCount(String sessionId) {
        return chatMemoryService.getMessageCount(sessionId);
    }

    /**
     * 获取会话信息
     * @param sessionId 会话ID
     * @return 会话信息
     */
    public Map<String, Object> getSessionInfo(String sessionId) {
        logger.info("获取会话信息: {}", sessionId);
        return chatMemoryService.getSessionInfo(sessionId);
    }

    /**
     * 记录用户活动（可用于统计）
     * @param sessionId 会话ID
     */
    public void recordUserActivity(String sessionId) {
        logger.debug("记录用户活动: {}", sessionId);
        chatMemoryService.incrementMessageCount(sessionId);
    }

    /**
     * 获取活跃会话总数
     * @return 活跃会话数量
     */
    public int getActiveSessionCount() {
        return chatMemoryService.getActiveSessionCount();
    }
}