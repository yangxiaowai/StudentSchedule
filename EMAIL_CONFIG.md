# 邮件服务配置指南

## 概述

本项目已集成邮件发送功能，支持密码重置和邮箱验证码发送。目前支持多种邮件服务提供商。

## 配置步骤

### 1. 修改 application.yml 配置

在 `src/main/resources/application.yml` 文件中配置邮件服务：

```yaml
spring:
  mail:
    host: smtp.qq.com          # SMTP服务器地址
    port: 587                  # SMTP端口
    username: your-email@qq.com    # 发送邮箱
    password: your-app-password    # 邮箱授权码（不是登录密码）
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
            required: true
    default-encoding: UTF-8
```

### 2. 常用邮件服务商配置

#### QQ邮箱
```yaml
spring:
  mail:
    host: smtp.qq.com
    port: 587
    username: your-email@qq.com
    password: your-app-password  # QQ邮箱授权码
```

#### 163邮箱
```yaml
spring:
  mail:
    host: smtp.163.com
    port: 25
    username: your-email@163.com
    password: your-app-password  # 163邮箱授权码
```

#### Gmail
```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: your-email@gmail.com
    password: your-app-password  # Gmail应用专用密码
```

#### 企业邮箱（腾讯企业邮箱）
```yaml
spring:
  mail:
    host: smtp.exmail.qq.com
    port: 587
    username: your-email@yourcompany.com
    password: your-password
```

### 3. 获取邮箱授权码

#### QQ邮箱授权码获取步骤：
1. 登录QQ邮箱
2. 点击「设置」→「账户」
3. 找到「POP3/IMAP/SMTP/Exchange/CardDAV/CalDAV服务」
4. 开启「POP3/SMTP服务」或「IMAP/SMTP服务」
5. 按照提示发送短信，获取授权码
6. 将授权码填入配置文件的 `password` 字段

#### 163邮箱授权码获取步骤：
1. 登录163邮箱
2. 点击「设置」→「POP3/SMTP/IMAP」
3. 开启「POP3/SMTP服务」
4. 设置客户端授权密码
5. 将授权密码填入配置文件的 `password` 字段

### 4. 安全注意事项

1. **不要使用登录密码**：邮件配置中的密码应该是授权码/应用专用密码，不是邮箱登录密码
2. **保护配置信息**：不要将包含真实邮箱密码的配置文件提交到版本控制系统
3. **使用环境变量**：生产环境建议使用环境变量配置敏感信息

### 5. 环境变量配置（推荐）

```yaml
spring:
  mail:
    host: ${MAIL_HOST:smtp.qq.com}
    port: ${MAIL_PORT:587}
    username: ${MAIL_USERNAME:your-email@qq.com}
    password: ${MAIL_PASSWORD:your-app-password}
```

然后在系统环境变量或启动参数中设置：
```bash
export MAIL_HOST=smtp.qq.com
export MAIL_USERNAME=your-email@qq.com
export MAIL_PASSWORD=your-app-password
```

## 功能说明

### 1. 密码重置邮件
- 接口：`POST /api/user/forgot-password`
- 功能：发送包含重置链接的邮件
- 有效期：30分钟

### 2. 邮箱验证码
- 发送接口：`POST /api/user/send-verification-code`
- 验证接口：`POST /api/user/verify-code`
- 验证码：6位数字
- 有效期：10分钟

## 测试

配置完成后，可以通过以下方式测试邮件功能：

1. 启动应用
2. 调用忘记密码接口测试密码重置邮件
3. 调用发送验证码接口测试验证码邮件
4. 检查邮箱是否收到邮件

## 故障排除

### 常见问题

1. **535 Authentication failed**
   - 检查用户名和密码是否正确
   - 确认使用的是授权码而不是登录密码
   - 检查邮箱是否开启了SMTP服务

2. **连接超时**
   - 检查SMTP服务器地址和端口
   - 确认网络连接正常
   - 检查防火墙设置

3. **SSL/TLS错误**
   - 检查端口配置（587通常用于STARTTLS，465用于SSL）
   - 确认SSL/TLS配置正确

### 调试建议

在 `application.yml` 中开启邮件调试日志：

```yaml
logging:
  level:
    org.springframework.mail: DEBUG
    com.sun.mail: DEBUG
```

这将输出详细的邮件发送日志，有助于排查问题。