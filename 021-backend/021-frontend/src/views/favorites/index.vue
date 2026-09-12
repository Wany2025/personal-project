<template>
  <div class="favorites-page">
    <div class="page-header">
      <h2>我的收藏</h2>
    </div>

    <div v-loading="loading" class="favorite-list">
      <template v-if="list.length">
        <div
          v-for="item in list"
          :key="item.id"
          class="favorite-card"
          @click="goDetail(item.productId)"
        >
          <div class="card-image">
            <el-image :src="getFirstImage(item.images)" fit="cover" lazy>
              <template #error>
                <div class="image-placeholder">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
            <span class="status-tag" :class="item.status" v-if="item.status !== 'on_sale'">
              {{ statusText(item.status) }}
            </span>
          </div>
          <div class="card-info">
            <h3 class="product-title">{{ item.title }}</h3>
            <div class="product-price">
              <span class="price">¥{{ item.price }}</span>
            </div>
            <div class="product-meta">
              <el-tag size="small" type="info">{{ item.condition }}</el-tag>
            </div>
            <div class="seller-info">
              <el-avatar :size="20" :src="item.sellerAvatar">
                {{ item.sellerName?.charAt(0) }}
              </el-avatar>
              <span class="seller-name">{{ item.sellerName }}</span>
              <span class="collect-time">收藏于 {{ formatTime(item.createTime) }}</span>
            </div>
          </div>
          <div class="card-actions" @click.stop>
            <el-button
              size="small"
              :icon="ChatDotRound"
              @click="contactSeller(item)"
              v-if="item.status === 'on_sale'"
            >
              联系卖家
            </el-button>
            <el-button
              size="small"
              type="danger"
              plain
              :icon="Delete"
              @click="handleRemove(item)"
            >
              取消收藏
            </el-button>
          </div>
        </div>
      </template>
      <el-empty v-else-if="!loading" description="还没有收藏任何商品">
        <el-button type="primary" @click="router.push('/')">去逛逛</el-button>
      </el-empty>
    </div>

    <!-- 分页 -->
    <div class="pagination" v-if="total > 0">
      <el-pagination
        v-model:current-page="query.current"
        v-model:page-size="query.size"
        :total="total"
        :page-sizes="[10, 20, 40]"
        layout="total, sizes, prev, pager, next, jumper"
        background
        @current-change="loadList"
        @size-change="loadList"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { favoriteApi } from '@/api/favorite'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Picture, ChatDotRound, Delete } from '@element-plus/icons-vue'

const router = useRouter()

const loading = ref(false)
const list = ref([])
const total = ref(0)

const query = reactive({
  current: 1,
  size: 10
})

const getFirstImage = (images) => {
  if (!images) return ''
  const arr = images.split(',')
  return arr[0] || ''
}

const statusText = (status) => {
  const map = { sold: '已售出', off_shelf: '已下架' }
  return map[status] || status
}

const formatTime = (time) => {
  if (!time) return ''
  return time.replace('T', ' ').substring(0, 16)
}

const loadList = async () => {
  loading.value = true
  try {
    const res = await favoriteApi.getMyFavorites(query)
    list.value = res.data.records || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

const goDetail = (productId) => {
  router.push(`/product/${productId}`)
}

const contactSeller = (item) => {
  router.push(`/chat/${item.sellerId}/${item.productId}`)
}

const handleRemove = (item) => {
  ElMessageBox.confirm(`确定取消收藏「${item.title}」吗？`, '提示', {
    type: 'warning',
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  }).then(async () => {
    await favoriteApi.removeFavorite(item.productId)
    ElMessage.success('已取消收藏')
    // 如果当前页删空了且不是第一页，回到上一页
    if (list.value.length === 1 && query.current > 1) {
      query.current--
    }
    loadList()
  }).catch(() => {})
}

onMounted(loadList)
</script>

<style scoped>
.favorites-page {
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

.favorite-list {
  min-height: 200px;
}

.favorite-card {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 12px;
  display: flex;
  gap: 16px;
  cursor: pointer;
  transition: box-shadow 0.2s;
}

.favorite-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.card-image {
  position: relative;
  width: 120px;
  height: 120px;
  border-radius: 6px;
  overflow: hidden;
  background: #f5f7fa;
  flex-shrink: 0;
}

.card-image .el-image {
  width: 100%;
  height: 100%;
}

.image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #c0c4cc;
  font-size: 32px;
}

.status-tag {
  position: absolute;
  top: 6px;
  left: 6px;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 11px;
  color: #fff;
  background: #909399;
}

.status-tag.sold {
  background: #f56c6c;
}

.card-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.product-title {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  margin: 0 0 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-price {
  margin-bottom: 8px;
}

.price {
  font-size: 20px;
  font-weight: 600;
  color: #f56c6c;
}

.product-meta {
  margin-bottom: 8px;
}

.seller-info {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #606266;
  margin-top: auto;
}

.seller-name {
  color: #606266;
}

.collect-time {
  color: #c0c4cc;
  margin-left: auto;
}

.card-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
  justify-content: center;
  flex-shrink: 0;
}

.pagination {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}

@media (max-width: 768px) {
  .favorite-card {
    flex-direction: column;
  }
  .card-image {
    width: 100%;
    height: 180px;
  }
  .card-actions {
    flex-direction: row;
  }
}
</style>
