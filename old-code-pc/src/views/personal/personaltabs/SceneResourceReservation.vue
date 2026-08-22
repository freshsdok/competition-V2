<template>
  <div class="scene-resource-page">
    <div class="toolbar">
      <div class="title">设备预约</div>
      <el-button type="primary" plain @click="loadAll" :loading="loading">刷新</el-button>
    </div>

    <el-tabs v-model="activeTab">
      <el-tab-pane label="可预约资源" name="resources">
        <div v-loading="loading" class="resource-grid">
          <el-empty v-if="!resources.length && !loading" description="暂无可预约资源" />
          <div
            v-for="item in resources"
            :key="item.scheduleResourceId"
            class="resource-card"
            :class="{ active: currentResource?.scheduleResourceId === item.scheduleResourceId }"
            @click="selectResource(item)"
          >
            <div class="card-head">
              <div>
                <div class="resource-name">{{ item.resourceName }}</div>
                <div class="muted">{{ item.brandModel || item.resourceType }}</div>
              </div>
              <el-tag :type="item.existingReservation ? 'warning' : 'success'">
                {{ item.existingReservation ? '已预约' : '可预约' }}
              </el-tag>
            </div>
            <div class="meta">
              <span>来源赛场：{{ item.scheduleName || item.userSourceScheduleId || '-' }}</span>
              <span>组别：{{ item.groupName || item.groupCode || '-' }}</span>
              <span>位置：{{ item.deploymentLocation || '-' }}</span>
              <span>剩余设备：{{ item.remainingDeviceCount ?? 0 }}</span>
              <span>剩余工位：{{ item.remainingWorkstationCount ?? 0 }}</span>
              <span>每台工位：{{ item.workstationsPerDevice || 1 }}</span>
            </div>
            <div class="next-slot" v-if="item.nextStartTime">
              下一时段：{{ item.nextStartTime }} 至 {{ item.nextEndTime }}
            </div>
          </div>
        </div>

        <div v-if="currentResource" class="detail-panel">
          <div class="detail-title">{{ currentResource.resourceName }}</div>
          <div class="detail-grid">
            <div>预约主体：{{ subjectLabel(currentResource) }}</div>
            <div>参赛人数：{{ currentResource.participantCount || 1 }}</div>
            <div>建议设备数：{{ currentResource.suggestedDeviceCount || 1 }}</div>
            <div>覆盖工位数：{{ currentResource.coveredWorkstationCount || currentResource.workstationsPerDevice || 1 }}</div>
            <div>来源赛场：{{ currentResource.scheduleName || currentResource.userSourceScheduleId || '-' }}</div>
            <div>组别：{{ currentResource.groupName || currentResource.groupCode || '-' }}</div>
          </div>
          <div
            v-if="currentResource.existingReservation"
            class="existing-reservation"
          >
            <div class="existing-title">{{ existingReservationTitle(currentResource.existingReservation) }}</div>
            <div class="existing-meta">
              <span>资源：{{ currentResource.existingReservation.resourceName || '-' }}</span>
              <span>时段：{{ formatReservationRange(currentResource.existingReservation) }}</span>
              <span>占用：{{ currentResource.existingReservation.occupyPeopleCount || 1 }} 人 / {{ currentResource.existingReservation.reservedDeviceCount || 0 }} 台 / {{ currentResource.existingReservation.reservedWorkstationCount || currentResource.existingReservation.coveredWorkstationCount || 0 }} 工位</span>
            </div>
          </div>
          <el-descriptions :column="1" border class="notice-box">
            <el-descriptions-item label="安全须知">{{ currentResource.safetyNotice || '-' }}</el-descriptions-item>
            <el-descriptions-item label="注意事项">{{ currentResource.attentionNotes || '-' }}</el-descriptions-item>
            <el-descriptions-item label="使用说明">{{ currentResource.usageInstructions || '-' }}</el-descriptions-item>
          </el-descriptions>

          <el-table :data="slots" border v-loading="slotLoading" empty-text="暂无开放时段">
            <el-table-column prop="startTime" label="开始时间" min-width="160" />
            <el-table-column prop="endTime" label="结束时间" min-width="160" />
            <el-table-column prop="remainingDeviceCount" label="剩余设备" width="100" />
            <el-table-column prop="remainingWorkstationCount" label="剩余工位" width="100" />
            <el-table-column label="允许组别" min-width="130" show-overflow-tooltip>
              <template #default="{ row }">
                {{ allowedGroupsText(row.allowedGroupNames) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" align="center">
              <template #default="{ row }">
                <el-button
                  type="primary"
                  link
                  :loading="reservingSlotId === row.slotId"
                  :disabled="!!currentResource.existingReservation || !!reservingSlotId || !!row.disabledReason"
                  @click="reserve(row)"
                >
                  {{ row.disabledReason || '预约' }}
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>

      <el-tab-pane label="我的预约" name="mine">
        <el-table :data="reservations" border v-loading="reservationLoading" empty-text="暂无预约记录">
          <el-table-column prop="resourceName" label="资源名称" min-width="140" />
          <el-table-column prop="deploymentLocation" label="部署位置" min-width="120" />
          <el-table-column prop="slotStartTime" label="开始时间" min-width="160" />
          <el-table-column prop="slotEndTime" label="结束时间" min-width="160" />
          <el-table-column label="预约主体" min-width="130" show-overflow-tooltip>
            <template #default="{ row }">
              {{ subjectLabel(row) }}
            </template>
          </el-table-column>
          <el-table-column prop="operatorName" label="操作人" min-width="100" show-overflow-tooltip />
          <el-table-column label="来源赛场" min-width="130" show-overflow-tooltip>
            <template #default="{ row }">
              {{ row.reservationSourceScheduleName || row.reservationSourceScheduleId || '-' }}
            </template>
          </el-table-column>
          <el-table-column label="组别" min-width="100" show-overflow-tooltip>
            <template #default="{ row }">
              {{ row.groupName || row.groupCode || '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="occupyPeopleCount" label="占用人数" width="90" />
          <el-table-column prop="reservedDeviceCount" label="设备数" width="90" />
          <el-table-column prop="reservedWorkstationCount" label="工位数" width="90" />
          <el-table-column label="状态" width="110">
            <template #default="{ row }">
              <el-tag :type="reservationTagType(row)">
                {{ reservationStatusText(row) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="110" align="center">
            <template #default="{ row }">
              <el-button
                type="danger"
                link
                :disabled="!canCancel(row)"
                @click="cancel(row)"
              >
                取消
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ElMessage, ElMessageBox } from "element-plus";
import {
  cancelSceneResourceReservation,
  getBookableSceneResource,
  listBookableSceneResource,
  listBookableSceneResourceSlot,
  listMySceneResourceReservation,
  submitSceneResourceReservation,
} from "@/api/personal/sceneResource";

const activeTab = ref("resources");
const loading = ref(false);
const slotLoading = ref(false);
const reservationLoading = ref(false);
const resources = ref([]);
const slots = ref([]);
const reservations = ref([]);
const currentResource = ref(null);
const reservingSlotId = ref(null);
const reservationIdempotencyKey = ref("");

const loadResources = async () => {
  loading.value = true;
  try {
    const res = await listBookableSceneResource();
    resources.value = res.data || [];
    if (!currentResource.value && resources.value.length) {
      await selectResource(resources.value[0]);
    }
  } finally {
    loading.value = false;
  }
};

const loadReservations = async () => {
  reservationLoading.value = true;
  try {
    const res = await listMySceneResourceReservation();
    reservations.value = res.data || [];
  } finally {
    reservationLoading.value = false;
  }
};

const loadAll = async () => {
  await Promise.all([loadResources(), loadReservations()]);
};

const selectResource = async (item) => {
  slotLoading.value = true;
  try {
    const detail = await getBookableSceneResource(item.scheduleResourceId);
    currentResource.value = detail.data || item;
    const slotRes = await listBookableSceneResourceSlot({
      scheduleResourceId: item.scheduleResourceId,
    });
    slots.value = slotRes.data || [];
  } catch (error) {
    if (error?.existingReservation) {
      currentResource.value = {
        ...item,
        existingReservation: error.existingReservation,
      };
    }
  } finally {
    slotLoading.value = false;
  }
};

const reserve = async (slot) => {
  if (!currentResource.value || reservingSlotId.value) return;
  try {
    await ElMessageBox.confirm(
      `确认预约 ${slot.startTime} 至 ${slot.endTime} 的设备资源？`,
      "预约确认",
      { type: "warning" }
    );
  } catch (error) {
    return;
  }

  reservingSlotId.value = slot.slotId;
  reservationIdempotencyKey.value = reservationIdempotencyKey.value || createIdempotencyKey();
  try {
    const res = await submitSceneResourceReservation({
      slotId: slot.slotId,
      idempotencyKey: reservationIdempotencyKey.value,
    });
    ElMessage.success("预约成功");
    applyExistingReservation(res.data);
    await loadAll();
    activeTab.value = "mine";
  } catch (error) {
    if (isAlreadyReserved(error) && error.existingReservation) {
      ElMessage.warning("当前参赛主体已有有效预约");
      applyExistingReservation(error.existingReservation);
      await loadReservations();
      activeTab.value = "mine";
    } else if (error?.errorCode === "IDEMPOTENCY_CONFLICT_RETRY_LATER") {
      ElMessage.warning("请求处理中，请刷新我的预约确认结果");
    } else if (error?.errorCode === "RESERVATION_CONFLICT_RETRY_LATER") {
      ElMessage.warning("预约冲突，请刷新后查看最新状态");
    }
  } finally {
    reservingSlotId.value = null;
    reservationIdempotencyKey.value = "";
  }
};

const cancel = async (row) => {
  try {
    await ElMessageBox.confirm("确认取消该预约？", "取消预约", { type: "warning" });
  } catch (error) {
    return;
  }

  try {
    await cancelSceneResourceReservation({
      reservationId: row.reservationId,
      cancelReason: "用户主动取消",
    });
    ElMessage.success("已取消预约");
    await loadAll();
  } catch (error) {
    if (error?.errorCode === "RESERVATION_NOT_CANCELABLE") {
      ElMessage.warning(error.msg || "预约时段已开始，不能取消");
      await loadReservations();
    } else if (error?.data?.reservationStatus || error?.reservationStatus) {
      ElMessage.info("预约状态已更新");
      await loadAll();
    }
  }
};

const subjectLabel = (item) => {
  return item.subjectType === "TEAM"
    ? `团队 ${item.subjectName || item.subjectCode || ""}`
    : `个人 ${item.subjectName || item.subjectCode || ""}`;
};

const createIdempotencyKey = () => {
  if (globalThis.crypto?.randomUUID) {
    return globalThis.crypto.randomUUID();
  }
  return `RESV-${Date.now()}-${Math.random().toString(16).slice(2)}-${Math.random().toString(16).slice(2)}`;
};

const isAlreadyReserved = (error) => {
  return ["ALREADY_RESERVED", "ALREADY_RESERVED_BY_SUBJECT"].includes(error?.errorCode);
};

const applyExistingReservation = (reservation) => {
  if (!reservation) return;
  if (currentResource.value) {
    currentResource.value.existingReservation = reservation;
  }
  resources.value = resources.value.map((item) => ({
    ...item,
    existingReservation: item.existingReservation || reservation,
    hasExistingReservation: true,
  }));
};

const existingReservationTitle = (reservation) => {
  const operator = reservation?.operatorName || "队友";
  if (reservation?.subjectType === "TEAM") {
    return `本队已由 ${operator} 预约`;
  }
  return `当前账号已由 ${operator} 预约`;
};

const formatReservationRange = (reservation) => {
  if (!reservation) return "-";
  return `${reservation.slotStartTime || "-"} 至 ${reservation.slotEndTime || "-"}`;
};

const allowedGroupsText = (names = []) => {
  return names && names.length ? names.join("、") : "不限组别";
};

const canCancel = (row) => {
  return row.reservationStatus === "RESERVED" && !row.expired;
};

const reservationStatusText = (row) => {
  if (row.expired && row.reservationStatus === "RESERVED") return "已过期";
  const map = {
    RESERVED: "已预约",
    CANCELLED: "已取消",
    CHECKED: "已核销",
    EXPIRED: "已过期",
  };
  return map[row.reservationStatus] || row.reservationStatus || "-";
};

const reservationTagType = (row) => {
  if (row.expired && row.reservationStatus === "RESERVED") return "info";
  if (row.reservationStatus === "RESERVED") return "success";
  if (row.reservationStatus === "CHECKED") return "warning";
  return "info";
};

onMounted(() => {
  loadAll();
});
</script>

<style scoped lang="scss">
.scene-resource-page {
  min-height: 520px;
}

.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 18px;
}

.title {
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
}

.resource-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
  min-height: 180px;
}

.resource-card {
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  padding: 16px;
  cursor: pointer;
  background: #fff;
}

.resource-card.active {
  border-color: #3169f8;
  box-shadow: 0 0 0 1px rgba(49, 105, 248, 0.08);
}

.card-head {
  display: flex;
  justify-content: space-between;
  gap: 10px;
}

.resource-name {
  font-size: 17px;
  font-weight: 600;
  color: #111827;
}

.muted,
.next-slot,
.meta {
  color: #6b7280;
  font-size: 13px;
}

.meta {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
  margin-top: 12px;
}

.next-slot {
  margin-top: 12px;
}

.detail-panel {
  margin-top: 20px;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  padding: 18px;
}

.detail-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 14px;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 14px;
  color: #374151;
}

.existing-reservation,
.notice-box {
  margin-bottom: 14px;
}

.existing-reservation {
  padding: 12px 14px;
  border: 1px solid #f5dab1;
  border-radius: 6px;
  background: #fdf6ec;
}

.existing-title {
  color: #b88230;
  font-size: 14px;
  font-weight: 600;
  line-height: 22px;
}

.existing-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 6px;
  color: #7a5b2b;
  font-size: 13px;
  line-height: 20px;
}
</style>
