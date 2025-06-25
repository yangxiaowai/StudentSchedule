const express = require('express');
const cors = require('cors');
const app = express();
const port = 8080;

// 中间件
app.use(cors());
app.use(express.json());

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

// 启动服务器
app.listen(port, () => {
  console.log(`模拟后端服务器运行在 http://localhost:${port}`);
  console.log('可用的测试账号:');
  console.log('- 用户名: testuser, 密码: 123456');
  console.log('- 用户名: admin, 密码: admin123');
});