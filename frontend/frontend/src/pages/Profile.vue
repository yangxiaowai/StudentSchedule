<template>
  <div class="profile-container">
    <div class="profile-content">
      <div class="profile-header">
        <div class="avatar">
          {{ userStore.userInfo?.username?.charAt(0)?.toUpperCase() || 'U' }}
        </div>
        <h2>个人信息</h2>
      </div>
      
      <!-- 错误提示 -->
      <div v-if="error" class="error-message">
        {{ error }}
      </div>
      
      <!-- 成功提示 -->
      <div v-if="success" class="success-message">
        {{ success }}
      </div>
      
      <!-- 加载状态 -->
      <div v-if="loading" class="loading">
        <div class="spinner"></div>
        <p>加载中...</p>
      </div>
      
      <!-- 用户信息显示 -->
      <div v-else-if="userStore.userInfo" class="profile-info">
        <div class="info-section">
          <h3>基本信息</h3>
          <div class="info-grid">
            <div class="info-item">
              <label>用户名:</label>
              <span>{{ userStore.userInfo.username }}</span>
            </div>
            <div class="info-item">
              <label>邮箱:</label>
              <span>{{ userStore.userInfo.email }}</span>
            </div>
            <div class="info-item">
              <label>用户ID:</label>
              <span>{{ userStore.userInfo.id }}</span>
            </div>
            <div class="info-item">
              <label>账户状态:</label>
              <span class="status" :class="{ active: userStore.userInfo.isActive }">
                {{ userStore.userInfo.isActive ? '激活' : '未激活' }}
              </span>
            </div>
            <div class="info-item" v-if="userStore.userInfo.createdAt">
              <label>注册时间:</label>
              <span>{{ formatDate(userStore.userInfo.createdAt) }}</span>
            </div>
            <div class="info-item" v-if="userStore.userInfo.updatedAt">
              <label>最后更新:</label>
              <span>{{ formatDate(userStore.userInfo.updatedAt) }}</span>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 操作按钮 -->
      <div class="profile-actions">
        <button @click="refreshUserInfo" class="refresh-btn" :disabled="refreshing">
          {{ refreshing ? '刷新中...' : '刷新信息' }}
        </button>
        <button @click="showChangePasswordModal = true" class="change-password-btn">
          修改密码
        </button>
        <button @click="handleLogout" class="logout-btn">
          退出登录
        </button>
      </div>
    </div>
    
    <!-- 修改密码模态框 -->
    <div v-if="showChangePasswordModal" class="modal-overlay" @click="closeModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>修改密码</h3>
          <button @click="closeModal" class="close-btn">×</button>
        </div>
        
        <div class="modal-body">
          <div v-if="passwordError" class="error-message">
            {{ passwordError }}
          </div>
          
          <form @submit.prevent="handleChangePassword">
            <div class="form-group">
              <label for="currentPassword">当前密码:</label>
              <input
                type="password"
                id="currentPassword"
                v-model="passwordForm.currentPassword"
                :disabled="passwordLoading"
                placeholder="请输入当前密码"
                required
              />
            </div>
            
            <div class="form-group">
              <label for="newPassword">新密码:</label>
              <input
                type="password"
                id="newPassword"
                v-model="passwordForm.newPassword"
                :disabled="passwordLoading"
                placeholder="请输入新密码"
                required
              />
              <small class="form-hint">密码长度至少6个字符</small>
            </div>
            
            <div class="form-group">
              <label for="confirmNewPassword">确认新密码:</label>
              <input
                type="password"
                id="confirmNewPassword"
                v-model="passwordForm.confirmNewPassword"
                :disabled="passwordLoading"
                placeholder="请再次输入新密码"
                required
              />
            </div>
            
            <div class="modal-actions">
              <button type="button" @click="closeModal" class="cancel-btn">
                取消
              </button>
              <button type="submit" class="confirm-btn" :disabled="passwordLoading">
                {{ passwordLoading ? '修改中...' : '确认修改' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { userStore } from '../store/user.js'

export default {
  name: 'Profile',
  setup() {
    const router = useRouter()
    
    const loading = ref(false)
    const refreshing = ref(false)
    const error = ref('')
    const success = ref('')
    
    // 修改密码相关
    const showChangePasswordModal = ref(false)
    const passwordLoading = ref(false)
    const passwordError = ref('')
    const passwordForm = reactive({
      currentPassword: '',
      newPassword: '',
      confirmNewPassword: ''
    })
    
    // 格式化日期
    const formatDate = (dateString) => {
      if (!dateString) return '未知'
      const date = new Date(dateString)
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    }
    
    // 刷新用户信息
    const refreshUserInfo = async () => {
      refreshing.value = true
      error.value = ''
      
      try {
        await userStore.refreshUserInfo()
        success.value = '用户信息已刷新'
        
        // 3秒后清除成功提示
        setTimeout(() => {
          success.value = ''
        }, 3000)
      } catch (err) {
        error.value = '刷新用户信息失败'
        console.error('刷新用户信息错误:', err)
      } finally {
        refreshing.value = false
      }
    }
    
    // 关闭模态框
    const closeModal = () => {
      showChangePasswordModal.value = false
      passwordError.value = ''
      passwordForm.currentPassword = ''
      passwordForm.newPassword = ''
      passwordForm.confirmNewPassword = ''
    }
    
    // 验证密码表单
    const validatePasswordForm = () => {
      passwordError.value = ''
      
      if (!passwordForm.currentPassword) {
        passwordError.value = '请输入当前密码'
        return false
      }
      
      if (!passwordForm.newPassword) {
        passwordError.value = '请输入新密码'
        return false
      }
      
      if (passwordForm.newPassword.length < 6) {
        passwordError.value = '新密码长度至少6个字符'
        return false
      }
      
      if (!passwordForm.confirmNewPassword) {
        passwordError.value = '请确认新密码'
        return false
      }
      
      if (passwordForm.newPassword !== passwordForm.confirmNewPassword) {
        passwordError.value = '两次输入的新密码不一致'
        return false
      }
      
      if (passwordForm.currentPassword === passwordForm.newPassword) {
        passwordError.value = '新密码不能与当前密码相同'
        return false
      }
      
      return true
    }
    
    // 修改密码
    const handleChangePassword = async () => {
      if (!validatePasswordForm()) {
        return
      }
      
      passwordLoading.value = true
      
      try {
        // 这里应该调用修改密码的API
        // const result = await changePassword(passwordForm)
        
        // 模拟API调用
        await new Promise(resolve => setTimeout(resolve, 1000))
        
        success.value = '密码修改成功'
        closeModal()
        
        // 3秒后清除成功提示
        setTimeout(() => {
          success.value = ''
        }, 3000)
      } catch (err) {
        passwordError.value = '密码修改失败，请稍后重试'
        console.error('修改密码错误:', err)
      } finally {
        passwordLoading.value = false
      }
    }
    
    // 退出登录
    const handleLogout = () => {
      if (confirm('确定要退出登录吗？')) {
        userStore.logout()
        router.push('/login')
      }
    }
    
    // 组件挂载时检查用户信息
    onMounted(() => {
      if (!userStore.userInfo) {
        refreshUserInfo()
      }
    })
    
    return {
      userStore,
      loading,
      refreshing,
      error,
      success,
      showChangePasswordModal,
      passwordLoading,
      passwordError,
      passwordForm,
      formatDate,
      refreshUserInfo,
      closeModal,
      handleChangePassword,
      handleLogout
    }
  }
}
</script>

<style scoped>
.profile-container {
  max-width: 800px;
  margin: 2rem auto;
  padding: 0 1rem;
}

.profile-content {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.profile-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 2rem;
  text-align: center;
}

.avatar {
  width: 80px;
  height: 80px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2rem;
  font-weight: bold;
  margin: 0 auto 1rem;
  border: 3px solid rgba(255, 255, 255, 0.3);
}

.profile-header h2 {
  margin: 0;
  font-size: 1.8rem;
  font-weight: 600;
}

.error-message {
  background-color: #fee;
  color: #c33;
  padding: 0.75rem 1.5rem;
  border-left: 4px solid #c33;
  margin: 1rem 1.5rem;
}

.success-message {
  background-color: #efe;
  color: #3c763d;
  padding: 0.75rem 1.5rem;
  border-left: 4px solid #3c763d;
  margin: 1rem 1.5rem;
}

.loading {
  text-align: center;
  padding: 3rem 1.5rem;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 1rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.profile-info {
  padding: 1.5rem;
}

.info-section h3 {
  color: #333;
  margin-bottom: 1rem;
  font-size: 1.2rem;
  font-weight: 600;
  border-bottom: 2px solid #f0f0f0;
  padding-bottom: 0.5rem;
}

.info-grid {
  display: grid;
  gap: 1rem;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
}

.info-item {
  display: flex;
  align-items: center;
  padding: 1rem;
  background: #f8f9fa;
  border-radius: 8px;
  border-left: 4px solid #667eea;
}

.info-item label {
  font-weight: 600;
  color: #555;
  min-width: 100px;
  margin-right: 1rem;
}

.info-item span {
  color: #333;
  flex: 1;
}

.status {
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: 600;
  background: #dc3545;
  color: white;
}

.status.active {
  background: #28a745;
}

.profile-actions {
  padding: 1.5rem;
  background: #f8f9fa;
  display: flex;
  gap: 1rem;
  justify-content: center;
  flex-wrap: wrap;
}

.refresh-btn,
.change-password-btn,
.logout-btn {
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.refresh-btn {
  background: linear-gradient(135deg, #00b894 0%, #00cec9 100%);
  color: white;
}

.refresh-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(0, 184, 148, 0.4);
}

.change-password-btn {
  background: linear-gradient(135deg, #fdcb6e 0%, #e17055 100%);
  color: white;
}

.change-password-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(253, 203, 110, 0.4);
}

.logout-btn {
  background: linear-gradient(135deg, #d63031 0%, #e84393 100%);
  color: white;
}

.logout-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(214, 48, 49, 0.4);
}

.refresh-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

/* 模态框样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 12px;
  width: 90%;
  max-width: 500px;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid #eee;
}

.modal-header h3 {
  margin: 0;
  color: #333;
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.5rem;
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

.modal-body {
  padding: 1.5rem;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  color: #555;
  font-weight: 500;
}

.form-group input {
  width: 100%;
  padding: 0.875rem;
  border: 2px solid #e1e5e9;
  border-radius: 8px;
  font-size: 1rem;
  transition: border-color 0.3s ease;
  box-sizing: border-box;
}

.form-group input:focus {
  outline: none;
  border-color: #667eea;
}

.form-group input:disabled {
  background-color: #f8f9fa;
  cursor: not-allowed;
}

.form-hint {
  display: block;
  margin-top: 0.25rem;
  color: #6c757d;
  font-size: 0.8rem;
}

.modal-actions {
  display: flex;
  gap: 1rem;
  justify-content: flex-end;
  margin-top: 2rem;
}

.cancel-btn,
.confirm-btn {
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.cancel-btn {
  background: #6c757d;
  color: white;
}

.cancel-btn:hover {
  background: #5a6268;
}

.confirm-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.confirm-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(102, 126, 234, 0.4);
}

.confirm-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}
</style>