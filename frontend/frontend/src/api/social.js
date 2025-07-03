import axios from 'axios'

const API_BASE_URL = 'http://localhost:8080/api'

// 创建axios实例
const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000
})

// 请求拦截器 - 添加token
api.interceptors.request.use(
  config => {
    const token = localStorage.getItem('accessToken')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器 - 处理错误
api.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      // Token过期，清除登录状态
      localStorage.removeItem('accessToken')
      localStorage.removeItem('refreshToken')
      localStorage.removeItem('isLogin')
      localStorage.removeItem('userInfo')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

// 学习小组相关API
export const studyGroupAPI = {
  // 创建学习小组
  createGroup: (groupData) => api.post('/study-groups', groupData),
  
  // 获取公开学习小组
  getPublicGroups: (page = 0, size = 10) => 
    api.get(`/study-groups/public?page=${page}&size=${size}`),
  
  // 根据学科搜索学习小组
  searchBySubject: (subject, page = 0, size = 10) => 
    api.get(`/study-groups/search/subject?subject=${subject}&page=${page}&size=${size}`),
  
  // 根据名称搜索学习小组
  searchByName: (name, page = 0, size = 10) => 
    api.get(`/study-groups/search/name?name=${name}&page=${page}&size=${size}`),
  
  // 获取热门学习小组
  getPopularGroups: (page = 0, size = 10) => 
    api.get(`/study-groups/popular?page=${page}&size=${size}`),
  
  // 加入学习小组
  joinGroup: (groupId) => api.post(`/study-groups/${groupId}/join`),
  
  // 通过邀请码加入学习小组
  joinByInviteCode: (inviteCode) => api.post(`/study-groups/join/${inviteCode}`),
  
  // 离开学习小组
  leaveGroup: (groupId, userId) => api.post(`/study-groups/${groupId}/leave?userId=${userId}`),
  
  // 获取用户的学习小组
  getUserGroups: (userId) => api.get(`/study-groups/user/${userId}`),
  
  // 获取学习小组成员
  getGroupMembers: (groupId) => api.get(`/study-groups/${groupId}/members`),
  
  // 获取学习小组详情
  getGroupDetails: (groupId) => api.get(`/study-groups/${groupId}`),
  
  // 解散学习小组
  disbandGroup: (groupId, userId) => api.delete(`/study-groups/${groupId}?userId=${userId}`),
  
  // 更新学习小组
  updateGroup: (groupId, groupData) => api.put(`/study-groups/${groupId}`, groupData),
  
  // 获取小组共享任务
  getGroupSharedTasks: (groupId) => api.get(`/study-groups/${groupId}/shared-tasks`),
  
  // 获取小组共享资料
  getGroupSharedMaterials: (groupId) => api.get(`/study-groups/${groupId}/shared-materials`),
  
  // 根据学科获取小组共享资料
  getGroupSharedMaterialsBySubject: (groupId, subject) => api.get(`/study-groups/${groupId}/shared-materials/subject?subject=${subject}`)
}

// 学习分享相关API
export const studyShareAPI = {
  // 创建学习分享
  createShare: (shareData) => api.post('/study-shares', shareData),
  
  // 获取公开学习分享
  getPublicShares: (page = 0, size = 10) => 
    api.get(`/study-shares/public?page=${page}&size=${size}`),
  
  // 根据类型获取学习分享
  getSharesByType: (type, page = 0, size = 10) => 
    api.get(`/study-shares/type/${type}?page=${page}&size=${size}`),
  
  // 根据学科获取学习分享
  getSharesBySubject: (subject, page = 0, size = 10) => 
    api.get(`/study-shares/subject/${subject}?page=${page}&size=${size}`),
  
  // 根据学习小组获取学习分享
  getSharesByGroup: (groupId, page = 0, size = 10) => 
    api.get(`/study-shares/group/${groupId}?page=${page}&size=${size}`),
  
  // 根据用户获取学习分享
  getSharesByUser: (userId, page = 0, size = 10) => 
    api.get(`/study-shares/user/${userId}?page=${page}&size=${size}`),
  
  // 根据关键词搜索学习分享
  searchShares: (keyword, page = 0, size = 10) => 
    api.get(`/study-shares/search?keyword=${keyword}&page=${page}&size=${size}`),
  
  // 获取热门学习分享
  getPopularShares: (page = 0, size = 10) => 
    api.get(`/study-shares/popular?page=${page}&size=${size}`),
  
  // 根据标签获取学习分享
  getSharesByTags: (tags, page = 0, size = 10) => 
    api.get(`/study-shares/tags?tags=${tags}&page=${page}&size=${size}`),
  
  // 获取推荐学习分享
  getRecommendedShares: (userId, page = 0, size = 10) => 
    api.get(`/study-shares/recommendations/${userId}?page=${page}&size=${size}`),
  
  // 点赞学习分享
  likeShare: (shareId, userId) => api.post(`/study-shares/${shareId}/like?userId=${userId}`),
  
  // 取消点赞学习分享
  unlikeShare: (shareId, userId) => api.post(`/study-shares/${shareId}/unlike?userId=${userId}`),
  
  // 增加浏览次数
  incrementViews: (shareId) => api.post(`/study-shares/${shareId}/view`),
  
  // 更新学习分享
  updateShare: (shareId, shareData) => api.put(`/study-shares/${shareId}`, shareData),
  
  // 删除学习分享
  deleteShare: (shareId) => api.delete(`/study-shares/${shareId}`)
}

// Q&A相关API
export const qaAPI = {
  // 创建问题
  createQuestion: (questionData) => api.post('/qa/questions', questionData),
  
  // 通用获取问题方法
  getQuestions: (page = 0, size = 10, viewMode = 'latest', userId = null) => {
    const userIdParam = userId ? `&userId=${userId}` : '';
    switch (viewMode) {
      case 'popular':
        return api.get(`/qa/questions/popular?page=${page}&size=${size}${userIdParam}`);
      case 'unresolved':
        return api.get(`/qa/questions/unresolved?page=${page}&size=${size}${userIdParam}`);
      case 'my':
        return userId ? api.get(`/qa/questions/user/${userId}?page=${page}&size=${size}`) : api.get(`/qa/questions?page=${page}&size=${size}${userIdParam}`);
      case 'recommended':
        return userId ? api.get(`/qa/questions/recommendations/${userId}?page=${page}&size=${size}`) : api.get(`/qa/questions?page=${page}&size=${size}${userIdParam}`);
      case 'latest':
      default:
        return api.get(`/qa/questions?page=${page}&size=${size}${userIdParam}`);
    }
  },
  
  // 获取用户的问题
  getUserQuestions: (userId, page = 0, size = 10) => 
    api.get(`/qa/questions/user/${userId}?page=${page}&size=${size}`),
  
  // 根据状态获取问题
  getQuestionsByStatus: (status, page = 0, size = 10) => 
    api.get(`/qa/questions/status/${status}?page=${page}&size=${size}`),
  
  // 根据学科获取问题
  getQuestionsBySubject: (subject, page = 0, size = 10) => 
    api.get(`/qa/questions/subject/${subject}?page=${page}&size=${size}`),
  
  // 根据难度获取问题
  getQuestionsByDifficulty: (difficulty, page = 0, size = 10) => 
    api.get(`/qa/questions/difficulty/${difficulty}?page=${page}&size=${size}`),
  
  // 根据学习小组获取问题
  getQuestionsByGroup: (groupId, page = 0, size = 10) => 
    api.get(`/qa/questions/group/${groupId}?page=${page}&size=${size}`),
  
  // 根据关键词搜索问题
  searchQuestions: (keyword, page = 0, size = 10) => 
    api.get(`/qa/questions/search?keyword=${keyword}&page=${page}&size=${size}`),
  
  // 获取热门问题
  getPopularQuestions: (page = 0, size = 10) => 
    api.get(`/qa/questions/popular?page=${page}&size=${size}`),
  
  // 获取未解决的问题
  getUnresolvedQuestions: (page = 0, size = 10) => 
    api.get(`/qa/questions/unresolved?page=${page}&size=${size}`),
  
  // 根据标签获取问题
  getQuestionsByTags: (tags, page = 0, size = 10) => 
    api.get(`/qa/questions/tags?tags=${tags}&page=${page}&size=${size}`),
  
  // 按标签搜索问题（别名方法）
  searchByTag: (tag, page = 0, size = 10) => 
    api.get(`/qa/questions/tags?tags=${tag}&page=${page}&size=${size}`),
  
  // 获取问题的回答（别名方法）
  getAnswers: (questionId, sortBy = 'createdAt', page = 0, size = 10) => 
    api.get(`/qa/answers/question/${questionId}?sortBy=${sortBy}&page=${page}&size=${size}`),
  
  // 筛选问题的通用方法
  getQuestionsByFilter: (status, subject, difficulty, page = 0, size = 10) => {
    if (status) {
      return api.get(`/qa/questions/status/${status}?page=${page}&size=${size}`);
    } else if (subject) {
      return api.get(`/qa/questions/subject/${subject}?page=${page}&size=${size}`);
    } else if (difficulty) {
      return api.get(`/qa/questions/difficulty/${difficulty}?page=${page}&size=${size}`);
    } else {
      return api.get(`/qa/questions?page=${page}&size=${size}`);
    }
  },
  
  // 切换问题点赞状态
  toggleQuestionLike: (questionId, isCurrentlyLiked, userId) => {
    if (isCurrentlyLiked) {
      return api.delete(`/qa/questions/${questionId}/like?userId=${userId}`);
    } else {
      return api.post(`/qa/questions/${questionId}/like?userId=${userId}`);
    }
  },
  
  // 获取推荐问题
  getRecommendedQuestions: (userId, page = 0, size = 10) => 
    api.get(`/qa/questions/recommendations/${userId}?page=${page}&size=${size}`),
  
  // 点赞问题
  likeQuestion: (questionId, userId) => api.post(`/qa/questions/${questionId}/like?userId=${userId}`),
  
  // 取消点赞问题
  unlikeQuestion: (questionId, userId) => api.delete(`/qa/questions/${questionId}/like?userId=${userId}`),
  
  // 增加问题浏览次数
  incrementQuestionViews: (questionId) => api.post(`/qa/questions/${questionId}/view`),
  
  // 查看问题详情（增加浏览量）
  viewQuestion: (questionId) => qaAPI.incrementQuestionViews(questionId),
  
  // 创建回答
  createAnswer: (answerData) => api.post('/qa/answers', answerData),
  
  // 获取问题的回答
  getQuestionAnswers: (questionId, sortBy = 'createdAt', page = 0, size = 10, userId = null) => {
    const userIdParam = userId ? `&userId=${userId}` : '';
    return api.get(`/qa/questions/${questionId}/answers?page=${page}&size=${size}${userIdParam}`);
  },
  
  // 获取用户的回答
  getUserAnswers: (userId, page = 0, size = 10) => 
    api.get(`/qa/answers/user/${userId}?page=${page}&size=${size}`),
  
  // 获取热门回答
  getPopularAnswers: (page = 0, size = 10) => 
    api.get(`/qa/answers/popular?page=${page}&size=${size}`),
  
  // 获取最佳回答
  getBestAnswers: (page = 0, size = 10) => 
    api.get(`/qa/answers/best?page=${page}&size=${size}`),
  
  // 点赞回答
  likeAnswer: (answerId, userId) => api.post(`/qa/answers/${answerId}/like?userId=${userId}`),
  
  // 取消点赞回答
  unlikeAnswer: (answerId, userId) => api.delete(`/qa/answers/${answerId}/like?userId=${userId}`),
  
  // 切换回答点赞状态
  toggleAnswerLike: (answerId, isCurrentlyLiked, userId) => {
    if (isCurrentlyLiked) {
      return api.delete(`/qa/answers/${answerId}/like?userId=${userId}`);
    } else {
      return api.post(`/qa/answers/${answerId}/like?userId=${userId}`);
    }
  },
  
  // 设置最佳回答
  setBestAnswer: (answerId) => api.post(`/qa/answers/${answerId}/best`)
}

// 排行榜相关API
export const leaderboardAPI = {
  // 获取用户学习统计
  getUserStats: (userId) => api.get(`/leaderboard/stats/${userId}`),
  
  // 获取学习时长排行榜
  getStudyHoursLeaderboard: (timeRange = 'total', page = 0, size = 10) => 
    api.get(`/leaderboard/study-hours/${timeRange}?page=${page}&size=${size}`),
  
  // 获取积分排行榜
  getPointsLeaderboard: (page = 0, size = 10) => 
    api.get(`/leaderboard/points?page=${page}&size=${size}`),
  
  // 获取经验值排行榜
  getExperienceLeaderboard: (page = 0, size = 10) => 
    api.get(`/leaderboard/experience?page=${page}&size=${size}`),
  
  // 获取连续学习天数排行榜
  getStreakLeaderboard: (page = 0, size = 10) => 
    api.get(`/leaderboard/streak?page=${page}&size=${size}`),
  
  // 获取任务完成数排行榜
  getTasksLeaderboard: (page = 0, size = 10) => 
    api.get(`/leaderboard/tasks?page=${page}&size=${size}`),
  
  // 获取最佳回答数排行榜
  getBestAnswersLeaderboard: (page = 0, size = 10) => 
    api.get(`/leaderboard/best-answers?page=${page}&size=${size}`),
  
  // 获取分享数排行榜
  getSharesLeaderboard: (page = 0, size = 10) => 
    api.get(`/leaderboard/shares?page=${page}&size=${size}`),
  
  // 获取获赞数排行榜
  getLikesLeaderboard: (page = 0, size = 10) => 
    api.get(`/leaderboard/likes?page=${page}&size=${size}`),
  
  // 获取用户积分排名
  getUserPointsRank: (userId) => api.get(`/leaderboard/rank/points/${userId}`),
  
  // 获取用户学习时长排名
  getUserStudyHoursRank: (userId) => api.get(`/leaderboard/rank/study-hours/${userId}`),
  
  // 更新学习时长
  updateStudyHours: (userId, hours) => 
    api.post(`/leaderboard/update/study-hours/${userId}?hours=${hours}`),
  
  // 更新任务完成数
  updateTasksCompleted: (userId) => 
    api.post(`/leaderboard/update/tasks/${userId}`),
  
  // 更新积分
  updatePoints: (userId, points) => 
    api.post(`/leaderboard/update/points/${userId}?points=${points}`),
  
  // 更新经验值
  updateExperience: (userId, experience) => 
    api.post(`/leaderboard/update/experience/${userId}?experience=${experience}`),
  
  // 重置周统计
  resetWeeklyStats: () => api.post('/leaderboard/reset/weekly'),
  
  // 重置月统计
  resetMonthlyStats: () => api.post('/leaderboard/reset/monthly')
}

// 用户API
const userAPI = {
  // 根据用户ID获取用户信息
  getUserInfo(userId) {
    return api.get(`/user/info/${userId}`);
  }
};

export { userAPI };

export default {
  studyGroupAPI,
  studyShareAPI,
  qaAPI,
  leaderboardAPI,
  userAPI
}