package com.example.learning.learning_habit_plan_backend.service;

public interface VerificationCodeService {
    
    /**
     * 发送邮箱验证码
     * @param email 邮箱地址
     * @return 验证码
     */
    String sendEmailVerificationCode(String email);
    
    /**
     * 验证邮箱验证码
     * @param email 邮箱地址
     * @param code 验证码
     * @return 是否验证成功
     */
    boolean verifyEmailCode(String email, String code);
    
    /**
     * 清除验证码
     * @param email 邮箱地址
     */
    void clearVerificationCode(String email);
}