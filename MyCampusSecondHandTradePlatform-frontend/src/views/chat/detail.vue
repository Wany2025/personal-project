<template>
  <div class="chat-detail-page">
    <!-- 顶部：返回 + 商品信息 + 对方信息 -->
    <div class="chat-header">
      <el-button :icon="ArrowLeft" text @click="router.back()">返回</el-button>
      <div class="header-center">
        <span class="target-name">{{ targetName || '对方' }}</span>
        <span class="product-info" v-if="productId">
          · 关于商品 #{{ productId }}
        </span>
      </div>
      <el-button text :icon="View" @click="goProduct" v-if="productId">
        查看商品
      </el-button>
    </div>

    <!-- 消息列表 -->
    <div ref="msgBoxRef" class="msg-box" v-loading="loading">
      <template v-if="messages.length">
        <div
          v-for="msg in messages"
          :key="msg.id"
          class="msg-row"
          :class="{ 'mine': msg.senderId === currentUserId }"
        >
          <el-avatar :size="36" :src="msg.senderAvatar">
            {{ msg.senderName?.charAt(0) }}
          </el-avatar>
          <div class="msg-content">
            <div class="msg-meta">
              <span class="sender">{{ msg.senderName }}</span>
              <span class="time">{{ formatTime(msg.createTime) }}</span>
            </div>
            <div class="msg-bubble">{{ msg.content }}</div>
          </div>
        </div>
      </template>
      <el-empty v-else-if="!loading" description="还没有消息，发个招呼吧" />
    </div>

    <!-- 输入区 -->
    <div class="input-box">
      <el-input
        v-model="inputText"
        type="textarea"
        :rows="2"
        resize="none"
        placeholder="输入消息，Ctrl + Enter 发送"
        @keydown.ctrl.enter="handleSend"
        maxlength="500"
        show-word-limit
      />
      <el-button
        type="primary"
        :icon="Promotion"
        @click="handleSend"
        :disabled="!inputText.trim()"
      >
        发送
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { chatApi } from '@/api/chat'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { ArrowLeft, View, Promotion } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const currentUserId = userStore.userInfo.id
const targetUserId = Number(route.params.userId)
const productId = Number(route.params.productId)

const loading = ref(false)
const messages = ref([])
const inputText = ref('')
const targetName = ref('')
const msgBoxRef = ref(null)
let timer = null

const formatTime = (time) => {
  if (!time) return ''
  return time.replace('T', ' ').substring(11, 16)
}

const loadMessages = async (silent = false) => {
  if (!silent) loading.value = true
  try {
    const res = await chatApi.getMessages({
      targetUserId,
      productId
    })
    const list = res.data || []
    // 取对方昵称（从第一条非自己的消息里抓）
    if (list.length > 0) {
      const otherMsg = list.find(m => m.senderId !== currentUserId) || list[0]
      targetName.value = otherMsg.senderName
    }
    const wasAtBottom = isAtBottom()
    messages.value = list
    if (wasAtBottom) {
      await nextTick()
      scrollToBottom()
    }
  } finally {
    loading.value = false
  }
}

const isAtBottom = () => {
  const el = msgBoxRef.value
  if (!el) return true
  return el.scrollHeight - el.scrollTop - el.clientHeight < 50
}

const scrollToBottom = () => {
  const el = msgBoxRef.value
  if (el) el.scrollTop = el.scrollHeight
}

const handleSend = async () => {
  const content = inputText.value.trim()
  if (!content) return
  try {
    await chatApi.sendMessage({
      receiverId: targetUserId,
      productId,
      content
    })
    inputText.value = ''
    await loadMessages(true)
    // 发送后立即标记已读（对方可能刚回了消息）
    await markRead()
  } catch (e) {
    // 拦截器已弹错误提示
  }
}

const markRead = async () => {
  try {
    await chatApi.markRead({ targetUserId, productId })
  } catch (e) {
    // 忽略
  }
}

const goProduct = () => {
  router.push(`/product/${productId}`)
}

onMounted(async () => {
  await loadMessages()
  await markRead()
  // 5秒轮询拉新消息
  timer = setInterval(() => loadMessages(true), 5000)
})

onBeforeUnmount(() => {
  if (timer) {
    clearInterval(timer)
    timer = null
  }
})
</script>

<style scoped>
.chat-detail-page {
  background: #fff;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  height: calc(100vh - 64px - 40px - 40px);
  min-height: 500px;
  overflow: hidden;
}

.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-bottom: 1px solid #ebeef5;
  flex-shrink: 0;
}

.header-center {
  flex: 1;
  text-align: center;
  font-size: 15px;
  color: #303133;
}

.target-name {
  font-weight: 500;
}

.product-info {
  color: #909399;
  font-size: 13px;
  margin-left: 6px;
}

.msg-box {
  flex: 1;
  overflow-y: auto;
  padding: 16px 20px;
  background: #f5f7fa;
}

.msg-row {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
  align-items: flex-start;
}

.msg-row.mine {
  flex-direction: row-reverse;
}

.msg-content {
  max-width: 60%;
  display: flex;
  flex-direction: column;
}

.msg-row.mine .msg-content {
  align-items: flex-end;
}

.msg-meta {
  display: flex;
  gap: 8px;
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.msg-row.mine .msg-meta {
  flex-direction: row-reverse;
}

.msg-bubble {
  background: #fff;
  padding: 10px 14px;
  border-radius: 8px;
  font-size: 14px;
  color: #303133;
  line-height: 1.5;
  word-break: break-word;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
}

.msg-row.mine .msg-bubble {
  background: #409eff;
  color: #fff;
}

.input-box {
  display: flex;
  gap: 12px;
  padding: 12px 16px;
  border-top: 1px solid #ebeef5;
  background: #fff;
  flex-shrink: 0;
  align-items: flex-end;
}

.input-box .el-input {
  flex: 1;
}
</style>
