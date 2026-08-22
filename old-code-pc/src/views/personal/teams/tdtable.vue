<template>
  <div class="">
    <el-table :data="queryParams.competitionApplyInfoList" style="width: 100%">
      <el-table-column
        prop="userName"
        label="姓名"
        min-width="100"
        align="center"
      >
        <template #default="{ row }">
          <el-input
            v-if="row.isEditing && row.isNew"
            v-model="row.ADDuserName"
            placeholder="请输入姓名"
            size="small"
          />
          <span v-else>{{ row.userName }}</span>
        </template>
      </el-table-column>
      <el-table-column
        prop="competitionRoleName"
        label="身份"
        min-width="80"
        align="center"
      >
        <template #default="{ row }">
          <el-select
            v-if="row.isEditing && row.isNew"
            v-model="row.ADDcompetitionRoleName"
            placeholder="请选择身份"
            size="small"
          >
            <el-option label="队长" value="队长" />
            <el-option label="队员" value="队员" />
          </el-select>
          <span v-else>{{ row.competitionRoleName }}</span>
        </template>
      </el-table-column>
      <el-table-column
        prop="idCard"
        label="身份证号"
        min-width="200"
        align="center"
      >
        <template #default="{ row }">
          <el-input
            v-if="row.isEditing && row.isNew"
            v-model="row.ADDidCard"
            placeholder="请输入身份证号"
            size="small"
          />
          <span v-else>{{ formatIdCard(row.idCard) }}</span>
        </template>
      </el-table-column>
      <el-table-column
        prop="phone"
        label="手机号"
        min-width="180"
        align="center"
      >
        <template #default="{ row }">
          <div v-if="row.isEditing">
            <el-input
              v-model="row.ADDphone"
              placeholder="请输入手机号"
              size="small"
              @blur="validatePhone(row)"
            />
          </div>

          <div v-else style="display: flex; justify-content: center">
            {{ row.phone }}
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="email" label="邮箱" min-width="200" align="center">
        <template #default="{ row }">
          <div v-if="row.isEditing">
            <el-input
              v-model="row.ADDemail"
              placeholder="请输入邮箱"
              size="small"
              @blur="validateEmail(row)"
            />
          </div>
          <div v-else style="display: flex; justify-content: center">
            {{ row.email }}
          </div>
        </template>
      </el-table-column>
      <el-table-column label="操作" fixed="right" width="200" align="center">
        <template #default="{ row }">
          <div v-if="chengyuan">
            <el-button
              size="mini"
              type="danger"
              @click="handleDelete(row, 'competitionApplyInfoList')"
              >删除</el-button
            >
          </div>
          <div v-else>
            <div
              v-if="row.isEditing"
              style="position: relative"
              :class="row.isxuesheng ? 'gaodu2' : 'gaodu'"
            >
              <el-button
                type="success"
                size="mini"
                @click="handleSave(row, '学生')"
                :loading="row.xueshengloading"
                link
                >保存</el-button
              >
              <el-button
                type="danger"
                size="mini"
                @click="handleCancel(row)"
                link
              >
                取消
              </el-button>

              <div class="danghang" v-if="row.isxuesheng">
                <CircleCloseFilled class="icon" />
                {{ row.isxuesheng }}
              </div>
            </div>
            <el-button
              v-else-if="permissionStatus[0].isshow"
              type="primary"
              size="mini"
              @click="handleEdit(row, 1)"
              :disabled="
                !isCurrentTimeInRange(
                  getOperationConfig(1)?.jsonallowedTimeRanges
                ) || row.applyInfoChangeOperateCount == 0
              "
              link
              >编辑
              <span
                v-if="
                  row.applyInfoChangeOperateCount != -1 &&
                  isCurrentTimeInRange(
                    getOperationConfig(1)?.jsonallowedTimeRanges
                  )
                "
                >({{ row.applyInfoChangeOperateCount }}次)</span
              ></el-button
            >
          </div>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { useRoute, useRouter } from "vue-router";
const route = useRoute();
const router = useRouter();
const props = defineProps({
  queryParams: {
    type: Object,
    default: () => {},
  },
});

// 加密身份证号
const formatIdCard = (idCard) => {
  if (!idCard) return "";
  return idCard.substring(0, 6) + "********" + idCard.substring(14);
};

</script>

<style scoped>
</style>