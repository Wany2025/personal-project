<template>
  <div class="my-products-page">
    <div class="page-header">
      <h2>我的发布</h2>
      <el-button type="primary" :icon="Plus" @click="router.push('/publish')">发布商品</el-button>
    </div>

    <el-table :data="products" v-loading="loading" style="background: #fff; border-radius: 8px">
      <el-table-column label="商品" min-width="280">
        <template #default="{ row }">
          <div class="product-cell">
            <el-image :src="getFirstImage(row.images)" fit="cover" class="thumb">
              <template #error>
                <div class="thumb-placeholder"><el-icon><Picture /></el-icon></div>
              </template>
            </el-image>
            <div class="product-text">
              <div class="title" @click="router.push(`/product/${row.id}`)">{{ row.title }}</div>
              <div class="cat">{{ row.categoryName }} · {{ row.condition }}</div>
            </div>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="价格" width="120">
        <template #default="{ row }">
          <span class="price">¥{{ row.price }}</span>
        </template>
      </el-table-column>

      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>

      <el-table-column label="浏览/收藏" width="110">
        <template #default="{ row }">
          <span>{{ row.viewCount }} / {{ row.favoriteCount }}</span>
        </template>
      </el-table-column>

      <el-table-column label="发布时间" width="170">
        <template #default="{ row }">
          {{ formatTime(row.createTime) }}
        </template>
      </el-table-column>

      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="router.push(`/product/${row.id}`)">查看</el-button>
          <el-button
            size="small"
            type="primary"
            :disabled="row.status === 'sold'"
            @click="router.push(`/publish?id=${row.id}`)"
          >编辑</el-button>
          <el-dropdown @command="(cmd) => handleCommand(cmd, row)" trigger="click">
            <el-button size="small">更多<el-icon><ArrowDown /></el-icon></el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item
                  command="shelf"
                  v-if="row.status === 'on_sale'"
                >下架</el-dropdown-item>
                <el-dropdown-item
                  command="shelf"
                  v-else-if="row.status === 'off_shelf'"
                >上架</el-dropdown-item>
                <el-dropdown-item command="delete" divided>删除</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination" v-if="total > 0">
      <el-pagination
        v-model:current-page="query.current"
        :page-size="query.size"
        :total="total"
        layout="total, prev, pager, next"
        background
        @current-change="loadProducts"
      />
    </div>

    <el-empty v-if="!loading && !products.length" description="还没有发布过商品">
      <el-button type="primary" @click="router.push('/publish')">去发布</el-button>
    </el-empty>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { productApi } from '@/api/product'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Picture, ArrowDown } from '@element-plus/icons-vue'

const router = useRouter()

const loading = ref(false)
const products = ref([])
const total = ref(0)

const query = reactive({
  current: 1,
  size: 10
})

const getFirstImage = (images) => {
  if (!images) return ''
  return images.split(',')[0] || ''
}

const statusText = (status) => {
  const map = { on_sale: '在售中', sold: '已售出', off_shelf: '已下架' }
  return map[status] || status
}

const statusType = (status) => {
  const map = { on_sale: 'success', sold: 'danger', off_shelf: 'info' }
  return map[status] || 'info'
}

const formatTime = (time) => {
  if (!time) return ''
  return time.replace('T', ' ').substring(0, 16)
}

const loadProducts = async () => {
  loading.value = true
  try {
    const res = await productApi.getMyProducts(query)
    products.value = res.data.records || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

const handleCommand = async (cmd, row) => {
  if (cmd === 'shelf') {
    const next = row.status === 'on_sale' ? 'off_shelf' : 'on_sale'
    await productApi.changeProductStatus(row.id, next)
    ElMessage.success(next === 'on_sale' ? '上架成功' : '下架成功')
    loadProducts()
  } else if (cmd === 'delete') {
    ElMessageBox.confirm('确定删除该商品吗？', '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    }).then(async () => {
      await productApi.deleteProduct(row.id)
      ElMessage.success('删除成功')
      loadProducts()
    }).catch(() => {})
  }
}

onMounted(loadProducts)
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  font-size: 20px;
  color: #303133;
  margin: 0;
}

.product-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.thumb {
  width: 60px;
  height: 60px;
  border-radius: 4px;
  flex-shrink: 0;
  background: #f5f7fa;
}

.thumb-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #c0c4cc;
  font-size: 24px;
}

.product-text {
  flex: 1;
}

.title {
  font-size: 14px;
  color: #303133;
  cursor: pointer;
}

.title:hover {
  color: #409eff;
}

.cat {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.price {
  color: #f56c6c;
  font-weight: 600;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>
