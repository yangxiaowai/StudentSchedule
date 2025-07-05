# AI Talk 模块

这个模块提供了AI对话的会话管理功能。实际的AI对话功能由前端直接调用阿里云API实现，后端只负责会话管理和统计。

## 架构说明

### 前端直接调用模式
- 前端 `AliCloudChat.vue` 组件直接调用阿里云百炼API
- 后端提供会话管理、统计和辅助功能
- 优势：减少网络延迟、降低后端负载、提高响应速度

## 功能特性

- 会话信息管理
- 消息计数统计
- 会话历史清除
- 活跃会话监控
- 跨域支持

## 主要组件

### Controller
- `ChatController`: 提供会话管理REST API接口

### Service
- `ChatService`: 聊天会话管理服务层
- `ChatMemoryService`: 会话信息和统计管理

## API 接口

### 获取会话信息
```
GET /api/chat/session/{sessionId}

响应:
{
    "sessionId": "session-123",
    "messageCount": 5,
    "timestamp": 1703123456789
}
```

### 清除会话历史
```
DELETE /api/chat/history/{sessionId}

响应:
{
    "success": true,
    "message": "Chat history cleared successfully",
    "sessionId": "session-123"
}
```

### 健康检查
```
GET /api/chat/health

响应:
{
    "status": "UP",
    "service": "Chat Session Management",
    "timestamp": 1703123456789,
    "note": "AI chat functionality is handled by frontend directly"
}
```

## 前端集成

前端 `AliCloudChat.vue` 组件配置：

```javascript
// 直接调用阿里云API
const apiUrl = 'https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions'
const apiKey = 'your-api-key'

// 可选：调用后端会话管理API
const sessionApi = '/api/chat/session/' + sessionId
```

## 配置说明

后端不再需要AI API配置，相关配置已在 `application.yml` 中注释：

```yaml
# AI API 配置 (可选，前端直接调用)
# qianwen:
#   api:
#     key: your-qianwen-api-key
#     url: https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation
#     model: qwen-turbo
```

## 安全配置

在 `SecurityConfig.java` 中，AI聊天相关API已配置为公开访问：

```java
.requestMatchers("/api/chat/**").permitAll()
.requestMatchers("/api/aitalk/**").permitAll()
```