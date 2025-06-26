package com.example.learning.learning_habit_plan_backend.service.impl;

import com.example.learning.learning_habit_plan_backend.service.EmailService;
import com.example.learning.learning_habit_plan_backend.service.VerificationCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Service
public class VerificationCodeServiceImpl implements VerificationCodeService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    @Autowired
    private EmailService emailService;
    
    private static final String EMAIL_CODE_PREFIX = "email_verification:";
    private static final int CODE_LENGTH = 6;
    private static final int CODE_EXPIRE_MINUTES = 10;
    
    @Override
    public String sendEmailVerificationCode(String email) {
        // 生成6位数字验证码
        String verificationCode = generateVerificationCode();
        
        // 存储到Redis，有效期10分钟
        String key = EMAIL_CODE_PREFIX + email;
        redisTemplate.opsForValue().set(key, verificationCode, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        
        // 发送邮件
        emailService.sendVerificationCodeEmail(email, verificationCode);
        
        return verificationCode;
    }
    
    @Override
    public boolean verifyEmailCode(String email, String code) {
        if (email == null || code == null) {
            return false;
        }
        
        String key = EMAIL_CODE_PREFIX + email;
        String storedCode = redisTemplate.opsForValue().get(key);
        
        return code.equals(storedCode);
    }
    
    @Override
    public void clearVerificationCode(String email) {
        String key = EMAIL_CODE_PREFIX + email;
        redisTemplate.delete(key);
    }
    
    /**
     * 生成6位数字验证码
     */
    private String generateVerificationCode() {
        SecureRandom random = new SecureRandom();
        StringBuilder code = new StringBuilder();
        
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(random.nextInt(10));
        }
        
        return code.toString();
    }
}