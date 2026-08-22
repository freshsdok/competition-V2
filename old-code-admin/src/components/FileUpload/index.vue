<template>
  <div class="upload-file">
    <el-upload
      multiple
      :http-request="handleHttpRequest"
      :before-upload="handleBeforeUpload"
      :file-list="fileList"
      :data="data"
      :limit="limit"
      :on-exceed="handleExceed"
      :show-file-list="false"
      class="upload-file-uploader"
      ref="fileUpload"
      v-if="!disabled"
    >
      <!-- 上传按钮 -->
      <el-button type="primary">选取文件</el-button>
    </el-upload>
    <!-- 上传提示 -->
    <div class="el-upload__tip" v-if="showTip">
      <slot name="showTip"></slot>
    </div>
    <!-- 文件列表 -->
    <transition-group ref="uploadFileList" class="upload-file-list el-upload-list el-upload-list--text" tag="ul">
      <div :key="file.uid" v-for="(file, index) in fileList">
        <li class="el-upload-list__item ele-upload-list__item-content" >
          <el-link :underline="false" @click="handleDownload(file)">
            <span class="el-icon-document"> {{ getFileName(file.name) }} </span>
          </el-link>
          <div class="ele-upload-list__item-content-action">
            <el-link :underline="false" @click="handleDelete(index)" type="danger" v-if="!disabled">&nbsp;删除</el-link>
          </div>
        </li>
        <el-progress
          v-if="file.percentage && file.percentage != 0 && file.percentage != 100"
          :percentage="file.percentage"
          :status="file.percentage == 100 ? 'success' : null"
          :format="formatProgress"
          style="width: 100%; margin-right: 10px"
        />
      </div>
    </transition-group>
  </div>
</template>

<script setup>
import Sortable from 'sortablejs'
import { ossFileFuc } from "@/hooks/download"
import { ref, computed, watch, onMounted, nextTick } from "vue"
import { ElMessage } from "element-plus"

const props = defineProps({
  modelValue: [String, Object, Array],
  // 上传携带的参数
  data: {
    type: Object
  },
  // 数量限制
  limit: {
    type: Number,
    default: 1
  },
  // 大小限制(MB)
  fileSize: {
    type: Number,
    default: 500
  },
  // 文件类型, 例如['png', 'jpg', 'jpeg']
  fileType: {
    type: Array,
    default: () => []
  },
  // 是否显示提示
  isShowTip: {
    type: Boolean,
    default: true
  },
  // 禁用组件（仅查看文件）
  disabled: {
    type: Boolean,
    default: false
  },
  // 拖动排序
  drag: {
    type: Boolean,
    default: true
  },
  // 阿里云上传配置
  ossConfig: {
    type: Object,
    default: () => ({})
  },
  // 返回值格式：'string' 使用逗号分隔的URL字符串，'array' 返回完整对象数组
  valueType: {
    type: String,
    default: 'string'
  }
})

const emit = defineEmits()
const number = ref(0)
const uploadList = ref([])
const fileList = ref([])
const showTip = computed(
  () => props.isShowTip && (props.fileType || props.fileSize)
)
const uploadFileList = ref(null)
const fileUpload = ref(null)

// 是否有文件正在上传
const isUploading = computed(() => {
  return number.value > 0 || fileList.value.some(file => file.percentage > 0 && file.percentage < 100)
})

// 暴露方法给父组件
defineExpose({
  isUploading
})

// 解构阿里云上传和下载方法
const { uploadOssFile, downloadOssFile } = ossFileFuc();

watch(() => props.modelValue, val => {
  if (val) {
    let temp = 1
    // 首先将值转为数组
    const list = Array.isArray(val) ? val : props.modelValue.split(',')
    // 然后将数组转为对象数组
    fileList.value = list.map(item => {
      if (typeof item === "string") {
        item = { name: item, url: item }
      }
      item.uid = item.uid || new Date().getTime() + temp++
      return item
    })
  } else {
    fileList.value = []
    return []
  }
},{ deep: true, immediate: true })

// 上传前校检格式和大小
function handleBeforeUpload(file) {
  // 校检文件类型
  if (props.fileType.length) {
    const fileName = file.name.split('.')
    const fileExt = fileName[fileName.length - 1]
    const isTypeOk = props.fileType.indexOf(fileExt) >= 0
    if (!isTypeOk) {
      ElMessage.error(`文件格式不正确，请上传${props.fileType.join("/")}格式文件!`)
      return false
    }
  }
  // 校检文件大小
  if (props.fileSize) {
    const isLt = file.size / 1024 / 1024 < props.fileSize
    if (!isLt) {
      ElMessage.error(`上传文件大小不能超过 ${props.fileSize} MB!`)
      return false
    }
  }
  number.value++
  return true
}

// 文件个数超出
function handleExceed() {
  ElMessage.error(`上传文件数量不能超过 ${props.limit} 个!`)
}

// 自定义上传处理
async function handleHttpRequest(config) {
  const fileItem = {
    uid: config.file.uid,
    name: config.file.name,
    size: config.file.size,
    percentage: 0
  }
  
  // 添加到文件列表
  fileList.value.push(fileItem)
  try {
    // 使用阿里云上传
    const res = await uploadOssFile(config.file, props.ossConfig, (progress) => {
      // 更新进度
      const index = fileList.value.findIndex(item => item.uid === config.file.uid)
      if (index !== -1) {
        fileList.value[index].percentage = Math.floor(progress * 100)
      }
    })
    
    // 上传成功
    const index = fileList.value.findIndex(item => item.uid === config.file.uid)
    if (index !== -1) {
      fileList.value[index] = {
        ...res,
        ...fileList.value[index],
        url: res.ossUrl,
        percentage: 100
      }
    }
    
    // 更新绑定值
    emit("update:modelValue", props.valueType === 'array' ? fileList.value : listToString(fileList.value))
    
  } catch (error) {
    // 上传失败
    const index = fileList.value.findIndex(item => item.uid === config.file.uid)
    if (index !== -1) {
      fileList.value.splice(index, 1)
    }
    ElMessage.error("上传文件失败")
    console.error("上传失败:", error)
  } finally {
    number.value--
  }
}

// 进度条格式化
function formatProgress(percentage) {
  return `${percentage}%`
}

// 删除文件
function handleDelete(index) {
  fileList.value.splice(index, 1)
  emit("update:modelValue", props.valueType === 'array' ? fileList.value : listToString(fileList.value))
}

// 获取文件名称
function getFileName(name) {
  // 如果是url那么取最后的名字 如果不是直接返回
  if (name.lastIndexOf("/") > -1) {
    return name.slice(name.lastIndexOf("/") + 1)
  } else {
    return name
  }
}

// 下载文件
function handleDownload(file) {
  if (!file.url) {
    ElMessage.warning('文件链接不存在')
    return
  }
  // 使用 downloadOssFile 方法，通过后端获取临时下载链接
  downloadOssFile(file.url, getFileName(file.name))
}

// 对象转成指定字符串分隔
function listToString(list, separator) {
  let strs = ""
  separator = separator || ","
  for (let i in list) {
    if (list[i].url) {
      strs += list[i].url + separator
    }
  }
  return strs != '' ? strs.substr(0, strs.length - 1) : ''
}

// 初始化拖拽排序
onMounted(() => {
  if (props.drag && !props.disabled) {
    nextTick(() => {
      const element = uploadFileList.value?.$el || uploadFileList.value
      if (element) {
        Sortable.create(element, {
          ghostClass: 'file-upload-darg',
          onEnd: (evt) => {
            const movedItem = fileList.value.splice(evt.oldIndex, 1)[0]
            fileList.value.splice(evt.newIndex, 0, movedItem)
            emit('update:modelValue', props.valueType === 'array' ? fileList.value : listToString(fileList.value))
          }
        })
      }
    })
  }
})
</script>
<style scoped lang="scss">
.file-upload-darg {
  opacity: 0.5;
  background: #c8ebfb;
}
.upload-file-uploader {
  margin-bottom: 5px;
}
.upload-file-list .el-upload-list__item {
  line-height: 2;
  margin-bottom: 10px;
  position: relative;
  transition: none !important;
}
.upload-file-list .ele-upload-list__item-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: inherit;
}
.ele-upload-list__item-content-action .el-link {
  margin-right: 10px;
}
</style>
