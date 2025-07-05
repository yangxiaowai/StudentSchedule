<template>
  <div class="ai-chat-container">
    <!-- 左侧边栏 -->
    <div class="sidebar">
      <div class="sidebar-header">
        <h4>AI助手</h4>
      </div>
      <div class="assistant-list">
        <div 
          v-for="(assistant, key) in assistants" 
          :key="key"
          :class="['assistant-item', { 'active': selectedAssistantType === key }]"
          @click="selectAssistant(key)"
        >
          <div class="assistant-icon">{{ assistant.name.split(' ')[0] }}</div>
          <div class="assistant-info">
            <div class="assistant-name">{{ assistant.name.split(' ').slice(1).join(' ') }}</div>
          </div>
        </div>
      </div>
      <div class="sidebar-actions">
        <button @click="newSession" class="sidebar-btn" :disabled="isLoading">
          ➕ 新建会话
        </button>
        <button @click="clearHistory" class="sidebar-btn" :disabled="isLoading">
          🗑️ 清空当前助手
        </button>
        <button @click="clearAllHistory" class="sidebar-btn clear-all-btn" :disabled="isLoading">
          🗑️ 清空所有历史
        </button>
      </div>
    </div>
    
    <!-- 主聊天区域 -->
    <div class="chat-main">
      <div class="chat-header">
        <h3>{{ currentAssistant.name }}</h3>
        <div class="chat-controls">
          <!-- 配置按钮移到这里 -->
          <button @click="showConfig = true" class="config-btn" title="API配置">
            ⚙️
          </button>
        </div>
      </div>
    
    <div class="chat-messages" ref="messagesContainer">
      <div 
          v-for="(message, index) in messages" 
          :key="index" 
          :class="['message', message.role === 'user' ? 'user-message' : 'assistant-message', { 'streaming': message.streaming }]"
        >
        <div class="message-content">
          <div class="message-text" v-html="formatMessage(message.content)"></div>
          <div class="message-time">{{ formatTime(message.timestamp) }}</div>
        </div>
      </div>
      
      <!-- 加载指示器 -->
      <div v-if="isLoading" class="message assistant-message">
        <div class="message-content">
          <div class="typing-indicator">
            <span></span>
            <span></span>
            <span></span>
          </div>
        </div>
      </div>
    </div>
    
    <div class="chat-input">
      <div class="input-container">
        <textarea 
          v-model="inputMessage" 
          @keydown.enter.prevent="handleEnter"
          @input="adjustTextareaHeight"
          placeholder="请输入您的问题..."
          class="message-input"
          ref="messageInput"
          :disabled="isLoading"
          rows="1"
        ></textarea>
        <button 
          @click="sendMessage" 
          :disabled="!inputMessage.trim() || isLoading"
          class="send-btn"
        >
          <svg v-if="!isLoading" width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
            <path d="M2,21L23,12L2,3V10L17,12L2,14V21Z" />
          </svg>
          <div v-else class="loading-spinner"></div>
        </button>
      </div>
    </div>
    
    <!-- API配置面板 -->
    <div class="api-config" v-if="showConfig">
      <div class="config-header">
        <h4>API配置</h4>
        <button @click="showConfig = false" class="close-btn">×</button>
      </div>
      <div class="config-form">
        <div class="form-group">
          <label>API Key: <span style="color: #28a745; font-size: 12px;">✅ 已预配置</span></label>
          <input 
            v-model="apiKey" 
            type="password" 
            placeholder="API Key已预配置，无需修改"
            class="config-input"
            readonly
            style="background-color: #f8f9fa; cursor: not-allowed;"
          />
        </div>
        <div class="form-group">
          <label>模型:</label>
          <select v-model="selectedModel" class="config-select">
            <option value="qwen-plus">Qwen-Plus (推荐)</option>
            <option value="qwen-max">Qwen-Max (最强)</option>
            <option value="qwen-turbo">Qwen-Turbo (快速)</option>
            <option value="qwen3-235b-a22b">Qwen3-235B</option>
            <option value="qwen3-32b">Qwen3-32B</option>
          </select>
        </div>
        <div class="form-group">
          <label>
            <input v-model="enableThinking" type="checkbox" />
            启用思考模式 (适用于复杂推理任务)
          </label>
        </div>
        <button @click="saveConfig" class="save-btn">保存配置</button>
      </div>
    </div>
    </div> <!-- 关闭 chat-main -->
  </div>
</template>

<script>
export default {
  name: 'AliCloudChat',
  data() {
    return {
      inputMessage: '',
      isLoading: false,
      showConfig: false,
      apiKey: 'sk-9a11b176a6a8498cb516bed9408ca7a8', // 默认API Key
      selectedModel: 'qwen-plus',
      enableThinking: false,
      openai: null,
      selectedAssistantType: 'study-planner',
      // 为每个助手保存独立的聊天历史
      assistantMessages: {
        'study-planner': [],
        'task-analyzer': [],
        'daily-chat': [],
        'entertainment': []
      },
      assistants: {
        'study-planner': {
          name: '📚 学习计划规划助手',
          systemPrompt: '你是一个专业的学习计划规划助手。你的主要职责是帮助学生制定科学合理的学习计划，包括：\n1. 分析学习目标和现状\n2. 制定阶段性学习计划\n3. 安排学习时间和进度\n4. 推荐学习方法和资源\n5. 监督学习进度和调整计划\n\n请用专业、耐心、鼓励的语气与用户交流，提供具体可行的学习建议。',
          welcomeMessage: '您好！我是您的学习计划规划助手 📚\n\n我可以帮助您：\n• 制定个性化学习计划\n• 分析学习目标和现状\n• 安排合理的学习时间\n• 推荐有效的学习方法\n• 跟踪学习进度\n\n请告诉我您的学习目标或当前遇到的学习规划问题，我将为您提供专业的建议！'
        },
        'task-analyzer': {
          name: '📊 学习任务分析助手',
          systemPrompt: '你是一个专业的学习任务分析助手。你的主要职责是帮助学生分析和优化学习任务，包括：\n1. 分解复杂学习任务\n2. 评估任务难度和时间需求\n3. 优化任务执行顺序\n4. 识别学习重点和难点\n5. 提供任务完成策略\n\n请用逻辑清晰、条理分明的方式分析问题，提供具体的解决方案。',
          welcomeMessage: '您好！我是您的学习任务分析助手 📊\n\n我可以帮助您：\n• 分解复杂的学习任务\n• 评估任务难度和时间\n• 优化学习任务顺序\n• 识别学习重点难点\n• 制定任务完成策略\n\n请描述您需要分析的学习任务或遇到的学习难题，我将为您提供详细的分析和建议！'
        },
        'daily-chat': {
          name: '💬 AI日常生活聊天助手',
          systemPrompt: '你是一个友善、有趣的日常生活聊天助手。你的特点是：\n1. 善于倾听和理解用户的情感\n2. 能够就各种日常话题进行有趣的对话\n3. 提供生活建议和情感支持\n4. 保持积极乐观的态度\n5. 适当使用幽默和表情符号\n\n请用轻松、友好、自然的语气与用户交流，让对话充满温暖和乐趣。',
          welcomeMessage: '嗨！我是您的日常生活聊天助手 💬\n\n我很高兴能和您聊天！我们可以聊：\n• 日常生活的点点滴滴\n• 兴趣爱好和娱乐\n• 心情感受和想法\n• 生活小贴士和建议\n• 或者任何您想聊的话题\n\n今天过得怎么样？有什么想和我分享的吗？😊'
        },
        'entertainment': {
          name: '🎮 AI娱乐对话助手',
          systemPrompt: '你是一个充满活力和创意的娱乐对话助手。你的特点是：\n1. 热爱各种娱乐活动和流行文化\n2. 能够讨论游戏、电影、音乐、动漫等话题\n3. 善于创造有趣的对话和游戏\n4. 了解最新的娱乐资讯和趋势\n5. 能够推荐娱乐内容和活动\n\n请用活泼、有趣、充满热情的语气与用户交流，让对话充满乐趣和惊喜。',
          welcomeMessage: '嘿！欢迎来到娱乐时光！🎮\n\n我是您的娱乐对话助手，我们可以聊：\n• 🎮 游戏攻略和推荐\n• 🎬 电影电视剧讨论\n• 🎵 音乐和明星八卦\n• 📺 动漫和二次元\n• 🎪 有趣的话题和游戏\n\n准备好开始我们的娱乐之旅了吗？有什么好玩的想和我分享？✨'
        }
      }
    }
  },
  computed: {
    currentAssistant() {
      return this.assistants[this.selectedAssistantType]
    },
    messages: {
      get() {
        return this.assistantMessages[this.selectedAssistantType] || []
      },
      set(value) {
        this.assistantMessages[this.selectedAssistantType] = value
      }
    }
  },
  mounted() {
    this.loadConfig()
    this.loadAssistantMessages()
    this.initializeWelcomeMessages()
    // 由于有预设API Key，自动初始化OpenAI客户端
    this.initOpenAI()
    
    // 添加全局代码复制函数
    window.copyCodeContent = (codeId) => {
      const codeElement = document.getElementById(codeId)
      if (codeElement) {
        const button = codeElement.parentElement.parentElement.querySelector('.copy-btn')
        navigator.clipboard.writeText(codeElement.textContent).then(() => {
          if (button) {
            button.textContent = '✅'
            setTimeout(() => {
              button.textContent = '📋'
            }, 2000)
          }
        }).catch(() => {
          if (button) {
            button.textContent = '❌'
            setTimeout(() => {
              button.textContent = '📋'
            }, 2000)
          }
        })
      }
    }
  },
  methods: {
    // 初始化OpenAI客户端
    async initOpenAI() {
      try {
        // 动态导入OpenAI
        const { default: OpenAI } = await import('openai')
        
        if (this.apiKey) {
          // 创建配置对象
          const config = {
            apiKey: this.apiKey,
            baseURL: 'https://dashscope.aliyuncs.com/compatible-mode/v1',
            dangerouslyAllowBrowser: true,
            // 添加默认头部以避免私有成员访问问题
            defaultHeaders: {
              'User-Agent': 'AliCloud-Chat/1.0'
            }
          }
          
          this.openai = new OpenAI(config)
          console.log('OpenAI客户端初始化成功')
        }
      } catch (error) {
        console.error('初始化OpenAI客户端失败:', error)
        this.addErrorMessage('OpenAI客户端初始化失败，请检查配置')
      }
    },
    
    // 加载配置
    loadConfig() {
      const savedConfig = localStorage.getItem('alicloud-chat-config')
      if (savedConfig) {
        const config = JSON.parse(savedConfig)
        this.apiKey = config.apiKey || 'sk-9a11b176a6a8498cb516bed9408ca7a8'
        this.selectedModel = config.selectedModel || 'qwen-plus'
        this.enableThinking = config.enableThinking || false
        this.selectedAssistantType = config.selectedAssistantType || 'study-planner'
      }
    },
    
    // 加载助手消息历史
    loadAssistantMessages() {
      const savedMessages = localStorage.getItem('alicloud-chat-messages')
      if (savedMessages) {
        try {
          const messages = JSON.parse(savedMessages)
          this.assistantMessages = { ...this.assistantMessages, ...messages }
        } catch (error) {
          console.error('加载聊天历史失败:', error)
        }
      }
    },
    
    // 保存助手消息历史
    saveAssistantMessages() {
      localStorage.setItem('alicloud-chat-messages', JSON.stringify(this.assistantMessages))
    },
    
    // 保存配置
    saveConfig() {
      const config = {
        apiKey: this.apiKey,
        selectedModel: this.selectedModel,
        enableThinking: this.enableThinking,
        selectedAssistantType: this.selectedAssistantType
      }
      localStorage.setItem('alicloud-chat-config', JSON.stringify(config))
      this.initOpenAI()
      this.showConfig = false
      
      // 显示保存成功消息
      this.addErrorMessage('✅ 配置已保存成功！')
    },
    
    // 初始化所有助手的欢迎消息
    initializeWelcomeMessages() {
      Object.keys(this.assistants).forEach(assistantType => {
        if (!this.assistantMessages[assistantType] || this.assistantMessages[assistantType].length === 0) {
          this.assistantMessages[assistantType] = [{
            role: 'assistant',
            content: this.assistants[assistantType].welcomeMessage,
            timestamp: new Date()
          }]
        }
      })
      this.saveAssistantMessages()
    },
    
    // 添加欢迎消息
    addWelcomeMessage(assistantType = null) {
      const type = assistantType || this.selectedAssistantType
      const welcomeMessage = {
        role: 'assistant',
        content: this.assistants[type].welcomeMessage,
        timestamp: new Date()
      }
      this.assistantMessages[type].push(welcomeMessage)
      this.saveAssistantMessages()
    },
    
    // 切换助手（保留历史记录）
    switchAssistant() {
      // 切换助手时不清空历史记录，只是切换到对应助手的历史
      this.$nextTick(() => {
        this.scrollToBottom()
      })
    },
    
    selectAssistant(assistantType) {
      if (this.selectedAssistantType !== assistantType) {
        this.selectedAssistantType = assistantType
        this.switchAssistant()
        // 保存当前选择的助手
        this.saveConfig()
      }
    },
    
    // 添加错误消息
    addErrorMessage(content) {
      const errorMessage = {
        role: 'assistant',
        content: content,
        timestamp: new Date()
      }
      this.assistantMessages[this.selectedAssistantType].push(errorMessage)
      this.saveAssistantMessages()
    },
    
    // 新建会话（仅清空当前助手的历史）
    newSession() {
      this.assistantMessages[this.selectedAssistantType] = []
      this.addWelcomeMessage()
      this.scrollToBottom()
    },
    
    // 清空当前助手的历史
    clearHistory() {
      this.assistantMessages[this.selectedAssistantType] = []
      this.addWelcomeMessage()
      this.saveAssistantMessages()
      this.scrollToBottom()
    },
    
    // 清空所有助手的历史
    clearAllHistory() {
      Object.keys(this.assistantMessages).forEach(assistantType => {
        this.assistantMessages[assistantType] = []
      })
      this.initializeWelcomeMessages()
      this.scrollToBottom()
    },
    
    // 发送消息
    async sendMessage() {
      if (!this.inputMessage.trim() || this.isLoading) return
      
      if (!this.openai) {
        this.addErrorMessage('请先配置API Key')
        this.showConfig = true
        return
      }
      
      const userMessage = this.inputMessage.trim()
      this.inputMessage = ''
      this.adjustTextareaHeight()
      
      // 添加用户消息
      const userMsg = {
        role: 'user',
        content: userMessage,
        timestamp: new Date()
      }
      this.assistantMessages[this.selectedAssistantType].push(userMsg)
      this.saveAssistantMessages()
      
      this.scrollToBottom()
      this.isLoading = true
      
      try {
        await this.callAliCloudAPI(userMessage)
      } catch (error) {
        console.error('发送消息失败:', error)
        this.addErrorMessage(`抱歉，发生了错误：${error.message}`)
      } finally {
        this.isLoading = false
      }
    },
    
    // 调用阿里云API
    async callAliCloudAPI(userMessage) {
      try {
        // 检查OpenAI客户端是否已初始化
        if (!this.openai) {
          await this.initOpenAI()
          if (!this.openai) {
            throw new Error('OpenAI客户端未初始化，请检查API Key配置')
          }
        }
        
        // 构建消息历史
        const messages = [
          { role: 'system', content: this.currentAssistant.systemPrompt },
          ...this.messages.filter(msg => msg.role === 'user' || msg.role === 'assistant').slice(-10).map(msg => ({
            role: msg.role,
            content: msg.content
          }))
        ]
        
        // 添加当前用户消息
        messages.push({ role: 'user', content: userMessage })
        
        // 准备API参数
        const apiParams = {
          model: this.selectedModel,
          messages: messages,
          stream: true,
          max_tokens: 2000,
          temperature: 0.7
        }
        
        // 如果启用思考模式且模型支持
        if (this.enableThinking && (this.selectedModel.includes('qwen3') || this.selectedModel.includes('qwq'))) {
          apiParams.enable_thinking = true
        }
        
        console.log('调用阿里云API:', apiParams)
        
        // 添加助手消息占位符
        const assistantMessage = {
          role: 'assistant',
          content: '',
          timestamp: new Date(),
          streaming: true
        }
        this.assistantMessages[this.selectedAssistantType].push(assistantMessage)
        const assistantMessageIndex = this.assistantMessages[this.selectedAssistantType].length - 1
        
        try {
          // 使用SSE方式进行流式调用
          await this.callStreamingAPI(apiParams, assistantMessageIndex)
          
        } catch (streamError) {
          console.error('流式处理错误:', streamError)
          // 如果流式处理失败，尝试非流式调用
          await this.fallbackToNonStreamAPI(apiParams, assistantMessageIndex)
        }
        
      } catch (error) {
        console.error('阿里云API调用失败:', error)
        // 移除可能添加的空消息
        const currentMessages = this.assistantMessages[this.selectedAssistantType]
        if (currentMessages[currentMessages.length - 1]?.content === '') {
          currentMessages.pop()
        }
        this.saveAssistantMessages()
        throw new Error(`API调用失败: ${error.message}`)
      }
    },
    
    // 使用SSE方式进行流式API调用
    async callStreamingAPI(apiParams, assistantMessageIndex) {
      try {
        console.log('开始SSE流式调用')
        
        const requestBody = {
          model: apiParams.model,
          messages: apiParams.messages,
          stream: true,
          max_tokens: apiParams.max_tokens || 2000,
          temperature: apiParams.temperature || 0.7
        }
        
        if (apiParams.enable_thinking) {
          requestBody.enable_thinking = true
        }
        
        const response = await fetch('https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${this.apiKey}`,
            'Accept': 'text/event-stream',
            'Cache-Control': 'no-cache'
          },
          body: JSON.stringify(requestBody)
        })
        
        if (!response.ok) {
          const errorText = await response.text()
          throw new Error(`HTTP ${response.status}: ${errorText}`)
        }
        
        const reader = response.body.getReader()
        const decoder = new TextDecoder()
        let fullContent = ''
        
        while (true) {
          const { done, value } = await reader.read()
          if (done) break
          
          const chunk = decoder.decode(value, { stream: true })
          const lines = chunk.split('\n')
          
          for (const line of lines) {
            if (line.startsWith('data: ')) {
              const data = line.slice(6).trim()
              
              if (data === '[DONE]') {
                  console.log('流式输出完成')
                  this.assistantMessages[this.selectedAssistantType][assistantMessageIndex].streaming = false
                  this.saveAssistantMessages()
                  return
                }
              
              try {
                const parsed = JSON.parse(data)
                const content = parsed.choices?.[0]?.delta?.content || ''
                
                if (content) {
                  fullContent += content
                  this.assistantMessages[this.selectedAssistantType][assistantMessageIndex].content = fullContent
                  
                  // 添加小延迟以创建更自然的打字机效果
                  await new Promise(resolve => setTimeout(resolve, 20))
                  
                  this.$nextTick(() => {
                    this.scrollToBottom()
                  })
                }
              } catch (parseError) {
                console.warn('解析SSE数据失败:', parseError, data)
              }
            }
          }
        }
        
        // 流式输出完成，移除流式状态
        this.assistantMessages[this.selectedAssistantType][assistantMessageIndex].streaming = false
        this.saveAssistantMessages()
        console.log('SSE流式调用完成，完整回复:', fullContent)
        
      } catch (error) {
        console.error('SSE流式调用失败:', error)
        throw error
      }
    },
    
    // 备用非流式API调用
     async fallbackToNonStreamAPI(apiParams, assistantMessageIndex) {
       try {
         console.log('尝试非流式API调用')
         const nonStreamParams = { ...apiParams, stream: false }
         const response = await this.openai.chat.completions.create(nonStreamParams)
         
         const content = response.choices?.[0]?.message?.content || '抱歉，没有收到有效回复'
         this.assistantMessages[this.selectedAssistantType][assistantMessageIndex].content = content
         this.assistantMessages[this.selectedAssistantType][assistantMessageIndex].streaming = false
         this.saveAssistantMessages()
         this.scrollToBottom()
         
         console.log('非流式API调用完成:', content)
       } catch (fallbackError) {
         console.error('非流式API调用也失败，尝试HTTP请求:', fallbackError)
         // 最终备用方案：直接HTTP请求
         await this.httpFallbackAPI(apiParams, assistantMessageIndex)
       }
     },
     
     // HTTP备用API调用
     async httpFallbackAPI(apiParams, assistantMessageIndex) {
       try {
         console.log('使用HTTP备用方案')
         
         const requestBody = {
           model: apiParams.model,
           messages: apiParams.messages,
           stream: false,
           max_tokens: apiParams.max_tokens || 2000,
           temperature: apiParams.temperature || 0.7
         }
         
         if (apiParams.enable_thinking) {
           requestBody.enable_thinking = true
         }
         
         const response = await fetch('https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions', {
           method: 'POST',
           headers: {
             'Content-Type': 'application/json',
             'Authorization': `Bearer ${this.apiKey}`,
             'User-Agent': 'AliCloud-Chat/1.0'
           },
           body: JSON.stringify(requestBody)
         })
         
         if (!response.ok) {
           const errorText = await response.text()
           throw new Error(`HTTP ${response.status}: ${errorText}`)
         }
         
         const data = await response.json()
         const content = data.choices?.[0]?.message?.content || '抱歉，没有收到有效回复'
         
         this.assistantMessages[this.selectedAssistantType][assistantMessageIndex].content = content
         this.assistantMessages[this.selectedAssistantType][assistantMessageIndex].streaming = false
         this.saveAssistantMessages()
         this.scrollToBottom()
         
         console.log('HTTP备用API调用完成:', content)
       } catch (httpError) {
         console.error('HTTP备用API也失败:', httpError)
         throw new Error(`所有API调用方式都失败: ${httpError.message}`)
       }
     },
    
    // 处理Enter键
    handleEnter(event) {
      if (event.shiftKey) {
        return // Shift+Enter 换行
      }
      this.sendMessage()
    },
    
    // 自动调整文本框高度
    adjustTextareaHeight() {
      this.$nextTick(() => {
        const textarea = this.$refs.messageInput
        if (textarea) {
          textarea.style.height = 'auto'
          textarea.style.height = Math.min(textarea.scrollHeight, 120) + 'px'
        }
      })
    },
    
    // 滚动到底部
    scrollToBottom() {
      this.$nextTick(() => {
        const container = this.$refs.messagesContainer
        if (container) {
          container.scrollTop = container.scrollHeight
        }
      })
    },
    
    // 格式化消息内容
    formatMessage(content) {
      if (!content) return ''
      
      let formatted = content
      
      // 1. 处理代码块（三个反引号）
      formatted = formatted.replace(/```([\s\S]*?)```/g, (match, code) => {
        const lines = code.trim().split('\n')
        const language = lines[0].trim()
        const codeContent = lines.slice(language ? 1 : 0).join('\n')
        const codeId = 'code_' + Math.random().toString(36).substr(2, 9)
        return `<div class="code-block">
          ${language ? `<div class="code-language">${language}</div>` : ''}
          <pre><code id="${codeId}">${this.escapeHtml(codeContent)}</code></pre>
          <button class="copy-btn" onclick="window.copyCodeContent('${codeId}')" title="复制代码">📋</button>
        </div>`
      })
      
      // 2. 处理数学公式（LaTeX格式）
      formatted = formatted.replace(/\$\$([\s\S]*?)\$\$/g, '<div class="math-block">$$$1$$</div>')
      formatted = formatted.replace(/\$([^$\n]+)\$/g, '<span class="math-inline">$$1$</span>')
      
      // 3. 处理表格
      formatted = formatted.replace(/(\|[^\n]+\|\n)(\|[-\s|]+\|\n)((\|[^\n]+\|\n?)+)/g, (match, header, separator, rows) => {
        const headerCells = header.split('|').filter(cell => cell.trim()).map(cell => `<th>${cell.trim()}</th>`).join('')
        const rowCells = rows.trim().split('\n').map(row => {
          const cells = row.split('|').filter(cell => cell.trim()).map(cell => `<td>${cell.trim()}</td>`).join('')
          return `<tr>${cells}</tr>`
        }).join('')
        return `<table class="message-table"><thead><tr>${headerCells}</tr></thead><tbody>${rowCells}</tbody></table>`
      })
      
      // 4. 处理列表
      formatted = formatted.replace(/^(\d+\.)\s+(.+)$/gm, '<li class="ordered-list">$2</li>')
      formatted = formatted.replace(/^[-*+]\s+(.+)$/gm, '<li class="unordered-list">$1</li>')
      
      // 5. 处理链接
      formatted = formatted.replace(/\[([^\]]+)\]\(([^)]+)\)/g, '<a href="$2" target="_blank" class="message-link">$1</a>')
      
      // 6. 处理引用
      formatted = formatted.replace(/^>\s+(.+)$/gm, '<blockquote class="message-quote">$1</blockquote>')
      
      // 7. 处理标题
      formatted = formatted.replace(/^###\s+(.+)$/gm, '<h3 class="message-h3">$1</h3>')
      formatted = formatted.replace(/^##\s+(.+)$/gm, '<h2 class="message-h2">$1</h2>')
      formatted = formatted.replace(/^#\s+(.+)$/gm, '<h1 class="message-h1">$1</h1>')
      
      // 8. 处理粗体和斜体
      formatted = formatted.replace(/\*\*(.*?)\*\*/g, '<strong class="message-bold">$1</strong>')
      formatted = formatted.replace(/\*(.*?)\*/g, '<em class="message-italic">$1</em>')
      
      // 9. 处理行内代码
      formatted = formatted.replace(/`([^`]+)`/g, '<code class="message-code">$1</code>')
      
      // 10. 处理删除线
      formatted = formatted.replace(/~~(.*?)~~/g, '<del class="message-strikethrough">$1</del>')
      
      // 11. 处理换行
      formatted = formatted.replace(/\n/g, '<br>')
      
      // 12. 包装列表项
      formatted = formatted.replace(/(<li class="ordered-list">.*?<\/li>)/g, '<ol class="message-ol">$1</ol>')
      formatted = formatted.replace(/(<li class="unordered-list">.*?<\/li>)/g, '<ul class="message-ul">$1</ul>')
      
      return formatted
    },
    
    // HTML转义
    escapeHtml(text) {
      const div = document.createElement('div')
      div.textContent = text
      return div.innerHTML
    },
    
    // 复制代码功能
    copyCode(button) {
      const codeBlock = button.parentElement.querySelector('code')
      if (codeBlock) {
        navigator.clipboard.writeText(codeBlock.textContent).then(() => {
          button.textContent = '✅'
          setTimeout(() => {
            button.textContent = '📋'
          }, 2000)
        }).catch(() => {
          button.textContent = '❌'
          setTimeout(() => {
            button.textContent = '📋'
          }, 2000)
        })
      }
    },
    
    // 格式化时间
    formatTime(timestamp) {
      return new Date(timestamp).toLocaleTimeString('zh-CN', {
        hour: '2-digit',
        minute: '2-digit'
      })
    }
  }
}
</script>

<style scoped>
.ai-chat-container {
  display: flex;
  flex-direction: row;
  height: 85vh;
  min-height: 700px;
  background: linear-gradient(135deg, #f5e6d3 0%, #e8d5b7 50%, #d4c4a8 100%);
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12), 0 2px 8px rgba(0, 0, 0, 0.08);
  overflow: hidden;
  position: relative;
  font-family: 'Inter', 'SF Pro Display', -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', sans-serif;
  backdrop-filter: blur(10px);
}

/* 左侧边栏样式 */
.sidebar {
  width: 300px;
  background: linear-gradient(135deg, #8b7355 0%, #6d5a47 50%, #5a4a3a 100%);
  color: white;
  display: flex;
  flex-direction: column;
  border-radius: 16px 0 0 16px;
  box-shadow: inset -2px 0 8px rgba(0, 0, 0, 0.1);
}

.sidebar-header {
  padding: 24px 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.15);
  text-align: center;
  background: rgba(255, 255, 255, 0.05);
}

.sidebar-header h4 {
  margin: 0;
  color: white;
  font-size: 22px;
  font-weight: 700;
  text-align: center;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
  letter-spacing: 0.5px;
}

.assistant-list {
  flex: 1;
  padding: 16px 12px;
  overflow-y: auto;
}

.assistant-item {
  display: flex;
  align-items: center;
  padding: 16px 18px;
  margin-bottom: 12px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(8px);
}

.assistant-item:hover {
  background: rgba(255, 255, 255, 0.18);
  transform: translateX(6px) scale(1.02);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.2);
}

.assistant-item.active {
  background: rgba(255, 255, 255, 0.25);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.3);
  border-color: rgba(255, 255, 255, 0.3);
  transform: translateX(8px);
}

.assistant-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.25) 0%, rgba(255, 255, 255, 0.15) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  font-size: 22px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
  border: 2px solid rgba(255, 255, 255, 0.2);
  transition: all 0.3s ease;
}

.assistant-item:hover .assistant-icon {
  transform: scale(1.1);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.3);
}

.assistant-info {
  flex: 1;
}

.assistant-name {
  font-size: 15px;
  font-weight: 600;
  color: white;
  margin: 0;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
  letter-spacing: 0.3px;
  line-height: 1.3;
}

.sidebar-actions {
  padding: 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.sidebar-btn {
  background: rgba(255, 255, 255, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: white;
  padding: 12px 18px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 10px;
  backdrop-filter: blur(8px);
  letter-spacing: 0.3px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.sidebar-btn:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.25);
  transform: translateY(-2px) scale(1.02);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.2);
}

.clear-all-btn {
  background: rgba(220, 53, 69, 0.1) !important;
  border: 1px solid rgba(220, 53, 69, 0.3) !important;
  color: #dc3545 !important;
}

.clear-all-btn:hover:not(:disabled) {
  background: rgba(220, 53, 69, 0.2) !important;
  border-color: rgba(220, 53, 69, 0.5) !important;
  box-shadow: 0 4px 12px rgba(220, 53, 69, 0.2) !important;
}

.sidebar-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
}

/* 主聊天区域样式 */
.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 0 16px 16px 0;
  backdrop-filter: blur(10px);
  box-shadow: inset 2px 0 8px rgba(0, 0, 0, 0.05);
}

.chat-header {
  background: linear-gradient(135deg, #8b7355 0%, #6d5a47 100%);
  color: white;
  padding: 20px 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.chat-header h3 {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
  letter-spacing: 0.5px;
}

.chat-controls {
  display: flex;
  gap: 8px;
  align-items: center;
}

.config-btn {
  background: rgba(255, 255, 255, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.25);
  color: white;
  padding: 10px 16px;
  border-radius: 8px;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(8px);
  font-weight: 500;
}

.config-btn:hover {
  background: rgba(255, 255, 255, 0.25);
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  background: linear-gradient(135deg, #faf8f3 0%, #f5f1e8 50%, #f0ebe0 100%);
  position: relative;
}

.message {
  display: flex;
  margin-bottom: 12px;
}

.user-message {
  justify-content: flex-end;
}

.assistant-message {
  justify-content: flex-start;
}

.message-content {
  max-width: 75%;
  padding: 14px 18px;
  border-radius: 20px;
  position: relative;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(8px);
}

.user-message .message-content {
  background: linear-gradient(135deg, #8b7355 0%, #6d5a47 100%);
  color: white;
  border-bottom-right-radius: 6px;
  box-shadow: 0 3px 12px rgba(139, 115, 85, 0.3);
}

.assistant-message .message-content {
  background: rgba(255, 255, 255, 0.9);
  color: #2c2c2c;
  border: 1px solid rgba(139, 115, 85, 0.2);
  border-bottom-left-radius: 6px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.message-text {
  line-height: 1.6;
  word-wrap: break-word;
  font-size: 15px;
  font-weight: 400;
  letter-spacing: 0.2px;
}

.message-time {
  font-size: 11px;
  opacity: 0.6;
  margin-top: 6px;
  text-align: right;
  font-weight: 500;
}

.assistant-message .message-time {
  text-align: left;
}

.typing-indicator {
  display: flex;
  align-items: center;
  gap: 4px;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #999;
  animation: typing 1.4s infinite ease-in-out;
}

.typing-indicator span:nth-child(1) { animation-delay: -0.32s; }
.typing-indicator span:nth-child(2) { animation-delay: -0.16s; }

@keyframes typing {
  0%, 80%, 100% {
    transform: scale(0);
    opacity: 0.5;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

.chat-input {
  padding: 20px 24px;
  background: rgba(255, 255, 255, 0.95);
  border-top: 1px solid rgba(139, 115, 85, 0.2);
  backdrop-filter: blur(10px);
}

.input-container {
  display: flex;
  align-items: flex-end;
  gap: 16px;
}

.message-input {
  flex: 1;
  border: 2px solid rgba(139, 115, 85, 0.3);
  border-radius: 24px;
  padding: 14px 20px;
  font-size: 15px;
  line-height: 1.5;
  resize: none;
  outline: none;
  transition: all 0.3s ease;
  min-height: 22px;
  max-height: 120px;
  background: rgba(255, 255, 255, 0.9);
  font-family: inherit;
  letter-spacing: 0.2px;
}

.message-input:focus {
  border-color: #8b7355;
  box-shadow: 0 0 0 3px rgba(139, 115, 85, 0.1);
  background: white;
}

.message-input:disabled {
  background: #f5f5f5;
  cursor: not-allowed;
}

.send-btn {
  background: linear-gradient(135deg, #8b7355 0%, #6d5a47 100%);
  border: none;
  color: white;
  width: 50px;
  height: 50px;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(139, 115, 85, 0.3);
  font-size: 18px;
}

.send-btn:hover:not(:disabled) {
  transform: scale(1.1);
  box-shadow: 0 6px 20px rgba(139, 115, 85, 0.4);
  background: linear-gradient(135deg, #9d8466 0%, #7a6352 100%);
}

.send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
}

.loading-spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top: 2px solid white;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* API配置面板 */
.api-config {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.api-config > div {
  background: white;
  border-radius: 12px;
  padding: 24px;
  width: 400px;
  max-width: 90%;
}

.config-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.config-header h4 {
  margin: 0;
  color: #333;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #999;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-btn:hover {
  color: #333;
}

.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  margin-bottom: 6px;
  color: #333;
  font-weight: 500;
}

.config-input, .config-select {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #e1e5e9;
  border-radius: 6px;
  font-size: 14px;
  outline: none;
  transition: border-color 0.2s;
}

.config-input:focus, .config-select:focus {
  border-color: #ff6b6b;
}

.save-btn {
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a24 100%);
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.save-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(255, 107, 107, 0.3);
}



/* 滚动条样式 */
.chat-messages::-webkit-scrollbar {
  width: 6px;
}

.chat-messages::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.chat-messages::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.chat-messages::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 消息格式化样式 */
.message-text .code-block {
  background: #f8f9fa;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  margin: 8px 0;
  position: relative;
  overflow: hidden;
}

.message-text .code-language {
  background: #e9ecef;
  padding: 4px 12px;
  font-size: 12px;
  color: #6c757d;
  border-bottom: 1px solid #dee2e6;
}

.message-text .code-block pre {
  margin: 0;
  padding: 12px;
  overflow-x: auto;
  background: #f8f9fa;
}

.message-text .code-block code {
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 13px;
  line-height: 1.4;
  color: #333;
}

.message-text .copy-btn {
  position: absolute;
  top: 8px;
  right: 8px;
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid #dee2e6;
  border-radius: 4px;
  padding: 4px 8px;
  cursor: pointer;
  font-size: 12px;
  transition: all 0.2s;
}

.message-text .copy-btn:hover {
  background: white;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.message-text .message-code {
  background: #f1f3f4;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 13px;
  color: #d73a49;
}

.message-text .message-table {
  border-collapse: collapse;
  width: 100%;
  margin: 8px 0;
  font-size: 14px;
}

.message-text .message-table th,
.message-text .message-table td {
  border: 1px solid #dee2e6;
  padding: 8px 12px;
  text-align: left;
}

.message-text .message-table th {
  background: #f8f9fa;
  font-weight: 600;
  color: #495057;
}

.message-text .message-table tr:nth-child(even) {
  background: #f8f9fa;
}

.message-text .message-ol,
.message-text .message-ul {
  margin: 8px 0;
  padding-left: 20px;
}

.message-text .message-ol li,
.message-text .message-ul li {
  margin: 4px 0;
  line-height: 1.5;
}

.message-text .message-quote {
  border-left: 4px solid #ff6b6b;
  background: #f8f9fa;
  padding: 8px 12px;
  margin: 8px 0;
  font-style: italic;
  color: #6c757d;
}

.message-text .message-h1,
.message-text .message-h2,
.message-text .message-h3 {
  margin: 16px 0 8px 0;
  color: #333;
}

.message-text .message-h1 {
  font-size: 20px;
  font-weight: 700;
  border-bottom: 2px solid #ff6b6b;
  padding-bottom: 4px;
}

.message-text .message-h2 {
  font-size: 18px;
  font-weight: 600;
}

.message-text .message-h3 {
  font-size: 16px;
  font-weight: 600;
}

.message-text .message-bold {
  font-weight: 600;
  color: #333;
}

.message-text .message-italic {
  font-style: italic;
  color: #6c757d;
}

.message-text .message-strikethrough {
  text-decoration: line-through;
  color: #6c757d;
}

.message-text .message-link {
  color: #ff6b6b;
  text-decoration: none;
  border-bottom: 1px solid transparent;
  transition: all 0.2s;
}

.message-text .message-link:hover {
  border-bottom-color: #ff6b6b;
}

.message-text .math-block {
  background: #f8f9fa;
  border: 1px solid #e9ecef;
  border-radius: 6px;
  padding: 12px;
  margin: 8px 0;
  text-align: center;
  font-family: 'Times New Roman', serif;
}

.message-text .math-inline {
  background: #f1f3f4;
  padding: 2px 4px;
  border-radius: 3px;
  font-family: 'Times New Roman', serif;
}

/* 流式输出动画 */
.message.streaming .message-text {
  position: relative;
}

.message.streaming .message-text::after {
  content: '▋';
  color: #ff6b6b;
  animation: blink 1s infinite;
}

@keyframes blink {
  0%, 50% { opacity: 1; }
  51%, 100% { opacity: 0; }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .ai-chat-container {
    height: 500px;
    flex-direction: column;
  }
  
  .sidebar {
    width: 100%;
    height: 120px;
    border-radius: 12px 12px 0 0;
  }
  
  .sidebar-header {
    padding: 10px 20px;
  }
  
  .assistant-list {
    flex-direction: row;
    padding: 5px 10px;
    overflow-x: auto;
    overflow-y: hidden;
  }
  
  .assistant-item {
    min-width: 120px;
    margin-right: 8px;
    margin-bottom: 0;
    flex-direction: column;
    text-align: center;
    padding: 8px;
  }
  
  .assistant-icon {
    margin-right: 0;
    margin-bottom: 4px;
    width: 30px;
    height: 30px;
    font-size: 16px;
  }
  
  .assistant-name {
    font-size: 12px;
  }
  
  .sidebar-actions {
    display: none;
  }
  
  .chat-main {
    border-radius: 0 0 12px 12px;
  }
  
  .message-content {
    max-width: 85%;
  }
  
  .chat-header {
    padding: 12px 16px;
  }
  
  .chat-messages {
    padding: 16px;
  }
  
  .chat-input {
    padding: 12px 16px;
  }
  
  .api-config > div {
    width: 350px;
  }
  
  .message-text .code-block {
    font-size: 12px;
  }
  
  .message-text .message-table {
    font-size: 12px;
  }
  
  .message-text .copy-btn {
    font-size: 10px;
    padding: 2px 4px;
  }
}
</style>