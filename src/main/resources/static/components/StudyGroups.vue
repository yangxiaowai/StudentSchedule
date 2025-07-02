<template>
  <div class="study-groups">
    <div class="header">
      <h2>学习小组</h2>
      <el-button type="primary" @click="showCreateDialog = true">
        <el-icon><Plus /></el-icon>
        创建小组
      </el-button>
    </div>

    <!-- 搜索和筛选 -->
    <div class="search-section">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索小组名称"
            @keyup.enter="searchGroups"
            clearable
          >
            <template #append>
              <el-button @click="searchGroups">
                <el-icon><Search /></el-icon>
              </el-button>
            </template>
          </el-input>
        </el-col>
        <el-col :span="6">
          <el-select v-model="selectedSubject" placeholder="选择学科" clearable @change="filterGroups">
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
        <el-col :span="6">
          <el-select v-model="viewMode" @change="loadGroups">
            <el-option label="公开小组" value="public" />
            <el-option label="我的小组" value="my" />
            <el-option label="热门小组" value="popular" />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-button @click="loadGroups">刷新</el-button>
        </el-col>
      </el-row>
    </div>

    <!-- 小组列表 -->
    <div class="groups-grid">
      <el-row :gutter="20">
        <el-col :span="8" v-for="group in groups" :key="group.id">
          <el-card class="group-card" shadow="hover" @click="viewGroupDetail(group)">
            <div class="group-header">
              <h3>{{ group.name }}</h3>
              <el-tag :type="group.isPublic ? 'success' : 'warning'">
                {{ group.isPublic ? '公开' : '私密' }}
              </el-tag>
            </div>
            <p class="group-description">{{ group.description }}</p>
            <div class="group-info">
              <div class="info-item">
                <el-icon><User /></el-icon>
                <span>{{ group.currentMembers }}/{{ group.maxMembers }}</span>
              </div>
              <div class="info-item">
                <el-icon><BookOpen /></el-icon>
                <span>{{ group.subject }}</span>
              </div>
              <div class="info-item">
                <el-icon><Target /></el-icon>
                <span>{{ group.studyGoal }}</span>
              </div>
            </div>
            <div class="group-actions">
              <el-button 
                v-if="!isGroupMember(group.id)" 
                type="primary" 
                size="small" 
                @click.stop="joinGroup(group)"
              >
                加入小组
              </el-button>
              <el-button 
                v-else 
                type="success" 
                size="small" 
                disabled
              >
                已加入
              </el-button>
              <el-button size="small" @click.stop="viewGroupDetail(group)">查看详情</el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 分页 -->
    <div class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[6, 12, 18, 24]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 创建小组对话框 -->
    <el-dialog v-model="showCreateDialog" title="创建学习小组" width="600px">
      <el-form :model="newGroup" :rules="groupRules" ref="groupForm" label-width="100px">
        <el-form-item label="小组名称" prop="name">
          <el-input v-model="newGroup.name" placeholder="请输入小组名称" />
        </el-form-item>
        <el-form-item label="小组描述" prop="description">
          <el-input 
            v-model="newGroup.description" 
            type="textarea" 
            :rows="3" 
            placeholder="请输入小组描述"
          />
        </el-form-item>
        <el-form-item label="学科" prop="subject">
          <el-select v-model="newGroup.subject" placeholder="选择学科">
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
        <el-form-item label="学习目标" prop="studyGoal">
          <el-input v-model="newGroup.studyGoal" placeholder="请输入学习目标" />
        </el-form-item>
        <el-form-item label="最大人数" prop="maxMembers">
          <el-input-number v-model="newGroup.maxMembers" :min="2" :max="50" />
        </el-form-item>
        <el-form-item label="小组类型">
          <el-radio-group v-model="newGroup.isPublic">
            <el-radio :label="true">公开小组</el-radio>
            <el-radio :label="false">私密小组</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showCreateDialog = false">取消</el-button>
          <el-button type="primary" @click="createGroup">创建</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 小组详情对话框 -->
    <el-dialog v-model="showDetailDialog" title="小组详情" width="800px">
      <div v-if="selectedGroup" class="group-detail">
        <div class="detail-header">
          <h3>{{ selectedGroup.name }}</h3>
          <div class="detail-tags">
            <el-tag type="primary">{{ selectedGroup.subject }}</el-tag>
            <el-tag :type="selectedGroup.isPublic ? 'success' : 'warning'">
              {{ selectedGroup.isPublic ? '公开' : '私密' }}
            </el-tag>
            <el-tag type="info">{{ selectedGroup.status }}</el-tag>
          </div>
        </div>
        <p><strong>描述：</strong>{{ selectedGroup.description }}</p>
        <p><strong>学习目标：</strong>{{ selectedGroup.studyGoal }}</p>
        <p><strong>成员：</strong>{{ selectedGroup.currentMembers }}/{{ selectedGroup.maxMembers }}</p>
        <p v-if="!selectedGroup.isPublic"><strong>邀请码：</strong>{{ selectedGroup.inviteCode }}</p>
        
        <div class="members-section">
          <h4>小组成员</h4>
          <el-table :data="groupMembers" style="width: 100%">
            <el-table-column prop="userId" label="用户ID" width="80" />
            <el-table-column prop="role" label="角色" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.role === 'ADMIN' ? 'danger' : 'primary'">
                  {{ scope.row.role === 'ADMIN' ? '管理员' : '成员' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="contributionScore" label="贡献分" width="100" />
            <el-table-column prop="studyHours" label="学习时长" width="100" />
            <el-table-column prop="tasksCompleted" label="完成任务" width="100" />
            <el-table-column prop="joinedAt" label="加入时间" />
          </el-table>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showDetailDialog = false">关闭</el-button>
          <el-button 
            v-if="selectedGroup && !isGroupMember(selectedGroup.id)" 
            type="primary" 
            @click="joinGroup(selectedGroup)"
          >
            加入小组
          </el-button>
          <el-button 
            v-if="selectedGroup && isGroupMember(selectedGroup.id)" 
            type="danger" 
            @click="leaveGroup(selectedGroup.id)"
          >
            退出小组
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 加入私密小组对话框 -->
    <el-dialog v-model="showJoinDialog" title="加入私密小组" width="400px">
      <el-form :model="joinForm" ref="joinFormRef">
        <el-form-item label="邀请码" prop="inviteCode">
          <el-input v-model="joinForm.inviteCode" placeholder="请输入邀请码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showJoinDialog = false">取消</el-button>
          <el-button type="primary" @click="joinByInviteCode">加入</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, User, BookOpen, Target } from '@element-plus/icons-vue'
import axios from 'axios'

export default {
  name: 'StudyGroups',
  components: {
    Plus,
    Search,
    User,
    BookOpen,
    Target
  },
  setup() {
    const groups = ref([])
    const groupMembers = ref([])
    const myGroups = ref([])
    const currentPage = ref(1)
    const pageSize = ref(6)
    const total = ref(0)
    const searchKeyword = ref('')
    const selectedSubject = ref('')
    const viewMode = ref('public')
    const showCreateDialog = ref(false)
    const showDetailDialog = ref(false)
    const showJoinDialog = ref(false)
    const selectedGroup = ref(null)
    const currentUserId = ref(1) // 假设当前用户ID为1

    const newGroup = reactive({
      name: '',
      description: '',
      subject: '',
      studyGoal: '',
      maxMembers: 10,
      isPublic: true,
      creatorId: 1
    })

    const joinForm = reactive({
      inviteCode: ''
    })

    const groupRules = {
      name: [{ required: true, message: '请输入小组名称', trigger: 'blur' }],
      description: [{ required: true, message: '请输入小组描述', trigger: 'blur' }],
      subject: [{ required: true, message: '请选择学科', trigger: 'change' }],
      studyGoal: [{ required: true, message: '请输入学习目标', trigger: 'blur' }]
    }

    // 加载小组列表
    const loadGroups = async () => {
      try {
        let url = '/api/study-groups'
        const params = {
          page: currentPage.value - 1,
          size: pageSize.value
        }

        if (viewMode.value === 'public') {
          url += '/public'
        } else if (viewMode.value === 'my') {
          url += `/user/${currentUserId.value}`
        } else if (viewMode.value === 'popular') {
          url += '/popular'
        }

        const response = await axios.get(url, { params })
        if (response.data.success) {
          groups.value = response.data.data
          total.value = response.data.totalElements
        }
      } catch (error) {
        ElMessage.error('加载小组列表失败')
      }
    }

    // 加载我的小组
    const loadMyGroups = async () => {
      try {
        const response = await axios.get(`/api/study-groups/user/${currentUserId.value}`)
        if (response.data.success) {
          myGroups.value = response.data.data
        }
      } catch (error) {
        console.error('加载我的小组失败', error)
      }
    }

    // 搜索小组
    const searchGroups = async () => {
      if (!searchKeyword.value.trim()) {
        loadGroups()
        return
      }
      
      try {
        const response = await axios.get('/api/study-groups/search', {
          params: {
            keyword: searchKeyword.value,
            page: currentPage.value - 1,
            size: pageSize.value
          }
        })
        if (response.data.success) {
          groups.value = response.data.data
          total.value = response.data.totalElements
        }
      } catch (error) {
        ElMessage.error('搜索失败')
      }
    }

    // 按学科筛选
    const filterGroups = async () => {
      if (!selectedSubject.value) {
        loadGroups()
        return
      }
      
      try {
        const response = await axios.get(`/api/study-groups/subject/${selectedSubject.value}`, {
          params: {
            page: currentPage.value - 1,
            size: pageSize.value
          }
        })
        if (response.data.success) {
          groups.value = response.data.data
          total.value = response.data.totalElements
        }
      } catch (error) {
        ElMessage.error('筛选失败')
      }
    }

    // 创建小组
    const createGroup = async () => {
      try {
        const response = await axios.post('/api/study-groups', newGroup)
        if (response.data.success) {
          ElMessage.success('小组创建成功')
          showCreateDialog.value = false
          Object.assign(newGroup, {
            name: '',
            description: '',
            subject: '',
            studyGoal: '',
            maxMembers: 10,
            isPublic: true,
            creatorId: 1
          })
          loadGroups()
          loadMyGroups()
        }
      } catch (error) {
        ElMessage.error('创建失败')
      }
    }

    // 加入小组
    const joinGroup = async (group) => {
      if (!group.isPublic) {
        showJoinDialog.value = true
        return
      }
      
      try {
        const response = await axios.post(`/api/study-groups/${group.id}/join`, null, {
          params: { userId: currentUserId.value }
        })
        if (response.data.success) {
          ElMessage.success('加入小组成功')
          loadGroups()
          loadMyGroups()
        }
      } catch (error) {
        ElMessage.error('加入失败')
      }
    }

    // 通过邀请码加入
    const joinByInviteCode = async () => {
      try {
        const response = await axios.post('/api/study-groups/join-by-code', null, {
          params: {
            inviteCode: joinForm.inviteCode,
            userId: currentUserId.value
          }
        })
        if (response.data.success) {
          ElMessage.success('加入小组成功')
          showJoinDialog.value = false
          joinForm.inviteCode = ''
          loadGroups()
          loadMyGroups()
        }
      } catch (error) {
        ElMessage.error('加入失败，请检查邀请码')
      }
    }

    // 退出小组
    const leaveGroup = async (groupId) => {
      try {
        await ElMessageBox.confirm('确定要退出这个小组吗？', '确认', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        const response = await axios.post(`/api/study-groups/${groupId}/leave`, null, {
          params: { userId: currentUserId.value }
        })
        if (response.data.success) {
          ElMessage.success('退出小组成功')
          showDetailDialog.value = false
          loadGroups()
          loadMyGroups()
        }
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('退出失败')
        }
      }
    }

    // 查看小组详情
    const viewGroupDetail = async (group) => {
      selectedGroup.value = group
      showDetailDialog.value = true
      
      // 加载小组成员
      try {
        const response = await axios.get(`/api/study-groups/${group.id}/members`)
        if (response.data.success) {
          groupMembers.value = response.data.data
        }
      } catch (error) {
        console.error('加载成员失败', error)
      }
    }

    // 检查是否是小组成员
    const isGroupMember = (groupId) => {
      return myGroups.value.some(group => group.id === groupId)
    }

    // 分页处理
    const handleSizeChange = (val) => {
      pageSize.value = val
      loadGroups()
    }

    const handleCurrentChange = (val) => {
      currentPage.value = val
      loadGroups()
    }

    onMounted(() => {
      loadGroups()
      loadMyGroups()
    })

    return {
      groups,
      groupMembers,
      myGroups,
      currentPage,
      pageSize,
      total,
      searchKeyword,
      selectedSubject,
      viewMode,
      showCreateDialog,
      showDetailDialog,
      showJoinDialog,
      selectedGroup,
      newGroup,
      joinForm,
      groupRules,
      loadGroups,
      searchGroups,
      filterGroups,
      createGroup,
      joinGroup,
      joinByInviteCode,
      leaveGroup,
      viewGroupDetail,
      isGroupMember,
      handleSizeChange,
      handleCurrentChange
    }
  }
}
</script>

<style scoped>
.study-groups {
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

.groups-grid {
  margin-bottom: 20px;
}

.group-card {
  margin-bottom: 20px;
  cursor: pointer;
  transition: transform 0.2s;
}

.group-card:hover {
  transform: translateY(-2px);
}

.group-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.group-header h3 {
  margin: 0;
  font-size: 18px;
}

.group-description {
  color: #666;
  margin-bottom: 15px;
  line-height: 1.5;
}

.group-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 15px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #666;
}

.group-actions {
  display: flex;
  gap: 10px;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.group-detail {
  padding: 10px 0;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.detail-tags {
  display: flex;
  gap: 10px;
}

.members-section {
  margin-top: 20px;
}

.members-section h4 {
  margin-bottom: 15px;
}
</style>