<template>
  <div class="leaderboard">
    <div class="header">
      <h2>学习排行榜</h2>
      <el-button @click="refreshStats">
        <el-icon><Refresh /></el-icon>
        刷新数据
      </el-button>
    </div>

    <!-- 排行榜类型选择 -->
    <div class="leaderboard-tabs">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="学习时长" name="studyHours">
          <div class="tab-content">
            <el-radio-group v-model="timeRange" @change="loadLeaderboard">
              <el-radio-button value="total">总排行</el-radio-button>
              <el-radio-button value="weekly">周排行</el-radio-button>
              <el-radio-button value="monthly">月排行</el-radio-button>
            </el-radio-group>
          </div>
        </el-tab-pane>
        <el-tab-pane label="积分排行" name="points" />
        <el-tab-pane label="经验值" name="experience" />
        <el-tab-pane label="连续学习" name="streak" />
        <el-tab-pane label="任务完成" name="tasks" />
        <el-tab-pane label="最佳答案" name="bestAnswers" />
        <el-tab-pane label="分享数量" name="shares" />
        <el-tab-pane label="获赞数量" name="likes" />
      </el-tabs>
    </div>

    <!-- 我的排名卡片 -->
    <div class="my-rank-card">
      <el-card shadow="hover">
        <div class="my-rank-content">
          <div class="rank-info">
            <div class="rank-number">
              <span class="rank-label">我的排名</span>
              <span class="rank-value">#{{ myRank || '--' }}</span>
            </div>
            <div class="rank-stats">
              <div class="stat-item">
                <span class="stat-label">{{ getStatLabel() }}</span>
                <span class="stat-value">{{ getMyStatValue() }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">等级</span>
                <span class="stat-value">{{ myStats?.level || '--' }}</span>
              </div>
            </div>
          </div>
          <div class="level-progress">
            <div class="level-info">
              <span>等级进度</span>
              <span>{{ myStats?.experiencePoints || 0 }} / {{ getNextLevelExp() }} EXP</span>
            </div>
            <el-progress 
              :percentage="getLevelProgress()" 
              :stroke-width="8"
              :show-text="false"
            />
          </div>
        </div>
      </el-card>
    </div>

    <!-- 排行榜列表 -->
    <div class="leaderboard-list">
      <el-card shadow="hover">
        <template #header>
          <div class="card-header">
            <span>{{ getLeaderboardTitle() }}</span>
            <el-tag type="info">共 {{ leaderboardData.length }} 人</el-tag>
          </div>
        </template>
        
        <div class="ranking-list">
          <div 
            v-for="(user, index) in leaderboardData" 
            :key="user.userId" 
            class="ranking-item"
            :class="{ 'my-ranking': user.userId === currentUserId }"
          >
            <div class="ranking-left">
              <div class="ranking-number">
                <span 
                  class="rank-badge"
                  :class="getRankBadgeClass(index + 1)"
                >
                  {{ index + 1 }}
                </span>
              </div>
              <div class="user-info">
                <div class="user-name">{{ user.userName || `用户${user.userId}` }}</div>
                <div class="user-level">{{ user.level || 'BEGINNER' }} 级</div>
              </div>
            </div>
            
            <div class="ranking-right">
              <div class="main-stat">
                <span class="stat-value">{{ formatStatValue(user) }}</span>
                <span class="stat-unit">{{ getStatUnit() }}</span>
              </div>
              <div class="additional-stats">
                <span v-if="activeTab === 'studyHours'">经验: {{ user.experiencePoints || 0 }}</span>
                <span v-else-if="activeTab === 'points'">学习时长: {{ user.totalStudyHours || 0 }}h</span>
                <span v-else-if="activeTab === 'experience'">积分: {{ user.totalPoints || 0 }}</span>
                <span v-else-if="activeTab === 'streak'">总学习: {{ user.totalStudyHours || 0 }}h</span>
                <span v-else-if="activeTab === 'tasks'">积分: {{ user.totalPoints || 0 }}</span>
                <span v-else-if="activeTab === 'bestAnswers'">回答数: {{ user.questionsAnswered || 0 }}</span>
                <span v-else-if="activeTab === 'shares'">获赞: {{ user.likesReceived || 0 }}</span>
                <span v-else-if="activeTab === 'likes'">分享数: {{ user.sharesCount || 0 }}</span>
              </div>
            </div>
            
            <!-- 前三名特殊标识 -->
            <div v-if="index < 3" class="medal">
              <el-icon v-if="index === 0" class="gold-medal"><Trophy /></el-icon>
              <el-icon v-else-if="index === 1" class="silver-medal"><Trophy /></el-icon>
              <el-icon v-else-if="index === 2" class="bronze-medal"><Trophy /></el-icon>
            </div>
          </div>
        </div>
        
        <!-- 空状态 -->
        <div v-if="leaderboardData.length === 0" class="empty-state">
          <el-empty description="暂无排行数据" />
        </div>
      </el-card>
    </div>

    <!-- 学习统计卡片 -->
    <div class="stats-cards">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-card-content">
              <div class="stat-icon study-hours">
                <el-icon><Clock /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ myStats?.totalStudyHours || 0 }}</div>
                <div class="stat-label">总学习时长(小时)</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-card-content">
              <div class="stat-icon points">
                <el-icon><Star /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ myStats?.totalPoints || 0 }}</div>
                <div class="stat-label">总积分</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-card-content">
              <div class="stat-icon streak">
                <el-icon><Lightning /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ myStats?.currentStreak || 0 }}</div>
                <div class="stat-label">连续学习(天)</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-card-content">
              <div class="stat-icon tasks">
                <el-icon><Check /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ myStats?.totalTasksCompleted || 0 }}</div>
                <div class="stat-label">完成任务</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 成就展示 -->
    <div class="achievements-section">
      <el-card shadow="hover">
        <template #header>
          <div class="card-header">
            <span>我的成就</span>
            <el-tag type="success">{{ achievements.length }} 个成就</el-tag>
          </div>
        </template>
        
        <div class="achievements-grid">
          <div 
            v-for="achievement in achievements" 
            :key="achievement.id" 
            class="achievement-item"
            :class="{ 'unlocked': achievement.unlocked }"
          >
            <div class="achievement-icon">
              <el-icon><Medal /></el-icon>
            </div>
            <div class="achievement-info">
              <div class="achievement-name">{{ achievement.name }}</div>
              <div class="achievement-desc">{{ achievement.description }}</div>
              <div v-if="achievement.unlocked" class="achievement-date">
                获得时间: {{ formatTime(achievement.unlockedAt) }}
              </div>
            </div>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  Refresh, Trophy, Clock, Star, Lightning, Check, Medal 
} from '@element-plus/icons-vue'
import socialAPI, { userAPI } from '../api/social.js'
const { leaderboardAPI } = socialAPI

export default {
  name: 'Leaderboard',
  components: {
    Refresh,
    Trophy,
    Clock,
    Star,
    Lightning,
    Check,
    Medal
  },
  setup() {
    const activeTab = ref('studyHours')
    const timeRange = ref('total')
    const leaderboardData = ref([])
    const myStats = ref(null)
    const myRank = ref(null)
    const currentUserId = ref(parseInt(localStorage.getItem('userId')) || 51) // 从localStorage获取用户ID
    const userInfoCache = ref(new Map()) // 用户信息缓存
    const achievements = ref([])

    // 加载排行榜数据
    const loadLeaderboard = async () => {
      try {
        let url = '/leaderboard'
        
        switch (activeTab.value) {
          case 'studyHours':
            if (timeRange.value === 'weekly') {
              url += '/weekly-study-hours'
            } else if (timeRange.value === 'monthly') {
              url += '/monthly-study-hours'
            } else {
              url += '/study-hours'
            }
            break
          case 'points':
            url += '/points'
            break
          case 'experience':
            url += '/experience'
            break
          case 'streak':
            url += '/streak'
            break
          case 'tasks':
            url += '/tasks-completed'
            break
          case 'bestAnswers':
            url += '/best-answers'
            break
          case 'shares':
            url += '/shares'
            break
          case 'likes':
            url += '/likes-received'
            break
        }
        
        try {
          const response = await leaderboardAPI.getLeaderboard(activeTab.value, timeRange.value)
          if (response.data.success) {
            leaderboardData.value = response.data.data
          }
        } catch (apiError) {
          // 如果API调用失败，使用模拟数据填满界面
          leaderboardData.value = generateMockData()
        }
        
        // 为每个用户获取用户名
        for (const user of leaderboardData.value) {
          user.userName = await getUserName(user.userId)
        }
      } catch (error) {
        // 使用模拟数据填满界面
        leaderboardData.value = generateMockData()
      }
    }

    // 生成模拟数据
    const generateMockData = () => {
      const mockUsers = []
      const levels = ['BEGINNER', 'INTERMEDIATE', 'ADVANCED', 'EXPERT', 'MASTER']
      
      for (let i = 1; i <= 50; i++) {
        const baseValue = Math.max(1000 - i * 15, 10)
        mockUsers.push({
          userId: i,
          level: levels[Math.floor(Math.random() * levels.length)],
          totalStudyHours: Math.floor(baseValue / 10) + Math.floor(Math.random() * 20),
          weeklyStudyHours: Math.floor(Math.random() * 40) + 5,
          monthlyStudyHours: Math.floor(Math.random() * 120) + 20,
          totalPoints: baseValue + Math.floor(Math.random() * 200),
          experiencePoints: baseValue * 2 + Math.floor(Math.random() * 500),
          currentStreak: Math.floor(Math.random() * 30) + 1,
          totalTasksCompleted: Math.floor(Math.random() * 100) + 10,
          bestAnswersCount: Math.floor(Math.random() * 20),
          sharesCount: Math.floor(Math.random() * 15),
          likesReceived: Math.floor(Math.random() * 50),
          questionsAnswered: Math.floor(Math.random() * 30)
        })
      }
      
      return mockUsers
    }

    // 获取用户名
    const getUserName = async (userId) => {
      if (userInfoCache.value.has(userId)) {
        return userInfoCache.value.get(userId).username
      }
      
      try {
        const response = await userAPI.getUserInfo(userId)
        if (response.data.success) {
          const userInfo = response.data.data
          userInfoCache.value.set(userId, userInfo)
          return userInfo.username
        }
      } catch (error) {
        console.error('获取用户信息失败:', error)
      }
      return `用户${userId}`
    }

    // 加载我的统计数据
    const loadMyStats = async () => {
      try {
        const response = await leaderboardAPI.getUserStats(currentUserId.value)
        if (response.data.success) {
          myStats.value = response.data.data
        }
      } catch (error) {
        console.error('加载个人统计失败', error)
      }
    }

    // 加载我的排名
    const loadMyRank = async () => {
      try {
        let response
        switch (activeTab.value) {
          case 'studyHours':
            response = await leaderboardAPI.getUserStudyHoursRank(currentUserId.value)
            break
          case 'points':
            response = await leaderboardAPI.getUserPointsRank(currentUserId.value)
            break
          default:
            response = await leaderboardAPI.getUserPointsRank(currentUserId.value)
        }
        
        if (response.data.success) {
          myRank.value = response.data.rank
        }
      } catch (error) {
        console.error('加载个人排名失败', error)
      }
    }

    // 加载成就数据
    const loadAchievements = () => {
      // 模拟成就数据
      achievements.value = [
        {
          id: 1,
          name: '初学者',
          description: '完成第一次学习',
          unlocked: true,
          unlockedAt: '2024-01-01T10:00:00'
        },
        {
          id: 2,
          name: '勤奋学习者',
          description: '连续学习7天',
          unlocked: myStats.value?.currentStreak >= 7,
          unlockedAt: myStats.value?.currentStreak >= 7 ? '2024-01-07T10:00:00' : null
        },
        {
          id: 3,
          name: '学习达人',
          description: '累计学习100小时',
          unlocked: (myStats.value?.totalStudyHours || 0) >= 100,
          unlockedAt: (myStats.value?.totalStudyHours || 0) >= 100 ? '2024-01-15T10:00:00' : null
        },
        {
          id: 4,
          name: '积分大师',
          description: '获得1000积分',
          unlocked: (myStats.value?.totalPoints || 0) >= 1000,
          unlockedAt: (myStats.value?.totalPoints || 0) >= 1000 ? '2024-01-20T10:00:00' : null
        },
        {
          id: 5,
          name: '分享达人',
          description: '发布10个学习分享',
          unlocked: (myStats.value?.sharesCount || 0) >= 10,
          unlockedAt: (myStats.value?.sharesCount || 0) >= 10 ? '2024-01-25T10:00:00' : null
        },
        {
          id: 6,
          name: '答疑专家',
          description: '获得5个最佳答案',
          unlocked: (myStats.value?.bestAnswersCount || 0) >= 5,
          unlockedAt: (myStats.value?.bestAnswersCount || 0) >= 5 ? '2024-01-30T10:00:00' : null
        }
      ]
    }

    // 刷新统计数据
    const refreshStats = async () => {
      await Promise.all([
        loadLeaderboard(),
        loadMyStats(),
        loadMyRank()
      ])
      loadAchievements()
      ElMessage.success('数据已刷新')
    }

    // 标签页切换
    const handleTabChange = (tabName) => {
      activeTab.value = tabName
      timeRange.value = 'total' // 重置时间范围
      loadLeaderboard()
      loadMyRank()
    }

    // 获取统计标签
    const getStatLabel = () => {
      const labels = {
        studyHours: timeRange.value === 'weekly' ? '本周学习' : 
                   timeRange.value === 'monthly' ? '本月学习' : '总学习时长',
        points: '总积分',
        experience: '经验值',
        streak: '连续学习',
        tasks: '完成任务',
        bestAnswers: '最佳答案',
        shares: '分享数量',
        likes: '获赞数量'
      }
      return labels[activeTab.value] || '统计值'
    }

    // 获取我的统计值
    const getMyStatValue = () => {
      if (!myStats.value) return '--'
      
      switch (activeTab.value) {
        case 'studyHours':
          if (timeRange.value === 'weekly') return myStats.value.weeklyStudyHours || 0
          if (timeRange.value === 'monthly') return myStats.value.monthlyStudyHours || 0
          return myStats.value.totalStudyHours || 0
        case 'points':
          return myStats.value.totalPoints || 0
        case 'experience':
          return myStats.value.experiencePoints || 0
        case 'streak':
          return myStats.value.currentStreak || 0
        case 'tasks':
          return myStats.value.totalTasksCompleted || 0
        case 'bestAnswers':
          return myStats.value.bestAnswersCount || 0
        case 'shares':
          return myStats.value.sharesCount || 0
        case 'likes':
          return myStats.value.likesReceived || 0
        default:
          return '--'
      }
    }

    // 获取排行榜标题
    const getLeaderboardTitle = () => {
      const titles = {
        studyHours: timeRange.value === 'weekly' ? '本周学习时长排行' : 
                   timeRange.value === 'monthly' ? '本月学习时长排行' : '总学习时长排行',
        points: '积分排行榜',
        experience: '经验值排行榜',
        streak: '连续学习排行榜',
        tasks: '任务完成排行榜',
        bestAnswers: '最佳答案排行榜',
        shares: '分享数量排行榜',
        likes: '获赞数量排行榜'
      }
      return titles[activeTab.value] || '排行榜'
    }

    // 格式化统计值
    const formatStatValue = (user) => {
      switch (activeTab.value) {
        case 'studyHours':
          if (timeRange.value === 'weekly') return user.weeklyStudyHours || 0
          if (timeRange.value === 'monthly') return user.monthlyStudyHours || 0
          return user.totalStudyHours || 0
        case 'points':
          return user.totalPoints || 0
        case 'experience':
          return user.experiencePoints || 0
        case 'streak':
          return user.currentStreak || 0
        case 'tasks':
          return user.totalTasksCompleted || 0
        case 'bestAnswers':
          return user.bestAnswersCount || 0
        case 'shares':
          return user.sharesCount || 0
        case 'likes':
          return user.likesReceived || 0
        default:
          return 0
      }
    }

    // 获取统计单位
    const getStatUnit = () => {
      const units = {
        studyHours: '小时',
        points: '分',
        experience: 'EXP',
        streak: '天',
        tasks: '个',
        bestAnswers: '个',
        shares: '个',
        likes: '个'
      }
      return units[activeTab.value] || ''
    }

    // 获取排名徽章样式
    const getRankBadgeClass = (rank) => {
      if (rank === 1) return 'rank-first'
      if (rank === 2) return 'rank-second'
      if (rank === 3) return 'rank-third'
      return 'rank-normal'
    }

    // 获取下一级经验值
    const getNextLevelExp = () => {
      const currentLevel = myStats.value?.level || 'BEGINNER'
      const levelExpMap = {
        BEGINNER: 100,
        INTERMEDIATE: 500,
        ADVANCED: 1000,
        EXPERT: 2000,
        MASTER: 5000
      }
      return levelExpMap[currentLevel] || 100
    }

    // 获取等级进度
    const getLevelProgress = () => {
      const currentExp = myStats.value?.experiencePoints || 0
      const nextLevelExp = getNextLevelExp()
      return Math.min((currentExp / nextLevelExp) * 100, 100)
    }

    // 格式化时间
    const formatTime = (time) => {
      return new Date(time).toLocaleDateString()
    }

    onMounted(() => {
      refreshStats()
    })

    return {
      activeTab,
      timeRange,
      leaderboardData,
      myStats,
      myRank,
      currentUserId,
      achievements,
      loadLeaderboard,
      generateMockData,
      refreshStats,
      handleTabChange,
      getStatLabel,
      getMyStatValue,
      getLeaderboardTitle,
      formatStatValue,
      getStatUnit,
      getRankBadgeClass,
      getNextLevelExp,
      getLevelProgress,
      formatTime
    }
  }
}
</script>

<style scoped>
.leaderboard {
  padding: 20px;
  width: calc(90vw - 96px);
  margin: 0 14px;
  box-sizing: border-box;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.leaderboard-tabs {
  margin-bottom: 20px;
}

.tab-content {
  padding: 10px 0;
}

.my-rank-card {
  margin-bottom: 20px;
}

.my-rank-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.rank-info {
  display: flex;
  align-items: center;
  gap: 30px;
}

.rank-number {
  text-align: center;
}

.rank-label {
  display: block;
  font-size: 14px;
  color: #666;
  margin-bottom: 5px;
}

.rank-value {
  display: block;
  font-size: 32px;
  font-weight: bold;
  color: #409eff;
}

.rank-stats {
  display: flex;
  gap: 20px;
}

.stat-item {
  text-align: center;
}

.stat-label {
  display: block;
  font-size: 14px;
  color: #666;
  margin-bottom: 5px;
}

.stat-value {
  display: block;
  font-size: 20px;
  font-weight: bold;
  color: #333;
}

.level-progress {
  width: 200px;
}

.level-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 14px;
  color: #666;
}

.leaderboard-list {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.ranking-list {
  space-y: 10px;
  max-height: 500px;
  overflow-y: auto;
}

.ranking-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  border: 1px solid #eee;
  border-radius: 8px;
  margin-bottom: 10px;
  position: relative;
  transition: all 0.3s ease;
}

.ranking-item:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.ranking-item.my-ranking {
  background: linear-gradient(135deg, #409eff20, #409eff10);
  border-color: #409eff;
}

.ranking-left {
  display: flex;
  align-items: center;
  gap: 15px;
}

.ranking-number {
  width: 40px;
  text-align: center;
}

.rank-badge {
  display: inline-block;
  width: 32px;
  height: 32px;
  line-height: 32px;
  text-align: center;
  border-radius: 50%;
  font-weight: bold;
  font-size: 14px;
}

.rank-badge.rank-first {
  background: linear-gradient(135deg, #ffd700, #ffed4e);
  color: #fff;
  box-shadow: 0 2px 8px rgba(255, 215, 0, 0.3);
}

.rank-badge.rank-second {
  background: linear-gradient(135deg, #c0c0c0, #e8e8e8);
  color: #333;
  box-shadow: 0 2px 8px rgba(192, 192, 192, 0.3);
}

.rank-badge.rank-third {
  background: linear-gradient(135deg, #cd7f32, #daa520);
  color: #fff;
  box-shadow: 0 2px 8px rgba(205, 127, 50, 0.3);
}

.rank-badge.rank-normal {
  background: #f5f5f5;
  color: #666;
}

.user-info {
  text-align: left;
}

.user-name {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 4px;
}

.user-level {
  font-size: 12px;
  color: #666;
}

.ranking-right {
  text-align: right;
}

.main-stat {
  margin-bottom: 4px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #333;
}

.stat-unit {
  font-size: 14px;
  color: #666;
  margin-left: 4px;
}

.additional-stats {
  font-size: 12px;
  color: #999;
}

.medal {
  position: absolute;
  top: 10px;
  right: 10px;
}

.gold-medal {
  color: #ffd700;
  font-size: 20px;
}

.silver-medal {
  color: #c0c0c0;
  font-size: 20px;
}

.bronze-medal {
  color: #cd7f32;
  font-size: 20px;
}

.empty-state {
  text-align: center;
  padding: 40px;
}

.stats-cards {
  margin-top: 20px;
}

.stat-card {
  height: 120px;
}

.stat-card-content {
  display: flex;
  align-items: center;
  height: 100%;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
  font-size: 24px;
}

.stat-icon.study-hours {
  background: linear-gradient(135deg, #409eff, #66b3ff);
  color: white;
}

.stat-icon.points {
  background: linear-gradient(135deg, #f56c6c, #ff8a8a);
  color: white;
}

.stat-icon.streak {
  background: linear-gradient(135deg, #e6a23c, #f0c78a);
  color: white;
}

.stat-icon.tasks {
  background: linear-gradient(135deg, #67c23a, #95d475);
  color: white;
}

.stat-info {
  flex: 1;
}

.stat-number {
  font-size: 28px;
  font-weight: bold;
  color: #333;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

.achievements-section {
  margin-bottom: 20px;
}

.achievements-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 15px;
}

.achievement-item {
  display: flex;
  align-items: center;
  padding: 15px;
  border: 1px solid #eee;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.achievement-item.unlocked {
  background: linear-gradient(135deg, #67c23a20, #67c23a10);
  border-color: #67c23a;
}

.achievement-item:not(.unlocked) {
  opacity: 0.5;
}

.achievement-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
  font-size: 20px;
  color: #666;
}

.achievement-item.unlocked .achievement-icon {
  background: linear-gradient(135deg, #67c23a, #95d475);
  color: white;
}

.achievement-info {
  flex: 1;
}

.achievement-name {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 4px;
}

.achievement-desc {
  font-size: 14px;
  color: #666;
  margin-bottom: 4px;
}

.achievement-date {
  font-size: 12px;
  color: #999;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .stat-number {
    font-size: 20px;
  }
}

@media (max-width: 768px) {
  .stats-cards .el-col {
    margin-bottom: 15px;
  }
  
  .stat-label {
    font-size: 11px;
  }
  
  .ranking-list {
    max-height: 400px;
  }
  
  .ranking-item {
    padding: 12px;
  }
  
  .leaderboard {
    padding: 15px;
  }
  
  .header h2 {
    font-size: 20px;
  }
  
  .user-name {
    font-size: 13px;
  }
  
  .stat-value {
    font-size: 14px;
  }
}

@media (max-width: 480px) {
  .stats-cards .el-row {
    flex-direction: column;
  }
  
  .stats-cards .el-col {
    width: 100% !important;
    margin-bottom: 10px;
  }
  
  .stat-number {
    font-size: 18px;
  }
  
  .ranking-item {
    padding: 10px;
  }
  
  .user-name {
    font-size: 12px;
  }
  
  .stat-value {
    font-size: 13px;
  }
  
  .leaderboard {
    padding: 10px;
  }
}
</style>