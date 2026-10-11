<template>
  <div class="layout">
    <!-- 顶部导航 -->
    <header class="header">
      <div class="header-inner">
        <div class="logo" @click="router.push('/')">
          <el-icon class="logo-icon"><Shop /></el-icon>
          <span class="logo-text">校园二手交易平台</span>
        </div>

        <nav class="nav-menu">
          <router-link to="/" class="nav-item">首页</router-link>
          <router-link to="/my-products" class="nav-item" v-if="userStore.isLoggedIn">我的发布</router-link>
          <router-link to="/favorites" class="nav-item" v-if="userStore.isLoggedIn">收藏</router-link>
          <router-link to="/chat" class="nav-item" v-if="userStore.isLoggedIn">
            消息
            <span class="unread-badge" v-if="unreadCount > 0">
              {{ unreadCount > 99 ? '99+' : unreadCount }}
            </span>
          </router-link>
          <router-link to="/profile" class="nav-item" v-if="userStore.isLoggedIn">个人中心</router-link>
        </nav>

        <div class="header-right">
          <el-button type="primary" :icon="Plus" @click="handlePublish" v-if="userStore.isLoggedIn">发布商品</el-button>
          <template v-if="userStore.isLoggedIn">
            <el-dropdown @command="handleCommand">
              <span class="user-info">
                <el-avatar :size="32" :src="userStore.userInfo.avatar">
                  {{ userStore.userInfo.realName?.charAt(0) }}
                </el-avatar>
                <span class="username">{{ userStore.userInfo.realName || userStore.userInfo.username }}</span>
                <el-icon><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                  <el-dropdown-item command="myProducts">我的发布</el-dropdown-item>
                  <el-dropdown-item command="favorites">我的收藏</el-dropdown-item>
                  <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button @click="router.push('/login')">登录</el-button>
            <el-button type="primary" @click="router.push('/register')">注册</el-button>
          </template>
        </div>
      </div>
    </header>

    <!-- 主内容区 -->
    <main class="main-content">
      <router-view />
    </main>

    <!-- 底部 -->
    <footer class="footer">
      <p>校园二手交易平台 © 2024 仅供学习交流使用</p>
    </footer>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessageBox, ElMessage } from 'element-plus'
import { Shop, Plus, ArrowDown } from '@element-plus/icons-vue'
import { chatApi } from '@/api/chat'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const unreadCount = ref(0)
let unreadTimer = null

const loadUnreadCount = async () => {
  if (!userStore.isLoggedIn) return
  try {
    const res = await chatApi.getUnreadCount()
    unreadCount.value = res.data || 0
  } catch (e) {
    // 忽略
  }
}

// 监听登录状态变化，登录后开始轮询
watch(
  () => userStore.isLoggedIn,
  (val) => {
    if (val) {
      loadUnreadCount()
      unreadTimer = setInterval(loadUnreadCount, 30000)
    } else {
      unreadCount.value = 0
      if (unreadTimer) {
        clearInterval(unreadTimer)
        unreadTimer = null
      }
    }
  }
)

// 路由变化时刷新未读数（从聊天页返回时立即更新）
watch(() => route.path, loadUnreadCount)

onMounted(() => {
  if (userStore.isLoggedIn) {
    loadUnreadCount()
    unreadTimer = setInterval(loadUnreadCount, 30000)
  }
})

onBeforeUnmount(() => {
  if (unreadTimer) {
    clearInterval(unreadTimer)
    unreadTimer = null
  }
})

const handlePublish = () => {
  router.push('/publish')
}

const handleCommand = (command) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'myProducts':
      router.push('/my-products')
      break
    case 'favorites':
      router.push('/favorites')
      break
    case 'logout':
      ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        userStore.logout()
        ElMessage.success('已退出登录')
        router.push('/')
      }).catch(() => {})
      break
  }
}
</script>

<style scoped>
.layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.header-inner {
  max-width: 1200px;
  margin: 0 auto;
  height: 64px;
  display: flex;
  align-items: center;
  padding: 0 20px;
  gap: 32px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  white-space: nowrap;
}

.logo-icon {
  font-size: 28px;
  color: #409eff;
}

.logo-text {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.nav-menu {
  display: flex;
  gap: 24px;
  flex: 1;
}

.nav-item {
  color: #606266;
  text-decoration: none;
  font-size: 15px;
  padding: 6px 4px;
  transition: color 0.2s;
}

.nav-item:hover,
.nav-item.router-link-active {
  color: #409eff;
}

.unread-badge {
  display: inline-block;
  min-width: 16px;
  height: 16px;
  padding: 0 5px;
  margin-left: 4px;
  border-radius: 8px;
  background: #f56c6c;
  color: #fff;
  font-size: 11px;
  line-height: 16px;
  text-align: center;
  vertical-align: middle;
  box-sizing: border-box;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  outline: none;
}

.username {
  font-size: 14px;
  color: #303133;
}

.main-content {
  flex: 1;
  max-width: 1200px;
  width: 100%;
  margin: 0 auto;
  padding: 20px;
}

.footer {
  text-align: center;
  padding: 20px;
  color: #909399;
  font-size: 13px;
  background: #fff;
  border-top: 1px solid #ebeef5;
}
</style>
