package com.example.learning.learning_habit_plan_backend.service;

import com.example.learning.learning_habit_plan_backend.dto.LoginRequest;
import com.example.learning.learning_habit_plan_backend.dto.LoginResponse;
import com.example.learning.learning_habit_plan_backend.dto.RegisterRequest;
import com.example.learning.learning_habit_plan_backend.entity.User;

public interface UserService {
    
    /**
     * 用户登录
     */
    LoginResponse login(LoginRequest loginRequest);
    
    /**
     * 用户注册
     */
    void register(RegisterRequest registerRequest);
    
    /**
     * 刷新Token
     */
    LoginResponse refreshToken(String refreshToken);
    
    /**
     * 根据用户名查找用户
     */
    User findByUsername(String username);
    
    /**
     * 根据邮箱查找用户
     */
    User findByEmail(String email);
    
    /**
     * 发送密码重置邮件
     */
    void sendPasswordResetEmail(String email);
    
    /**
     * 重置密码
     */
    void resetPassword(String token, String newPassword);
    
    /**
     * 验证密码重置Token
     */
    boolean validatePasswordResetToken(String token);
}

