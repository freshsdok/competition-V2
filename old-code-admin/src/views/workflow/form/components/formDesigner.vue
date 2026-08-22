<template>
    <div class="detail-box">
        <el-drawer
            v-model="drawer"
            size="100%"
            modal-class="modalClassVariantForm mb0"
            :direction="direction"
            :show-close="false"
            :close-on-click-modal="false"
            :close-on-press-escape="false"
            :with-header="false"
            append-to-body>

            <!-- 表单设计 -->
            <variantForm :form="props.form" @save="handleSave" @cancel="handleBack"></variantForm>
            
        </el-drawer>

    </div>
</template>
<script setup name="VFormDesigner">
import { updateForm } from "@/api/workflow/form";
import variantForm from '@/views/tool/variantForm';

const { proxy } = getCurrentInstance();
const emits = defineEmits(['save', 'cancel']);
const props = defineProps({
  // 是否显示
  form: {
    type: Object
  }
})

const drawer = ref(true);
const direction = ref("rtl");

/** 保存操作 */
function handleSave (formJson) {
    let params = Object.assign(props.form, {});
    params.content = formJson
    updateForm(params).then(res => {
        proxy.$modal.msgSuccess("表单保存成功");
        emits('save')
    })
}

/** 返回操作 */
function handleBack () {
    emits('cancel')
}

</script>
<style lang="scss">
.modalClassVariantForm {
    .el-drawer__body {
        padding: 0;
    }
}
</style>