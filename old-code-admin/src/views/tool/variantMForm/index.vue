<template>
    <div class="vm-form-designer-container">
        <vm-form-designer
            ref="vfDesigner"
            :designer-config="designerConfig">
            <!-- 自定义按钮插槽演示 -->
            <template #customToolButtons v-if="props.form">
              <el-button type="primary" icon="Pointer" @click="handleSave">保存</el-button>
              <el-button type="primary" icon="Close" @click="handleCancel">取消</el-button>
            </template>
        </vm-form-designer>
    </div>
</template>
<script setup name="VariantMForm">

const { proxy } = getCurrentInstance();
const props = defineProps({
  // 是否显示
  form: {
    type: Object
  }
});

const emits = defineEmits(['save', 'cancel']);
const vfDesigner = ref();

const data = reactive({
  designerConfig: {
    languageMenu: false, // 是否显示语言切换菜单
    externalLink: false, // 是否显示GitHub、文档等外部链接
    importJsonButton: true, //是否显示导入JSON按钮
    exportJsonButton: true, //是否显示导出JSON器按钮
    exportCodeButton: false, //是否显示导出代码按钮
    generateSFCButton: false, //是否显示生成SFC按钮
    // Pro收费
    productName: '流程管理表单生成',  //设置自定义产品名称（仅Pro）
    productTitle: '流程管理表单生成', //设置自定义产品标题（仅Pro）
    logoHeader: false, //是否显示顶部LOGO条（仅Pro）
  },
})
const { designerConfig } = toRefs(data);

watch(() => props.form, (val) => {
  if (val && val.contentMobile) {
    setTimeout(() => {
      proxy.$nextTick(() => {
        vfDesigner.value?.setFormJson(JSON.parse(val.contentMobile));
      })
    },);
  } else {
    setTimeout(() => {
      proxy.$nextTick(() => {
        vfDesigner.value?.clearDesigner();
      })
    },);
  }
}, { deep: true, immediate: true })

/** JSON保存 */
function handleSave () {
  let formJson = vfDesigner.value.getFormJson();
  emits('save', JSON.stringify(formJson));
}

/** 取消 */
function handleCancel () {
  emits('cancel');
}


</script>

<style lang="scss">
  body {
    margin: 0;  /* 如果页面出现垂直滚动条，则加入此行CSS以消除之 */
  }
  .vm-form-designer-container {
    height: 100%;
    .left-aside-toggle-bar, .right-aside-toggle-bar{
      box-sizing: content-box;
    }
  }
</style>