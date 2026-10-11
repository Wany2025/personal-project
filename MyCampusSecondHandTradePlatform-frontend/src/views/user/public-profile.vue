<template>
  <div class="public-profile-page" v-loading="loading">
    <el-button :icon="ArrowLeft" text @click="router.back()">返回</el-button>

    <el-card shadow="never" v-if="user" class="profile-card">
      <div class="profile-top">
        <el-avatar :size="80" :src="user.avatar">
          {{ user.realName?.charAt(0) }}
        </el-avatar>
        <div class="profile-info">
          <h2>{{ user.realName }}</h2>
          <p class="username">@{{ user.username }}</p>
          <el-rate
            v-model="creditLevel"
            disabled
            :max="5"
            :colors="['#f56c6c', '#e6a23c', '#67c23a']"
          />
          <span class="credit-score">信用分 {{ user.creditScore }}</span>
        </div>
      </div>

      <el-divider />

      <el-descriptions :column="2" border>
        <el-descriptions-item label="学院">{{ user.college }}</el-descriptions-item>
        <el-descriptions-item label="宿舍">{{ user.dorm }}</el-descriptions-item>
        <el-descriptions-item label="注册时间">{{ formatTime(user.createTime) }}</el-descriptions-item>
      </el-descriptions>

      <div class="actions">
        <el-button type="primary" :icon="ChatDotRound" @click="handleContact">联系对方</el-button>
      </div>
    </el-card>

    <el-empty v-else-if="!loading" description="用户不存在" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { userApi } from '@/api/user'
import { ElMessage } from 'element-plus'
import { ArrowLeft, ChatDotRound } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const user = ref(null)

const creditLevel = computed(() => {
  const s = user.value?.creditScore || 0
  if (s >= 90) return 5
  if (s >= 70) return 4
  if (s >= 60) return 3
  if (s >= 40) return 2
  return 1
})

const formatTime = (time) => {
  if (!time) return ''
  return time.replace('T', ' ').substring(0, 16)
}

const loadUser = async () => {
  loading.value = true
  try {
    const res = await userApi.getUserProfile(route.params.id)
    user.value = res.data
  } finally {
    loading.value = false
  }
}

const handleContact = () => {
  ElMessage.info('聊天功能开发中')
}

watch(() => route.params.id, loadUser)

onMounted(loadUser)
</script>

<style scoped>
.public-profile-page {
  max-width: 700px;
  margin: 0 auto;
}

.profile-card {
  margin-top: 12px;
}

.profile-top {
  display: flex;
  align-items: center;
  gap: 20px;
}

.profile-info h2 {
  font-size: 22px;
  color: #303133;
  margin: 0 0 4px;
}

.username {
  color: #909399;
  font-size: 14px;
  margin: 0 0 8px;
}

.credit-score {
  margin-left: 12px;
  color: #e6a23c;
  font-weight: 600;
  vertical-align: middle;
}

.actions {
  margin-top: 24px;
  text-align: center;
}
</style>
