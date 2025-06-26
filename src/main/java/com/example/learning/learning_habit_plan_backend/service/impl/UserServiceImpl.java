package com.example.learning.learning_habit_plan_backend.service.impl;

import com.example.learning.learning_habit_plan_backend.dto.LoginRequest;
import com.example.learning.learning_habit_plan_backend.dto.LoginResponse;
import com.example.learning.learning_habit_plan_backend.dto.RegisterRequest;
import com.example.learning.learning_habit_plan_backend.entity.User;
import com.example.learning.learning_habit_plan_backend.mapper.UserMapper;
import com.example.learning.learning_habit_plan_backend.service.EmailService;
import com.example.learning.learning_habit_plan_backend.service.UserService;
import com.example.learning.learning_habit_plan_backend.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    @Autowired
    private EmailService emailService;

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        User user = userMapper.selectByUsername(loginRequest.getUsername());
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (!user.getIsActive()) {
            throw new RuntimeException("账户已被禁用");
        }
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        
        // 生成JWT Token
        String accessToken = jwtUtil.generateAccessToken(user.getUsername(), user.getId());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername(), user.getId());
        
        // 将refresh token存储到Redis中
        String refreshTokenKey = "refresh_token:" + user.getId();
        redisTemplate.opsForValue().set(refreshTokenKey, refreshToken, 7, TimeUnit.DAYS);
        
        // 构建用户信息
        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setEmail(user.getEmail());
        
        return new LoginResponse(accessToken, refreshToken, 24 * 60 * 60L, userInfo);
    }

    @Override
    public void register(RegisterRequest registerRequest) {
        // 验证确认密码
        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            throw new RuntimeException("两次输入的密码不一致");
        }
        
        // 检查用户名是否已存在
        if (userMapper.selectByUsername(registerRequest.getUsername()) != null) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 检查邮箱是否已存在
        if (userMapper.selectByEmail(registerRequest.getEmail()) != null) {
            throw new RuntimeException("邮箱已被注册");
        }
        
        // 创建用户
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setEmail(registerRequest.getEmail());
        user.setIsActive(true);
        
        userMapper.insert(user);
    }
    
    @Override
    public LoginResponse refreshToken(String refreshToken) {
        try {
            String username = jwtUtil.getUsernameFromToken(refreshToken);
            Long userId = jwtUtil.getUserIdFromToken(refreshToken);
            
            // 验证refresh token类型
            if (!"refresh".equals(jwtUtil.getTokenType(refreshToken))) {
                throw new RuntimeException("无效的刷新令牌");
            }
            
            // 从Redis中验证refresh token
            String refreshTokenKey = "refresh_token:" + userId;
            String storedToken = redisTemplate.opsForValue().get(refreshTokenKey);
            if (!refreshToken.equals(storedToken)) {
                throw new RuntimeException("刷新令牌已失效");
            }
            
            User user = userMapper.selectByUsername(username);
            if (user == null || !user.getIsActive()) {
                throw new RuntimeException("用户不存在或已被禁用");
            }
            
            // 生成新的access token
            String newAccessToken = jwtUtil.generateAccessToken(username, userId);
            
            LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo();
            userInfo.setId(user.getId());
            userInfo.setUsername(user.getUsername());
            userInfo.setEmail(user.getEmail());
            
            return new LoginResponse(newAccessToken, refreshToken, 24 * 60 * 60L, userInfo);
            
        } catch (Exception e) {
            throw new RuntimeException("刷新令牌无效: " + e.getMessage());
        }
    }
    
    @Override
    public User findByUsername(String username) {
        return userMapper.selectByUsername(username);
    }
    
    @Override
    public User findByEmail(String email) {
        return userMapper.selectByEmail(email);
    }
    
    @Override
    public void sendPasswordResetEmail(String email) {
        User user = userMapper.selectByEmail(email);
        if (user == null) {
            throw new RuntimeException("邮箱不存在");
        }
        
        // 生成密码重置token
        String resetToken = jwtUtil.generateAccessToken(user.getUsername(), user.getId());
        
        // 将重置token存储到Redis中，有效期30分钟
        String resetTokenKey = "password_reset:" + user.getId();
        redisTemplate.opsForValue().set(resetTokenKey, resetToken, 30, TimeUnit.MINUTES);
        
        // 发送密码重置邮件
        emailService.sendPasswordResetEmail(email, resetToken);
    }
    
    @Override
    public void resetPassword(String token, String newPassword) {
        try {
            String username = jwtUtil.getUsernameFromToken(token);
            Long userId = jwtUtil.getUserIdFromToken(token);
            
            // 验证重置token
            String resetTokenKey = "password_reset:" + userId;
            String storedToken = redisTemplate.opsForValue().get(resetTokenKey);
            if (!token.equals(storedToken)) {
                throw new RuntimeException("重置令牌已失效");
            }
            
            User user = userMapper.selectByUsername(username);
            if (user == null) {
                throw new RuntimeException("用户不存在");
            }
            
            // 更新密码
            user.setPassword(passwordEncoder.encode(newPassword));
            userMapper.updateById(user);
            
            // 删除重置token
            redisTemplate.delete(resetTokenKey);
            
        } catch (Exception e) {
            throw new RuntimeException("密码重置失败: " + e.getMessage());
        }
    }
    
    @Override
    public boolean validatePasswordResetToken(String token) {
        try {
            Long userId = jwtUtil.getUserIdFromToken(token);
            String resetTokenKey = "password_reset:" + userId;
            String storedToken = redisTemplate.opsForValue().get(resetTokenKey);
            return token.equals(storedToken) && !jwtUtil.isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public void resetPasswordByEmail(String email, String newPassword) {
        try {
            User user = userMapper.selectByEmail(email);
            if (user == null) {
                throw new RuntimeException("用户不存在");
            }
            
            // 更新密码
            user.setPassword(passwordEncoder.encode(newPassword));
            userMapper.updateById(user);
            
        } catch (Exception e) {
            throw new RuntimeException("密码重置失败: " + e.getMessage());
        }
    }
}

