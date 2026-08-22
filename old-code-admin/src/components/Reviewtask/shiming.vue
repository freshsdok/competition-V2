
<template>
  <div class="" style="padding: 16px">
    <el-form
      ref="noticeInfoRef"
      :model="form"
      :rules="rules"
      label-width="120px"
      disabled
    >
      <el-row>
        <el-col :span="12">
          <el-form-item label="证件类型" prop="noticeTitle">
            <el-select
              v-model="form.idCardType"
              placeholder="请选择证件类型"
              clearable
            >
              <el-option
                v-for="dict in document_type"
                :key="dict.value"
                :label="dict.label"
                :value="dict.value"
              ></el-option>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="国籍" prop="countryName">
             <el-input
              v-model="form.countryName"
              class="ipth50"
              maxlength="20"
              show-word-limit
            />
            <!-- <el-select v-model="form.countryName" placeholder="请选择国籍">
              <el-option
                v-for="dict in country_name"
                :key="dict.value"
                :label="dict.label"
                :value="dict.value"
              ></el-option>
            </el-select> -->
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="真实姓名" prop="realName">
            <el-input
              v-model="form.realName"
              class="ipth50"
              maxlength="20"
              show-word-limit
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="证件号" prop="idCard">
            <el-input
              v-model="form.idCard"
              class="ipth50"
              maxlength="18"
              show-word-limit
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="证件正面照" prop="realName">
            <img :src="form.idCardFront" alt="" style="width: 300px" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="证件背面照" prop="idCard">
            <img :src="form.idCardContrary" alt="" style="width: 300px" />
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
  </div>
</template>
<script setup>
import { useDict } from "@/utils/dict";
import Editor from "@/components/Editor";
import { encrypt, decrypt } from "@/utils/jsencrypt.js";
const { document_type } = useDict(
  "document_type",

);
const props = defineProps({
  form: {
    type: Object,
    default: () => {},
  },
});

const { form } = toRefs(props);
form.value.idCard = decrypt(form.value.idCard);
console.log(props);
</script>

<style scoped>
</style>