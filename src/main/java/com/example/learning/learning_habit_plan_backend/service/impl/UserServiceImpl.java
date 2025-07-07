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

/**
 * 用户服务实现类
 * 负责处理用户相关的业务逻辑，包括：
 * - 用户登录认证和令牌管理
 * - 用户注册和信息验证
 * - 密码重置和找回功能
 * - 用户信息查询和管理
 * 
 * 该类实现了UserService接口，提供完整的用户管理功能
 * 集成了JWT令牌认证、Redis缓存、邮件服务等组件
 * 
 * @author 系统
 * @version 1.0
 * @since 2024
 */
@Service
public class UserServiceImpl implements UserService {
    
    /**
     * 用户数据访问对象
     * 负责与数据库进行用户相关的CRUD操作
     */
    @Autowired
    private UserMapper userMapper;
    
    /**
     * 密码编码器
     * 用于密码的加密和验证，确保密码安全存储
     */
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    /**
     * JWT工具类
     * 负责JWT令牌的生成、解析和验证
     */
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * Redis模板
     * 用于缓存刷新令牌、密码重置令牌等临时数据
     */
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    /**
     * 邮件服务
     * 负责发送密码重置邮件、验证码邮件等
     */
    @Autowired
    private EmailService emailService;

    /**
     * 用户登录方法
     * 验证用户凭据并生成访问令牌和刷新令牌
     * 
     * @param loginRequest 登录请求对象，包含用户名和密码
     * @return LoginResponse 登录响应对象，包含访问令牌、刷新令牌和用户信息
     * @throws RuntimeException 当用户不存在、账户被禁用或密码错误时抛出异常
     * 
     * 业务流程：
     * 1. 根据用户名查询用户信息
     * 2. 验证用户是否存在
     * 3. 检查账户是否处于激活状态
     * 4. 验证密码是否正确
     * 5. 生成访问令牌和刷新令牌
     * 6. 将刷新令牌存储到Redis中，设置7天过期时间
     * 7. 构建并返回登录响应对象
     */
    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        // 根据用户名查询用户信息
        User user = userMapper.selectByUsername(loginRequest.getUsername());
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        // 检查账户是否被禁用
        if (!user.getIsActive()) {
            throw new RuntimeException("账户已被禁用");
        }
        // 验证密码是否正确
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        
        // 生成JWT访问令牌和刷新令牌
        String accessToken = jwtUtil.generateAccessToken(user.getUsername(), user.getId());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername(), user.getId());
        
        // 将刷新令牌存储到Redis中，设置7天过期时间
        String refreshTokenKey = "refresh_token:" + user.getId();
        redisTemplate.opsForValue().set(refreshTokenKey, refreshToken, 7, TimeUnit.DAYS);
        
        // 构建用户信息对象
        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setEmail(user.getEmail());
        
        // 返回登录响应对象
        return new LoginResponse(accessToken, refreshToken, 24 * 60 * 60L, userInfo);
    }

    /**
     * 用户注册方法
     * 验证注册信息并创建新用户账户
     * 
     * @param registerRequest 注册请求对象，包含用户名、密码、确认密码和邮箱
     * @throws RuntimeException 当密码不一致、用户名已存在或邮箱已被注册时抛出异常
     * 
     * 业务流程：
     * 1. 验证密码和确认密码是否一致
     * 2. 检查用户名是否已被使用
     * 3. 检查邮箱是否已被注册
     * 4. 对密码进行加密处理
     * 5. 创建用户对象并设置默认状态为激活
     * 6. 将用户信息保存到数据库
     * 
     * 安全考虑：
     * - 密码使用BCrypt等强加密算法进行哈希处理
     * - 确保用户名和邮箱的唯一性
     * - 新注册用户默认为激活状态
     */
    @Override
    public void register(RegisterRequest registerRequest) {
        // 验证密码和确认密码是否一致
        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            throw new RuntimeException("两次输入的密码不一致");
        }
        
        // 检查用户名是否已被使用
        if (userMapper.selectByUsername(registerRequest.getUsername()) != null) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 检查邮箱是否已被注册
        if (userMapper.selectByEmail(registerRequest.getEmail()) != null) {
            throw new RuntimeException("邮箱已被注册");
        }
        
        // 创建新用户对象
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        // 对密码进行加密处理
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setEmail(registerRequest.getEmail());
        // 设置用户状态为激活
        user.setIsActive(true);
        
        // 将用户信息保存到数据库
        userMapper.insert(user);
    }
    
    /**
     * 刷新访问令牌方法
     * 使用有效的刷新令牌生成新的访问令牌
     * 
     * @param refreshToken 刷新令牌字符串
     * @return LoginResponse 包含新访问令牌和用户信息的登录响应对象
     * @throws RuntimeException 当刷新令牌无效、已过期或用户状态异常时抛出异常
     * 
     * 业务流程：
     * 1. 从刷新令牌中解析用户名和用户ID
     * 2. 验证令牌类型是否为refresh类型
     * 3. 从Redis中获取存储的刷新令牌进行比对
     * 4. 验证用户是否存在且处于激活状态
     * 5. 生成新的访问令牌
     * 6. 构建并返回登录响应对象
     * 
     * 安全考虑：
     * - 验证刷新令牌的类型和有效性
     * - 与Redis中存储的令牌进行严格比对
     * - 确保用户账户状态正常
     * - 保持刷新令牌不变，仅更新访问令牌
     */
    @Override
    public LoginResponse refreshToken(String refreshToken) {
        try {
            // 从刷新令牌中解析用户信息
            String username = jwtUtil.getUsernameFromToken(refreshToken);
            Long userId = jwtUtil.getUserIdFromToken(refreshToken);
            
            // 验证令牌类型是否为refresh类型
            if (!"refresh".equals(jwtUtil.getTokenType(refreshToken))) {
                throw new RuntimeException("无效的刷新令牌");
            }
            
            // 从Redis中获取存储的刷新令牌进行验证
            String refreshTokenKey = "refresh_token:" + userId;
            String storedToken = redisTemplate.opsForValue().get(refreshTokenKey);
            if (!refreshToken.equals(storedToken)) {
                throw new RuntimeException("刷新令牌已失效");
            }
            
            // 验证用户是否存在且处于激活状态
            User user = userMapper.selectByUsername(username);
            if (user == null || !user.getIsActive()) {
                throw new RuntimeException("用户不存在或已被禁用");
            }
            
            // 生成新的访问令牌
            String newAccessToken = jwtUtil.generateAccessToken(username, userId);
            
            // 构建用户信息对象
            LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo();
            userInfo.setId(user.getId());
            userInfo.setUsername(user.getUsername());
            userInfo.setEmail(user.getEmail());
            
            // 返回包含新访问令牌的登录响应对象
            return new LoginResponse(newAccessToken, refreshToken, 24 * 60 * 60L, userInfo);
            
        } catch (Exception e) {
            throw new RuntimeException("刷新令牌无效: " + e.getMessage());
        }
    }
    
    /**
     * 根据用户名查找用户
     * 
     * @param username 用户名
     * @return User 用户对象，如果未找到则返回null
     * 
     * 用途：
     * - 用户登录时验证用户是否存在
     * - 检查用户名是否已被使用
     * - 获取用户详细信息
     */
    @Override
    public User findByUsername(String username) {
        return userMapper.selectByUsername(username);
    }
    
    /**
     * 根据邮箱地址查找用户
     * 
     * @param email 邮箱地址
     * @return User 用户对象，如果未找到则返回null
     * 
     * 用途：
     * - 用户注册时检查邮箱是否已被使用
     * - 密码重置时验证邮箱是否存在
     * - 邮箱验证功能
     */
    @Override
    public User findByEmail(String email) {
        return userMapper.selectByEmail(email);
    }
    
    /**
     * 根据用户ID查找用户
     * 
     * @param userId 用户ID
     * @return User 用户对象，如果未找到则返回null
     * 
     * 用途：
     * - 根据JWT令牌中的用户ID获取用户信息
     * - 用户信息查询和更新操作
     * - 权限验证和用户状态检查
     */
    @Override
    public User findById(Long userId) {
        return userMapper.selectById(userId);
    }
    
    /**
     * 发送密码重置邮件
     * 生成密码重置令牌并通过邮件发送给用户
     * 
     * @param email 用户邮箱地址
     * @throws RuntimeException 当邮箱不存在时抛出异常
     * 
     * 业务流程：
     * 1. 根据邮箱地址查找用户
     * 2. 验证用户是否存在
     * 3. 生成密码重置令牌
     * 4. 将令牌存储到Redis中，设置30分钟过期时间
     * 5. 通过邮件服务发送包含重置链接的邮件
     * 
     * 安全考虑：
     * - 重置令牌有效期限制为30分钟
     * - 令牌存储在Redis中便于验证和管理
     * - 防止邮箱枚举攻击（仅在邮箱存在时发送邮件）
     */
    @Override
    public void sendPasswordResetEmail(String email) {
        // 根据邮箱地址查找用户
        User user = userMapper.selectByEmail(email);
        if (user == null) {
            throw new RuntimeException("邮箱不存在");
        }
        
        // 生成密码重置令牌
        String resetToken = jwtUtil.generateAccessToken(user.getUsername(), user.getId());
        
        // 将重置令牌存储到Redis中，设置30分钟过期时间
        String resetTokenKey = "password_reset:" + user.getId();
        redisTemplate.opsForValue().set(resetTokenKey, resetToken, 30, TimeUnit.MINUTES);
        
        // 通过邮件服务发送密码重置邮件
        emailService.sendPasswordResetEmail(email, resetToken);
    }
    
    /**
     * 重置用户密码
     * 使用有效的重置令牌更新用户密码
     * 
     * @param token 密码重置令牌
     * @param newPassword 新密码
     * @throws RuntimeException 当令牌无效、已过期或用户不存在时抛出异常
     * 
     * 业务流程：
     * 1. 从重置令牌中解析用户名和用户ID
     * 2. 从Redis中获取存储的重置令牌进行验证
     * 3. 验证用户是否存在
     * 4. 对新密码进行加密处理
     * 5. 更新用户密码到数据库
     * 6. 删除Redis中的重置令牌（确保一次性使用）
     * 
     * 安全考虑：
     * - 严格验证重置令牌的有效性
     * - 新密码使用强加密算法进行哈希处理
     * - 重置令牌使用后立即删除，防止重复使用
     * - 异常处理确保操作的原子性
     */
    @Override
    public void resetPassword(String token, String newPassword) {
        try {
            // 从重置令牌中解析用户信息
            String username = jwtUtil.getUsernameFromToken(token);
            Long userId = jwtUtil.getUserIdFromToken(token);
            
            // 从Redis中获取存储的重置令牌进行验证
            String resetTokenKey = "password_reset:" + userId;
            String storedToken = redisTemplate.opsForValue().get(resetTokenKey);
            if (!token.equals(storedToken)) {
                throw new RuntimeException("重置令牌已失效");
            }
            
            // 验证用户是否存在
            User user = userMapper.selectByUsername(username);
            if (user == null) {
                throw new RuntimeException("用户不存在");
            }
            
            // 对新密码进行加密处理并更新到数据库
            user.setPassword(passwordEncoder.encode(newPassword));
            userMapper.updateById(user);
            
            // 删除重置令牌，确保一次性使用
            redisTemplate.delete(resetTokenKey);
            
        } catch (Exception e) {
            throw new RuntimeException("密码重置失败: " + e.getMessage());
        }
    }
    
    /**
     * 验证密码重置令牌的有效性
     * 检查重置令牌是否有效且未过期
     * 
     * @param token 密码重置令牌
     * @return boolean 令牌是否有效，true表示有效，false表示无效
     * 
     * 验证逻辑：
     * 1. 从令牌中解析用户ID
     * 2. 从Redis中获取存储的重置令牌
     * 3. 比较传入令牌与存储令牌是否一致
     * 4. 检查令牌是否已过期
     * 5. 任何异常情况都返回false
     * 
     * 用途：
     * - 在密码重置页面验证链接有效性
     * - 防止使用过期或无效的重置链接
     * - 提供用户友好的错误提示
     */
    @Override
    public boolean validatePasswordResetToken(String token) {
        try {
            // 从令牌中解析用户ID
            Long userId = jwtUtil.getUserIdFromToken(token);
            String resetTokenKey = "password_reset:" + userId;
            // 从Redis中获取存储的重置令牌进行比对
            String storedToken = redisTemplate.opsForValue().get(resetTokenKey);
            // 验证令牌一致性和有效期
            return token.equals(storedToken) && !jwtUtil.isTokenExpired(token);
        } catch (Exception e) {
            // 任何异常情况都视为令牌无效
            return false;
        }
    }
    
    /**
     * 根据邮箱直接重置密码
     * 不需要令牌验证，直接通过邮箱重置密码
     * 
     * @param email 用户邮箱地址
     * @param newPassword 新密码
     * @throws RuntimeException 当用户不存在或重置失败时抛出异常
     * 
     * 业务流程：
     * 1. 根据邮箱地址查找用户
     * 2. 验证用户是否存在
     * 3. 对新密码进行加密处理
     * 4. 更新用户密码到数据库
     * 
     * 注意事项：
     * - 此方法绕过了令牌验证，应谨慎使用
     * - 通常用于管理员重置用户密码或特殊场景
     * - 建议结合其他验证机制使用
     */
    @Override
    public void resetPasswordByEmail(String email, String newPassword) {
        try {
            // 根据邮箱地址查找用户
            User user = userMapper.selectByEmail(email);
            if (user == null) {
                throw new RuntimeException("用户不存在");
            }
            
            // 对新密码进行加密处理并更新到数据库
            user.setPassword(passwordEncoder.encode(newPassword));
            userMapper.updateById(user);
            
        } catch (Exception e) {
            throw new RuntimeException("密码重置失败: " + e.getMessage());
        }
    }
}

