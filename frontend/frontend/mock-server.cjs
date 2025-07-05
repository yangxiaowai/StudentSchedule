const express = require('express');
const cors = require('cors');
const app = express();
const port = 8080;

// 中间件
app.use(cors({
  origin: '*',
  methods: ['GET', 'POST', 'PUT', 'DELETE', 'OPTIONS'],
  allowedHeaders: ['Content-Type', 'Authorization']
}));
app.use(express.json());

// 添加请求日志中间件
app.use((req, res, next) => {
  console.log(`${new Date().toISOString()} - ${req.method} ${req.url}`);
  next();
});

// 模拟用户数据
const users = [
  { username: 'testuser', password: '123456', id: 1, name: '测试用户' },
  { username: 'admin', password: 'admin123', id: 2, name: '管理员' }
];

// 登录接口
app.post('/api/user/login', (req, res) => {
  const { username, password } = req.body;
  
  console.log('登录请求:', { username, password });
  
  const user = users.find(u => u.username === username && u.password === password);
  
  if (user) {
    const response = {
      success: true,
      message: '登录成功',
      data: {
        accessToken: 'mock-access-token-' + Date.now(),
        refreshToken: 'mock-refresh-token-' + Date.now(),
        userInfo: {
          id: user.id,
          username: user.username,
          name: user.name
        }
      }
    };
    console.log('登录成功响应:', response);
    res.json(response);
  } else {
    const response = {
      success: false,
      message: '用户名或密码错误'
    };
    console.log('登录失败响应:', response);
    res.json(response);
  }
});

// 用户信息接口
app.get('/api/user/info', (req, res) => {
  const token = req.headers.authorization;
  if (token && token.startsWith('Bearer mock-access-token')) {
    res.json({
      success: true,
      data: {
        id: 1,
        username: 'testuser',
        name: '测试用户'
      }
    });
  } else {
    res.status(401).json({
      success: false,
      message: '未授权'
    });
  }
});

// 任务分析接口
app.post('/api/analysis/tasks', (req, res) => {
  const taskIds = req.body;
  console.log('分析任务请求:', taskIds);
  
  // 模拟分析数据
  const response = {
    subjectNames: ['语文', '数学', '英语', '物理', '化学'],
    subjectData: [85, 78, 92, 70, 65],
    dayNames: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
    dayData: [80, 75, 90, 85, 70, 60, 95],
    planSubjects: [80, 80, 85, 75, 70],
    planDays: [85, 80, 85, 80, 75, 70, 90],
    suggestion: '建议加强数学和化学的学习，保持英语的良好状态。周六的学习效率较低，可以适当调整学习计划。'
  };
  
  console.log('分析任务响应:', response);
  
  // 设置CORS头
  res.header('Access-Control-Allow-Origin', '*');
  res.header('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS');
  res.header('Access-Control-Allow-Headers', 'Content-Type, Authorization');
  
  // 延迟500ms响应，模拟网络延迟
  setTimeout(() => {
    res.json(response);
  }, 500);
});

// 智能建议接口
app.post('/api/analysis/smart-suggestions', (req, res) => {
  console.log('智能建议请求:', req.body);
  
  // 模拟智能建议数据
  const response = {
    suggestions: [
      { icon: '📚', title: '学习方法优化', content: '建议采用番茄工作法提高数学学习效率', priority: 'high' },
      { icon: '⏰', title: '时间管理', content: '周六的学习时间过于分散，建议集中在上午和晚上', priority: 'medium' },
      { icon: '🧠', title: '记忆技巧', content: '英语单词记忆可以尝试联想记忆法', priority: 'low' },
      { icon: '📝', title: '笔记方法', content: '物理学科建议采用康奈尔笔记法整理知识点', priority: 'medium' }
    ],
    efficiency: {
      bestTimeSlot: '晚上',
      worstTimeSlot: '下午',
      bestSubject: '英语',
      improvementAreas: ['数学', '化学']
    },
    prediction: {
      weeklyPrediction: [82, 78, 85, 75, 88, 70, 90],
      monthlyTrend: 'upward',
      potentialIssues: ['周六学习效率低', '数学学科需要加强']
    },
    patterns: {
      timeEfficiency: [65, 85, 75, 90, 45],
      consistencyScore: 78,
      regularityScore: 82
    },
    motivation: {
      message: '你的英语学习表现出色！坚持当前的学习方法，并将这种积极性延伸到其他学科。'
    }
  };
  
  console.log('智能建议响应:', response);
  res.json(response);
});

// 个性化学习计划接口
app.post('/api/analysis/personalized-plan', (req, res) => {
  console.log('个性化学习计划请求:', req.body);
  
  // 模拟个性化学习计划数据
  const response = {
    plan: [
      { time: '周一 08:00-10:00', subject: '数学', task: '重点复习函数与导数', duration: '2小时' },
      { time: '周一 14:00-15:30', subject: '英语', task: '阅读理解训练', duration: '1.5小时' },
      { time: '周二 19:00-21:00', subject: '物理', task: '力学问题专项练习', duration: '2小时' },
      { time: '周三 16:00-17:30', subject: '化学', task: '有机化学知识点整理', duration: '1.5小时' },
      { time: '周四 20:00-21:30', subject: '语文', task: '古文阅读与翻译', duration: '1.5小时' },
      { time: '周五 15:00-17:00', subject: '数学', task: '概率统计专题训练', duration: '2小时' },
      { time: '周日 09:00-11:30', subject: '综合复习', task: '本周知识点回顾与测试', duration: '2.5小时' }
    ]
  };
  
  console.log('个性化学习计划响应:', response);
  res.json(response);
});

// 启动服务器
const server = app.listen(port, () => {
  console.log(`模拟后端服务器运行在 http://localhost:${port}`);
  console.log('可用的测试账号:');
  console.log('- 用户名: testuser, 密码: 123456');
  console.log('- 用户名: admin, 密码: admin123');
});

// 保持进程运行
process.on('SIGINT', () => {
  console.log('关闭服务器...');
  server.close(() => {
    console.log('服务器已关闭');
    process.exit(0);
  });
});

console.log('服务器正在运行中...');