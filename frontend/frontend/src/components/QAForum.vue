<template>
  <div class="qa-forum">
    <div class="header">
      <h2>互助答疑</h2>
      <el-button type="primary" @click="showAskDialog = true">
        <el-icon><Plus /></el-icon>
        提问
      </el-button>
    </div>

    <!-- 搜索和筛选 -->
    <div class="search-section">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索问题"
            @keyup.enter="searchQuestions"
            clearable
          >
            <template #append>
              <el-button @click="searchQuestions">
                <el-icon><Search /></el-icon>
              </el-button>
            </template>
          </el-input>
        </el-col>
        <el-col :span="4">
          <el-select v-model="selectedStatus" placeholder="问题状态" clearable @change="filterQuestions">
            <el-option label="待解决" value="OPEN" />
            <el-option label="已解决" value="RESOLVED" />
            <el-option label="已关闭" value="CLOSED" />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-select v-model="selectedSubject" placeholder="选择学科" clearable @change="filterQuestions">
            <el-option label="数学" value="数学" />
            <el-option label="英语" value="英语" />
            <el-option label="物理" value="物理" />
            <el-option label="化学" value="化学" />
            <el-option label="生物" value="生物" />
            <el-option label="历史" value="历史" />
            <el-option label="地理" value="地理" />
            <el-option label="政治" value="政治" />
            <el-option label="计算机" value="计算机" />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-select v-model="selectedDifficulty" placeholder="难度等级" clearable @change="filterQuestions">
            <el-option label="简单" value="EASY" />
            <el-option label="中等" value="MEDIUM" />
            <el-option label="困难" value="HARD" />
          </el-select>
        </el-col>
        <el-col :span="3">
          <el-select v-model="viewMode" @change="loadQuestions">
            <el-option label="最新问题" value="latest" />
            <el-option label="热门问题" value="popular" />
            <el-option label="推荐问题" value="recommended" />
            <el-option label="我的问题" value="my" />
            <el-option label="未解决" value="unresolved" />
          </el-select>
        </el-col>
        <el-col :span="3">
          <el-button @click="loadQuestions">刷新</el-button>
        </el-col>
      </el-row>
    </div>

    <!-- 问题列表 -->
    <div class="questions-list">
      <el-card v-for="question in questions" :key="question.id" class="question-card" shadow="hover">
        <div class="question-header">
          <div class="question-title">
            <h3 @click="viewQuestionDetail(question)">{{ question.title }}</h3>
            <div class="question-meta">
              <el-tag :type="getStatusColor(question.status)">{{ getStatusText(question.status) }}</el-tag>
              <el-tag :type="getDifficultyColor(question.difficultyLevel)">{{ getDifficultyText(question.difficultyLevel) }}</el-tag>
              <el-tag type="info">{{ question.subject }}</el-tag>
              <span class="question-time">{{ formatTime(question.createdAt) }}</span>
            </div>
          </div>
          <div class="question-actions">
            <el-button 
              :type="question.isLiked ? 'danger' : 'default'" 
              size="small" 
              @click="toggleLike(question)"
            >
              <el-icon><Star /></el-icon>
              {{ question.likeCount }}
            </el-button>
            <el-button size="small" @click="viewQuestionDetail(question)">
              <el-icon><View /></el-icon>
              {{ question.viewCount }}
            </el-button>
          </div>
        </div>
        
        <div class="question-content">
          <p>{{ question.content.substring(0, 200) }}{{ question.content.length > 200 ? '...' : '' }}</p>
          <div v-if="question.imageUrl" class="question-image">
            <el-image :src="question.imageUrl" style="width: 200px; height: 150px" fit="cover" />
          </div>
        </div>
        
        <div class="question-tags" v-if="question.tags">
          <el-tag 
            v-for="tag in parseTagsArray(question.tags)" 
            :key="tag" 
            size="small" 
            @click="searchByTag(tag)"
            style="cursor: pointer; margin-right: 8px;"
          >
            #{{ tag }}
          </el-tag>
        </div>
        
        <div class="question-footer">
          <div class="question-stats">
            <span><el-icon><ChatDotRound /></el-icon> {{ question.answerCount }} 回答</span>
            <span><el-icon><View /></el-icon> {{ question.viewCount }} 浏览</span>
            <span v-if="question.rewardPoints > 0">
              <el-icon><Trophy /></el-icon> {{ question.rewardPoints }} 积分
            </span>
          </div>
          <div class="question-user">
            <span>{{ question.userName || `用户${question.userId}` }}</span>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 分页 -->
    <div class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[5, 10, 20, 30]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 提问对话框 -->
    <el-dialog v-model="showAskDialog" title="提问" width="800px">
      <el-form :model="newQuestion" :rules="questionRules" ref="questionForm" label-width="100px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="newQuestion.title" placeholder="请输入问题标题" />
        </el-form-item>
        <el-form-item label="学科" prop="subject">
          <el-select v-model="newQuestion.subject" placeholder="选择学科">
            <el-option label="数学" value="数学" />
            <el-option label="英语" value="英语" />
            <el-option label="物理" value="物理" />
            <el-option label="化学" value="化学" />
            <el-option label="生物" value="生物" />
            <el-option label="历史" value="历史" />
            <el-option label="地理" value="地理" />
            <el-option label="政治" value="政治" />
            <el-option label="计算机" value="计算机" />
          </el-select>
        </el-form-item>
        <el-form-item label="难度等级" prop="difficultyLevel">
          <el-select v-model="newQuestion.difficultyLevel" placeholder="选择难度等级">
            <el-option label="简单" value="EASY" />
            <el-option label="中等" value="MEDIUM" />
            <el-option label="困难" value="HARD" />
          </el-select>
        </el-form-item>
        <el-form-item label="问题描述" prop="content">
          <el-input 
            v-model="newQuestion.content" 
            type="textarea" 
            :rows="8" 
            placeholder="请详细描述你的问题"
          />
        </el-form-item>
        <el-form-item label="标签">
          <el-input 
            v-model="newQuestion.tags" 
            placeholder="请输入标签，用逗号分隔，如：高考,数学,解题技巧"
          />
        </el-form-item>
        <el-form-item label="小组">
          <el-select v-model="newQuestion.groupId" placeholder="选择提问的小组（可选）" clearable>
            <el-option 
              v-for="group in myGroups" 
              :key="group.id" 
              :label="group.name" 
              :value="group.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="图片">
          <el-upload
            class="upload-demo"
            :action="uploadUrl"
            :on-success="handleImageSuccess"
            :before-upload="beforeImageUpload"
            :file-list="imageList"
            list-type="picture"
          >
            <el-button size="small" type="primary">上传图片</el-button>
            <template #tip>
              <div class="el-upload__tip">
                支持上传题目图片等
              </div>
            </template>
          </el-upload>
        </el-form-item>
        <el-form-item label="悬赏积分">
          <el-input-number v-model="newQuestion.rewardPoints" :min="0" :max="1000" />
          <span style="margin-left: 10px; color: #999;">设置悬赏积分可以吸引更多回答</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showAskDialog = false">取消</el-button>
          <el-button type="primary" @click="createQuestion">提问</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 问题详情对话框 -->
    <el-dialog v-model="showDetailDialog" title="问题详情" width="1000px">
      <div v-if="selectedQuestion" class="question-detail">
        <div class="detail-header">
          <h2>{{ selectedQuestion.title }}</h2>
          <div class="detail-meta">
            <el-tag :type="getStatusColor(selectedQuestion.status)">
              {{ getStatusText(selectedQuestion.status) }}
            </el-tag>
            <el-tag :type="getDifficultyColor(selectedQuestion.difficultyLevel)">
              {{ getDifficultyText(selectedQuestion.difficultyLevel) }}
            </el-tag>
            <el-tag type="info">{{ selectedQuestion.subject }}</el-tag>
            <span class="detail-time">{{ formatTime(selectedQuestion.createdAt) }}</span>
          </div>
        </div>
        
        <div class="detail-content">
          <div class="content-text">{{ selectedQuestion.content }}</div>
          <div v-if="selectedQuestion.imageUrl" class="detail-image">
            <h4>图片：</h4>
            <el-image :src="selectedQuestion.imageUrl" style="max-width: 500px" fit="contain" />
          </div>
        </div>
        
        <div class="detail-tags" v-if="selectedQuestion.tags">
          <h4>标签：</h4>
          <el-tag 
            v-for="tag in parseTagsArray(selectedQuestion.tags)" 
            :key="tag" 
            size="small" 
            style="margin-right: 8px;"
          >
            #{{ tag }}
          </el-tag>
        </div>
        
        <div class="detail-stats">
          <div class="stats-item">
            <el-icon><Star /></el-icon>
            <span>{{ selectedQuestion.likeCount }} 点赞</span>
          </div>
          <div class="stats-item">
            <el-icon><View /></el-icon>
            <span>{{ selectedQuestion.viewCount }} 浏览</span>
          </div>
          <div class="stats-item">
            <el-icon><ChatDotRound /></el-icon>
            <span>{{ selectedQuestion.answerCount }} 回答</span>
          </div>
          <div v-if="selectedQuestion.rewardPoints > 0" class="stats-item">
            <el-icon><Trophy /></el-icon>
            <span>{{ selectedQuestion.rewardPoints }} 积分悬赏</span>
          </div>
        </div>

        <!-- 回答列表 -->
        <div class="answers-section">
          <div class="answers-header">
            <h3>回答 ({{ answers.length }})</h3>
            <el-button type="primary" @click="showAnswerDialog = true">
              <el-icon><Plus /></el-icon>
              回答问题
            </el-button>
          </div>
          
          <div class="answers-list">
            <div v-for="answer in answers" :key="answer.id" class="answer-item">
              <div class="answer-header">
                <div class="answer-user">
                  <span>{{ answer.userName || `用户${answer.userId}` }}</span>
                  <span class="answer-time">{{ formatTime(answer.createdAt) }}</span>
                  <el-tag v-if="answer.isBestAnswer" type="success" size="small">最佳答案</el-tag>
                </div>
                <div class="answer-actions">
                  <el-button 
                    :type="answer.isLiked ? 'danger' : 'default'" 
                    size="small" 
                    @click="toggleAnswerLike(answer)"
                  >
                    <el-icon><Star /></el-icon>
                    {{ answer.likeCount }}
                  </el-button>
                  <el-button 
                    v-if="!answer.isBestAnswer && selectedQuestion.userId === currentUserId"
                    type="success" 
                    size="small" 
                    @click="setBestAnswer(answer)"
                  >
                    设为最佳答案
                  </el-button>
                </div>
              </div>
              <div class="answer-content">
                <p>{{ answer.content }}</p>
                <div v-if="answer.imageUrl" class="answer-image">
                  <el-image :src="answer.imageUrl" style="max-width: 300px" fit="contain" />
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showDetailDialog = false">关闭</el-button>
          <el-button 
            :type="selectedQuestion && selectedQuestion.isLiked ? 'danger' : 'primary'" 
            @click="toggleLike(selectedQuestion)"
          >
            {{ selectedQuestion && selectedQuestion.isLiked ? '取消点赞' : '点赞' }}
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 回答问题对话框 -->
    <el-dialog v-model="showAnswerDialog" title="回答问题" width="600px">
      <el-form :model="newAnswer" :rules="answerRules" ref="answerForm" label-width="80px">
        <el-form-item label="回答内容" prop="content">
          <el-input 
            v-model="newAnswer.content" 
            type="textarea" 
            :rows="6" 
            placeholder="请输入你的回答"
          />
        </el-form-item>
        <el-form-item label="图片">
          <el-upload
            class="upload-demo"
            :action="uploadUrl"
            :on-success="handleAnswerImageSuccess"
            :before-upload="beforeImageUpload"
            :file-list="answerImageList"
            list-type="picture"
          >
            <el-button size="small" type="primary">上传图片</el-button>
            <template #tip>
              <div class="el-upload__tip">
                支持上传解答图片
              </div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showAnswerDialog = false">取消</el-button>
          <el-button type="primary" @click="createAnswer">提交回答</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Search, Star, View, ChatDotRound, Trophy } from '@element-plus/icons-vue'
import socialAPI, { userAPI } from '../api/social.js'
const { qaAPI } = socialAPI

export default {
  name: 'QAForum',
  components: {
    Plus,
    Search,
    Star,
    View,
    ChatDotRound,
    Trophy
  },
  setup() {
    const questions = ref([])
    const answers = ref([])
    const myGroups = ref([])
    const currentPage = ref(1)
    const pageSize = ref(10)
    const total = ref(0)
    const searchKeyword = ref('')
    const selectedStatus = ref('')
    const selectedSubject = ref('')
    const selectedDifficulty = ref('')
    const viewMode = ref('latest')
    const showAskDialog = ref(false)
    const showDetailDialog = ref(false)
    const showAnswerDialog = ref(false)
    const selectedQuestion = ref(null)
    const currentUserId = ref(parseInt(localStorage.getItem('userId')) || 51) // 从localStorage获取用户ID
    const currentUserName = ref('')
    const isLoggedIn = ref(false)
    const userInfoCache = ref(new Map()) // 用户信息缓存
    const imageList = ref([])
    const answerImageList = ref([])
    const uploadUrl = '/api/upload' // 图片上传接口

    const newQuestion = reactive({
      title: '',
      content: '',
      subject: '',
      difficultyLevel: '',
      tags: '',
      groupId: null,
      imageUrl: '',
      rewardPoints: 0,
      userId: parseInt(localStorage.getItem('userId')) || 51
    })

    const newAnswer = reactive({
      content: '',
      imageUrl: '',
      userId: parseInt(localStorage.getItem('userId')) || 51,
      questionId: null
    })

    const questionRules = {
      title: [{ required: true, message: '请输入问题标题', trigger: 'blur' }],
      content: [{ required: true, message: '请输入问题描述', trigger: 'blur' }],
      subject: [{ required: true, message: '请选择学科', trigger: 'change' }],
      difficultyLevel: [{ required: true, message: '请选择难度等级', trigger: 'change' }]
    }

    const answerRules = {
      content: [{ required: true, message: '请输入回答内容', trigger: 'blur' }]
    }

    // 加载问题列表
    const loadQuestions = async () => {
      try {
        const params = {
          page: currentPage.value - 1,
          size: pageSize.value
        }

        const response = await qaAPI.getQuestions(params.page, params.size, viewMode.value, currentUserId.value)
        if (response.data.success) {
          questions.value = response.data.data
          total.value = response.data.totalElements
          // 为每个问题获取用户名
          for (const question of questions.value) {
            question.userName = await getUserName(question.userId)
          }
        }
      } catch (error) {
        ElMessage.error('加载问题列表失败')
      }
    }

    // 加载我的小组
    const loadMyGroups = async () => {
      try {
        const response = await socialAPI.studyGroupAPI.getUserGroups(currentUserId.value)
        if (response.data.success) {
          myGroups.value = response.data.data
        }
      } catch (error) {
        console.error('加载我的小组失败', error)
      }
    }

    // 加载问题的回答
    const loadAnswers = async (questionId) => {
      try {
        const response = await qaAPI.getQuestionAnswers(questionId, 'createdAt', 0, 20, currentUserId.value)
        if (response.data.success) {
          answers.value = response.data.data
          // 为每个回答获取用户名
          for (const answer of answers.value) {
            answer.userName = await getUserName(answer.userId)
          }
        }
      } catch (error) {
        ElMessage.error('加载回答失败')
      }
    }

    // 搜索问题
    const searchQuestions = async () => {
      if (!searchKeyword.value.trim()) {
        loadQuestions()
        return
      }
      
      try {
        const response = await qaAPI.searchQuestions(searchKeyword.value, currentPage.value - 1, pageSize.value)
        if (response.data.success) {
          questions.value = response.data.data
          total.value = response.data.totalElements
        }
      } catch (error) {
        ElMessage.error('搜索失败')
      }
    }

    // 按标签搜索
    const searchByTag = async (tag) => {
      try {
        const response = await qaAPI.searchByTag(tag, currentPage.value - 1, pageSize.value)
        if (response.data.success) {
          questions.value = response.data.data
          total.value = response.data.totalElements
        }
      } catch (error) {
        ElMessage.error('搜索失败')
      }
    }

    // 筛选问题
    const filterQuestions = async () => {
      try {
        let url = '/qa/questions'
        const params = {
          page: currentPage.value - 1,
          size: pageSize.value
        }

        if (selectedStatus.value) {
          url += `/status/${selectedStatus.value}`
        } else if (selectedSubject.value) {
          url += `/subject/${selectedSubject.value}`
        } else if (selectedDifficulty.value) {
          url += `/difficulty/${selectedDifficulty.value}`
        }

        const response = await qaAPI.getQuestionsByFilter(selectedStatus.value, selectedSubject.value, selectedDifficulty.value, currentPage.value - 1, pageSize.value)
        if (response.data.success) {
          questions.value = response.data.data
          total.value = response.data.totalElements
        }
      } catch (error) {
        ElMessage.error('筛选失败')
      }
    }

    // 创建问题
    const createQuestion = async () => {
      try {
        const response = await qaAPI.createQuestion(newQuestion)
        if (response.data.success) {
          ElMessage.success('提问成功')
          showAskDialog.value = false
          resetNewQuestion()
          loadQuestions()
        }
      } catch (error) {
        ElMessage.error('提问失败')
      }
    }

    // 创建回答
    const createAnswer = async () => {
      try {
        newAnswer.questionId = selectedQuestion.value.id
        const response = await qaAPI.createAnswer(newAnswer)
        if (response.data.success) {
          ElMessage.success('回答成功')
          showAnswerDialog.value = false
          resetNewAnswer()
          loadAnswers(selectedQuestion.value.id)
          selectedQuestion.value.answerCount += 1
        }
      } catch (error) {
        ElMessage.error('回答失败')
      }
    }

    // 重置新问题表单
    const resetNewQuestion = () => {
      Object.assign(newQuestion, {
        title: '',
        content: '',
        subject: '',
        difficultyLevel: '',
        tags: '',
        groupId: null,
        imageUrl: '',
        rewardPoints: 0,
        userId: 1
      })
      imageList.value = []
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

    // 重置新回答表单
    const resetNewAnswer = () => {
      Object.assign(newAnswer, {
        content: '',
        imageUrl: '',
        userId: 1,
        questionId: null
      })
      answerImageList.value = []
    }

    // 点赞/取消点赞问题
    const toggleLike = async (question) => {
      try {
        const url = question.isLiked 
          ? `/qa/questions/${question.id}/unlike`
          : `/qa/questions/${question.id}/like`
        
        const response = await qaAPI.toggleQuestionLike(question.id, question.isLiked, currentUserId.value)
        
        if (response.data.success) {
          question.isLiked = !question.isLiked
          question.likeCount += question.isLiked ? 1 : -1
          ElMessage.success(question.isLiked ? '点赞成功' : '取消点赞成功')
        }
      } catch (error) {
        ElMessage.error('操作失败')
      }
    }

    // 点赞/取消点赞回答
    const toggleAnswerLike = async (answer) => {
      try {
        const url = answer.isLiked 
          ? `/qa/answers/${answer.id}/unlike`
          : `/qa/answers/${answer.id}/like`
        
        const response = await qaAPI.toggleAnswerLike(answer.id, answer.isLiked, currentUserId.value)
        
        if (response.data.success) {
          answer.isLiked = !answer.isLiked
          answer.likeCount += answer.isLiked ? 1 : -1
          ElMessage.success(answer.isLiked ? '点赞成功' : '取消点赞成功')
        }
      } catch (error) {
        ElMessage.error('操作失败')
      }
    }

    // 设置最佳答案
    const setBestAnswer = async (answer) => {
      try {
        const response = await qaAPI.setBestAnswer(answer.id, currentUserId.value)
        
        if (response.data.success) {
          answer.isBestAnswer = true
          selectedQuestion.value.status = 'RESOLVED'
          selectedQuestion.value.bestAnswerId = answer.id
          ElMessage.success('设置最佳答案成功')
        }
      } catch (error) {
        ElMessage.error('设置失败')
      }
    }

    // 查看问题详情
    const viewQuestionDetail = async (question) => {
      selectedQuestion.value = question
      showDetailDialog.value = true
      
      // 加载回答
      await loadAnswers(question.id)
      
      // 增加浏览量
      try {
        await qaAPI.viewQuestion(question.id)
        question.viewCount += 1
      } catch (error) {
        console.error('更新浏览量失败', error)
      }
    }

    // 图片上传成功回调
    const handleImageSuccess = (response, file) => {
      newQuestion.imageUrl = response.url
      ElMessage.success('图片上传成功')
    }

    // 回答图片上传成功回调
    const handleAnswerImageSuccess = (response, file) => {
      newAnswer.imageUrl = response.url
      ElMessage.success('图片上传成功')
    }

    // 图片上传前检查
    const beforeImageUpload = (file) => {
      const isImage = file.type.startsWith('image/')
      const isLt5M = file.size / 1024 / 1024 < 5
      
      if (!isImage) {
        ElMessage.error('只能上传图片文件!')
      }
      if (!isLt5M) {
        ElMessage.error('图片大小不能超过 5MB!')
      }
      return isImage && isLt5M
    }

    // 解析标签数组
    const parseTagsArray = (tags) => {
      if (!tags) return []
      return tags.split(',')
    }

    // 获取状态颜色
    const getStatusColor = (status) => {
      const colors = {
        OPEN: 'warning',
        RESOLVED: 'success',
        CLOSED: 'info'
      }
      return colors[status] || 'default'
    }

    // 获取状态文本
    const getStatusText = (status) => {
      const texts = {
        OPEN: '待解决',
        RESOLVED: '已解决',
        CLOSED: '已关闭'
      }
      return texts[status] || status
    }

    // 获取难度颜色
    const getDifficultyColor = (difficulty) => {
      const colors = {
        EASY: 'success',
        MEDIUM: 'warning',
        HARD: 'danger'
      }
      return colors[difficulty] || 'default'
    }

    // 获取难度文本
    const getDifficultyText = (difficulty) => {
      const texts = {
        EASY: '简单',
        MEDIUM: '中等',
        HARD: '困难'
      }
      return texts[difficulty] || difficulty
    }

    // 格式化时间
    const formatTime = (time) => {
      return new Date(time).toLocaleString()
    }

    // 分页处理
    const handleSizeChange = (val) => {
      pageSize.value = val
      loadQuestions()
    }

    const handleCurrentChange = (val) => {
      currentPage.value = val
      loadQuestions()
    }

    onMounted(() => {
      loadQuestions()
      loadMyGroups()
    })

    return {
      questions,
      answers,
      myGroups,
      currentPage,
      pageSize,
      total,
      searchKeyword,
      selectedStatus,
      selectedSubject,
      selectedDifficulty,
      viewMode,
      showAskDialog,
      showDetailDialog,
      showAnswerDialog,
      selectedQuestion,
      currentUserId,
      newQuestion,
      newAnswer,
      questionRules,
      answerRules,
      imageList,
      answerImageList,
      uploadUrl,
      loadQuestions,
      searchQuestions,
      searchByTag,
      filterQuestions,
      createQuestion,
      createAnswer,
      toggleLike,
      toggleAnswerLike,
      setBestAnswer,
      viewQuestionDetail,
      handleImageSuccess,
      handleAnswerImageSuccess,
      beforeImageUpload,
      parseTagsArray,
      getStatusColor,
      getStatusText,
      getDifficultyColor,
      getDifficultyText,
      formatTime,
      handleSizeChange,
      handleCurrentChange
    }
  }
}
</script>

<style scoped>
.qa-forum {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.search-section {
  margin-bottom: 20px;
}

.questions-list {
  margin-bottom: 20px;
}

.question-card {
  margin-bottom: 20px;
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 15px;
}

.question-title h3 {
  margin: 0 0 10px 0;
  font-size: 18px;
  cursor: pointer;
  color: #409eff;
}

.question-title h3:hover {
  text-decoration: underline;
}

.question-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
}

.question-time {
  color: #999;
}

.question-actions {
  display: flex;
  gap: 10px;
}

.question-content {
  margin-bottom: 15px;
}

.question-content p {
  line-height: 1.6;
  color: #333;
  margin-bottom: 10px;
}

.question-image {
  margin-top: 10px;
}

.question-tags {
  margin-bottom: 15px;
}

.question-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 15px;
  border-top: 1px solid #eee;
  font-size: 14px;
  color: #666;
}

.question-stats {
  display: flex;
  gap: 20px;
}

.question-stats span {
  display: flex;
  align-items: center;
  gap: 5px;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.question-detail {
  padding: 10px 0;
}

.detail-header {
  margin-bottom: 20px;
}

.detail-header h2 {
  margin: 0 0 10px 0;
}

.detail-meta {
  display: flex;
  align-items: center;
  gap: 10px;
}

.detail-time {
  color: #999;
  font-size: 14px;
}

.detail-content {
  margin-bottom: 20px;
}

.content-text {
  line-height: 1.8;
  font-size: 16px;
  margin-bottom: 15px;
  white-space: pre-wrap;
}

.detail-image {
  margin-top: 15px;
}

.detail-image h4 {
  margin: 0 0 10px 0;
}

.detail-tags {
  margin-bottom: 20px;
}

.detail-tags h4 {
  margin: 0 0 10px 0;
}

.detail-stats {
  display: flex;
  gap: 30px;
  padding: 15px;
  background: #f9f9f9;
  border-radius: 4px;
  margin-bottom: 30px;
}

.stats-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
}

.answers-section {
  border-top: 2px solid #eee;
  padding-top: 20px;
}

.answers-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.answers-header h3 {
  margin: 0;
}

.answers-list {
  space-y: 20px;
}

.answer-item {
  padding: 20px;
  border: 1px solid #eee;
  border-radius: 8px;
  margin-bottom: 15px;
}

.answer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.answer-user {
  display: flex;
  align-items: center;
  gap: 15px;
}

.answer-time {
  color: #999;
  font-size: 14px;
}

.answer-actions {
  display: flex;
  gap: 10px;
}

.answer-content {
  line-height: 1.6;
}

.answer-content p {
  margin-bottom: 10px;
}

.answer-image {
  margin-top: 10px;
}

.upload-demo {
  width: 100%;
}

.question-detail-dialog .el-dialog,
    .new-question-dialog .el-dialog {
      max-height: 80vh;
      overflow-y: auto;
      margin: 5vh auto;
      position: fixed;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
    }
    
    .question-detail-dialog .el-dialog__body,
    .new-question-dialog .el-dialog__body {
      max-height: 60vh;
      overflow-y: auto;
      padding: 20px;
    }
    
    /* 确保弹窗背景遮罩层正确显示 */
    .el-overlay {
      position: fixed;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      z-index: 2000;
    }
</style>