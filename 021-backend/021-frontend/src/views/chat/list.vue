<template>
  <div class="chat-list-page">
    <div class="page-header">
      <h2>消息</h2>
    </div>

    <div v-loading="loading" class="session-list">
      <template v-if="list.length">
        <div
          v-for="item in list"
          :key="`${item.targetUserId}-${item.productId}`"
          class="session-card"
          @click="goChat(item)"
        >
          <div class="avatar-wrap">
            <el-avatar :size="48" :src="item.targetAvatar">
              {{ item.targetName?.charAt(0) }}
            </el-avatar>
            <span class="unread-badge" v-if="item.unreadCount > 0">
              {{ item.unreadCount > 99 ? '99+' : item.unreadCount }}
            </span>
          </div>
          <div class="session-info">
            <div class="info-top">
              <span class="target-name">{{ item.targetName }}</span>
              <span class="last-time">{{ formatTime(item.lastTime) }}</span>
            </div>
            <div class="info-bottom">
              <span class="product-tag">[商品] {{ item.productTitle }}</span>
              <span class="last-msg">{{ item.lastMessage }}</span>
            </div>
          </div>
        </div>
      </template>
      <el-empty v-else-if="!loading" description="暂无消息">
        <el-button type="primary" @click="router.push('/')">去逛逛</el-button>
      </el-empty>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { chatApi } from '@/api/chat'

const router = useRouter()

const loading = ref(false)
const list = ref([])
let timer = null

const formatTime = (time) => {
  if (!time) return ''
  const t = time.replace('T', ' ').substring(0, 16)
  // 简化展示：今天的只显示时分
  const today = new Date().toISOString().substring(0, 10)
  if (time.startsWith(today)) {
    return t.substring(11)
  }
  return t.substring(5, 16)
}

const loadList = async () => {
  // 轮询时不显示loading
  if (list.value.length === 0) loading.value = true
  try {
    const res = await chatApi.getSessionList()
    list.value = res.data || []
  } finally {
    loading.value = false
  }
}

const goChat = (item) => {
  router.push(`/chat/${item.targetUserId}/${item.productId}`)
}

onMounted(() => {
  loadList()
  // 5秒轮询
  timer = setInterval(loadList, 5000)
})

onBeforeUnmount(() => {
  if (timer) {
    clearInterval(timer)
    timer = null
  }
})
</script>

<style scoped>
.chat-list-page {
  padding-bottom: 20px;
}

.page-header {
  margin-bottom: 20px;
}
.page-header h2 {
  font-size: 20px;
  color: #303133;
  margin: 0;
}

.session-list {
  background: #fff;
  border-radius: 8px;
  min-height: 300px;
  overflow: hidden;
}

.session-card {
  display: flex;
  gap: 12px;
  padding: 16px 20px;
  cursor: pointer;
  border-bottom: 1px solid #f0f0f0;
  transition: background 0.2s;
}

.session-card:last-child {
  border-bottom: none;
}

.session-card:hover {
  background: #f5f7fa;
}

.avatar-wrap {
  position: relative;
  flex-shrink: 0;
}

.unread-badge {
  position: absolute;
  top: -4px;
  right: -4px;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  border-radius: 9px;
  background: #f56c6c;
  color: #fff;
  font-size: 11px;
  line-height: 18px;
  text-align: center;
  box-sizing: border-box;
}

.session-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.info-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.target-name {
  font-size: 15px;
  font-weight: 500;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.last-time {
  font-size: 12px;
  color: #c0c4cc;
  flex-shrink: 0;
  margin-left: 8px;
}

.info-bottom {
  display: flex;
  gap: 8px;
  font-size: 13px;
  color: #909399;
  overflow: hidden;
}

.product-tag {
  color: #409eff;
  flex-shrink: 0;
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.last-msg {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
