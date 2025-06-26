package com.example.learning.learning_habit_plan_backend.controller;

import com.example.learning.learning_habit_plan_backend.common.Result;
import com.example.learning.learning_habit_plan_backend.dto.LoginRequest;
import com.example.learning.learning_habit_plan_backend.dto.LoginResponse;
import com.example.learning.learning_habit_plan_backend.dto.RegisterRequest;
import com.example.learning.learning_habit_plan_backend.entity.User;
import com.example.learning.learning_habit_plan_backend.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@Validated
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<String> register(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            userService.register(registerRequest);
            return Result.success("注册成功");
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            LoginResponse loginResponse = userService.login(loginRequest);
            return Result.success(loginResponse);
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }
    
    /**
     * 刷新Token
     */
    @PostMapping("/refresh")
    public Result<LoginResponse> refreshToken(@RequestBody Map<String, String> request) {
        try {
            String refreshToken = request.get("refreshToken");
            if (refreshToken == null || refreshToken.trim().isEmpty()) {
                return Result.failure("刷新令牌不能为空");
            }
            LoginResponse loginResponse = userService.refreshToken(refreshToken);
            return Result.success(loginResponse);
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }
    
    /**
     * 获取当前用户信息
     */
    @GetMapping("/profile")
    public Result<LoginResponse.UserInfo> getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userService.findByUsername(username);
            if (user == null) {
                return Result.failure("用户不存在");
            }
            LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setEmail(user.getEmail());
            return Result.success(userInfo);
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }
    
    /**
     * 发送密码重置邮件
     */
    @PostMapping("/forgot-password")
    public Result<String> forgotPassword(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            if (email == null || email.trim().isEmpty()) {
                return Result.failure("邮箱不能为空");
            }
            userService.sendPasswordResetEmail(email);
            return Result.success("密码重置邮件已发送");
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }
    
    /**
     * 重置密码
     */
    @PostMapping("/reset-password")
    public Result<String> resetPassword(@RequestBody Map<String, String> request) {
        try {
            String token = request.get("token");
            String newPassword = request.get("newPassword");
            
            if (token == null || token.trim().isEmpty()) {
                return Result.failure("重置令牌不能为空");
            }
            if (newPassword == null || newPassword.length() < 6) {
                return Result.failure("密码长度不能少于6位");
            }
            
            userService.resetPassword(token, newPassword);
            return Result.success("密码重置成功");
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }
    
    /**
     * 验证密码重置令牌
     */
    @GetMapping("/validate-reset-token")
    public Result<Boolean> validateResetToken(@RequestParam String token) {
        try {
            boolean isValid = userService.validatePasswordResetToken(token);
            return Result.success(isValid);
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }
    
    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public Result<String> logout() {
        try {
            // 清除Security Context
            SecurityContextHolder.clearContext();
            return Result.success("登出成功");
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }
}