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
                <el-icon><Reading /></el-icon>
                <span>{{ group.subject }}</span>
              </div>
              <div class="info-item">
                <el-icon><Aim /></el-icon>
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
    <el-dialog v-model="showCreateDialog" title="创建学习小组" width="500px">
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
        <!-- 所有小组都是公开的，移除小组类型选择 -->
        <el-form-item label="功能设置">
          <div style="display: flex; flex-direction: column; gap: 10px;">
            <el-checkbox v-model="newGroup.taskSharingEnabled">
              开启任务共享功能
              <el-tooltip content="开启后，小组成员可以查看和下载其他成员的任务，但不能修改或删除" placement="top">
                <el-icon style="margin-left: 5px; color: #909399;"><QuestionFilled /></el-icon>
              </el-tooltip>
            </el-checkbox>
            <el-checkbox v-model="newGroup.resourceSharingEnabled">
              开启资料库共享功能
              <el-tooltip content="开启后，小组成员可以查看和下载其他成员的学习资料，但不能修改或删除" placement="top">
                <el-icon style="margin-left: 5px; color: #909399;"><QuestionFilled /></el-icon>
              </el-tooltip>
            </el-checkbox>
          </div>
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
    <el-dialog 
      v-model="showDetailDialog" 
      title="小组详情" 
      width="600px"
      :top="'50vh'"
      :align-center="true"
      draggable
      :close-on-click-modal="false"
    >
      <div v-if="selectedGroup" class="dialog-content">
        <h3>{{ selectedGroup.name }}</h3>
        <p><strong>描述：</strong>{{ selectedGroup.description }}</p>
        <p><strong>学科：</strong>{{ selectedGroup.subject }}</p>
        <p><strong>学习目标：</strong>{{ selectedGroup.studyGoal }}</p>
        <p><strong>成员：</strong>{{ selectedGroup.currentMembers }}/{{ selectedGroup.maxMembers }}</p>
        <p><strong>类型：</strong>公开小组</p>
        <div class="sharing-features">
          <p><strong>功能设置：</strong></p>
          <div style="margin-left: 20px;">
            <p>
              <el-icon style="color: #67c23a;" v-if="selectedGroup.taskSharingEnabled"><Check /></el-icon>
              <el-icon style="color: #f56c6c;" v-else><Close /></el-icon>
              任务共享功能：{{ selectedGroup.taskSharingEnabled ? '已开启' : '未开启' }}
            </p>
            <p>
              <el-icon style="color: #67c23a;" v-if="selectedGroup.resourceSharingEnabled"><Check /></el-icon>
              <el-icon style="color: #f56c6c;" v-else><Close /></el-icon>
              资料库共享功能：{{ selectedGroup.resourceSharingEnabled ? '已开启' : '未开启' }}
            </p>
          </div>
        </div>
        
        <!-- 小组共享功能 -->
        <div v-if="isGroupMember(selectedGroup?.id)" class="sharing-section">
          <h4>小组共享功能</h4>
          <div class="sharing-buttons">
            <el-button 
              v-if="selectedGroup.taskSharingEnabled" 
              type="primary" 
              @click="showMemberSelectionForTasks"
              :icon="Folder"
            >
              查看共享任务
            </el-button>
            <el-button 
              v-if="selectedGroup.resourceSharingEnabled" 
              type="success" 
              @click="showMemberSelectionForMaterials"
              :icon="Folder"
            >
              查看共享资料库
            </el-button>
            <el-text v-if="!selectedGroup.taskSharingEnabled && !selectedGroup.resourceSharingEnabled" type="info">
              该小组未开启任何共享功能
            </el-text>
          </div>
        </div>
        
        <div class="members-section">
          <h4>小组成员</h4>
          <el-table :data="groupMembers" style="width: 100%">
            <el-table-column prop="username" label="用户名" width="120" />
            <el-table-column prop="role" label="角色" width="100">
              <template #default="scope">
                <el-tag :type="(scope.row.role === 'ADMIN' || scope.row.role === 'CREATOR') ? 'danger' : 'primary'">
                  {{ (scope.row.role === 'ADMIN' || scope.row.role === 'CREATOR') ? '管理员' : '成员' }}
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
            v-if="isGroupMember(selectedGroup?.id)" 
            type="danger" 
            :plain="isGroupCreator(selectedGroup)"
            @click="isGroupCreator(selectedGroup) ? deleteGroup(selectedGroup.id) : leaveGroup(selectedGroup.id)"
          >
            <el-icon v-if="isGroupCreator(selectedGroup)"><Delete /></el-icon>
            {{ isGroupCreator(selectedGroup) ? '删除小组' : '退出小组' }}
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 加入小组对话框 -->
    <el-dialog v-model="showJoinDialog" title="加入小组" width="400px">
      <el-form :model="joinForm" label-width="100px">
        <el-form-item label="邀请码">
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

    <!-- 成员选择对话框 -->
    <el-dialog v-model="showMemberSelectionDialog" :title="memberSelectionTitle" width="600px">
      <div class="member-selection">
        <div class="selection-info">
          <el-alert
            title="只读模式"
            type="info"
            description="您将以只读模式查看所选成员的内容，可以预览和下载，但不能修改。"
            :closable="false"
            show-icon
          />
        </div>
        <p class="selection-tip">请选择要查看的成员：</p>
        <el-table 
          :data="filteredGroupMembers" 
          @row-click="selectMember" 
          highlight-current-row
          class="member-table"
        >
          <el-table-column prop="username" label="用户名" width="120" />
          <el-table-column prop="role" label="角色" width="100">
            <template #default="scope">
              <el-tag :type="(scope.row.role === 'ADMIN' || scope.row.role === 'CREATOR') ? 'danger' : 'primary'">
                {{ (scope.row.role === 'ADMIN' || scope.row.role === 'CREATOR') ? '管理员' : '成员' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="contributionScore" label="贡献分" width="100" />
          <el-table-column prop="joinedAt" label="加入时间" />
          <el-table-column label="操作" width="80">
            <template #default="scope">
              <el-button type="text" size="small" @click="selectMember(scope.row)">
                查看
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <div v-if="filteredGroupMembers.length === 0" class="no-members">
          <el-empty description="暂无其他成员" />
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showMemberSelectionDialog = false">取消</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, User, Reading, Aim, QuestionFilled, Check, Close, Folder, Delete } from '@element-plus/icons-vue'
import socialAPI from '../api/social.js'
const { studyGroupAPI } = socialAPI

export default {
  name: 'StudyGroups',
  components: {
    Plus,
    Search,
    User,
    Reading,
    Aim,
    QuestionFilled,
    Check,
    Close,
    Folder,
    Delete
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
    const showMemberSelectionDialog = ref(false)
    const selectedGroup = ref(null)
    const memberSelectionTitle = ref('')
    const memberSelectionType = ref('') // 'tasks' 或 'materials'
    
    const newGroup = reactive({
      name: '',
      description: '',
      subject: '',
      studyGoal: '',
      maxMembers: 10,
      taskSharingEnabled: false,
      resourceSharingEnabled: false
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
    
    // 从localStorage中的userInfo获取当前用户ID
    const getCurrentUserId = () => {
      try {
        const userInfo = localStorage.getItem('userInfo')
        if (userInfo) {
          const user = JSON.parse(userInfo)
          return user.id || user.userId || '1'
        }
      } catch (error) {
        console.error('获取用户ID失败:', error)
      }
      return '1'
    }
    
    const currentUserId = ref(getCurrentUserId())
    
    // 过滤成员列表，排除当前用户
    const filteredGroupMembers = computed(() => {
      return groupMembers.value.filter(member => 
        member.userId.toString() !== currentUserId.value.toString()
      )
    })
    
    // 加载小组列表
    const loadGroups = async () => {
      try {
        let response
        
        if (viewMode.value === 'public') {
          response = await studyGroupAPI.getPublicGroups(currentPage.value - 1, pageSize.value)
        } else if (viewMode.value === 'my') {
          response = await studyGroupAPI.getUserGroups(currentUserId.value)
          // 对于我的小组，需要特殊处理数据格式
          if (response.data.success) {
            const myGroupsData = response.data.data
            // 获取每个小组的详细信息
            const groupPromises = myGroupsData.map(member => 
              studyGroupAPI.getGroupDetails(member.groupId)
            )
            const groupResponses = await Promise.all(groupPromises)
            groups.value = groupResponses
              .filter(res => res.data.success)
              .map(res => res.data.data)
            total.value = groups.value.length
            return
          }

        } else {
          response = await studyGroupAPI.getPublicGroups(currentPage.value - 1, pageSize.value)
        }
        
        if (response.data.success) {
          groups.value = response.data.data
          total.value = response.data.totalElements || response.data.data.length
        }
      } catch (error) {
        console.error('加载小组失败', error)
        ElMessage.error('加载小组失败')
      }
    }
    
    // 加载我的小组
    const loadMyGroups = async () => {
      try {
        console.log('正在加载我的小组，用户ID:', currentUserId.value)
        const response = await studyGroupAPI.getUserGroups(currentUserId.value)
        console.log('getUserGroups API响应:', response.data)
        
        if (response.data.success) {
          myGroups.value = response.data.data
          console.log('成功加载我的小组:', myGroups.value)
          console.log('我的小组数量:', myGroups.value.length)
        } else {
          console.log('加载我的小组失败:', response.data.message)
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
        const response = await studyGroupAPI.searchGroups(searchKeyword.value, currentPage.value - 1, pageSize.value)
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
      // 如果没有选择学科，则加载所有小组
      if (!selectedSubject.value) {
        loadGroups()
        return
      }
      
      try {
        const response = await studyGroupAPI.getGroupsBySubject(selectedSubject.value, currentPage.value - 1, pageSize.value)
        if (response.data.success) {
          groups.value = response.data.data
          total.value = response.data.totalElements
        }
      } catch (error) {
        console.error('筛选失败:', error)
        ElMessage.error('筛选失败')
      }
    }
    
    // 创建小组
    const createGroup = async () => {
      try {
        const response = await studyGroupAPI.createGroup(newGroup)
        if (response.data.success) {
          ElMessage.success('创建成功')
          showCreateDialog.value = false
          Object.assign(newGroup, {
            name: '',
            description: '',
            subject: '',
            studyGoal: '',
            maxMembers: 10,
            taskSharingEnabled: false,
            resourceSharingEnabled: false
          })
          loadGroups()
          loadMyGroups() // 更新用户的小组列表，确保新创建的小组不显示"加入小组"按钮
        }
      } catch (error) {
        ElMessage.error('创建失败')
      }
    }
    
    // 加入小组
    const joinGroup = async (group) => {
      try {
        const response = await studyGroupAPI.joinGroup(group.id)
        if (response.data.success) {
          ElMessage.success('加入成功')
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
        const response = await studyGroupAPI.joinByCode(joinForm.inviteCode)
        if (response.data.success) {
          ElMessage.success('加入成功')
          showJoinDialog.value = false
          joinForm.inviteCode = ''
          loadGroups()
          loadMyGroups()
        }
      } catch (error) {
        ElMessage.error('加入失败')
      }
    }
    
    // 退出小组
    const leaveGroup = async (groupId) => {
      try {
        await ElMessageBox.confirm('确定要退出这个小组吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        const response = await studyGroupAPI.leaveGroup(groupId, currentUserId.value)
        if (response.data.success) {
          ElMessage.success('退出成功')
          showDetailDialog.value = false
          loadGroups()
          loadMyGroups()
        } else {
          ElMessage.error(response.data.message || '退出失败')
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('退出小组失败', error)
          if (error.response && error.response.data && error.response.data.message) {
            ElMessage.error(error.response.data.message)
          } else {
            ElMessage.error('退出失败，请重试')
          }
        }
      }
    }
    
    // 删除小组（仅创建者可用）
    const deleteGroup = async (groupId) => {
      try {
        await ElMessageBox.confirm(
          '确定要删除这个小组吗？删除后将无法恢复，所有成员将被移除。', 
          '删除小组', 
          {
            confirmButtonText: '确定删除',
            cancelButtonText: '取消',
            type: 'error',
            dangerouslyUseHTMLString: true
          }
        )
        
        const response = await studyGroupAPI.disbandGroup(groupId, currentUserId.value)
         if (response.data.success) {
           ElMessage.success('小组删除成功')
           showDetailDialog.value = false
           loadGroups()
           loadMyGroups()
         } else {
           ElMessage.error(response.data.message || '删除失败')
         }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除小组失败', error)
          ElMessage.error('删除失败，请重试')
        }
      }
    }
    
    // 查看小组详情
    const viewGroupDetail = async (group) => {
      try {
        // 获取完整的小组详情信息（包括creatorId）
        console.log('正在获取小组详情，小组ID:', group.id)
        const groupDetailResponse = await studyGroupAPI.getGroupDetails(group.id)
        console.log('小组详情API响应:', groupDetailResponse.data)
        
        if (groupDetailResponse.data.success) {
          selectedGroup.value = groupDetailResponse.data.data
          console.log('设置selectedGroup:', selectedGroup.value)
          console.log('creatorId:', selectedGroup.value.creatorId)
          console.log('currentUserId:', currentUserId.value)
          console.log('isGroupCreator结果:', isGroupCreator(selectedGroup.value))
        } else {
          selectedGroup.value = group
        }
        
        showDetailDialog.value = true
        
        // 获取小组成员信息
        console.log('正在获取小组成员信息，小组ID:', group.id)
        const response = await studyGroupAPI.getGroupMembers(group.id)
        console.log('小组成员API响应:', response.data)
        
        if (response.data.success) {
          groupMembers.value = response.data.data
          console.log('设置groupMembers:', groupMembers.value)
          console.log('成员数量:', groupMembers.value.length)
          if (groupMembers.value.length > 0) {
            console.log('第一个成员的结构:', groupMembers.value[0])
          }
        } else {
          console.error('获取小组成员失败:', response.data.message)
          groupMembers.value = []
        }
      } catch (error) {
        console.error('加载小组详情失败', error)
        // 如果获取详情失败，仍然显示基本信息
        selectedGroup.value = group
        showDetailDialog.value = true
        groupMembers.value = []
      }
    }
    
    // 检查是否是小组成员
    const isGroupMember = (groupId) => {
      console.log('检查是否是小组成员，小组ID:', groupId)
      console.log('当前myGroups:', myGroups.value)
      console.log('myGroups中的小组ID列表:', myGroups.value.map(g => g.groupId))
      
      const isMember = myGroups.value.some(group => group.groupId === groupId)
      console.log('是否是成员:', isMember)
      
      return isMember
    }
    
    // 检查是否是小组创建者
    const isGroupCreator = (group) => {
      if (!group || !group.creatorId) return false
      return group.creatorId.toString() === currentUserId.value.toString()
    }
    
    // 显示成员选择对话框（任务）
    const showMemberSelectionForTasks = () => {
      memberSelectionTitle.value = '选择成员查看任务'
      memberSelectionType.value = 'tasks'
      showMemberSelectionDialog.value = true
    }
    
    // 显示成员选择对话框（资料）
    const showMemberSelectionForMaterials = () => {
      memberSelectionTitle.value = '选择成员查看资料'
      memberSelectionType.value = 'materials'
      showMemberSelectionDialog.value = true
    }
    
    // 选择成员并跳转
    const selectMember = (member) => {
      showMemberSelectionDialog.value = false
      
      if (memberSelectionType.value === 'tasks') {
        // 跳转到任务管理页面，并传递用户ID参数
        window.open(`/task-manager?userId=${member.userId}&readonly=true`, '_blank')
      } else if (memberSelectionType.value === 'materials') {
        // 跳转到资料管理页面，并传递用户ID参数
        window.open(`/data-integration?userId=${member.userId}&readonly=true`, '_blank')
      }
    }
    
    // 查看共享任务（保留原方法作为备用）
    const viewSharedTasks = async () => {
      if (!selectedGroup.value) return
      
      try {
        // 检查用户是否登录
        const token = localStorage.getItem('accessToken')
        if (!token) {
          ElMessage.error('请先登录')
          return
        }
        
        console.log('正在获取小组共享任务，小组ID:', selectedGroup.value.id)
        const response = await studyGroupAPI.getGroupSharedTasks(selectedGroup.value.id)
        console.log('API响应:', response.data)
        
        if (response.data.success) {
          const tasks = response.data.data
          if (tasks.length === 0) {
            ElMessage.info('该小组暂无共享任务')
            return
          }
          
          // 格式化任务信息并显示
          const taskList = tasks.map(task => 
            `• ${task.title} (${task.subject}) - ${task.priority}优先级`
          ).join('\n')
          
          ElMessageBox.alert(
            taskList,
            `${selectedGroup.value.name} - 共享任务 (${tasks.length}个)`,
            {
              confirmButtonText: '确定',
              type: 'info'
            }
          )
        } else {
          ElMessage.error(response.data.message || '获取共享任务失败')
        }
      } catch (error) {
        console.error('获取共享任务失败', error)
        if (error.response) {
          console.error('错误响应:', error.response.data)
          ElMessage.error(error.response.data.message || '获取共享任务失败')
        } else {
          ElMessage.error('网络错误，请检查连接')
        }
      }
    }
    
    // 查看共享资料（保留原方法作为备用）
    const viewSharedMaterials = async () => {
      if (!selectedGroup.value) return
      
      try {
        // 检查用户是否登录
        const token = localStorage.getItem('accessToken')
        if (!token) {
          ElMessage.error('请先登录')
          return
        }
        
        console.log('正在获取小组共享资料，小组ID:', selectedGroup.value.id)
        const response = await studyGroupAPI.getGroupSharedMaterials(selectedGroup.value.id)
        console.log('API响应:', response.data)
        
        if (response.data.success) {
          const materials = response.data.data
          if (materials.length === 0) {
            ElMessage.info('该小组暂无共享资料')
            return
          }
          
          // 格式化资料信息并显示
          const materialList = materials.map(material => 
            `• ${material.title} (${material.subject}) - ${material.type}`
          ).join('\n')
          
          ElMessageBox.alert(
            materialList,
            `${selectedGroup.value.name} - 共享资料 (${materials.length}个)`,
            {
              confirmButtonText: '确定',
              type: 'info'
            }
          )
        } else {
          ElMessage.error(response.data.message || '获取共享资料失败')
        }
      } catch (error) {
        console.error('获取共享资料失败', error)
        if (error.response) {
          console.error('错误响应:', error.response.data)
          ElMessage.error(error.response.data.message || '获取共享资料失败')
        } else {
          ElMessage.error('网络错误，请检查连接')
        }
      }
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
      // 重新获取当前用户ID，确保是最新的
      currentUserId.value = getCurrentUserId()
      console.log('组件挂载时的用户ID:', currentUserId.value)
      console.log('localStorage中的userInfo:', localStorage.getItem('userInfo'))
      
      loadGroups()
      loadMyGroups()
      
      // 添加调试信息
      setTimeout(() => {
        console.log('加载完成后的myGroups:', myGroups.value)
        console.log('myGroups数组长度:', myGroups.value.length)
      }, 2000)
    })
    
    return {
      groups,
      groupMembers,
      filteredGroupMembers,
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
      showMemberSelectionDialog,
      selectedGroup,
      memberSelectionTitle,
      memberSelectionType,
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
      deleteGroup,
      viewGroupDetail,
      isGroupMember,
      isGroupCreator,
      showMemberSelectionForTasks,
      showMemberSelectionForMaterials,
      selectMember,
      viewSharedTasks,
      viewSharedMaterials,
      handleSizeChange,
      handleCurrentChange
    }
  }
}
</script>

<style scoped>
.study-groups {
  padding: 24px;
  background: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%);
  min-height: 100vh;
  width: calc(90vw - 96px);
  margin: 0 14px;
  box-sizing: border-box;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding: 20px 0;
  max-width: 100%;
}

.header h2 {
  font-size: 28px;
  font-weight: 700;
  color: #2d3748;
  margin: 0;
}

.search-section {
  margin-bottom: 30px;
  padding: 20px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
  max-width: 100%;
}

.groups-grid {
  margin-bottom: 30px;
  max-width: 100%;
}

.group-card {
  margin-bottom: 24px;
  cursor: pointer;
  transition: all 0.3s ease;
  border-radius: 16px;
  overflow: hidden;
  border: none;
  background: linear-gradient(145deg, #ffffff 0%, #f8fafc 100%);
}

.group-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 15px 30px rgba(0, 0, 0, 0.1);
}

.group-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #e2e8f0;
}

.group-header h3 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #2d3748;
}

.group-description {
  color: #718096;
  line-height: 1.6;
  margin-bottom: 16px;
}

.group-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 20px;
  padding: 16px;
  background: rgba(102, 126, 234, 0.05);
  border-radius: 12px;
  border-left: 4px solid #667eea;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #4a5568;
  font-size: 14px;
}

.info-item .el-icon {
  color: #667eea;
  font-size: 16px;
}

.group-actions {
  display: flex;
  gap: 12px;
  padding-top: 16px;
  border-top: 1px solid #e2e8f0;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 30px;
  padding: 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  max-width: 100%;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 20px 0;
}

.dialog-content {
  max-height: 60vh;
  overflow-y: auto;
  padding-right: 8px;
}

.dialog-content::-webkit-scrollbar {
  width: 6px;
}

.dialog-content::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.dialog-content::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.dialog-content::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e2e8f0;
}

.detail-tags {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.sharing-actions {
  margin-bottom: 24px;
  padding: 20px;
  background: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%);
  border-radius: 12px;
  border: 1px solid #e2e8f0;
}

.sharing-actions h4 {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 600;
  color: #2d3748;
}

.sharing-actions .el-button {
  height: 44px;
  font-weight: 500;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.sharing-actions .el-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.sharing-section {
  margin-bottom: 24px;
  padding: 20px;
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
  border-radius: 12px;
  border: 1px solid #bae6fd;
}

.sharing-section h4 {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 600;
  color: #0c4a6e;
}

.sharing-buttons {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.sharing-buttons .el-button {
  height: 40px;
  font-weight: 500;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.sharing-buttons .el-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.members-section {
  margin-top: 24px;
}

.members-section h4 {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 600;
  color: #2d3748;
  padding: 20px;
  background: #f8fafc;
  border-radius: 12px;
}

.members-section h4 {
  margin-bottom: 16px;
  color: #2d3748;
  font-weight: 600;
}

/* 成员选择对话框样式 */
.member-selection {
  padding: 8px 0;
}

.selection-info {
  margin-bottom: 20px;
}

.selection-tip {
  margin: 16px 0 12px 0;
  font-size: 14px;
  font-weight: 500;
  color: #374151;
}

.member-table {
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.member-table .el-table__row {
  cursor: pointer;
  transition: all 0.2s ease;
}

.member-table .el-table__row:hover {
  background-color: #f8fafc;
}

.member-table .el-button--text {
  color: #3b82f6;
  font-weight: 500;
}

.member-table .el-button--text:hover {
  color: #1d4ed8;
  background-color: #eff6ff;
}

.no-members {
  margin-top: 20px;
  text-align: center;
  padding: 40px 20px;
  background: #f9fafb;
  border-radius: 8px;
  border: 1px dashed #d1d5db;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.group-card {
  animation: fadeInUp 0.6s ease-out;
}

.group-card:nth-child(1) { animation-delay: 0.1s; }
.group-card:nth-child(2) { animation-delay: 0.2s; }
.group-card:nth-child(3) { animation-delay: 0.3s; }

/* 响应式设计 */
@media (max-width: 768px) {
  .study-groups {
    padding: 16px;
  }
  
  .header {
    flex-direction: column;
    gap: 16px;
    text-align: center;
  }
  
  .search-section {
    padding: 16px;
  }
  
  .group-card {
    margin-bottom: 16px;
  }
  
  .group-actions {
    flex-direction: column;
  }
  
  .group-actions .el-button {
    width: 100%;
  }
}
</style>