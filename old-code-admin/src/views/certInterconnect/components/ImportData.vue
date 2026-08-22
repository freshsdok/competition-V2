<template>
  <el-dialog v-model="open" :title="title" width="480px" :show-close="!disabled" :close-on-click-modal="!disabled">
    <el-form ref="formRef" :model="form" :label-width="labelWidth" :rules="rules">
      <slot />
      <el-row :gutter="20" style="margin: 0">
        <el-col :span="24">
          <el-form-item label="下载导入模板">
            <el-link :underline="false" @click="handleDownload" type="primary">{{ tempName }}导入模板.{{ defaultSuffix
            }}</el-link>
            <el-link :underline="false" @click="handleDownload" type="primary" style="margin-left: 10px">下载</el-link>
          </el-form-item>
          <el-form-item label="上传附件" style="align-items: start;" prop="file">
            <el-upload ref="uploadRef" v-model:file-list="fileList" action="#" :before-upload="beforeUpload"
              :http-request="httpRequest">
              <el-button type="primary">{{ fileList.length > 0 ? '重新上传' : '点击上传' }}</el-button>
              <template #tip>
                <div class="el-upload__tip">
                  提示：上传文件仅支持{{ suffix.join("、") }}；单个文件不超过{{ convertToMB(size, 'B') }}
                </div>
              </template>
            </el-upload>
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
    <el-table style="border: 1px solid #ebeef5;" :data="errorData" v-if="errorData.length > 0" :show-header="false">
      <el-table-column />
    </el-table>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="open = false" :disabled="disabled">取消</el-button>
        <el-button type="primary" @click="handleImport" :disabled="disabled" :loading="disabled">导入</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
  // ******** 工具***********
  import modal from "@/plugins/modal";

  // ********* props ***********
  const props = defineProps({
    modelValue: {
      type: Boolean,
      default: false,
    },
    title: {
      type: String,
    },
    tempName: {
      type: String,
    },
    tempUrl: {
      type: String,
    },
    defaultSuffix: {
      type: String,
      default: 'xlsx'
    },
    suffix: {
      type: Array,
      default: () => (['xlsx', 'xls'])
    },
    size: {
      type: Number,
      default: 1024 * 1024 * 100
    },
    labelWidth: {
      type: String,
      default: '100px'
    }
  })
  /**
 * 文件大小转换函数 - 统一转换为 MB 展示
 * @param {number|string} size - 文件大小数值（必填）
 * @param {string} unit - 原始单位，默认 B（支持：B/KB/MB/GB/TB）
 * @param {number} decimal - 保留小数位数，默认 2
 * @returns {string} 转换后的大小，带单位 MB，例如：2.56 MB
 */
  function convertToMB(size, unit = 'B', decimal = 2) {
    // 处理非数字/空值
    if (!size || isNaN(Number(size))) {
      return '0 MB';
    }

    const fileSize = Number(size);
    // 单位换算基数（1024进制，标准文件存储换算）
    const base = 1024;
    // 单位对应层级：B=0, KB=1, MB=2, GB=3, TB=4
    const unitLevels = { b: 0, kb: 1, mb: 2, gb: 3, tb: 4 };

    // 统一转为小写，避免大小写问题
    const currentUnit = unit.toLowerCase();
    // 获取当前单位层级
    const currentLevel = unitLevels[currentUnit] || 0;
    // 目标单位 MB 的层级
    const targetLevel = unitLevels.mb;

    // 核心计算：统一转换为 MB
    let mbSize = fileSize * Math.pow(base, currentLevel - targetLevel);
    // 处理极小值（小于0.01MB显示0.00）
    mbSize = mbSize <= 0 ? 0 : mbSize;

    // 保留指定位数小数 + 拼接单位
    return `${mbSize.toFixed(decimal)} MB`;
  }
  // ********* 初始化 ***********
  const formRef = ref()
  const form = reactive({
    file: null,
  })
  const rules = reactive({
    file: [{ required: true, message: '请上传文件', trigger: ['blur', 'change'] }]
  })
  const fileList = ref([])
  const disabled = ref(false)
  const errorData = ref([])
  const uploadRef = ref()

  // ****** emit ******
  const emit = defineEmits(['update:modelValue', 'import'])

  // ********* computed **********
  // open计算属性，双向绑定父组件传入的modelValue
  const open = computed({
    get: () => props.modelValue,
    set: (val) => emit('update:modelValue', val)
  })

  // ********* 业务 **********
  // 下载模板
  const handleDownload = () => {
    downloadJS(import.meta.env.VITE_APP_BASE_API + `/file/excel/download`,
      `${props.tempName}导入模板.${props.defaultSuffix}`,'addName')
  }
  // 上传前钩子
  const beforeUpload = (file) => {
    if (uploadRef.value) {
      uploadRef.value.clearFiles()
    }
    const fileSuffix = file?.name.split(".").pop().toLowerCase();
    if (!props.suffix.includes(fileSuffix)) {
      modal.msgWarning(`上传文件格式不正确，仅支持 ${props.suffix.join("、")} 格式`);
      return false;
    }
    if (file.size > props.size) {
      modal.msgWarning(`文件大小不能超过${convertToMB(props.size, 'B')}`);
      return false
    }
    return true;
  }
  // 上传的文件
  const httpRequest = ({ file }) => {
    fileList.value = [file]
    Object.assign(form, { file })
  }

  // 导入
  const handleImport = () => {
    if (formRef.value) {
      formRef.value.validate((valid) => {
        if (valid) {
          disabled.value = true;
          emit('import', form.file)
        }
      })
    }
  }

  // 导入结果回调
  const callback = (code) => {
    disabled.value = false
    if (code && code === 200) {
      open.value = false;
    }

  }

  // 重置方法 - 清除上传的文件和表单数据
  const reset = () => {
    fileList.value = []
    form.file = null
    errorData.value = []
    if (uploadRef.value) {
      uploadRef.value.clearFiles()
    }
  }

  // 监听弹框打开状态，打开时自动重置
  watch(() => open.value, (newVal) => {
    if (newVal) {
      reset()
    }
  })

  // 监听 fileList 变化，当文件被删除时同步清空 form.file
  watch(() => fileList.value, (newVal) => {
    if (newVal.length === 0) {
      form.file = null
    }
  }, { deep: true })

  // 暴露方法给父组件 ✅
  defineExpose({
    callback,
    reset,
  })
</script>

<style scoped></style>