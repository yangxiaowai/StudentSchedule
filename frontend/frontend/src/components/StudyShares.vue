<template>
  <div class="study-shares">
    <div class="header">
      <h2>学习分享</h2>
      <el-button type="primary" @click="showCreateDialog = true">
        <el-icon><Plus /></el-icon>
        发布分享
      </el-button>
    </div>

    <!-- 搜索和筛选 -->
    <div class="search-section">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索分享内容"
            @keyup.enter="searchShares"
            clearable
          >
            <template #append>
              <el-button @click="searchShares">
                <el-icon><Search /></el-icon>
              </el-button>
            </template>
          </el-input>
        </el-col>
        <el-col :span="4">
          <el-select v-model="selectedType" placeholder="分享类型" clearable @change="filterShares">
            <el-option label="学习笔记" value="NOTE" />
            <el-option label="学习资源" value="RESOURCE" />
            <el-option label="学习经验" value="EXPERIENCE" />
            <el-option label="问题讨论" value="QUESTION" />
            <el-option label="学习总结" value="SUMMARY" />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-select v-model="selectedSubject" placeholder="选择学科" clearable @change="filterShares">
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
          <el-select v-model="viewMode" @change="loadShares">
            <el-option label="最新分享" value="public" />
            <el-option label="热门分享" value="popular" />
            <el-option label="推荐分享" value="recommended" />
            <el-option label="我的分享" value="my" />
          </el-select>
        </el-col>
        <el-col :span="3">
          <el-input v-model="tagSearch" placeholder="搜索标签" @keyup.enter="searchByTag" />
        </el-col>
        <el-col :span="3">
          <el-button @click="loadShares">刷新</el-button>
        </el-col>
      </el-row>
    </div>

    <!-- 分享列表 -->
    <div class="shares-list">
      <el-card v-for="share in shares" :key="share.id" class="share-card" shadow="hover">
        <div class="share-header">
          <div class="share-title">
            <h3 @click="viewShareDetail(share)">{{ share.title }}</h3>
            <div class="share-meta">
              <el-tag :type="getShareTypeColor(share.shareType)">{{ getShareTypeText(share.shareType) }}</el-tag>
              <el-tag type="info">{{ share.subject }}</el-tag>
              <span class="share-time">{{ formatTime(share.createdAt) }}</span>
            </div>
          </div>
          <div class="share-actions">
            <el-button 
              :type="share.isLiked ? 'danger' : 'default'" 
              size="small" 
              @click="toggleLike(share)"
            >
              <el-icon><Star /></el-icon>
              {{ share.likeCount }}
            </el-button>
            <el-button size="small" @click="viewShareDetail(share)">
              <el-icon><View /></el-icon>
              {{ share.viewCount }}
            </el-button>
          </div>
        </div>
        
        <div class="share-content">
          <p>{{ share.content.substring(0, 200) }}{{ share.content.length > 200 ? '...' : '' }}</p>
          <div v-if="share.fileUrl" class="share-file">
            <el-link :href="share.fileUrl" target="_blank">
              <el-icon><Document /></el-icon>
              {{ share.fileName }}
            </el-link>
          </div>
        </div>
        
        <div class="share-tags" v-if="share.tags">
          <el-tag 
            v-for="tag in parseTagsArray(share.tags)" 
            :key="tag" 
            size="small" 
            @click="searchByTag(tag)"
            style="cursor: pointer; margin-right: 8px;"
          >
            #{{ tag }}
          </el-tag>
        </div>
        
        <div class="share-footer">
          <div class="share-stats">
            <span><el-icon><ChatDotRound /></el-icon> {{ share.commentCount }} 评论</span>
            <span><el-icon><View /></el-icon> {{ share.viewCount }} 浏览</span>
          </div>
          <div class="share-user">
            <span>{{ share.userName || `用户${share.userId}` }}</span>
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

    <!-- 创建分享对话框 -->
    <el-dialog v-model="showCreateDialog" title="发布学习分享" width="800px">
      <el-form :model="newShare" :rules="shareRules" ref="shareForm" label-width="100px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="newShare.title" placeholder="请输入分享标题" />
        </el-form-item>
        <el-form-item label="分享类型" prop="shareType">
          <el-select v-model="newShare.shareType" placeholder="选择分享类型">
            <el-option label="学习笔记" value="NOTE" />
            <el-option label="学习资源" value="RESOURCE" />
            <el-option label="学习经验" value="EXPERIENCE" />
            <el-option label="问题讨论" value="QUESTION" />
            <el-option label="学习总结" value="SUMMARY" />
          </el-select>
        </el-form-item>
        <el-form-item label="学科" prop="subject">
          <el-select v-model="newShare.subject" placeholder="选择学科">
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
        <el-form-item label="内容" prop="content">
          <el-input 
            v-model="newShare.content" 
            type="textarea" 
            :rows="8" 
            placeholder="请输入分享内容"
          />
        </el-form-item>
        <el-form-item label="标签">
          <el-input 
            v-model="newShare.tags" 
            placeholder="请输入标签，用逗号分隔，如：高考,数学,解题技巧"
          />
        </el-form-item>
        <el-form-item label="小组">
          <el-select v-model="newShare.groupId" placeholder="选择分享到的小组（可选）" clearable>
            <el-option 
              v-for="group in myGroups" 
              :key="group.id" 
              :label="group.name" 
              :value="group.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="文件">
          <el-upload
            class="upload-demo"
            :action="uploadUrl"
            :on-success="handleFileSuccess"
            :before-upload="beforeFileUpload"
            :file-list="fileList"
          >
            <el-button size="small" type="primary">点击上传</el-button>
            <template #tip>
              <div class="el-upload__tip">
                支持上传文档、图片等学习资料
              </div>
            </template>
          </el-upload>
        </el-form-item>
        <el-form-item label="公开设置">
          <el-radio-group v-model="newShare.isPublic">
            <el-radio :value="true">公开分享</el-radio>
            <el-radio :value="false">仅小组可见</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showCreateDialog = false">取消</el-button>
          <el-button type="primary" @click="createShare">发布</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 分享详情对话框 -->
    <el-dialog 
      v-model="showDetailDialog" 
      title="分享详情" 
      width="900px"
      :top="'5vh'"
      draggable
      :close-on-click-modal="false"
    >
      <div v-if="selectedShare" class="share-detail">
        <div class="detail-header">
          <h2>{{ selectedShare.title }}</h2>
          <div class="detail-meta">
            <el-tag :type="getShareTypeColor(selectedShare.shareType)">
              {{ getShareTypeText(selectedShare.shareType) }}
            </el-tag>
            <el-tag type="info">{{ selectedShare.subject }}</el-tag>
            <span class="detail-time">{{ formatTime(selectedShare.createdAt) }}</span>
          </div>
        </div>
        
        <div class="detail-content">
          <div class="content-text">{{ selectedShare.content }}</div>
          <div v-if="selectedShare.fileUrl" class="detail-file">
            <h4>附件：</h4>
            <el-link :href="selectedShare.fileUrl" target="_blank">
              <el-icon><Document /></el-icon>
              {{ selectedShare.fileName }}
            </el-link>
          </div>
        </div>
        
        <div class="detail-tags" v-if="selectedShare.tags">
          <h4>标签：</h4>
          <el-tag 
            v-for="tag in parseTagsArray(selectedShare.tags)" 
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
            <span>{{ selectedShare.likeCount }} 点赞</span>
          </div>
          <div class="stats-item">
            <el-icon><View /></el-icon>
            <span>{{ selectedShare.viewCount }} 浏览</span>
          </div>
          <div class="stats-item">
            <el-icon><ChatDotRound /></el-icon>
            <span>{{ selectedShare.commentCount }} 评论</span>
          </div>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showDetailDialog = false">关闭</el-button>
          <el-button 
            :type="selectedShare && selectedShare.isLiked ? 'danger' : 'primary'" 
            @click="toggleLike(selectedShare)"
          >
            {{ selectedShare && selectedShare.isLiked ? '取消点赞' : '点赞' }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Search, Star, View, Document, ChatDotRound } from '@element-plus/icons-vue'
import socialAPI, { userAPI } from '../api/social.js'
const { studyShareAPI } = socialAPI

export default {
  name: 'StudyShares',
  components: {
    Plus,
    Search,
    Star,
    View,
    Document,
    ChatDotRound
  },
  setup() {
    const shares = ref([])
    const myGroups = ref([])
    const currentPage = ref(1)
    const pageSize = ref(10)
    const total = ref(0)
    const searchKeyword = ref('')
    const selectedType = ref('')
    const selectedSubject = ref('')
    const viewMode = ref('public')
    const tagSearch = ref('')
    const showCreateDialog = ref(false)
    const showDetailDialog = ref(false)
    const selectedShare = ref(null)
    const currentUserId = ref(parseInt(localStorage.getItem('userId')) || 51) // 从localStorage获取用户ID
    const userInfoCache = ref(new Map()) // 用户信息缓存
    const fileList = ref([])
    const uploadUrl = '/api/upload' // 文件上传接口

    const newShare = reactive({
      title: '',
      content: '',
      shareType: '',
      subject: '',
      tags: '',
      groupId: null,
      fileUrl: '',
      fileName: '',
      isPublic: true,
      userId: parseInt(localStorage.getItem('userId')) || 51
    })

    const shareRules = {
      title: [{ required: true, message: '请输入分享标题', trigger: 'blur' }],
      content: [{ required: true, message: '请输入分享内容', trigger: 'blur' }],
      shareType: [{ required: true, message: '请选择分享类型', trigger: 'change' }],
      subject: [{ required: true, message: '请选择学科', trigger: 'change' }]
    }

    // 加载分享列表
    const loadShares = async () => {
      try {
        const page = currentPage.value - 1
        const size = pageSize.value
        let response

        if (viewMode.value === 'public') {
          response = await studyShareAPI.getPublicShares(page, size)
        } else if (viewMode.value === 'popular') {
          response = await studyShareAPI.getPopularShares(page, size)
        } else if (viewMode.value === 'recommended') {
          response = await studyShareAPI.getRecommendedShares(currentUserId.value, page, size)
        } else if (viewMode.value === 'my') {
          response = await studyShareAPI.getSharesByUser(currentUserId.value, page, size)
        } else {
          response = await studyShareAPI.getPublicShares(page, size)
        }

        if (response.data) {
          // 后端直接返回分页对象
          const pageData = response.data
          shares.value = pageData.content.map(share => ({
            ...share,
            isLiked: false // 这里应该从后端获取用户是否已点赞
          }))
          // 为每个分享获取用户名
          for (const share of shares.value) {
            share.userName = await getUserName(share.userId)
          }
          total.value = pageData.totalElements
        }
      } catch (error) {
        console.error('加载分享列表失败:', error)
        ElMessage.error('加载分享列表失败')
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

    // 搜索分享
    const searchShares = async () => {
      if (!searchKeyword.value.trim()) {
        loadShares()
        return
      }
      
      try {
        const response = await studyShareAPI.searchShares(searchKeyword.value, currentPage.value - 1, pageSize.value)
        if (response.data) {
          const pageData = response.data
          shares.value = pageData.content.map(share => ({
            ...share,
            isLiked: false
          }))
          total.value = pageData.totalElements
        }
      } catch (error) {
        console.error('搜索失败:', error)
        ElMessage.error('搜索失败')
      }
    }

    // 按标签搜索
    const searchByTag = async (tag) => {
      const searchTag = tag || tagSearch.value
      if (!searchTag.trim()) {
        loadShares()
        return
      }
      
      try {
        const response = await studyShareAPI.getSharesByTags(searchTag, currentPage.value - 1, pageSize.value)
        if (response.data) {
          const pageData = response.data
          shares.value = pageData.content.map(share => ({
            ...share,
            isLiked: false
          }))
          total.value = pageData.totalElements
        }
      } catch (error) {
        console.error('搜索失败:', error)
        ElMessage.error('搜索失败')
      }
    }

    // 筛选分享
    const filterShares = async () => {
      try {
        const page = currentPage.value - 1
        const size = pageSize.value
        let response

        if (selectedType.value) {
          response = await studyShareAPI.getSharesByType(selectedType.value, page, size)
        } else if (selectedSubject.value) {
          response = await studyShareAPI.getSharesBySubject(selectedSubject.value, page, size)
        } else {
          response = await studyShareAPI.getPublicShares(page, size)
        }

        if (response.data) {
          const pageData = response.data
          shares.value = pageData.content.map(share => ({
            ...share,
            isLiked: false
          }))
          total.value = pageData.totalElements
        }
      } catch (error) {
        console.error('筛选失败:', error)
        ElMessage.error('筛选失败')
      }
    }

    // 创建分享
    const createShare = async () => {
      try {
        const response = await studyShareAPI.createShare(newShare)
        if (response.data && response.data.success) {
          ElMessage.success('分享发布成功')
          showCreateDialog.value = false
          resetNewShare()
          loadShares()
        } else {
          ElMessage.error(response.data?.message || '发布失败')
        }
      } catch (error) {
        console.error('发布分享失败:', error)
        ElMessage.error('发布失败: ' + (error.response?.data?.message || error.message || '未知错误'))
      }
    }

    // 重置新分享表单
    const resetNewShare = () => {
      Object.assign(newShare, {
        title: '',
        content: '',
        shareType: '',
        subject: '',
        tags: '',
        groupId: null,
        fileUrl: '',
        fileName: '',
        isPublic: true,
        userId: parseInt(localStorage.getItem('userId')) || 51
      })
      fileList.value = []
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

    // 点赞/取消点赞
    const toggleLike = async (share) => {
      try {
        let response
        if (share.isLiked) {
          response = await studyShareAPI.unlikeShare(share.id, currentUserId.value)
        } else {
          response = await studyShareAPI.likeShare(share.id, currentUserId.value)
        }
        
        // 更新本地状态
        share.isLiked = !share.isLiked
        share.likeCount += share.isLiked ? 1 : -1
        ElMessage.success(share.isLiked ? '点赞成功' : '取消点赞成功')
      } catch (error) {
        console.error('点赞操作失败:', error)
        ElMessage.error('操作失败: ' + (error.response?.data?.message || error.message))
      }
    }

    // 查看分享详情
    const viewShareDetail = async (share) => {
      selectedShare.value = share
      showDetailDialog.value = true
      
      // 增加浏览量
      try {
        await studyShareAPI.incrementViews(share.id)
        share.viewCount += 1
      } catch (error) {
        console.error('更新浏览量失败', error)
      }
    }

    // 文件上传成功回调
    const handleFileSuccess = (response, file) => {
      newShare.fileUrl = response.url
      newShare.fileName = file.name
      ElMessage.success('文件上传成功')
    }

    // 文件上传前检查
    const beforeFileUpload = (file) => {
      const isLt10M = file.size / 1024 / 1024 < 10
      if (!isLt10M) {
        ElMessage.error('文件大小不能超过 10MB!')
      }
      return isLt10M
    }

    // 解析标签数组
    const parseTagsArray = (tags) => {
      if (!tags) return []
      return tags.split(',')
    }

    // 获取分享类型颜色
    const getShareTypeColor = (type) => {
      const colors = {
        NOTE: 'primary',
        RESOURCE: 'success',
        EXPERIENCE: 'warning',
        QUESTION: 'danger',
        SUMMARY: 'info'
      }
      return colors[type] || 'default'
    }

    // 获取分享类型文本
    const getShareTypeText = (type) => {
      const texts = {
        NOTE: '学习笔记',
        RESOURCE: '学习资源',
        EXPERIENCE: '学习经验',
        QUESTION: '问题讨论',
        SUMMARY: '学习总结'
      }
      return texts[type] || type
    }

    // 格式化时间
    const formatTime = (time) => {
      return new Date(time).toLocaleString()
    }

    // 分页处理
    const handleSizeChange = (val) => {
      pageSize.value = val
      loadShares()
    }

    const handleCurrentChange = (val) => {
      currentPage.value = val
      loadShares()
    }

    onMounted(() => {
      loadShares()
      loadMyGroups()
    })

    return {
      shares,
      myGroups,
      currentPage,
      pageSize,
      total,
      searchKeyword,
      selectedType,
      selectedSubject,
      viewMode,
      tagSearch,
      showCreateDialog,
      showDetailDialog,
      selectedShare,
      newShare,
      shareRules,
      fileList,
      uploadUrl,
      loadShares,
      searchShares,
      searchByTag,
      filterShares,
      createShare,
      toggleLike,
      viewShareDetail,
      handleFileSuccess,
      beforeFileUpload,
      parseTagsArray,
      getShareTypeColor,
      getShareTypeText,
      formatTime,
      handleSizeChange,
      handleCurrentChange
    }
  }
}
</script>

<style scoped>
.study-shares {
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

.shares-list {
  margin-bottom: 20px;
}

.share-card {
  margin-bottom: 20px;
}

.share-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 15px;
}

.share-title h3 {
  margin: 0 0 10px 0;
  font-size: 18px;
  cursor: pointer;
  color: #409eff;
}

.share-title h3:hover {
  text-decoration: underline;
}

.share-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
}

.share-time {
  color: #999;
}

.share-actions {
  display: flex;
  gap: 10px;
}

.share-content {
  margin-bottom: 15px;
}

.share-content p {
  line-height: 1.6;
  color: #333;
  margin-bottom: 10px;
}

.share-file {
  padding: 10px;
  background: #f5f5f5;
  border-radius: 4px;
}

.share-tags {
  margin-bottom: 15px;
}

.share-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 15px;
  border-top: 1px solid #eee;
  font-size: 14px;
  color: #666;
}

.share-stats {
  display: flex;
  gap: 20px;
}

.share-stats span {
  display: flex;
  align-items: center;
  gap: 5px;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.share-detail {
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

.detail-file {
  padding: 15px;
  background: #f5f5f5;
  border-radius: 8px;
  margin-bottom: 15px;
}

.detail-file h4 {
  margin: 0 0 10px 0;
  color: #333;
}

.detail-file p {
  margin: 0;
  color: #666;
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
}

.stats-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
}

.upload-demo {
  width: 100%;
}

/* 对话框样式优化 */
:deep(.el-dialog) {
  margin-top: 5vh !important;
  margin-bottom: 5vh !important;
  max-height: 90vh;
  overflow-y: auto;
}

:deep(.el-dialog__body) {
  max-height: 70vh;
  overflow-y: auto;
}

@media (max-width: 768px) {
  :deep(.el-dialog) {
    width: 95% !important;
    margin: 2vh auto !important;
    max-height: 96vh;
  }
  
  :deep(.el-dialog__body) {
    max-height: 80vh;
  }
}
</style>