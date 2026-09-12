<template>
  <div class="home-page">
    <!-- 搜索区 -->
    <div class="search-section">
      <el-input
        v-model="query.keyword"
        placeholder="搜索商品标题或描述"
        size="large"
        clearable
        @keyup.enter="handleSearch"
      >
        <template #append>
          <el-button :icon="Search" @click="handleSearch">搜索</el-button>
        </template>
      </el-input>
    </div>

    <!-- 分类筛选 -->
    <div class="filter-section">
      <div class="filter-row">
        <span class="filter-label">分类：</span>
        <el-radio-group v-model="query.categoryId" @change="loadProducts">
          <el-radio-button :label="null">全部</el-radio-button>
          <el-radio-button
            v-for="cat in productStore.categories"
            :key="cat.id"
            :label="cat.id"
          >
            {{ cat.categoryName }}
          </el-radio-button>
        </el-radio-group>
      </div>

      <div class="filter-row">
        <span class="filter-label">成色：</span>
        <el-radio-group v-model="query.condition" @change="loadProducts">
          <el-radio-button label="">全部</el-radio-button>
          <el-radio-button label="全新">全新</el-radio-button>
          <el-radio-button label="九成新">九成新</el-radio-button>
          <el-radio-button label="八成新">八成新</el-radio-button>
          <el-radio-button label="七成新">七成新</el-radio-button>
        </el-radio-group>
      </div>

      <div class="filter-row">
        <span class="filter-label">排序：</span>
        <el-radio-group v-model="sortField" @change="handleSortChange">
          <el-radio-button label="default">默认</el-radio-button>
          <el-radio-button label="price">价格</el-radio-button>
          <el-radio-button label="viewCount">浏览</el-radio-button>
          <el-radio-button label="favoriteCount">收藏</el-radio-button>
        </el-radio-group>
        <el-radio-group v-model="query.sortOrder" class="sort-order" @change="loadProducts" v-if="sortField !== 'default'">
          <el-radio-button label="asc">升序</el-radio-button>
          <el-radio-button label="desc">降序</el-radio-button>
        </el-radio-group>
      </div>
    </div>

    <!-- 商品列表 -->
    <div v-loading="loading" class="product-grid">
      <template v-if="products.length">
        <div
          v-for="product in products"
          :key="product.id"
          class="product-card"
          @click="goDetail(product.id)"
        >
          <div class="product-image">
            <el-image :src="getFirstImage(product.images)" fit="cover" lazy>
              <template #error>
                <div class="image-placeholder">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
            <span class="status-tag" :class="product.status" v-if="product.status !== 'on_sale'">
              {{ statusText(product.status) }}
            </span>
          </div>
          <div class="product-info">
            <h3 class="product-title">{{ product.title }}</h3>
            <div class="product-price">
              <span class="price">¥{{ product.price }}</span>
              <span class="original-price" v-if="product.originalPrice">
                ¥{{ product.originalPrice }}
              </span>
            </div>
            <div class="product-meta">
              <el-tag size="small" type="info">{{ product.condition }}</el-tag>
              <span class="meta-item"><el-icon><View /></el-icon>{{ product.viewCount }}</span>
              <span class="meta-item"><el-icon><Star /></el-icon>{{ product.favoriteCount }}</span>
            </div>
            <div class="seller-info">
              <el-avatar :size="20" :src="product.sellerAvatar">
                {{ product.sellerName?.charAt(0) }}
              </el-avatar>
              <span class="seller-name">{{ product.sellerName }}</span>
              <span class="category-name">{{ product.categoryName }}</span>
            </div>
          </div>
        </div>
      </template>
      <el-empty v-else-if="!loading" description="暂无商品" />
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
        @current-change="loadProducts"
        @size-change="loadProducts"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { productApi } from '@/api/product'
import { useProductStore } from '@/stores/product'
import { Search, Picture, View, Star } from '@element-plus/icons-vue'

const router = useRouter()
const productStore = useProductStore()

const loading = ref(false)
const products = ref([])
const total = ref(0)

const query = reactive({
  categoryId: null,
  keyword: '',
  condition: '',
  sortBy: '',
  sortOrder: 'desc',
  current: 1,
  size: 10
})

const sortField = ref('default')

const getFirstImage = (images) => {
  if (!images) return ''
  const list = images.split(',')
  return list[0] || ''
}

const statusText = (status) => {
  const map = { sold: '已售出', off_shelf: '已下架' }
  return map[status] || status
}

const loadProducts = async () => {
  loading.value = true
  try {
    const params = { ...query }
    if (!params.categoryId) delete params.categoryId
    if (!params.keyword) delete params.keyword
    if (!params.condition) delete params.condition
    if (!params.sortBy) delete params.sortBy
    const res = await productApi.getProductList(params)
    products.value = res.data.records || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  query.current = 1
  loadProducts()
}

const handleSortChange = () => {
  query.sortBy = sortField.value === 'default' ? '' : sortField.value
  query.current = 1
  loadProducts()
}

const goDetail = (id) => {
  router.push(`/product/${id}`)
}

onMounted(async () => {
  await productStore.getCategories()
  await loadProducts()
})
</script>

<style scoped>
.home-page {
  padding-bottom: 20px;
}

.search-section {
  max-width: 600px;
  margin: 0 auto 24px;
}

.filter-section {
  background: #fff;
  border-radius: 8px;
  padding: 16px 20px;
  margin-bottom: 20px;
}

.filter-row {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.filter-row:last-child {
  margin-bottom: 0;
}

.filter-label {
  color: #606266;
  font-size: 14px;
  width: 60px;
  flex-shrink: 0;
}

.sort-order {
  margin-left: 12px;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  min-height: 200px;
}

.product-card {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.product-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.12);
}

.product-image {
  position: relative;
  width: 100%;
  height: 180px;
  background: #f5f7fa;
}

.product-image .el-image {
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
  font-size: 40px;
}

.status-tag {
  position: absolute;
  top: 8px;
  left: 8px;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  color: #fff;
  background: #909399;
}

.status-tag.sold {
  background: #f56c6c;
}

.status-tag.off_shelf {
  background: #909399;
}

.product-info {
  padding: 12px;
}

.product-title {
  font-size: 14px;
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
  font-size: 18px;
  font-weight: 600;
  color: #f56c6c;
}

.original-price {
  font-size: 12px;
  color: #c0c4cc;
  text-decoration: line-through;
  margin-left: 8px;
}

.product-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #909399;
  margin-bottom: 8px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 2px;
}

.seller-info {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #606266;
  border-top: 1px solid #f0f0f0;
  padding-top: 8px;
}

.seller-name {
  color: #606266;
}

.category-name {
  margin-left: auto;
  color: #909399;
}

.pagination {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}

@media (max-width: 992px) {
  .product-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 768px) {
  .product-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
