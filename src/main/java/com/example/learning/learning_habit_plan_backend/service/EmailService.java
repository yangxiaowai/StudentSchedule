package com.example.learning.learning_habit_plan_backend.service;

public interface EmailService {
    
    /**
     * 发送密码重置邮件
     * @param to 收件人邮箱
     * @param resetToken 重置令牌
     */
    void sendPasswordResetEmail(String to, String resetToken);
    
    /**
     * 发送验证码邮件
     * @param to 收件人邮箱
     * @param verificationCode 验证码
     */
    void sendVerificationCodeEmail(String to, String verificationCode);
}