package com.example.learning.learning_habit_plan_backend.controller;

import com.example.learning.learning_habit_plan_backend.common.Result;
import com.example.learning.learning_habit_plan_backend.dto.LoginRequest;
import com.example.learning.learning_habit_plan_backend.dto.LoginResponse;
import com.example.learning.learning_habit_plan_backend.dto.RegisterRequest;
import com.example.learning.learning_habit_plan_backend.entity.User;
import com.example.learning.learning_habit_plan_backend.entity.UserStats;
import com.example.learning.learning_habit_plan_backend.service.UserService;
import com.example.learning.learning_habit_plan_backend.service.UserStatsService;
import com.example.learning.learning_habit_plan_backend.service.VerificationCodeService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.Map;

/**
 * 用户控制器
 * 负责处理用户相关的HTTP请求，包括用户注册、登录、密码重置、邮箱验证等功能
 *
 * @author 系统开发团队
 * @version 1.0
 * @since 2024
 */
@RestController
@RequestMapping("/api/user")
@Validated
public class UserController {

    /**
     * 用户服务接口，处理用户相关的业务逻辑
     */
    private final UserService userService;

    /**
     * 邮箱验证码服务接口，处理验证码的生成、发送和验证
     */
    private final VerificationCodeService verificationCodeService;

    /**
     * 用户统计服务接口，处理用户统计数据的获取和更新
     */
    private final UserStatsService userStatsService;

    /**
     * 构造函数，通过依赖注入初始化服务组件
     *
     * @param userService 用户服务实例
     * @param verificationCodeService 验证码服务实例
     * @param userStatsService 用户统计服务实例
     */
    public UserController(UserService userService, VerificationCodeService verificationCodeService, UserStatsService userStatsService) {
        this.userService = userService;
        this.verificationCodeService = verificationCodeService;
        this.userStatsService = userStatsService;
    }

    /**
     * 用户注册接口
     * 处理新用户的注册请求，包括用户名、密码、邮箱等信息的验证和存储
     *
     * @param registerRequest 注册请求对象，包含用户名、密码、确认密码、邮箱等信息
     * @return Result<String> 注册结果，成功返回成功消息，失败返回错误信息
     *
     * 业务流程：
     * 1. 验证请求参数的有效性（通过@Valid注解自动验证）
     * 2. 检查用户名和邮箱是否已存在
     * 3. 验证密码和确认密码是否一致
     * 4. 对密码进行加密处理
     * 5. 将用户信息保存到数据库
     * 6. 返回注册结果
     */
    @PostMapping(value = "/register", produces = "application/json")
    public Result<String> register(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            // 调用用户服务进行注册处理
            userService.register(registerRequest);
            return Result.success("注册成功");
        } catch (Exception e) {
            // 捕获并返回注册过程中的异常信息
            return Result.failure(e.getMessage());
        }
    }

    /**
     * 用户登录接口
     * 处理用户登录请求，验证用户凭据并返回JWT令牌
     *
     * @param loginRequest 登录请求对象，包含用户名和密码
     * @return Result<LoginResponse> 登录结果，成功返回包含JWT令牌和用户信息的响应对象
     *
     * 业务流程：
     * 1. 验证请求参数的有效性（用户名和密码不能为空）
     * 2. 根据用户名查找用户信息
     * 3. 验证密码是否正确（使用BCrypt加密算法）
     * 4. 生成访问令牌（Access Token）和刷新令牌（Refresh Token）
     * 5. 将刷新令牌存储到Redis中，设置过期时间
     * 6. 返回登录响应，包含令牌信息和用户基本信息
     */
    @PostMapping(value = "/login", produces = "application/json")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            // 调用用户服务进行登录验证和令牌生成
            LoginResponse loginResponse = userService.login(loginRequest);
            return Result.success(loginResponse);
        } catch (Exception e) {
            // 捕获并返回登录过程中的异常信息（如用户不存在、密码错误等）
            return Result.failure(e.getMessage());
        }
    }
    
    /**
     * 刷新访问令牌接口
     * 使用有效的刷新令牌获取新的访问令牌，延长用户会话时间
     *
     * @param request 包含刷新令牌的请求体
     * @return Result<LoginResponse> 刷新结果，成功返回新的令牌信息
     *
     * 业务流程：
     * 1. 从请求体中提取刷新令牌
     * 2. 验证刷新令牌的有效性和格式
     * 3. 检查刷新令牌是否在Redis中存在且未过期
     * 4. 从令牌中解析用户信息
     * 5. 生成新的访问令牌和刷新令牌
     * 6. 更新Redis中的刷新令牌
     * 7. 返回新的令牌信息
     */
    @PostMapping(value = "/refresh", produces = "application/json")
    public Result<LoginResponse> refreshToken(@RequestBody Map<String, String> request) {
        try {
            String refreshToken = request.get("refreshToken");
            // 验证刷新令牌是否为空
            if (refreshToken == null || refreshToken.trim().isEmpty()) {
                return Result.failure("刷新令牌不能为空");
            }
            // 调用用户服务进行令牌刷新
            LoginResponse loginResponse = userService.refreshToken(refreshToken);
            return Result.success(loginResponse);
        } catch (Exception e) {
            // 捕获并返回令牌刷新过程中的异常信息（如令牌无效、已过期等）
            return Result.failure(e.getMessage());
        }
    }
    
    /**
     * 获取当前登录用户信息接口
     * 通过JWT令牌获取当前登录用户的基本信息
     *
     * @return Result<LoginResponse.UserInfo> 用户信息，包含用户ID、用户名、邮箱等
     *
     * 业务流程：
     * 1. 从Spring Security上下文中获取当前认证信息
     * 2. 提取用户名（从JWT令牌中解析得到）
     * 3. 根据用户名查询用户详细信息
     * 4. 构造用户信息响应对象
     * 5. 返回用户基本信息（不包含敏感信息如密码）
     *
     * 注意：此接口需要用户已登录，请求头中需要携带有效的JWT令牌
     */
    @GetMapping(value = "/profile", produces = "application/json")
    public Result<LoginResponse.UserInfo> getCurrentUser() {
        try {
            // 从Spring Security上下文中获取当前用户的认证信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            // 根据用户名查询用户信息
            User user = userService.findByUsername(username);
            if (user == null) {
                return Result.failure("用户不存在");
            }

            // 构造用户信息响应对象
            LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo();
            userInfo.setId(user.getId());
            userInfo.setUsername(user.getUsername());
            userInfo.setEmail(user.getEmail());
            return Result.success(userInfo);
        } catch (Exception e) {
            // 捕获并返回获取用户信息过程中的异常
            return Result.failure(e.getMessage());
        }
    }
    
    /**
     * 根据用户ID获取用户信息接口
     * 通过指定的用户ID查询并返回用户的基本信息
     *
     * @param userId 用户ID，通过URL路径参数传递
     * @return Result<LoginResponse.UserInfo> 用户信息，包含用户ID、用户名、邮箱等
     *
     * 业务流程：
     * 1. 从URL路径中提取用户ID参数
     * 2. 根据用户ID查询数据库中的用户信息
     * 3. 验证用户是否存在
     * 4. 构造用户信息响应对象
     * 5. 返回用户基本信息（不包含敏感信息如密码）
     *
     * 使用场景：用于获取其他用户的公开信息，如在用户列表、好友信息等场景中使用
     */
    @GetMapping(value = "/info/{userId}", produces = "application/json")
    public Result<LoginResponse.UserInfo> getUserById(@PathVariable Long userId) {
        try {
            // 根据用户ID查询用户信息
            User user = userService.findById(userId);
            if (user == null) {
                return Result.failure("用户不存在");
            }

            // 构造用户信息响应对象
            LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo();
            userInfo.setId(user.getId());
            userInfo.setUsername(user.getUsername());
            userInfo.setEmail(user.getEmail());
            return Result.success(userInfo);
        } catch (Exception e) {
            // 捕获并返回查询用户信息过程中的异常
            return Result.failure(e.getMessage());
        }
    }

    /**
     * 发送密码重置邮件接口
     * 当用户忘记密码时，通过邮箱发送密码重置链接
     *
     * @param request 包含用户邮箱地址的请求体
     * @return Result<String> 发送结果，成功返回成功消息
     *
     * 业务流程：
     * 1. 从请求体中提取用户邮箱地址
     * 2. 验证邮箱地址是否为空
     * 3. 检查邮箱是否在系统中注册
     * 4. 生成密码重置JWT令牌（包含用户信息和过期时间）
     * 5. 将重置令牌存储到Redis中，设置过期时间（通常为15分钟）
     * 6. 构造包含重置链接的邮件内容
     * 7. 发送密码重置邮件到用户邮箱
     * 8. 返回发送结果
     *
     * 安全考虑：
     * - 重置令牌具有时效性，防止长期有效的安全风险
     * - 即使邮箱不存在也返回成功消息，防止邮箱枚举攻击
     */
    @PostMapping(value = "/forgot-password", produces = "application/json")
    public Result<String> forgotPassword(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            System.out.println("收到忘记密码请求，邮箱: " + email);

            // 验证邮箱地址是否为空
            if (email == null || email.trim().isEmpty()) {
                return Result.failure("邮箱不能为空");
            }

            // 调用用户服务发送密码重置邮件
            userService.sendPasswordResetEmail(email);
            System.out.println("密码重置邮件发送成功: " + email);
            return Result.success("密码重置邮件已发送");
        } catch (Exception e) {
            System.err.println("忘记密码处理失败: " + e.getMessage());
            e.printStackTrace();
            // 捕获并返回发送邮件过程中的异常信息
            return Result.failure(e.getMessage());
        }
    }
    
    /**
     * 重置密码接口
     * 使用有效的重置令牌来重置用户密码
     *
     * @param request 包含重置令牌和新密码的请求体
     * @return Result<String> 重置结果，成功返回成功消息
     *
     * 业务流程：
     * 1. 从请求体中提取重置令牌和新密码
     * 2. 验证重置令牌和新密码的有效性
     * 3. 解析JWT重置令牌，获取用户信息
     * 4. 验证令牌是否在Redis中存在且未过期
     * 5. 对新密码进行BCrypt加密
     * 6. 更新数据库中的用户密码
     * 7. 从Redis中删除已使用的重置令牌
     * 8. 返回重置结果
     *
     * 安全考虑：
     * - 重置令牌一次性使用，使用后立即失效
     * - 新密码需要满足最小长度要求
     * - 密码使用BCrypt算法加密存储
     */
    @PostMapping(value = "/reset-password", produces = "application/json")
    public Result<String> resetPassword(@RequestBody Map<String, String> request) {
        try {
            String token = request.get("token");
            String newPassword = request.get("newPassword");
            
            // 验证重置令牌是否为空
            if (token == null || token.trim().isEmpty()) {
                return Result.failure("重置令牌不能为空");
            }
            // 验证新密码长度
            if (newPassword == null || newPassword.length() < 6) {
                return Result.failure("密码长度不能少于6位");
            }
            
            // 调用用户服务进行密码重置
            userService.resetPassword(token, newPassword);
            return Result.success("密码重置成功");
        } catch (Exception e) {
            // 捕获并返回密码重置过程中的异常信息（如令牌无效、已过期等）
            return Result.failure(e.getMessage());
        }
    }
    
    /**
     * 验证密码重置令牌接口
     * 验证密码重置令牌的有效性，通常在用户点击重置链接时调用
     *
     * @param token 密码重置令牌，通过URL查询参数传递
     * @return Result<Boolean> 验证结果，true表示令牌有效，false表示无效
     *
     * 业务流程：
     * 1. 从查询参数中获取重置令牌
     * 2. 解析JWT令牌的格式和签名
     * 3. 检查令牌是否在Redis中存在
     * 4. 验证令牌是否已过期
     * 5. 返回验证结果
     *
     * 使用场景：前端在显示密码重置页面前，先验证令牌有效性
     */
    @GetMapping(value = "/validate-reset-token", produces = "application/json")
    public Result<Boolean> validateResetToken(@RequestParam String token) {
        try {
            // 调用用户服务验证重置令牌的有效性
            boolean isValid = userService.validatePasswordResetToken(token);
            return Result.success(isValid);
        } catch (Exception e) {
            // 捕获并返回令牌验证过程中的异常信息
            return Result.failure(e.getMessage());
        }
    }
    
    /**
     * 用户登出接口
     * 清除用户的登录状态和安全上下文
     *
     * @return Result<String> 登出结果，成功返回成功消息
     *
     * 业务流程：
     * 1. 清除Spring Security上下文中的认证信息
     * 2. 返回登出成功消息
     *
     * 注意：
     * - 此接口主要清除服务端的安全上下文
     * - 客户端需要自行清除本地存储的JWT令牌
     * - 由于JWT是无状态的，服务端无法主动使令牌失效
     * - 如需实现真正的令牌失效，可考虑将令牌加入黑名单
     */
    @PostMapping(value = "/logout", produces = "application/json")
    public Result<String> logout() {
        try {
            // 清除Spring Security上下文中的认证信息
            SecurityContextHolder.clearContext();
            return Result.success("登出成功");
        } catch (Exception e) {
            // 捕获并返回登出过程中的异常信息
            return Result.failure(e.getMessage());
        }
    }
    
    /**
     * 发送邮箱验证码接口
     * 向指定邮箱发送6位数字验证码，用于邮箱验证或密码重置
     *
     * @param request 包含邮箱地址的请求体
     * @return Result<String> 发送结果，成功返回成功消息
     *
     * 业务流程：
     * 1. 从请求体中提取邮箱地址
     * 2. 验证邮箱地址是否为空
     * 3. 使用正则表达式验证邮箱格式
     * 4. 生成6位随机数字验证码
     * 5. 将验证码存储到Redis中，设置过期时间（通常为10分钟）
     * 6. 构造验证码邮件内容
     * 7. 发送验证码邮件到用户邮箱
     * 8. 返回发送结果
     *
     * 安全考虑：
     * - 验证码具有时效性，防止长期有效的安全风险
     * - 可以添加发送频率限制，防止恶意刷验证码
     * - 验证码使用后应立即清除
     */
    @PostMapping(value = "/send-verification-code", produces = "application/json")
    public Result<String> sendVerificationCode(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            // 验证邮箱地址是否为空
            if (email == null || email.trim().isEmpty()) {
                return Result.failure("邮箱不能为空");
            }
            
            // 使用正则表达式验证邮箱格式
            if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                return Result.failure("邮箱格式不正确");
            }
            
            // 调用验证码服务发送邮箱验证码
            verificationCodeService.sendEmailVerificationCode(email);
            return Result.success("验证码已发送到您的邮箱");
        } catch (Exception e) {
            // 捕获并返回发送验证码过程中的异常信息
            return Result.failure(e.getMessage());
        }
    }
    
    /**
     * 验证邮箱验证码接口
     * 验证用户输入的邮箱验证码是否正确
     *
     * @param request 包含邮箱地址和验证码的请求体
     * @return Result<String> 验证结果，成功返回成功消息
     *
     * 业务流程：
     * 1. 从请求体中提取邮箱地址和验证码
     * 2. 验证邮箱和验证码是否为空
     * 3. 从Redis中获取该邮箱对应的验证码
     * 4. 比较用户输入的验证码与存储的验证码
     * 5. 验证成功后立即清除Redis中的验证码
     * 6. 返回验证结果
     *
     * 安全考虑：
     * - 验证码一次性使用，验证成功后立即清除
     * - 验证码具有时效性，过期自动失效
     * - 可以添加验证次数限制，防止暴力破解
     */
    @PostMapping(value = "/verify-code", produces = "application/json")
    public Result<String> verifyCode(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String code = request.get("code");
            
            // 验证邮箱地址是否为空
            if (email == null || email.trim().isEmpty()) {
                return Result.failure("邮箱不能为空");
            }
            // 验证验证码是否为空
            if (code == null || code.trim().isEmpty()) {
                return Result.failure("验证码不能为空");
            }
            
            // 调用验证码服务验证邮箱验证码
            boolean isValid = verificationCodeService.verifyEmailCode(email, code);
            if (isValid) {
                // 验证成功后立即清除验证码，防止重复使用
                verificationCodeService.clearVerificationCode(email);
                return Result.success("验证码验证成功");
            } else {
                return Result.failure("验证码错误或已过期");
            }
        } catch (Exception e) {
            // 捕获并返回验证过程中的异常信息
            return Result.failure(e.getMessage());
        }
    }

    /**
     * 基于验证码重置密码接口
     * 使用邮箱验证码来重置用户密码，提供另一种密码重置方式
     *
     * @param request 包含邮箱地址、验证码和新密码的请求体
     * @return Result<String> 重置结果，成功返回成功消息
     *
     * 业务流程：
     * 1. 从请求体中提取邮箱地址、验证码和新密码
     * 2. 验证所有必要参数的有效性
     * 3. 验证邮箱验证码是否正确且未过期
     * 4. 根据邮箱地址查找用户信息
     * 5. 对新密码进行BCrypt加密
     * 6. 更新数据库中的用户密码
     * 7. 清除已使用的验证码
     * 8. 返回重置结果
     *
     * 与令牌重置的区别：
     * - 此方式使用验证码而非JWT令牌
     * - 验证码通常有效期较短（10分钟）
     * - 用户体验更简单，无需点击邮件链接
     *
     * 安全考虑：
     * - 验证码一次性使用，使用后立即清除
     * - 新密码需要满足最小长度要求
     * - 密码使用BCrypt算法加密存储
     */
    @PostMapping(value = "/reset-password-by-code", produces = "application/json")
    public Result<String> resetPasswordByCode(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String code = request.get("code");
            String newPassword = request.get("newPassword");
            
            // 验证邮箱地址是否为空
            if (email == null || email.trim().isEmpty()) {
                return Result.failure("邮箱不能为空");
            }
            // 验证验证码是否为空
            if (code == null || code.trim().isEmpty()) {
                return Result.failure("验证码不能为空");
            }
            // 验证新密码长度
            if (newPassword == null || newPassword.length() < 6) {
                return Result.failure("密码长度不能少于6位");
            }
            
            // 验证邮箱验证码是否正确
            boolean isValid = verificationCodeService.verifyEmailCode(email, code);
            if (!isValid) {
                return Result.failure("验证码错误或已过期");
            }
            
            // 调用用户服务根据邮箱重置密码
            userService.resetPasswordByEmail(email, newPassword);
            
            // 清除已使用的验证码，防止重复使用
            verificationCodeService.clearVerificationCode(email);
            
            return Result.success("密码重置成功");
        } catch (Exception e) {
            // 捕获并返回密码重置过程中的异常信息
            return Result.failure(e.getMessage());
        }
    }

    /**
     * 获取当前用户统计信息接口
     * 获取当前登录用户的统计数据，包括加入小组数量、学习分享数量、提出问题数量等
     *
     * @return Result<UserStats> 用户统计信息
     *
     * 业务流程：
     * 1. 从Spring Security上下文中获取当前用户信息
     * 2. 根据用户ID获取或创建用户统计记录
     * 3. 同步最新的统计数据（从相关表中实时计算）
     * 4. 返回完整的用户统计信息
     *
     * 注意：此接口需要用户已登录，请求头中需要携带有效的JWT令牌
     */
    @GetMapping(value = "/stats", produces = "application/json")
    public Result<UserStats> getCurrentUserStats() {
        try {
            // 从Spring Security上下文中获取当前用户的认证信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            // 根据用户名查询用户信息
            User user = userService.findByUsername(username);
            if (user == null) {
                return Result.failure("用户不存在");
            }

            // 获取用户统计信息（会自动同步最新数据）
            UserStats userStats = userStatsService.refreshUserStats(user.getId());
            return Result.success(userStats);
        } catch (Exception e) {
            // 捕获并返回获取用户统计信息过程中的异常
            return Result.failure(e.getMessage());
        }
    }

    /**
     * 根据用户ID获取用户统计信息接口
     * 通过指定的用户ID查询并返回用户的统计信息
     *
     * @param userId 用户ID，通过URL路径参数传递
     * @return Result<UserStats> 用户统计信息
     *
     * 业务流程：
     * 1. 从URL路径中提取用户ID参数
     * 2. 根据用户ID获取或创建用户统计记录
     * 3. 同步最新的统计数据
     * 4. 返回完整的用户统计信息
     *
     * 使用场景：用于获取其他用户的统计信息，如在用户资料页面等场景中使用
     */
    @GetMapping(value = "/stats/{userId}", produces = "application/json")
    public Result<UserStats> getUserStatsById(@PathVariable Long userId) {
        try {
            // 根据用户ID获取用户统计信息
            UserStats userStats = userStatsService.refreshUserStats(userId);
            return Result.success(userStats);
        } catch (Exception e) {
            // 捕获并返回查询用户统计信息过程中的异常
            return Result.failure(e.getMessage());
        }
    }
}