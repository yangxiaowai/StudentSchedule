package com.example.learning.learning_habit_plan_backend.controller;

import com.example.learning.learning_habit_plan_backend.common.Result;
import com.example.learning.learning_habit_plan_backend.dto.LoginRequest;
import com.example.learning.learning_habit_plan_backend.dto.LoginResponse;
import com.example.learning.learning_habit_plan_backend.dto.RegisterRequest;
import com.example.learning.learning_habit_plan_backend.entity.User;
import com.example.learning.learning_habit_plan_backend.service.UserService;
import com.example.learning.learning_habit_plan_backend.service.VerificationCodeService;
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
    private final VerificationCodeService verificationCodeService;

    public UserController(UserService userService, VerificationCodeService verificationCodeService) {
        this.userService = userService;
        this.verificationCodeService = verificationCodeService;
    }

    /**
     * 用户注册
     */
    @PostMapping(value = "/register", produces = "application/json")
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
    @PostMapping(value = "/login", produces = "application/json")
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
    @PostMapping(value = "/refresh", produces = "application/json")
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
    @GetMapping(value = "/profile", produces = "application/json")
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
    @PostMapping(value = "/forgot-password", produces = "application/json")
    public Result<String> forgotPassword(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            System.out.println("收到忘记密码请求，邮箱: " + email);
            if (email == null || email.trim().isEmpty()) {
                return Result.failure("邮箱不能为空");
            }
            userService.sendPasswordResetEmail(email);
            System.out.println("密码重置邮件发送成功: " + email);
            return Result.success("密码重置邮件已发送");
        } catch (Exception e) {
            System.err.println("忘记密码处理失败: " + e.getMessage());
            e.printStackTrace();
            return Result.failure(e.getMessage());
        }
    }
    
    /**
     * 重置密码
     */
    @PostMapping(value = "/reset-password", produces = "application/json")
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
    @GetMapping(value = "/validate-reset-token", produces = "application/json")
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
    @PostMapping(value = "/logout", produces = "application/json")
    public Result<String> logout() {
        try {
            // 清除Security Context
            SecurityContextHolder.clearContext();
            return Result.success("登出成功");
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }
    
    /**
     * 发送邮箱验证码
     */
    @PostMapping(value = "/send-verification-code", produces = "application/json")
    public Result<String> sendVerificationCode(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            if (email == null || email.trim().isEmpty()) {
                return Result.failure("邮箱不能为空");
            }
            
            // 验证邮箱格式
            if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                return Result.failure("邮箱格式不正确");
            }
            
            verificationCodeService.sendEmailVerificationCode(email);
            return Result.success("验证码已发送到您的邮箱");
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }
    
    /**
     * 验证邮箱验证码
     */
    @PostMapping(value = "/verify-code", produces = "application/json")
    public Result<String> verifyCode(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String code = request.get("code");
            
            if (email == null || email.trim().isEmpty()) {
                return Result.failure("邮箱不能为空");
            }
            if (code == null || code.trim().isEmpty()) {
                return Result.failure("验证码不能为空");
            }
            
            boolean isValid = verificationCodeService.verifyEmailCode(email, code);
            if (isValid) {
                // 验证成功后清除验证码
                verificationCodeService.clearVerificationCode(email);
                return Result.success("验证码验证成功");
            } else {
                return Result.failure("验证码错误或已过期");
            }
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    /**
     * 基于验证码重置密码
     */
    @PostMapping(value = "/reset-password-by-code", produces = "application/json")
    public Result<String> resetPasswordByCode(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String code = request.get("code");
            String newPassword = request.get("newPassword");
            
            if (email == null || email.trim().isEmpty()) {
                return Result.failure("邮箱不能为空");
            }
            if (code == null || code.trim().isEmpty()) {
                return Result.failure("验证码不能为空");
            }
            if (newPassword == null || newPassword.length() < 6) {
                return Result.failure("密码长度不能少于6位");
            }
            
            // 验证验证码
            boolean isValid = verificationCodeService.verifyEmailCode(email, code);
            if (!isValid) {
                return Result.failure("验证码错误或已过期");
            }
            
            // 重置密码
            userService.resetPasswordByEmail(email, newPassword);
            
            // 清除验证码
            verificationCodeService.clearVerificationCode(email);
            
            return Result.success("密码重置成功");
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }
}