package com.example.learning.learning_habit_plan_backend.aitalk.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 聊天会话管理服务
 * 负责管理用户会话的基本信息和统计数据
 * 注意：实际的AI对话功能由前端直接调用阿里云API实现
 */
@Service
public class ChatMemoryService {

    // 使用线程安全的Map存储会话信息，key为sessionId，value为会话数据
    private final Map<String, Map<String, Object>> sessionData = new ConcurrentHashMap<>();

    /**
     * 获取会话信息
     * @param sessionId 会话ID
     * @return 会话信息
     */
    public Map<String, Object> getSessionInfo(String sessionId) {
        return sessionData.computeIfAbsent(sessionId, k -> {
            Map<String, Object> info = new HashMap<>();
            info.put("sessionId", sessionId);
            info.put("messageCount", 0);
            info.put("createdAt", System.currentTimeMillis());
            info.put("lastActiveAt", System.currentTimeMillis());
            return info;
        });
    }

    /**
     * 增加会话消息计数
     * @param sessionId 会话ID
     */
    public void incrementMessageCount(String sessionId) {
        Map<String, Object> info = getSessionInfo(sessionId);
        int currentCount = (Integer) info.get("messageCount");
        info.put("messageCount", currentCount + 1);
        info.put("lastActiveAt", System.currentTimeMillis());
    }

    /**
     * 获取会话消息数量
     * @param sessionId 会话ID
     * @return 消息数量
     */
    public int getMessageCount(String sessionId) {
        Map<String, Object> info = getSessionInfo(sessionId);
        return (Integer) info.get("messageCount");
    }

    /**
     * 更新会话活跃时间
     * @param sessionId 会话ID
     */
    public void updateLastActiveTime(String sessionId) {
        Map<String, Object> info = getSessionInfo(sessionId);
        info.put("lastActiveAt", System.currentTimeMillis());
    }

    /**
     * 清除会话数据
     * @param sessionId 会话ID
     */
    public void clearSession(String sessionId) {
        sessionData.remove(sessionId);
    }

    /**
     * 获取所有活跃会话数量
     * @return 活跃会话数量
     */
    public int getActiveSessionCount() {
        return sessionData.size();
    }
}