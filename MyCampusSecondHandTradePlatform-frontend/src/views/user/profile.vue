<template>
  <div class="profile-page">
    <div class="page-header">
      <h2>个人中心</h2>
    </div>

    <el-card shadow="never" v-loading="loading">
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        size="large"
        :disabled="!editing"
      >
        <el-form-item label="头像">
          <el-upload
            class="avatar-upload"
            :show-file-list="false"
            :before-upload="handleAvatar"
            accept="image/*"
            :disabled="!editing"
          >
            <el-avatar :size="80" :src="form.avatar">
              {{ form.realName?.charAt(0) }}
            </el-avatar>
            <span class="avatar-tip" v-if="editing">点击修改</span>
          </el-upload>
        </el-form-item>

        <el-form-item label="用户名">
          <el-input v-model="form.username" disabled />
        </el-form-item>

        <el-form-item label="学号" prop="studentId">
          <el-input v-model="form.studentId" placeholder="请输入学号" />
        </el-form-item>

        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入真实姓名" />
        </el-form-item>

        <el-form-item label="学院" prop="college">
          <el-input v-model="form.college" placeholder="请输入学院" />
        </el-form-item>

        <el-form-item label="宿舍号" prop="dorm">
          <el-input v-model="form.dorm" placeholder="请输入宿舍号" />
        </el-form-item>

        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>

        <el-form-item label="修改密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="留空表示不修改密码"
            show-password
          />
        </el-form-item>

        <el-form-item label="信用分">
          <el-rate
            v-model="creditLevel"
            disabled
            :max="5"
            :colors="['#f56c6c', '#e6a23c', '#67c23a']"
          />
          <span class="credit-score">{{ form.creditScore }} 分</span>
        </el-form-item>

        <el-form-item label="注册时间">
          <span>{{ formatTime(form.createTime) }}</span>
        </el-form-item>

        <el-form-item>
          <template v-if="!editing">
            <el-button type="primary" :disabled="false" @click="editing = true">编辑资料</el-button>
            <el-button :disabled="false" @click="router.push('/my-products')">我的发布</el-button>
          </template>
          <template v-else>
            <el-button type="primary" :loading="submitting" @click="handleSave">保存</el-button>
            <el-button @click="handleCancel">取消</el-button>
          </template>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref()
const loading = ref(false)
const submitting = ref(false)
const editing = ref(false)

const form = reactive({
  username: '',
  studentId: '',
  realName: '',
  college: '',
  dorm: '',
  phone: '',
  avatar: '',
  creditScore: 100,
  createTime: ''
})

// 备份用于取消
const backup = ref({})

const creditLevel = computed(() => {
  const s = form.creditScore || 0
  if (s >= 90) return 5
  if (s >= 70) return 4
  if (s >= 60) return 3
  if (s >= 40) return 2
  return 1
})

const rules = {
  studentId: [{ required: true, message: '请输入学号', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  college: [{ required: true, message: '请输入学院', trigger: 'blur' }],
  dorm: [{ required: true, message: '请输入宿舍号', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ]
}

const formatTime = (time) => {
  if (!time) return ''
  return time.replace('T', ' ').substring(0, 16)
}

const handleAvatar = (file) => {
  if (!file.type.startsWith('image/')) {
    ElMessage.error('只能上传图片')
    return false
  }
  const reader = new FileReader()
  reader.onload = (e) => {
    form.avatar = e.target.result
  }
  reader.readAsDataURL(file)
  return false
}

const loadInfo = async () => {
  loading.value = true
  try {
    await userStore.getUserInfo()
    Object.assign(form, userStore.userInfo)
    backup.value = { ...form }
  } finally {
    loading.value = false
  }
}

const handleSave = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      await userStore.updateUserInfo({
        username: form.username,
        password: form.password || '',
        studentId: form.studentId,
        realName: form.realName,
        college: form.college,
        dorm: form.dorm,
        phone: form.phone,
        avatar: form.avatar
      })
      ElMessage.success('保存成功')
      editing.value = false
      backup.value = { ...form }
    } finally {
      submitting.value = false
    }
  })
}

const handleCancel = () => {
  Object.assign(form, backup.value)
  editing.value = false
}

onMounted(loadInfo)
</script>

<style scoped>
.profile-page {
  max-width: 700px;
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

.avatar-upload {
  display: inline-block;
  position: relative;
}

.avatar-tip {
  margin-left: 12px;
  color: #409eff;
  font-size: 13px;
  vertical-align: middle;
}

.credit-score {
  margin-left: 12px;
  color: #e6a23c;
  font-weight: 600;
}
</style>
