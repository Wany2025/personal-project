<template>
  <div class="publish-page">
    <div class="page-header">
      <h2>{{ isEdit ? '编辑商品' : '发布商品' }}</h2>
    </div>

    <el-card shadow="never" v-loading="loading">
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        size="large"
      >
        <el-form-item label="商品标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入商品标题" maxlength="100" show-word-limit />
        </el-form-item>

        <el-form-item label="商品分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%">
            <el-option
              v-for="cat in productStore.categories"
              :key="cat.id"
              :label="cat.categoryName"
              :value="cat.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="价格" prop="price">
          <el-input-number
            v-model="form.price"
            :min="0.01"
            :precision="2"
            :step="1"
            placeholder="请输入价格"
            controls-position="right"
            style="width: 220px"
          />
          <span class="form-tip">元</span>
        </el-form-item>

        <el-form-item label="原价" prop="originalPrice">
          <el-input-number
            v-model="form.originalPrice"
            :min="0"
            :precision="2"
            :step="1"
            placeholder="选填"
            controls-position="right"
            style="width: 220px"
          />
          <span class="form-tip">元（选填）</span>
        </el-form-item>

        <el-form-item label="成色" prop="condition">
          <el-radio-group v-model="form.condition">
            <el-radio label="全新">全新</el-radio>
            <el-radio label="九成新">九成新</el-radio>
            <el-radio label="八成新">八成新</el-radio>
            <el-radio label="七成新">七成新</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="商品图片" prop="images">
          <div class="image-upload">
            <div
              v-for="(img, index) in form.images"
              :key="index"
              class="image-item"
            >
              <el-image :src="img" fit="cover" class="preview" />
              <span class="remove-btn" @click="removeImage(index)">
                <el-icon><Close /></el-icon>
              </span>
            </div>
            <el-upload
              v-if="form.images.length < 6"
              class="upload-trigger"
              :show-file-list="false"
              :before-upload="handleUpload"
              accept="image/*"
            >
              <div class="upload-content">
                <el-icon class="upload-icon"><Plus /></el-icon>
                <span class="upload-text">添加图片</span>
              </div>
            </el-upload>
          </div>
          <div class="form-tip">最多上传6张图片，建议尺寸1:1</div>
        </el-form-item>

        <el-form-item label="商品描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="6"
            placeholder="详细描述商品的使用情况、瑕疵等信息"
            maxlength="1000"
            show-word-limit
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">
            {{ isEdit ? '保存修改' : '发布' }}
          </el-button>
          <el-button @click="router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { productApi } from '@/api/product'
import { useProductStore } from '@/stores/product'
import { ElMessage } from 'element-plus'
import { Plus, Close } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const productStore = useProductStore()

const formRef = ref()
const loading = ref(false)
const submitting = ref(false)

const isEdit = computed(() => !!route.query.id)

const form = reactive({
  title: '',
  categoryId: null,
  price: null,
  originalPrice: null,
  condition: '',
  images: [],
  description: ''
})

const rules = {
  title: [{ required: true, message: '请输入商品标题', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  condition: [{ required: true, message: '请选择成色', trigger: 'change' }],
  description: [{ required: true, message: '请输入商品描述', trigger: 'blur' }]
}

// 图片转 Base64 上传
const handleUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isImage) {
    ElMessage.error('只能上传图片格式')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB')
    return false
  }
  const reader = new FileReader()
  reader.onload = (e) => {
    form.images.push(e.target.result)
  }
  reader.readAsDataURL(file)
  return false
}

const removeImage = (index) => {
  form.images.splice(index, 1)
}

const loadProduct = async () => {
  if (!isEdit.value) return
  loading.value = true
  try {
    const res = await productApi.getProductDetail(route.query.id)
    const data = res.data
    form.title = data.title
    form.categoryId = data.categoryId
    form.price = data.price
    form.originalPrice = data.originalPrice
    form.condition = data.condition
    form.description = data.description
    form.images = data.images ? data.images.split(',').filter(Boolean) : []
  } finally {
    loading.value = false
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      const payload = {
        ...form,
        price: Number(form.price),
        originalPrice: form.originalPrice ? Number(form.originalPrice) : null,
        images: form.images
      }
      if (isEdit.value) {
        await productApi.updateProduct(route.query.id, payload)
        ElMessage.success('修改成功')
        router.push(`/product/${route.query.id}`)
      } else {
        const res = await productApi.publishProduct(payload)
        ElMessage.success('发布成功')
        router.push(`/product/${res.data}`)
      }
    } finally {
      submitting.value = false
    }
  })
}

onMounted(async () => {
  if (!productStore.categories.length) {
    await productStore.getCategories()
  }
  await loadProduct()
})
</script>

<style scoped>
.publish-page {
  max-width: 800px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  font-size: 20px;
  color: #303133;
  margin: 0;
}

.form-tip {
  color: #909399;
  font-size: 13px;
  margin-left: 8px;
}

.image-upload {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.image-item {
  position: relative;
  width: 120px;
  height: 120px;
  border-radius: 6px;
  overflow: hidden;
  border: 1px solid #ebeef5;
}

.preview {
  width: 100%;
  height: 100%;
}

.remove-btn {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.5);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.upload-trigger {
  width: 120px;
  height: 120px;
}

.upload-trigger :deep(.el-upload) {
  width: 100%;
  height: 100%;
}

.upload-content {
  width: 120px;
  height: 120px;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #909399;
  cursor: pointer;
  transition: border-color 0.2s;
}

.upload-content:hover {
  border-color: #409eff;
  color: #409eff;
}

.upload-icon {
  font-size: 28px;
}

.upload-text {
  font-size: 12px;
  margin-top: 6px;
}
</style>
