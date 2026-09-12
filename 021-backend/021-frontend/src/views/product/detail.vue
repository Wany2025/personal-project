<template>
  <div class="detail-page" v-loading="loading">
    <template v-if="product">
      <el-button :icon="ArrowLeft" text @click="router.back()">返回</el-button>

      <div class="detail-content">
        <!-- 左侧：图片 -->
        <div class="image-section">
          <el-carousel
            v-if="imageList.length"
            :autoplay="false"
            indicator-position="outside"
            height="420px"
          >
            <el-carousel-item v-for="(img, index) in imageList" :key="index">
              <el-image :src="img" fit="contain" class="carousel-image" />
            </el-carousel-item>
          </el-carousel>
          <div v-else class="no-image">
            <el-icon><Picture /></el-icon>
            <span>暂无图片</span>
          </div>
        </div>

        <!-- 右侧：信息 -->
        <div class="info-section">
          <h1 class="product-title">{{ product.title }}</h1>

          <div class="price-box">
            <span class="price">¥{{ product.price }}</span>
            <span class="original-price" v-if="product.originalPrice">
              原价 ¥{{ product.originalPrice }}
            </span>
          </div>

          <div class="info-tags">
            <el-tag>{{ product.condition }}</el-tag>
            <el-tag type="info">{{ product.categoryName }}</el-tag>
            <el-tag type="success" v-if="product.status === 'on_sale'">在售中</el-tag>
            <el-tag type="danger" v-else-if="product.status === 'sold'">已售出</el-tag>
            <el-tag type="info" v-else>已下架</el-tag>
          </div>

          <div class="stat-row">
            <span class="stat-item"><el-icon><View /></el-icon>{{ product.viewCount }} 浏览</span>
            <span class="stat-item"><el-icon><Star /></el-icon>{{ product.favoriteCount }} 收藏</span>
            <span class="stat-item"><el-icon><Clock /></el-icon>{{ formatTime(product.createTime) }}</span>
          </div>

          <el-divider />

          <div class="seller-box">
            <el-avatar :size="48" :src="product.sellerAvatar">
              {{ product.sellerName?.charAt(0) }}
            </el-avatar>
            <div class="seller-detail">
              <div class="seller-name">{{ product.sellerName }}</div>
              <div class="seller-meta">
                信用分 {{ product.sellerCreditScore }} · {{ product.sellerCollege }} · {{ product.sellerDorm }}
              </div>
            </div>
            <el-button
              type="primary"
              plain
              @click="goSellerProfile"
            >
              查看卖家
            </el-button>
          </div>

          <!-- 联系 + 收藏 按钮 -->
          <div class="action-box" v-if="userStore.isLoggedIn && product.status === 'on_sale' && !isOwner">
            <el-button type="primary" size="large" :icon="ChatDotRound" @click="handleContact">
              联系卖家
            </el-button>
            <el-button
              size="large"
              :type="isFavorited ? 'warning' : 'default'"
              :icon="isFavorited ? StarFilled : Star"
              :loading="favLoading"
              @click="handleToggleFavorite"
            >
              {{ isFavorited ? '已收藏' : '收藏' }}
            </el-button>
          </div>

          <!-- 编辑按钮（自己商品） -->
          <div class="action-box" v-if="isOwner && product.status !== 'sold'">
            <el-button :icon="Edit" @click="handleEdit">编辑</el-button>
            <el-button :icon="Delete" type="danger" @click="handleDelete">删除</el-button>
            <el-button v-if="product.status === 'on_sale'" @click="handleShelf('off_shelf')">下架</el-button>
            <el-button v-else-if="product.status === 'off_shelf'" type="success" @click="handleShelf('on_sale')">上架</el-button>
          </div>
        </div>
      </div>

      <!-- 商品描述 -->
      <el-card class="desc-card" shadow="never">
        <template #header>
          <span>商品描述</span>
        </template>
        <div class="description">{{ product.description }}</div>
      </el-card>
    </template>

    <el-empty v-else-if="!loading" description="商品不存在" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { productApi } from '@/api/product'
import { favoriteApi } from '@/api/favorite'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowLeft, Picture, View, Star, StarFilled, Clock,
  ChatDotRound, Edit, Delete
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const product = ref(null)
const isFavorited = ref(false)
const favLoading = ref(false)

const imageList = computed(() => {
  if (!product.value || !product.value.images) return []
  return product.value.images.split(',').filter(Boolean)
})

const isOwner = computed(() => {
  return product.value && userStore.userInfo.id === product.value.sellerId
})

const formatTime = (time) => {
  if (!time) return ''
  return time.replace('T', ' ').substring(0, 16)
}

const loadDetail = async () => {
  loading.value = true
  try {
    const res = await productApi.getProductDetail(route.params.id)
    product.value = res.data
    // 已登录且非自己的商品 → 查询收藏状态
    if (userStore.isLoggedIn && product.value && userStore.userInfo.id !== product.value.sellerId) {
      await loadFavoriteStatus()
    }
  } finally {
    loading.value = false
  }
}

const loadFavoriteStatus = async () => {
  try {
    const res = await favoriteApi.checkFavorite(route.params.id)
    isFavorited.value = res.data === true
  } catch (e) {
    // 忽略
  }
}

const goSellerProfile = () => {
  if (!product.value) return
  router.push(`/user/${product.value.sellerId}`)
}

const handleContact = () => {
  // 跳转到与该卖家关于此商品的聊天会话
  router.push(`/chat/${product.value.sellerId}/${product.value.id}`)
}

const handleToggleFavorite = async () => {
  if (!product.value) return
  favLoading.value = true
  try {
    if (isFavorited.value) {
      await favoriteApi.removeFavorite(product.value.id)
      isFavorited.value = false
      // 同步更新界面上的收藏数
      product.value.favoriteCount = Math.max((product.value.favoriteCount || 1) - 1, 0)
      ElMessage.success('已取消收藏')
    } else {
      await favoriteApi.addFavorite(product.value.id)
      isFavorited.value = true
      product.value.favoriteCount = (product.value.favoriteCount || 0) + 1
      ElMessage.success('收藏成功')
    }
  } finally {
    favLoading.value = false
  }
}

const handleEdit = () => {
  router.push(`/publish?id=${product.value.id}`)
}

const handleDelete = () => {
  ElMessageBox.confirm('确定删除该商品吗？', '提示', {
    type: 'warning',
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  }).then(async () => {
    await productApi.deleteProduct(product.value.id)
    ElMessage.success('删除成功')
    router.push('/my-products')
  }).catch(() => {})
}

const handleShelf = async (status) => {
  await productApi.changeProductStatus(product.value.id, status)
  ElMessage.success(status === 'on_sale' ? '上架成功' : '下架成功')
  loadDetail()
}

watch(() => route.params.id, loadDetail)

onMounted(loadDetail)
</script>

<style scoped>
.detail-page {
  min-height: 400px;
}

.detail-content {
  display: flex;
  gap: 32px;
  margin-top: 12px;
  background: #fff;
  border-radius: 8px;
  padding: 24px;
}

.image-section {
  width: 420px;
  flex-shrink: 0;
}

.carousel-image {
  width: 100%;
  height: 420px;
  background: #f5f7fa;
}

.no-image {
  width: 420px;
  height: 420px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  color: #c0c4cc;
  font-size: 48px;
  gap: 12px;
}

.info-section {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.product-title {
  font-size: 22px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 16px;
  line-height: 1.4;
}

.price-box {
  background: #fff8f0;
  padding: 12px 16px;
  border-radius: 6px;
  margin-bottom: 16px;
}

.price {
  font-size: 28px;
  font-weight: 700;
  color: #f56c6c;
}

.original-price {
  font-size: 14px;
  color: #c0c4cc;
  text-decoration: line-through;
  margin-left: 12px;
}

.info-tags {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.stat-row {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: #909399;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.seller-box {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.seller-detail {
  flex: 1;
}

.seller-name {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.seller-meta {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}

.action-box {
  display: flex;
  gap: 12px;
  margin-top: 8px;
}

.desc-card {
  margin-top: 20px;
}

.description {
  white-space: pre-wrap;
  line-height: 1.8;
  color: #606266;
  font-size: 15px;
}

@media (max-width: 992px) {
  .detail-content {
    flex-direction: column;
  }
  .image-section,
  .no-image {
    width: 100%;
  }
}
</style>
