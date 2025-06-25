# 前端项目结构说明

本前端项目建议基于 Vue3 + Vite 构建，目录结构如下：

```
frontend/
├── api/                # 封装所有后端接口请求
│   └── user.js
│   └── task.js
│   └── analysis.js
├── assets/             # 静态资源（图片、样式等）
├── components/         # 通用组件
│   └── TaskCard.vue
│   └── TaskForm.vue
│   └── AnalysisChart.vue
├── pages/              # 页面组件
│   └── Login.vue
│   └── Register.vue
│   └── Activate.vue
│   └── Home.vue
│   └── TaskList.vue
│   └── TaskDetail.vue
│   └── TaskAnalysis.vue
│   └── Profile.vue
├── router/             # 路由配置
│   └── index.js
├── store/              # 状态管理（如pinia/vuex）
│   └── user.js
│   └── task.js
├── App.vue
└── main.js
```

## 目录说明
- `api/`：所有与后端交互的接口请求封装。
- `assets/`：图片、全局样式等静态资源。
- `components/`：可复用的UI组件。
- `pages/`：每个页面对应一个Vue组件。
- `router/`：前端路由配置。
- `store/`：全局状态管理。
- `App.vue`、`main.js`：项目入口文件。

> 建议在项目根目录下执行 `npm create vite@latest frontend -- --template vue` 初始化前端项目，然后按上述结构补充文件。 