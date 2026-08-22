<template>
  <el-dialog v-model="open" title="赛证互通配置信息" width="1000px" :show-close="!disabled" :close-on-click-modal="!disabled">
    <div class="config-content">
      <el-form label-position="top" :model="form" ref="formRef" :rules="rules">
        <el-row :gutter="24">
          <el-col :span="24">
            <el-form-item label="规则名称：" prop="rulerName">
              <el-input v-model="form.rulerName" placeholder="请输入规则名称" clearable :disabled="readonly" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="赛证互通封面：" prop="icon">
              <el-upload :action="uploadFileUrl" :file-list="uploadFileList" :limit="1"
                :on-success="handleUploadSuccess" :on-preview="handlePreview" :on-remove="handleRemove"
                :show-file-list="true" :headers="headers" class="upload-file-uploader" list-type="picture-card"
                ref="upload" :disabled="readonly">
                <el-icon>
                  <Plus />
                </el-icon>
              </el-upload>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="申请条件说明：" prop="applyDesc">
              <Editor v-model="form.applyDesc" :min-height="300" :readOnly="readonly" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="源证书兑换关系：" prop="certConditions" label-position="left">
              <el-switch v-model="form.certConditions" inline-prompt active-text="且" inactive-text="或" active-value="1"
                inactive-value="2" style="--el-switch-off-color: #E6A23C" :disabled="readonly" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="是否首页显示：" prop="isTope" label-position="left">
              <el-radio-group v-model="form.isTope" :disabled="readonly">
                <el-radio v-for="dict in ruler_top" :key="dict.value" :label="dict.label"
                  :value="dict.value"></el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="源证书配置：">
              <el-table style="border: 1px solid #ebeef5;width: 100%;" :data="originCertList" stripe>
                <el-table-column prop="certConfigName" label="配置名称" min-width="100" show-overflow-tooltip
                  align="center" />
                <el-table-column label="赛事名称" prop="name" min-width="180" show-overflow-tooltip>
                  <template #default="scope">
                    {{ `${scope.row.competitionName ?? '-'}` }}
                  </template>
                </el-table-column>
                <el-table-column label="赛道/组别" prop="name" min-width="180" show-overflow-tooltip>
                  <template #default="scope">
                    {{ `${scope.row.competitionTrackName ?? ''}-${scope.row.secondLevelName ?? ''}` }}
                  </template>
                </el-table-column>
                <el-table-column prop="competitionSeriesName" label="届数" width="100" show-overflow-tooltip
                  align="center" />
                <el-table-column prop="competitionStageName" label="阶段" min-width="100" show-overflow-tooltip
                  align="center" />
                <el-table-column prop="awardsName" label="奖项类型" width="100" show-overflow-tooltip align="center">
                  <template #default="{ row }">
                    {{awards_name.find(item => item.value === row.awardsName)?.label ?? '-'}}
                  </template>
                </el-table-column>
                <el-table-column prop="ownYear" label="拥有年限" width="120" align="center" />
                <el-table-column prop="originCertScore" :label="`源证书分值`" width="100" align="center" />
                <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="50"
                  fixed="right" v-if="originCertList.length > 1">
                  <template #default="scope">
                    <el-tooltip effect="dark" content="删除" placement="top">
                      <el-button link type="danger" @click="handleDelete('source', scope.row)" icon="Delete" />
                    </el-tooltip>
                  </template>
                </el-table-column>
              </el-table>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="目标证书：">
              <el-table style="border: 1px solid #ebeef5;width: 100%;" :data="targetCertList" stripe>
                <el-table-column prop="certConfigName" label="配置名称" min-width="100" show-overflow-tooltip
                  align="center" />
                <el-table-column label="赛事名称" prop="name" min-width="180" show-overflow-tooltip>
                  <template #default="scope">
                    {{ `${scope.row.competitionName ?? '-'}` }}
                  </template>
                </el-table-column>
                <el-table-column label="赛道/组别" prop="name" min-width="180" show-overflow-tooltip>
                  <template #default="scope">
                    {{ `${scope.row.competitionTrackName ?? ''}-${scope.row.secondLevelName ?? ''}` }}
                  </template>
                </el-table-column>
                <el-table-column prop="competitionSeriesName" label="届数" width="100" show-overflow-tooltip
                  align="center" />
                <el-table-column prop="competitionStageName" label="阶段" min-width="100" show-overflow-tooltip
                  align="center" />
                <el-table-column prop="awardsName" label="奖项类型" width="100" show-overflow-tooltip align="center">
                  <template #default="{ row }">
                    {{awards_name.find(item => item.value === row.awardsName)?.label ?? '-'}}
                  </template>
                </el-table-column>
                <el-table-column prop="targetCertScore" :label="`目标证书分值`" width="130" align="center" />
                <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="50"
                  fixed="right" v-if="targetCertList.length > 1">
                  <template #default="scope">
                    <el-tooltip effect="dark" content="删除" placement="top">
                      <el-button link type="danger" @click="handleDelete('target', scope.row)" icon="Delete" />
                    </el-tooltip>
                  </template>
                </el-table-column>
              </el-table>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </div>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="open = false" :disabled="disabled">取消</el-button>
        <el-button v-hasPermi="['competition:competitionCertExchangeRule:save']" type="primary" @click="handleSubmit"
          :disabled="disabled" :loading="disabled" v-if="!readonly">暂
          存</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
  // ******** 插件 ********
  const { proxy } = getCurrentInstance();

  // ****** 工具 ******
  import { getToken } from "@/utils/auth"
  import { replaceFileOrigin } from "@/utils/fileOrigin"
  import modal from "@/plugins/modal";

  // ******* API ********
  import { addCertExchangeRule, updateCertExchangeRule } from "@/api/certInterconnect/interconnectConfig.js"

  // ****** props ******
  const props = defineProps({
    modelValue: {
      type: Boolean,
      default: false
    },
    readonly: {
      type: Boolean,
      default: false
    },
    data: {
      type: Object,
      default: () => ({})
    }
  })

  // ****** emit ******
  const emit = defineEmits(['update:modelValue', 'update:data', 'confirm'])

  // ******* 字典 ********
  const {
    cert_conditions,
    ruler_top,
    awards_name,
  } = proxy.useDict(
    "cert_conditions",
    "ruler_top",
    "awards_name",
  );

  // ****** computed ******
  // open计算属性，双向绑定父组件传入的modelValue
  const open = computed({
    get: () => props.modelValue,
    set: (val) => emit('update:modelValue', val)
  })
  // sourceData计算属性，直接使用父组件传入的数据
  const originCertList = computed({
    get: () => props.data.originCertList ?? [],
    set: (originCertList) => emit('update:data', Object.assign(props.data, { originCertList }))
  })
  // targetData计算属性，直接使用父组件传入的数据
  const targetCertList = computed({
    get: () => props.data.targetCertList ?? [],
    set: (targetCertList) => emit('update:data', Object.assign(props.data, { targetCertList }))
  })
  // readonly计算属性，直接使用父组件传入的readonly值
  const readonly = computed(() => props.readonly)

  // ****** 初始化 ******
  // 表单数据
  const form = reactive({
    rulerName: '',
    icon: undefined,
    applyDesc: '',
    isTope: '0',
    certConditions: '1',
    rulerStatus: '0'
  })
  const rules = reactive({
    rulerName: [{ required: true, message: '请输入规则名称', trigger: 'blur' }],
    icon: [{ required: true, message: '请上传规则封面图', trigger: ['blur', 'change'] }],
    applyDesc: [{ required: true, message: '请输入申请条件说明', trigger: ['blur', 'change'] }],
    certConditions: [{ required: true, message: '请选择证书关系', trigger: ['blur', 'change'] }],
  })
  // 表单ref
  const formRef = ref()
  // 是否禁用表单，默认为false
  const disabled = ref(false)
  // 文件上传
  const uploadFileList = ref([])
  const uploadFileUrl = ref(import.meta.env.VITE_APP_BASE_API + "/file/upload"); // 上传附件的服务器地址
  const headers = ref({ Authorization: "Bearer " + getToken() });

  //******* watch ************/
  watch(() => props.modelValue, (val) => {
    if (val) {
      const { ruleId, rulerName, applyDesc, icon = undefined, isTope = '0', certConditions = '1', rulerStatus = '0' } = props.data
      uploadFileList.value = icon ? [{ url: icon }] : []
      Object.assign(form, {
        ...(ruleId && { ruleId }),
        rulerName,
        applyDesc,
        icon,
        isTope,
        certConditions,
        rulerStatus
      })
    }

  }, { immediate: true })

  // ****** 业务 ******
  // 处理删除逻辑，例如从sourceData或targetData中移除该行数据
  const handleDelete = (activeTab, row) => {
    if (activeTab === 'source') {
      if (originCertList.value.length <= 1) return; // 避免删除最后一行导致表格异常
      originCertList.value = originCertList.value.filter(item => item.certConfigId !== row.certConfigId);
    } else if (activeTab === 'target') {
      if (targetCertList.value.length <= 1) return; // 避免删除最后一行导致表格异常
      targetCertList.value = targetCertList.value.filter(item => item.certConfigId !== row.certConfigId);
    }
  }
  // 处理表单提交逻辑，例如发送请求保存数据
  const handleSubmit = async () => {
    try {
      formRef.value?.validate(async (valid) => {
        if (!valid) return;
        disabled.value = true
        const params = {
          ...form,
          originCertList: originCertList.value.map(item => ({ certConfigId: item.certConfigId, certConfigName: item.certConfigName, originCertScore: item.originCertScore, competitionSeriesId: item.competitionSeriesId, competitionTrackId: item.competitionTrackId, secondLevelCode: item.secondLevelCode, ownYear: item.ownYear })),
          targetCertList: targetCertList.value.map(item => ({ certConfigId: item.certConfigId, certConfigName: item.certConfigName, targetCertScore: item.targetCertScore, competitionSeriesId: item.competitionSeriesId, competitionTrackId: item.competitionTrackId, secondLevelCode: item.secondLevelCode })),
        }
        if (form.ruleId) {
          const { code } = await updateCertExchangeRule(params)
          if (code !== 200) return;
          modal.msgSuccess('证书互通规则修改成功')
        } else {
          const { code } = await addCertExchangeRule(params)
          if (code !== 200) return;
          modal.msgSuccess('证书互通规则配置成功')
        }
        setTimeout(() => {
          disabled.value = false;
          open.value = false;
          emit('confirm')
        }, 500)
      })

    } catch (error) { }

  }
  //  文件上传成功
  const handleUploadSuccess = (res) => {
    res = replaceFileOrigin(res)
    uploadFileList.value = []
    uploadFileList.value.push({ url: res.data.url })
    Object.assign(form, { icon: res.data.url })
  }
  // 文件预览
  const handlePreview = (file) => {
    window.open(file.url, '_blank')
  }
  // 文件删除
  const handleRemove = (file) => {
    uploadFileList.value = []
    form.icon = undefined
  }
</script>

<style lang="scss" scoped></style>
