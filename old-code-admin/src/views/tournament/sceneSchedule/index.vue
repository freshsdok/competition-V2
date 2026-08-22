<template>
  <div class="app-container scene-schedule-page">
    <el-form
      ref="queryRef"
      :model="queryParams"
      :inline="true"
      v-show="showSearch"
      label-width="96px"
    >
      <el-form-item label="安排ID" prop="scheduleId">
        <el-input
          v-model.trim="queryParams.scheduleId"
          placeholder="请输入"
          clearable
          style="width: 180px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="安排名称" prop="scheduleName">
        <el-input
          v-model.trim="queryParams.scheduleName"
          placeholder="请输入"
          clearable
          style="width: 180px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="关联赛事" prop="competitionSeriesId">
        <el-select
          v-model="queryParams.competitionSeriesId"
          placeholder="请选择"
          clearable
          filterable
          style="width: 220px"
        >
          <el-option
            v-for="item in competitionOptions"
            :key="item.competitionSeriesId"
            :label="item.competitionName"
            :value="item.competitionSeriesId"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择" clearable style="width: 130px">
          <el-option label="启用" value="0" />
          <el-option label="停用" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="Plus"
          @click="handleAddSchedule"
          v-hasPermi="['competition:sceneSchedule:add']"
        >新增赛场安排</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Bell"
          @click="openAnnouncementManager"
          v-hasPermi="['competition:sceneNotice:list']"
        >大赛公告</el-button>
      </el-col>
      
      <right-toolbar v-model:showSearch="showSearch" @queryTable="refreshActiveTab" />
    </el-row>

    <div v-if="selectedSchedule" class="selected-line">
      <span>当前赛场安排：</span>
      <strong>{{ selectedSchedule.scheduleName }}</strong>
      <span class="muted">{{ selectedSchedule.competitionName || '-' }}</span>
      <el-col :span="1.5" style="margin-left: 20px">
        <el-button
          type="success"
          plain
          icon="Connection"
          :disabled="!selectedSchedule"
          @click="handleMatchSelected"
          v-hasPermi="['competition:sceneSchedule:edit']"
        >自动导入人员</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Tickets"
          :disabled="!selectedSchedule"
          @click="handleGenerateSelectedSchedule"
          v-hasPermi="['competition:sceneCredential:add']"
        >生成所有证件</el-button>
      </el-col>
    </div>

    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="赛场安排" name="schedule">
        <el-table
          v-loading="scheduleLoading"
          :data="scheduleList"
          stripe
          highlight-current-row
          @current-change="handleCurrentScheduleChange"
        >
          <el-table-column label="安排ID" prop="scheduleId"/>
          <el-table-column label="安排名称" prop="scheduleName" min-width="180" show-overflow-tooltip />
          <el-table-column label="赛事" prop="competitionName" min-width="190" show-overflow-tooltip>
            <template #default="{ row }">{{ row.competitionName || '-' }}</template>
          </el-table-column>
          <el-table-column label="报道信息" min-width="230" show-overflow-tooltip>
            <template #default="{ row }">
              <div>{{ formatRange(row.reportStartTime, row.reportEndTime) }}</div>
              <div class="muted">{{ row.reportLocation || '-' }}</div>
            </template>
          </el-table-column>
          <el-table-column label="赛场信息" min-width="250" show-overflow-tooltip>
            <template #default="{ row }">
              <div>{{ formatRange(row.contestStartTime, row.contestEndTime) }}</div>
              <div class="muted">{{ [row.contestLocation, row.contestRoom].filter(Boolean).join(' / ') || '-' }}</div>
            </template>
          </el-table-column>
          <el-table-column label="赛道/组别" min-width="160" show-overflow-tooltip>
            <template #default="{ row }">
              {{ [row.competitionTrackName, row.secondLevelName].filter(Boolean).join(' / ') || '-' }}
            </template>
          </el-table-column>
          <el-table-column label="状态" prop="status" width="90" align="center">
            <template #default="{ row }">
              <el-tag :type="row.status === '0' ? 'success' : 'info'">
                {{ row.status === '0' ? '启用' : '停用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="360" align="center" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="handleViewTargets(row)">人员</el-button>
              <el-button link type="primary" @click="handleViewCredentials(row)">证件</el-button>
              <el-button
                link
                type="primary"
                @click="handleUpdateSchedule(row)"
                v-hasPermi="['competition:sceneSchedule:edit']"
              >修改</el-button>
              <el-button
                link
                type="danger"
                @click="handleDeleteSchedule(row)"
                v-hasPermi="['competition:sceneSchedule:remove']"
              >删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <pagination
          v-show="scheduleTotal > 0"
          :total="scheduleTotal"
          v-model:page="queryParams.pageNum"
          v-model:limit="queryParams.pageSize"
          @pagination="getScheduleList"
        />
      </el-tab-pane>

      <el-tab-pane label="绑定对象" name="target">
        <div class="pane-toolbar">
          <el-form :model="targetQuery" :inline="true" label-width="80px">
            <el-form-item label="安排ID">
              <el-input v-model.trim="targetQuery.scheduleId" placeholder="请选择安排" clearable style="width: 140px" />
            </el-form-item>
            <el-form-item label="对象类型">
              <el-select v-model="targetQuery.targetType" placeholder="请选择" clearable style="width: 150px">
                <el-option
                  v-for="item in targetTypeOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="团队">
              <el-input v-model.trim="targetQuery.teamName" placeholder="团队名" clearable style="width: 160px" />
            </el-form-item>
            <el-form-item label="姓名">
              <el-input v-model.trim="targetQuery.userName" placeholder="姓名" clearable style="width: 140px" />
            </el-form-item>
            <el-form-item label="评审对象">
              <el-input v-model.trim="targetQuery.reviewObjectId" placeholder="对象ID" clearable style="width: 130px" />
            </el-form-item>
            <el-form-item label="证件编号">
              <el-input v-model.trim="targetQuery.certificateCode" placeholder="证件编号" clearable style="width: 160px" />
            </el-form-item>
            <el-form-item label="证件类型">
              <el-select v-model="targetQuery.credentialType" placeholder="请选择" clearable style="width: 140px">
                <el-option
                  v-for="item in credentialTypeOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="角色">
              <el-select v-model="targetQuery.competitionRoleName" placeholder="请选择" clearable style="width: 160px">
                <el-option
                  v-for="item in targetRoleOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" icon="Search" @click="handleTargetQuery">查询</el-button>
              <el-button icon="Refresh" @click="resetTargetQuery">重置</el-button>
              <!-- <el-button
                type="primary"
                plain
                icon="Plus"
                :disabled="!selectedSchedule"
                @click="openBindTargetDialog('REVIEW_OBJECT')"
                v-hasPermi="['competition:sceneSchedule:add']"
              >添加评审对象</el-button>
              <el-button
                type="success"
                plain
                icon="Plus"
                :disabled="!selectedSchedule"
                @click="openBindTargetDialog('TEAM')"
                v-hasPermi="['competition:sceneSchedule:add']"
              >添加团队</el-button>
              <el-button
                type="info"
                plain
                icon="Plus"
                :disabled="!selectedSchedule"
                @click="openBindTargetDialog('PERSON')"
                v-hasPermi="['competition:sceneSchedule:add']"
              >添加人员</el-button> -->
              <el-button
                type="primary"
                plain
                icon="EditPen"
                :disabled="!selectedSchedule"
                @click="openManualTargetDialog"
                v-hasPermi="['competition:sceneSchedule:add']"
              >手工对象</el-button>
              <el-button
                plain
                icon="Plus"
                :disabled="!selectedSchedule"
                @click="handleAddTarget"
                v-hasPermi="['competition:sceneSchedule:add']"
              >新增人员</el-button>
              <el-button
                plain
                icon="Sort"
                :disabled="targetList.length === 0"
                @click="handleAutoGenerateSequence(false)"
                v-hasPermi="['competition:sceneSchedule:edit']"
              >自动顺序</el-button>
              <el-button
                plain
                icon="Rank"
                :disabled="targetList.length === 0"
                @click="handleAutoGenerateSequence(true)"
                v-hasPermi="['competition:sceneSchedule:edit']"
              >重排全部</el-button>
              <el-button
                plain
                icon="SortUp"
                :disabled="!selectedSchedule"
                @click="openNameSequenceDialog"
                v-hasPermi="['competition:sceneSchedule:edit']"
              >按姓名排序</el-button>
              <el-button
                plain
                icon="Finished"
                :disabled="targetList.length === 0"
                @click="handleSaveTargetSequence"
                v-hasPermi="['competition:sceneSchedule:edit']"
              >保存顺序</el-button>
              <el-button
                type="warning"
                plain
                icon="Connection"
                :disabled="!selectedSchedule"
                @click="openSyncReviewSessionDialog"
                v-hasPermi="['competition:sceneSchedule:edit']"
              >同步评审场次</el-button>
              <el-button
                type="warning"
                plain
                icon="Message"
                :disabled="selectedTargetRows.length === 0"
                @click="openBatchPersonalNoticeManager"
                v-hasPermi="['competition:sceneNotice:add']"
              >批量个人通知</el-button>
              <el-button
                type="warning"
                plain
                icon="Tickets"
                :disabled="selectedTargetRows.length === 0"
                @click="handleGenerateTargets"
                v-hasPermi="['competition:sceneCredential:add']"
              >生成所选证件</el-button>
              <el-button
                type="danger"
                plain
                icon="Delete"
                :disabled="selectedTargetRows.length === 0"
                @click="handleDeleteTargets"
                v-hasPermi="['competition:sceneSchedule:remove']"
              >删除所选</el-button>
            </el-form-item>
          </el-form>
        </div>
        <el-table
          v-loading="targetLoading"
          :data="targetList"
          stripe
          @selection-change="handleTargetSelectionChange"
        >
          <el-table-column type="selection" width="55" />
          <el-table-column label="顺序" width="110" align="center" fixed="left">
            <template #default="{ row }">
              <el-input-number
                v-model="row.sequenceNo"
                :min="1"
                :max="9999"
                :controls="false"
                size="small"
                style="width: 78px"
              />
            </template>
          </el-table-column>
          <el-table-column label="对象类型" prop="targetType" width="120" align="center">
            <template #default="{ row }">
              <el-tag :type="targetTypeTagType(inferTargetType(row))">
                {{ targetTypeLabel(inferTargetType(row)) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="绑定对象" min-width="210" show-overflow-tooltip>
            <template #default="{ row }">
              <div>{{ targetDisplayName(row) }}</div>
              <div class="muted">
                <span v-if="row.reviewObjectId">评审对象ID：{{ row.reviewObjectId }}</span>
                <span v-else-if="row.sourceBizId">来源ID：{{ row.sourceBizId }}</span>
                <span v-else>-</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="对象来源" prop="targetSource" width="100" align="center">
            <template #default="{ row }">
              <el-tag>{{ tagLabel(targetSourceOptions, row.targetSource) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="团队" min-width="180" show-overflow-tooltip>
            <template #default="{ row }">
              <div>{{ row.teamName || '-' }}</div>
              <div class="muted">{{ row.teamCode || '' }}</div>
            </template>
          </el-table-column>
          <el-table-column label="人员" min-width="160" show-overflow-tooltip>
            <template #default="{ row }">
              <div>{{ row.userName || '-' }}</div>
              <div class="muted">{{ row.phone || row.email || '' }}</div>
            </template>
          </el-table-column>
          <el-table-column label="身份后六位" prop="idCardSuffix" width="110" align="center" />
          <el-table-column label="证件类型" prop="credentialType" width="110" align="center">
            <template #default="{ row }">
              <el-tag :type="credentialTypeTagType(row.credentialType)">
                {{ credentialTypeLabel(row.credentialType) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="证件编号" prop="certificateCode" min-width="150" show-overflow-tooltip>
            <template #default="{ row }">{{ row.certificateCode || '-' }}</template>
          </el-table-column>
          <el-table-column label="角色" prop="competitionRoleName" width="130" show-overflow-tooltip>
            <template #default="{ row }">{{ targetRoleLabel(row.competitionRoleName) }}</template>
          </el-table-column>
          <el-table-column label="学校/机构" min-width="180" show-overflow-tooltip>
            <template #default="{ row }">{{ row.schoolName || row.orgName || '-' }}</template>
          </el-table-column>
          <el-table-column label="赛道/组别" min-width="170" show-overflow-tooltip>
            <template #default="{ row }">
              {{ [row.competitionTrackName, row.secondLevelName].filter(Boolean).join(' / ') || '-' }}
            </template>
          </el-table-column>
          <el-table-column label="候场组" min-width="140" show-overflow-tooltip>
            <template #default="{ row }">{{ row.waitingGroupName || row.waitingGroupCode || '-' }}</template>
          </el-table-column>
          <el-table-column label="状态" prop="status" width="90" align="center">
            <template #default="{ row }">
              <el-tag :type="row.status === '0' ? 'success' : 'info'">
                {{ row.status === '0' ? '有效' : '停用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="240" align="center" fixed="right">
            <template #default="{ row }">
              <el-button
                link
                type="warning"
                @click="openPersonalNoticeManager(row)"
                v-hasPermi="['competition:sceneNotice:list']"
              >个人通知</el-button>
              <el-button link type="primary" @click="handleUpdateTarget(row)" v-hasPermi="['competition:sceneSchedule:edit']">修改</el-button>
              <el-button link type="danger" @click="handleDeleteTarget(row)" v-hasPermi="['competition:sceneSchedule:remove']">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <pagination
          v-show="targetTotal > 0"
          :total="targetTotal"
          v-model:page="targetQuery.pageNum"
          v-model:limit="targetQuery.pageSize"
          @pagination="getTargetList"
        />
      </el-tab-pane>

      <el-tab-pane label="证件列表" name="credential">
        <div class="pane-toolbar">
          <el-form :model="credentialQuery" :inline="true" label-width="80px">
            <el-form-item label="安排ID">
              <el-input v-model.trim="credentialQuery.scheduleId" placeholder="请选择安排" clearable style="width: 140px" />
            </el-form-item>
            <el-form-item label="证件编号">
              <el-input v-model.trim="credentialQuery.credentialNo" placeholder="请输入" clearable style="width: 180px" />
            </el-form-item>
            <el-form-item label="作用域">
              <el-select v-model="credentialQuery.scopeType" placeholder="请选择" clearable style="width: 130px">
                <el-option
                  v-for="item in credentialScopeOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="团队">
              <el-input v-model.trim="credentialQuery.teamName" placeholder="团队名" clearable style="width: 150px" />
            </el-form-item>
            <el-form-item label="姓名">
              <el-input v-model.trim="credentialQuery.userName" placeholder="姓名" clearable style="width: 130px" />
            </el-form-item>
            <el-form-item label="证件状态">
              <el-select v-model="credentialQuery.credentialStatus" placeholder="请选择" clearable style="width: 130px">
                <el-option
                  v-for="item in credentialStatusOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" icon="Search" @click="handleCredentialQuery">查询</el-button>
              <el-button icon="Refresh" @click="resetCredentialQuery">重置</el-button>
              <el-button
                type="danger"
                plain
                icon="Delete"
                :disabled="selectedCredentialRows.length === 0"
                @click="handleDeleteCredentials"
                v-hasPermi="['competition:sceneCredential:remove']"
              >删除所选</el-button>
            </el-form-item>
          </el-form>
        </div>
        <el-table
          v-loading="credentialLoading"
          :data="credentialList"
          stripe
          @selection-change="handleCredentialSelectionChange"
        >
          <el-table-column type="selection" width="55" />
          <el-table-column label="证件编号" prop="credentialNo" min-width="210" show-overflow-tooltip />
          <el-table-column label="二维码内容" prop="qrContent" min-width="190" show-overflow-tooltip />
          <el-table-column label="人员" min-width="180" show-overflow-tooltip>
            <template #default="{ row }">
              <div>{{ row.teamName || row.userName || '-' }}</div>
              <div class="muted">{{ row.teamCode || row.phone || '' }}</div>
            </template>
          </el-table-column>
          <el-table-column label="证件名称" width="130" align="center">
            <template #default="{ row }">
              <el-tag :type="credentialTypeTagType(row.credentialType)">
                {{ row.credentialName || credentialTypeLabel(row.credentialType) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="作用域" width="110" align="center">
            <template #default="{ row }">
              <el-tag :type="tagType(credentialScopeOptions, row.scopeType || 'SCHEDULE')">
                {{ tagLabel(credentialScopeOptions, row.scopeType || 'SCHEDULE') }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="赛场" min-width="200" show-overflow-tooltip>
            <template #default="{ row }">
              {{ [row.contestLocation, row.contestRoom].filter(Boolean).join(' / ') || '-' }}
            </template>
          </el-table-column>
          <el-table-column label="报到/资料/候场" width="170" align="center">
            <template #default="{ row }">
              <el-tag size="small" :type="row.reportStatus === '1' ? 'success' : 'info'">报到</el-tag>
              <el-tag size="small" :type="row.materialStatus === '1' ? 'success' : 'info'">资料</el-tag>
              <el-tag v-if="(row.scopeType || 'SCHEDULE') !== 'COMPETITION'" size="small" :type="row.waitingStatus === '1' ? 'success' : 'info'">候场</el-tag>
              <div v-if="row.materialStatus === '1' && row.materialDelegateName" class="muted">
                {{ row.materialDelegateRelation === 'TEAM_MEMBER' ? '代领' : '领取' }}：{{ row.materialDelegateName }}
              </div>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="tagType(credentialStatusOptions, row.credentialStatus)">
                {{ tagLabel(credentialStatusOptions, row.credentialStatus) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="文件" width="130" align="center">
            <template #default="{ row }">
              <el-link v-if="row.credentialFileUrl" type="primary" :href="row.credentialFileUrl" target="_blank">下载</el-link>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" align="center" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="handleUpdateCredential(row)" v-hasPermi="['competition:sceneCredential:edit']">维护</el-button>
              <el-button link type="danger" @click="handleDeleteCredential(row)" v-hasPermi="['competition:sceneCredential:remove']">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <pagination
          v-show="credentialTotal > 0"
          :total="credentialTotal"
          v-model:page="credentialQuery.pageNum"
          v-model:limit="credentialQuery.pageSize"
          @pagination="getCredentialList"
        />
      </el-tab-pane>

      <el-tab-pane label="资源与预约" name="resource">
        <resource-reservation-tab
          ref="resourceTabRef"
          :schedule="selectedSchedule"
          :active="activeTab === 'resource'"
        />
      </el-tab-pane>

      <el-tab-pane label="操作流水" name="log">
        <div class="pane-toolbar">
          <el-form :model="logQuery" :inline="true" label-width="80px">
            <el-form-item label="安排ID">
              <el-input v-model.trim="logQuery.scheduleId" placeholder="请选择安排" clearable style="width: 140px" />
            </el-form-item>
            <el-form-item label="证件编号">
              <el-input v-model.trim="logQuery.credentialNo" placeholder="请输入" clearable style="width: 180px" />
            </el-form-item>
            <el-form-item label="操作类型">
              <el-select v-model="logQuery.operationType" placeholder="请选择" clearable style="width: 160px">
                <el-option
                  v-for="item in operationTypeOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="结果">
              <el-select v-model="logQuery.operationResult" placeholder="请选择" clearable style="width: 130px">
                <el-option
                  v-for="item in operationResultOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" icon="Search" @click="handleLogQuery">查询</el-button>
              <el-button icon="Refresh" @click="resetLogQuery">重置</el-button>
            </el-form-item>
          </el-form>
        </div>
        <el-table v-loading="logLoading" :data="logList" stripe>
          <el-table-column label="操作时间" prop="operationTime" width="170" />
          <el-table-column label="证件编号" prop="credentialNo" min-width="190" show-overflow-tooltip />
          <el-table-column label="人员" min-width="170" show-overflow-tooltip>
            <template #default="{ row }">{{ row.teamName || row.userName || '-' }}</template>
          </el-table-column>
          <el-table-column label="操作类型" prop="operationType" width="130">
            <template #default="{ row }">{{ tagLabel(operationTypeOptions, row.operationType) }}</template>
          </el-table-column>
          <el-table-column label="阶段" prop="operationStage" width="90" align="center" />
          <el-table-column label="结果" prop="operationResult" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="tagType(operationResultOptions, row.operationResult)">
                {{ tagLabel(operationResultOptions, row.operationResult) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="说明" prop="resultMessage" min-width="220" show-overflow-tooltip />
          <el-table-column label="备注" prop="remark" min-width="180" show-overflow-tooltip />
          <el-table-column label="操作人" prop="operatorName" width="120" show-overflow-tooltip />
          <el-table-column label="设备" prop="deviceInfo" min-width="160" show-overflow-tooltip />
        </el-table>
        <pagination
          v-show="logTotal > 0"
          :total="logTotal"
          v-model:page="logQuery.pageNum"
          v-model:limit="logQuery.pageSize"
          @pagination="getLogList"
        />
      </el-tab-pane>
    </el-tabs>

    <el-dialog :title="scheduleDialogTitle" v-model="scheduleDialogOpen" width="920px" append-to-body>
      <el-form ref="scheduleFormRef" :model="scheduleForm" :rules="scheduleRules" label-width="140px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="安排名称" prop="scheduleName">
              <el-input v-model.trim="scheduleForm.scheduleName" placeholder="请输入安排名称" maxlength="255" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="关联赛事" prop="competitionSeriesId">
              <el-select
                v-model="scheduleForm.competitionSeriesId"
                placeholder="请选择赛事"
                filterable
                clearable
                style="width: 100%"
                @change="handleScheduleCompetitionChange"
              >
                <el-option
                  v-for="item in competitionOptions"
                  :key="item.competitionSeriesId"
                  :label="item.competitionName"
                  :value="item.competitionSeriesId"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="scheduleForm.status">
                <el-radio label="0">启用</el-radio>
                <el-radio label="1">停用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="赛事阶段">
              <el-select
                v-model="scheduleForm.competitionStageId"
                placeholder="请选择阶段"
                clearable
                filterable
                :loading="competitionDetailLoading"
                :disabled="!scheduleForm.competitionSeriesId || competitionDetailLoading || scheduleStageOptions.length === 0"
                style="width: 100%"
                @change="handleScheduleStageChange"
              >
                <el-option
                  v-for="item in scheduleStageOptions"
                  :key="item.stageId"
                  :label="item.stageName"
                  :value="item.stageId"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="赛道名称">
              <el-select
                v-model="scheduleForm.competitionTrackId"
                placeholder="请选择赛道"
                clearable
                filterable
                :loading="scheduleTrackLoading"
                :disabled="!scheduleForm.competitionSeriesId || scheduleTrackLoading || scheduleTrackOptions.length === 0"
                style="width: 100%"
                @change="handleScheduleTrackChange"
              >
                <el-option
                  v-for="item in scheduleTrackOptions"
                  :key="item.id"
                  :label="item.label"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="组别名称">
              <el-input
                v-model.trim="scheduleForm.secondLevelName"
                placeholder="请输入组别名称"
                @input="handleScheduleSecondLevelNameInput"
              />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="资料领取">
              <el-input v-model.trim="scheduleForm.materialLocation" placeholder="请输入" />
            </el-form-item>
          </el-col>
          <!-- <el-col :span="12">
            <el-form-item label="候场组名称">
              <el-input v-model.trim="scheduleForm.waitingGroupName" placeholder="可选" />
            </el-form-item>
          </el-col> -->
          <el-col :span="24">
            <el-form-item label="签到地点">
              <el-input v-model.trim="scheduleForm.reportLocation" placeholder="请输入赛场签到地点" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="签到开始时间">
              <el-date-picker
                v-model="scheduleForm.reportStartTime"
                type="datetime"
                value-format="YYYY-MM-DD HH:mm:ss"
                placeholder="请选择"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="签到结束时间">
              <el-date-picker
                v-model="scheduleForm.reportEndTime"
                type="datetime"
                value-format="YYYY-MM-DD HH:mm:ss"
                placeholder="请选择"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="候场地点">
              <el-input v-model.trim="scheduleForm.waitingLocation" placeholder="请输入候场地点" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="比赛开始时间">
              <el-date-picker
                v-model="scheduleForm.contestStartTime"
                type="datetime"
                value-format="YYYY-MM-DD HH:mm:ss"
                placeholder="请选择"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="比赛结束时间">
              <el-date-picker
                v-model="scheduleForm.contestEndTime"
                type="datetime"
                value-format="YYYY-MM-DD HH:mm:ss"
                placeholder="请选择"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="赛场地点">
              <el-input v-model.trim="scheduleForm.contestLocation" placeholder="请输入赛场地点" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="赛场备注">
              <el-input v-model.trim="scheduleForm.contestRoom" placeholder="请输入" />
            </el-form-item>
          </el-col>
          <!-- <el-col :span="12">
            <el-form-item label="候场开始">
              <el-date-picker
                v-model="scheduleForm.waitingStartTime"
                type="datetime"
                value-format="YYYY-MM-DD HH:mm:ss"
                placeholder="请选择"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="候场结束">
              <el-date-picker
                v-model="scheduleForm.waitingEndTime"
                type="datetime"
                value-format="YYYY-MM-DD HH:mm:ss"
                placeholder="请选择"
                style="width: 100%"
              />
            </el-form-item>
          </el-col> -->
          
          <el-col :span="24">
            <el-form-item label="注意事项">
              <el-input
                v-model="scheduleForm.notice"
                type="textarea"
                :rows="4"
                placeholder="请输入现场注意事项"
                maxlength="2000"
                show-word-limit
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="scheduleDialogOpen = false">取 消</el-button>
          <el-button type="primary" @click="submitScheduleForm">保 存</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog :title="targetDialogTitle" v-model="targetDialogOpen" width="840px" append-to-body>
      <el-form ref="targetFormRef" :model="targetForm" :rules="targetRules" label-width="120px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="安排ID" prop="scheduleId">
              <el-input v-model="targetForm.scheduleId" disabled />
            </el-form-item>
          </el-col>
          <el-col v-if="targetForm.targetId" :span="12">
            <el-form-item label="对象来源">
              <el-select v-model="targetForm.targetSource" style="width: 100%">
                <el-option
                  v-for="item in targetSourceOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col v-if="targetForm.targetId" :span="12">
            <el-form-item label="对象类型">
              <el-select v-model="targetForm.targetType" style="width: 100%">
                <el-option
                  v-for="item in targetTypeOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col v-if="targetForm.targetId" :span="12">
            <el-form-item label="显示名称">
              <el-input v-model.trim="targetForm.targetName" placeholder="请输入绑定对象名称" />
            </el-form-item>
          </el-col>
          <el-col v-if="targetForm.targetId" :span="12">
            <el-form-item label="评审对象ID">
              <el-input-number v-model="targetForm.reviewObjectId" :min="1" :precision="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col v-if="targetForm.targetId" :span="12">
            <el-form-item label="赛场顺序">
              <el-input-number v-model="targetForm.sequenceNo" :min="1" :precision="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="证件类型" prop="credentialType">
              <el-select v-model="targetForm.credentialType" placeholder="请选择证件类型" style="width: 100%">
                <el-option
                  v-for="item in credentialTypeOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col v-if="targetForm.targetId" :span="12">
            <el-form-item label="团队编号">
              <el-input v-model.trim="targetForm.teamCode" placeholder="团队维度时填写" />
            </el-form-item>
          </el-col>
          <el-col v-if="targetForm.targetId" :span="12">
            <el-form-item label="团队名称">
              <el-input v-model.trim="targetForm.teamName" placeholder="请输入" />
            </el-form-item>
          </el-col>
          <el-col v-if="targetForm.targetId" :span="12">
            <el-form-item label="报名成员ID">
              <el-input v-model.trim="targetForm.memberId" placeholder="个人维度可填" />
            </el-form-item>
          </el-col>
          <el-col v-if="targetForm.targetId" :span="12">
            <el-form-item label="参赛证编号">
              <el-input v-model.trim="targetForm.certificateCode" placeholder="现场扫码证件编号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="关联用户" :required="!targetForm.targetId">
              <div class="target-user-picker">
                <el-input
                  :model-value="formatSelectedTargetUserLabel()"
                  placeholder="请选择用户"
                  readonly
                  clearable
                  @clear="clearTargetUsers"
                >
                  <template #append>
                    <el-button icon="User" @click="openUserSelectDialog">选择</el-button>
                  </template>
                </el-input>
                <el-button v-if="hasSelectedTargetUsers" link type="danger" @click="clearTargetUsers">清空</el-button>
              </div>
            </el-form-item>
          </el-col>
          <el-col v-if="!targetForm.targetId" :span="12">
            <el-form-item label="用户组白名单">
              <div class="target-user-picker">
                <el-input
                  :model-value="formatSelectedUserGroupLabel()"
                  placeholder="请选择用户组"
                  readonly
                >
                  <template #append>
                    <el-button icon="UserFilled" @click="openGroupSelectDialog">选择</el-button>
                  </template>
                </el-input>
              </div>
            </el-form-item>
          </el-col>
          <el-col v-if="!targetForm.targetId && targetSelectedUsers.length > 0" :span="24">
            <el-form-item label="已选用户">
              <div class="selected-user-tags">
                <el-tag
                  v-for="user in targetSelectedUsers.slice(0, 12)"
                  :key="user.userId"
                  closable
                  @close="removeSelectedTargetUser(user)"
                >
                  {{ getUserDisplayName(user) }}
                </el-tag>
                <el-tag v-if="targetSelectedUsers.length > 12" type="info">
                  +{{ targetSelectedUsers.length - 12 }}
                </el-tag>
              </div>
            </el-form-item>
          </el-col>
          <el-col v-if="targetForm.targetId" :span="12">
            <el-form-item label="姓名">
              <el-input v-model.trim="targetForm.userName" placeholder="请输入" />
            </el-form-item>
          </el-col>
          <el-col v-if="targetForm.targetId" :span="12">
            <el-form-item label="手机号">
              <el-input v-model.trim="targetForm.phone" placeholder="请输入" />
            </el-form-item>
          </el-col>
          <el-col v-if="targetForm.targetId" :span="12">
            <el-form-item label="邮箱">
              <el-input v-model.trim="targetForm.email" placeholder="请输入" />
            </el-form-item>
          </el-col>
          <el-col v-if="targetForm.targetId" :span="12">
            <el-form-item label="证件类型">
              <el-input v-model.trim="targetForm.idCardType" placeholder="如身份证/护照" />
            </el-form-item>
          </el-col>
          <el-col v-if="targetForm.targetId" :span="12">
            <el-form-item label="证件号">
              <el-input v-model.trim="targetForm.idCard" placeholder="仅用于生成hash和后六位" show-password />
            </el-form-item>
          </el-col>
          <el-col v-if="targetForm.targetId" :span="12">
            <el-form-item label="证件后六位">
              <el-input v-model.trim="targetForm.idCardSuffix" placeholder="未填证件号时可手工填" />
            </el-form-item>
          </el-col>
          <el-col v-if="targetForm.targetId" :span="12">
            <el-form-item label="学校名称">
              <el-input v-model.trim="targetForm.schoolName" placeholder="请输入" />
            </el-form-item>
          </el-col>
          <el-col v-if="targetForm.targetId" :span="12">
            <el-form-item label="机构名称">
              <el-input v-model.trim="targetForm.orgName" placeholder="请输入" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="角色" prop="competitionRoleName">
              <el-select v-model="targetForm.competitionRoleName" placeholder="请选择角色" style="width: 100%">
                <el-option
                  v-for="item in targetRoleOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="座位/工位">
              <el-input v-model.trim="targetForm.seatNo" placeholder="可选" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="候场组编码">
              <el-input v-model="targetForm.waitingGroupCode" disabled placeholder="系统自动生成/无" />
            </el-form-item>
          </el-col>
          <!-- <el-col :span="12">
            <el-form-item label="候场组名称">
              <el-input v-model="targetForm.waitingGroupName" disabled placeholder="无" />
            </el-form-item>
          </el-col> -->
          <el-col :span="12">
            <el-form-item label="状态">
              <el-radio-group v-model="targetForm.status">
                <el-radio label="0">有效</el-radio>
                <el-radio label="1">停用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="targetDialogOpen = false">取 消</el-button>
          <el-button type="primary" :loading="targetSubmitting" @click="submitTargetForm">保 存</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog :title="bindTargetDialogTitle" v-model="bindTargetDialogOpen" width="640px" append-to-body>
      <el-alert
        type="info"
        :closable="false"
        show-icon
        title="可用逗号、空格或换行分隔多个值；重复绑定会由后端跳过。"
        style="margin-bottom: 12px"
      />
      <el-form label-width="120px">
        <el-form-item label="赛场安排">
          <el-input :model-value="selectedSchedule?.scheduleName || selectedSchedule?.scheduleId || '-'" disabled />
        </el-form-item>
        <el-form-item :label="bindTargetInputLabel">
          <el-input
            v-model="bindTargetForm.rawValues"
            type="textarea"
            :rows="6"
            :placeholder="bindTargetPlaceholder"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="bindTargetDialogOpen = false">取 消</el-button>
          <el-button type="primary" :loading="targetActionLoading" @click="submitBindTargetDialog">确认绑定</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog title="手工新增赛场对象" v-model="manualTargetDialogOpen" width="640px" append-to-body>
      <el-form ref="manualTargetFormRef" :model="manualTargetForm" :rules="manualTargetRules" label-width="120px">
        <el-form-item label="赛场安排">
          <el-input :model-value="selectedSchedule?.scheduleName || selectedSchedule?.scheduleId || '-'" disabled />
        </el-form-item>
        <el-form-item label="对象名称" prop="targetName">
          <el-input v-model.trim="manualTargetForm.targetName" placeholder="请输入现场临时对象名称" />
        </el-form-item>
        <el-form-item label="所属单位">
          <el-input v-model.trim="manualTargetForm.orgName" placeholder="可选" />
        </el-form-item>
        <el-form-item label="联系方式">
          <el-input v-model.trim="manualTargetForm.contactPhone" placeholder="可选" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="manualTargetForm.remark" type="textarea" :rows="3" placeholder="请输入" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="manualTargetDialogOpen = false">取 消</el-button>
          <el-button type="primary" :loading="targetActionLoading" @click="submitManualTargetDialog">保 存</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog title="按姓名批量排序" v-model="nameSequenceDialogOpen" width="640px" append-to-body>
      <el-alert
        type="info"
        :closable="false"
        show-icon
        title="按输入顺序匹配绑定对象中的人员姓名/对象名称。匹配到的对象排前，未匹配对象保留原相对顺序排在后面。"
        style="margin-bottom: 12px"
      />
      <el-form label-width="110px">
        <el-form-item label="赛场安排">
          <el-input :model-value="selectedSchedule?.scheduleName || selectedSchedule?.scheduleId || '-'" disabled />
        </el-form-item>
        <el-form-item label="姓名名单" required>
          <el-input
            v-model="nameSequenceForm.namesText"
            type="textarea"
            :rows="8"
            placeholder="例如：蒋承城，袁忠豪，李忠桂，李金苗&#10;也可以每行一个姓名"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="nameSequenceDialogOpen = false">取 消</el-button>
          <el-button type="primary" :loading="targetActionLoading" @click="submitNameSequenceDialog">自动排序</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog title="同步到评审现场场次" v-model="syncReviewSessionDialogOpen" width="920px" append-to-body>
      <el-alert
        type="warning"
        :closable="false"
        show-icon
        title="仅同步能匹配到评审对象的绑定项；不会改变扫码校验、专家分配或评分记录。"
        style="margin-bottom: 12px"
      />
      <el-form label-width="130px">
        <el-form-item label="赛场安排">
          <el-input :model-value="selectedSchedule?.scheduleName || selectedSchedule?.scheduleId || '-'" disabled />
        </el-form-item>
        <el-form-item label="已选评审场次" required>
          <el-input
            :model-value="selectedReviewSessionLabel"
            disabled
            placeholder="请在下方列表选择评审场次"
          />
        </el-form-item>
      </el-form>
      <el-form :model="reviewSessionQuery" :inline="true" label-width="78px">
        <el-form-item label="场次名称">
          <el-input v-model.trim="reviewSessionQuery.sessionName" placeholder="请输入" clearable style="width: 180px" @keyup.enter="handleReviewSessionQuery" />
        </el-form-item>
        <el-form-item label="场次编码">
          <el-input v-model.trim="reviewSessionQuery.sessionCode" placeholder="请输入" clearable style="width: 160px" @keyup.enter="handleReviewSessionQuery" />
        </el-form-item>
        <el-form-item label="活动ID">
          <el-input v-model.trim="reviewSessionQuery.activityId" placeholder="可选" clearable style="width: 130px" @keyup.enter="handleReviewSessionQuery" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleReviewSessionQuery">查询</el-button>
          <el-button icon="Refresh" @click="resetReviewSessionQuery">重置</el-button>
        </el-form-item>
      </el-form>
      <el-table
        v-loading="reviewSessionLoading"
        :data="reviewSessionList"
        highlight-current-row
        stripe
        @row-click="selectReviewSession"
      >
        <el-table-column label="选择" width="70" align="center">
          <template #default="{ row }">
            <el-radio :model-value="syncReviewSessionForm.sessionId" :label="row.id" @change="selectReviewSession(row)">&nbsp;</el-radio>
          </template>
        </el-table-column>
        <el-table-column label="场次名称" prop="sessionName" min-width="180" show-overflow-tooltip />
        <el-table-column label="场次编码" prop="sessionCode" min-width="130" show-overflow-tooltip />
        <el-table-column label="活动ID" prop="activityId" width="100" align="center" />
        <el-table-column label="轮次ID" prop="roundId" width="100" align="center" />
        <el-table-column label="地点" prop="location" min-width="150" show-overflow-tooltip />
        <el-table-column label="状态" prop="status" width="110" align="center" />
      </el-table>
      <pagination
        v-show="reviewSessionTotal > 0"
        :total="reviewSessionTotal"
        v-model:page="reviewSessionQuery.pageNum"
        v-model:limit="reviewSessionQuery.pageSize"
        @pagination="getReviewSessionList"
      />
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="syncReviewSessionDialogOpen = false">取 消</el-button>
          <el-button type="primary" :loading="targetActionLoading" @click="submitSyncReviewSessionDialog">开始同步</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog title="选择用户" v-model="userDialogOpen" width="980px" append-to-body>
      <el-form ref="userQueryRef" :model="userQuery" :inline="true" label-width="78px">
        <el-form-item label="用户账号" prop="userName">
          <el-input
            v-model.trim="userQuery.userName"
            placeholder="请输入"
            clearable
            style="width: 160px"
            @keyup.enter="handleUserQuery"
          />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input
            v-model.trim="userQuery.realName"
            placeholder="请输入"
            clearable
            style="width: 150px"
            @keyup.enter="handleUserQuery"
          />
        </el-form-item>
        <el-form-item label="手机号" prop="phonenumber">
          <el-input
            v-model.trim="userQuery.phonenumber"
            placeholder="请输入"
            clearable
            style="width: 150px"
            @keyup.enter="handleUserQuery"
          />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input
            v-model.trim="userQuery.email"
            placeholder="请输入"
            clearable
            style="width: 180px"
            @keyup.enter="handleUserQuery"
          />
        </el-form-item>
        <el-form-item label="学校" prop="schoolName">
          <el-input
            v-model.trim="userQuery.schoolName"
            placeholder="请输入"
            clearable
            style="width: 180px"
            @keyup.enter="handleUserQuery"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="userQuery.status" placeholder="请选择" clearable style="width: 120px">
            <el-option label="正常" value="0" />
            <el-option label="停用" value="1" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleUserQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetUserQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <div v-if="userDialogBatchMode" class="user-batch-query">
        <div class="user-batch-query__input">
          <el-input
            v-model="userBatchPhoneText"
            type="textarea"
            :rows="3"
            placeholder="手机号批量查询，每行一个手机号"
            clearable
          />
        </div>
        <div class="user-batch-query__actions">
          <el-button
            type="primary"
            icon="Search"
            :loading="userBatchQueryLoading"
            @click="handleBatchPhoneQuery"
          >
            批量查询
          </el-button>
          <span class="muted">{{ userBatchQuerySummary }}</span>
        </div>
      </div>
      <div v-if="userBatchNotFoundPhones.length > 0" class="user-batch-not-found">
        <span class="user-batch-not-found__label">未搜索到手机号（{{ userBatchNotFoundPhones.length }}）：</span>
        <el-tag
          v-for="phone in userBatchNotFoundPhones"
          :key="phone"
          type="warning"
          size="small"
        >
          {{ phone }}
        </el-tag>
      </div>

      <el-table
        ref="userTableRef"
        v-loading="userLoading || userBatchQueryLoading"
        :data="userList"
        row-key="userId"
        stripe
        highlight-current-row
        height="420"
        @selection-change="handleUserSelectionChange"
        @current-change="handleUserCurrentChange"
        @row-dblclick="handleUserRowDblClick"
      >
        <el-table-column v-if="userDialogBatchMode" type="selection" reserve-selection width="50" align="center" />
        <el-table-column label="用户账号" prop="userName" min-width="130" show-overflow-tooltip />
        <el-table-column label="姓名" min-width="120" show-overflow-tooltip>
          <template #default="{ row }">{{ getUserDisplayName(row) }}</template>
        </el-table-column>
        <el-table-column label="手机号" prop="phonenumber" min-width="120" show-overflow-tooltip />
        <el-table-column label="邮箱" prop="email" min-width="170" show-overflow-tooltip />
        <el-table-column label="学校/机构" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">{{ getUserSchoolName(row) || row.orgName || '-' }}</template>
        </el-table-column>
        <el-table-column label="认证状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getAuthStatusType(row?.authInfo?.authStatus)">
              {{ getAuthStatusLabel(row?.authInfo?.authStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="账号状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="String(row.status) === '0' ? 'success' : 'info'">
              {{ String(row.status) === '0' ? '正常' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
      <pagination
        v-show="userTotal > 0"
        :total="userTotal"
        v-model:page="userQuery.pageNum"
        v-model:limit="userQuery.pageSize"
        @pagination="getUserList"
      />

      <template #footer>
        <div class="dialog-footer user-dialog-footer">
          <span class="muted">{{ formatUserDialogFooterText() }}</span>
          <span>
            <el-button @click="userDialogOpen = false">取 消</el-button>
            <el-button type="primary" :disabled="isUserConfirmDisabled" @click="confirmUserSelection">确 定</el-button>
          </span>
        </div>
      </template>
    </el-dialog>

    <el-dialog title="选择用户组" v-model="groupDialogOpen" width="900px" append-to-body>
      <el-form ref="groupQueryRef" :model="groupQuery" :inline="true" label-width="96px">
        <el-form-item label="用户组名称" prop="name">
          <el-input
            v-model.trim="groupQuery.name"
            placeholder="请输入"
            clearable
            style="width: 180px"
            @keyup.enter="handleGroupQuery"
          />
        </el-form-item>
        <el-form-item label="用户组管理员" prop="groupManager">
          <el-input
            v-model.trim="groupQuery.groupManager"
            placeholder="请输入"
            clearable
            style="width: 180px"
            @keyup.enter="handleGroupQuery"
          />
        </el-form-item>
        <el-form-item label="关联身份" prop="identifyType">
          <el-select v-model="groupQuery.identifyType" placeholder="请选择" clearable style="width: 180px">
            <el-option label="教师（已通过教师认证）" value="teacher" />
            <el-option label="学生（已通过学生认证）" value="student" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleGroupQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetGroupQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table
        v-loading="groupLoading"
        :data="groupList"
        stripe
        highlight-current-row
        height="420"
        @current-change="handleGroupCurrentChange"
        @row-dblclick="handleGroupRowDblClick"
      >
        <el-table-column label="用户组名称" prop="name" min-width="180" show-overflow-tooltip />
        <el-table-column label="用户组管理员" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row?.groupManagerList?.map(item => item.userName).join(', ') || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="人数" prop="userIdCount" width="90" align="center" />
        <el-table-column label="用户组说明" prop="descripe" min-width="200" show-overflow-tooltip />
      </el-table>
      <pagination
        v-show="groupTotal > 0"
        :total="groupTotal"
        v-model:page="groupQuery.pageNum"
        v-model:limit="groupQuery.pageSize"
        @pagination="getGroupList"
      />

      <template #footer>
        <div class="dialog-footer user-dialog-footer">
          <span class="muted">{{ groupCurrentRow ? `当前选择：${groupCurrentRow.name}` : '未选择用户组' }}</span>
          <span>
            <el-button @click="groupDialogOpen = false">取 消</el-button>
            <el-button type="primary" :loading="groupImporting" :disabled="!groupCurrentRow" @click="confirmGroupSelection">确 定</el-button>
          </span>
        </div>
      </template>
    </el-dialog>

    <el-dialog title="维护证件" v-model="credentialDialogOpen" width="680px" append-to-body>
      <el-form ref="credentialFormRef" :model="credentialForm" label-width="120px">
        <el-form-item label="证件编号">
          <el-input v-model="credentialForm.credentialNo" disabled />
        </el-form-item>
        <el-form-item label="证件状态">
          <el-select v-model="credentialForm.credentialStatus" style="width: 100%">
            <el-option
              v-for="item in credentialStatusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="credentialForm.remark" type="textarea" :rows="3" placeholder="请输入" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="credentialDialogOpen = false">取 消</el-button>
          <el-button type="primary" @click="submitCredentialForm">保 存</el-button>
        </div>
      </template>
    </el-dialog>

    <SceneNoticeManagerDialog
      v-model="noticeManagerOpen"
      :mode="noticeManagerMode"
      :target="noticeManagerTarget"
      :targets="noticeManagerTargets"
      :competition-options="competitionOptions"
      :initial-series-id="noticeManagerSeriesId"
    />
  </div>
</template>

<script setup name="SceneSchedule">
import {
  listSceneSchedule,
  getSceneSchedule,
  addSceneSchedule,
  updateSceneSchedule,
  delSceneSchedule,
  matchSceneSchedule,
  listSceneTarget,
  addSceneTarget,
  updateSceneTarget,
  delSceneTarget,
  bindSceneReviewObjects,
  bindSceneTeams,
  bindScenePersons,
  addSceneManualTarget,
  saveSceneTargetSequence,
  autoGenerateSceneTargetSequence,
  sortSceneTargetSequenceByNames,
  syncSceneTargetsToReviewSession,
  listSceneCredential,
  generateSceneCredential,
  updateSceneCredential,
  delSceneCredential,
  listSceneVerifyLog
} from '@/api/tournament/sceneSchedule'
import { listReviewSession } from '@/api/review/session'
import { listUser } from '@/api/system/user'
import { systemUserGroupMangerList, systemUserGroupDetail } from '@/api/fileTask'
import { getSelectCompetitionList } from '@/api/certInterconnect/certConfig'
import { getCompetition, listCompetitionTracks } from '@/api/tournament/competition'
import Pagination from '@/components/Pagination'
import RightToolbar from '@/components/RightToolbar'
import modal from '@/plugins/modal'
import ResourceReservationTab from './components/ResourceReservationTab.vue'
import SceneNoticeManagerDialog from './components/SceneNoticeManagerDialog.vue'

const showSearch = ref(true)
const activeTab = ref('schedule')
const queryRef = ref(null)
const scheduleFormRef = ref(null)
const targetFormRef = ref(null)
const credentialFormRef = ref(null)
const userQueryRef = ref(null)
const userTableRef = ref(null)
const resourceTabRef = ref(null)

const credentialTypeOptions = [
  { label: '参赛证', value: 'PARTICIPANT', type: 'success' },
  { label: '教师证', value: 'TEACHER', type: 'warning' },
  { label: '专家证', value: 'EXPERT', type: 'primary' },
  { label: '工作人员证', value: 'STAFF', type: 'info' },
  { label: '贵宾证', value: 'VIP', type: 'danger' },
  { label: '临时证', value: 'TEMP', type: 'info' }
]
const credentialTypeLegacyOptions = [
  ...credentialTypeOptions,
  { label: '参赛证', value: 'COMPETITOR', type: 'success' }
]
const targetRoleOptions = [
  { label: '教师', value: 'TEACHER' },
  { label: '队员', value: 'MEMBER' },
  { label: '专家', value: 'EXPERT' },
  { label: '队长', value: 'CAPTAIN' },
  { label: '发资料工作人员', value: 'MATERIAL_STAFF' },
  { label: '签到工作人员', value: 'CHECKIN_STAFF' },
  { label: '志愿者', value: 'VOLUNTEER' }
]
const targetSourceOptions = [
  { label: '报名', value: 'APPLY' },
  { label: '导入', value: 'IMPORT' },
  { label: '手工', value: 'MANUAL' }
]
const targetTypeOptions = [
  { label: '评审对象', value: 'REVIEW_OBJECT', type: 'success' },
  { label: '团队', value: 'TEAM', type: 'primary' },
  { label: '报名人员', value: 'PERSON', type: 'warning' },
  { label: '系统用户', value: 'USER', type: 'info' },
  { label: '证件', value: 'CREDENTIAL', type: 'info' },
  { label: '手工对象', value: 'MANUAL', type: 'danger' }
]
const credentialStatusOptions = [
  { label: '有效', value: 'EFFECTIVE', type: 'success' },
  { label: '作废', value: 'REVOKED', type: 'danger' },
  { label: '过期', value: 'EXPIRED', type: 'info' }
]
const credentialScopeOptions = [
  { label: '大赛级', value: 'COMPETITION', type: 'primary' },
  { label: '赛场级', value: 'SCHEDULE', type: 'success' },
  { label: '贵宾', value: 'VIP', type: 'danger' },
  { label: '专家', value: 'EXPERT', type: 'warning' },
  { label: '工作人员', value: 'STAFF', type: 'info' },
  { label: '临时', value: 'TEMP', type: 'info' }
]
const operationTypeOptions = [
  { label: '核验', value: 'VERIFY' },
  { label: '报道签到', value: 'REPORT_SIGN' },
  { label: '资料领取', value: 'MATERIAL_RECEIVE' },
  { label: '候场确认', value: 'WAITING_CHECK_IN' },
  { label: '取消报道', value: 'CANCEL_REPORT_SIGN' },
  { label: '取消领取', value: 'CANCEL_MATERIAL_RECEIVE' },
  { label: '取消候场', value: 'CANCEL_WAITING_CHECK_IN' },
  { label: '专家评审入口', value: 'EXPERT_REVIEW_ENTRY' }
]
const operationResultOptions = [
  { label: '通过', value: 'PASS', type: 'success' },
  { label: '失败', value: 'FAIL', type: 'danger' },
  { label: '重复', value: 'DUPLICATE', type: 'warning' },
  { label: '异常', value: 'EXCEPTION', type: 'danger' }
]

const competitionOptions = ref([])
const competitionDetailLoading = ref(false)
const scheduleTrackLoading = ref(false)
const scheduleStageOptions = ref([])
const scheduleTrackOptions = ref([])
const selectedSchedule = ref(null)
const noticeManagerOpen = ref(false)
const noticeManagerMode = ref('ANNOUNCEMENT')
const noticeManagerTarget = ref(null)
const noticeManagerTargets = ref([])
const noticeManagerSeriesId = ref(undefined)
let competitionDetailRequestIndex = 0
let scheduleTrackRequestIndex = 0

const scheduleLoading = ref(false)
const scheduleList = ref([])
const scheduleTotal = ref(0)
const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  scheduleName: undefined,
  competitionSeriesId: undefined,
  status: undefined
})

const targetLoading = ref(false)
const targetList = ref([])
const targetTotal = ref(0)
const selectedTargetRows = ref([])
const targetQuery = ref({
  pageNum: 1,
  pageSize: 10,
  scheduleId: undefined,
  targetType: undefined,
  reviewObjectId: undefined,
  teamName: undefined,
  userName: undefined,
  certificateCode: undefined,
  credentialType: undefined,
  competitionRoleName: undefined
})

const credentialLoading = ref(false)
const credentialList = ref([])
const credentialTotal = ref(0)
const selectedCredentialRows = ref([])
const credentialQuery = ref({
  pageNum: 1,
  pageSize: 10,
  scheduleId: undefined,
  credentialNo: undefined,
  scopeType: undefined,
  teamName: undefined,
  userName: undefined,
  credentialStatus: undefined
})

const logLoading = ref(false)
const logList = ref([])
const logTotal = ref(0)
const logQuery = ref({
  pageNum: 1,
  pageSize: 10,
  scheduleId: undefined,
  credentialNo: undefined,
  operationType: undefined,
  operationResult: undefined
})

const scheduleDialogOpen = ref(false)
const scheduleDialogTitle = ref('')
const scheduleForm = ref({})
const scheduleRules = {
  scheduleName: [{ required: true, message: '请输入安排名称', trigger: 'blur' }],
  competitionSeriesId: [{ required: true, message: '请选择赛事', trigger: 'change' }]
}

const targetDialogOpen = ref(false)
const targetDialogTitle = ref('')
const targetForm = ref({})
const targetSelectedUsers = ref([])
const selectedUserGroup = ref(null)
const targetSubmitting = ref(false)
const targetActionLoading = ref(false)
const bindTargetDialogOpen = ref(false)
const bindTargetMode = ref('')
const bindTargetForm = ref({ rawValues: '' })
const manualTargetDialogOpen = ref(false)
const manualTargetFormRef = ref(null)
const manualTargetForm = ref({
  targetName: '',
  orgName: '',
  contactPhone: '',
  remark: ''
})
const nameSequenceDialogOpen = ref(false)
const nameSequenceForm = ref({
  namesText: ''
})
const syncReviewSessionDialogOpen = ref(false)
const syncReviewSessionForm = ref({
  sessionId: undefined
})
const selectedReviewSession = ref(null)
const reviewSessionLoading = ref(false)
const reviewSessionList = ref([])
const reviewSessionTotal = ref(0)
const reviewSessionQuery = ref({
  pageNum: 1,
  pageSize: 5,
  sessionName: undefined,
  sessionCode: undefined,
  activityId: undefined
})
const targetRules = {
  scheduleId: [{ required: true, message: '请选择赛场安排', trigger: 'change' }],
  credentialType: [{ required: true, message: '请选择证件类型', trigger: 'change' }],
  competitionRoleName: [{ required: true, message: '请选择角色', trigger: 'change' }]
}
const manualTargetRules = {
  targetName: [{ required: true, message: '请输入对象名称', trigger: 'blur' }]
}

const credentialDialogOpen = ref(false)
const credentialForm = ref({})

const userDialogOpen = ref(false)
const userLoading = ref(false)
const userList = ref([])
const userTotal = ref(0)
const userCurrentRow = ref(null)
const userDialogSelectedRows = ref([])
const syncingUserSelection = ref(false)
const userBatchPhoneText = ref('')
const userBatchQueryLoading = ref(false)
const userBatchQuerySummary = ref('')
const userBatchNotFoundPhones = ref([])
const userQuery = ref({
  pageNum: 1,
  pageSize: 10,
  userName: undefined,
  nickName: undefined,
  phonenumber: undefined,
  email: undefined,
  schoolName: undefined,
  status: '0'
})

const groupDialogOpen = ref(false)
const groupLoading = ref(false)
const groupImporting = ref(false)
const groupList = ref([])
const groupTotal = ref(0)
const groupCurrentRow = ref(null)
const groupQueryRef = ref(null)
const groupQuery = ref({
  pageNum: 1,
  pageSize: 10,
  name: undefined,
  groupManager: undefined,
  identifyType: undefined
})

function tagLabel(options, value) {
  const item = options.find(option => option.value === value)
  return item ? item.label : (value || '-')
}

function tagType(options, value) {
  const item = options.find(option => option.value === value)
  return item ? item.type : 'info'
}

function credentialTypeLabel(value) {
  return tagLabel(credentialTypeLegacyOptions, value)
}

function credentialTypeTagType(value) {
  return tagType(credentialTypeLegacyOptions, value)
}

function normalizeCredentialType(value) {
  if (value === 'COMPETITOR') return 'PARTICIPANT'
  return value || ''
}

function targetRoleLabel(value) {
  return tagLabel(targetRoleOptions, value)
}

function targetTypeLabel(value) {
  return tagLabel(targetTypeOptions, value)
}

function targetTypeTagType(value) {
  return tagType(targetTypeOptions, value)
}

function inferTargetType(row) {
  if (row?.targetType) return row.targetType
  if (row?.reviewObjectId) return 'REVIEW_OBJECT'
  if (row?.memberId) return 'PERSON'
  if (row?.teamCode) return 'TEAM'
  if (row?.userId) return 'USER'
  return 'MANUAL'
}

function targetDisplayName(row) {
  return row?.targetName || row?.teamName || row?.userName || row?.sourceBizId || '-'
}

function normalizeTargetRole(value) {
  const map = {
    TEACHER: 'TEACHER',
    MEMBER: 'MEMBER',
    EXPERT: 'EXPERT',
    CAPTAIN: 'CAPTAIN',
    MATERIAL_STAFF: 'MATERIAL_STAFF',
    CHECKIN_STAFF: 'CHECKIN_STAFF',
    VOLUNTEER: 'VOLUNTEER',
    教师: 'TEACHER',
    指导教师: 'TEACHER',
    队员: 'MEMBER',
    专家: 'EXPERT',
    队长: 'CAPTAIN',
    发资料工作人员: 'MATERIAL_STAFF',
    资料工作人员: 'MATERIAL_STAFF',
    签到工作人员: 'CHECKIN_STAFF',
    志愿者: 'VOLUNTEER',
    赛场志愿者: 'VOLUNTEER'
  }
  return map[value] || ''
}

const bindTargetDialogTitle = computed(() => {
  const label = targetTypeLabel(bindTargetMode.value) || '绑定对象'
  return `添加${label}`
})
const bindTargetInputLabel = computed(() => {
  if (bindTargetMode.value === 'REVIEW_OBJECT') return '评审对象ID'
  if (bindTargetMode.value === 'TEAM') return '团队编号'
  if (bindTargetMode.value === 'PERSON') return '报名成员ID'
  return '对象标识'
})
const bindTargetPlaceholder = computed(() => {
  if (bindTargetMode.value === 'REVIEW_OBJECT') return '例如：10001,10002 或每行一个评审对象ID'
  if (bindTargetMode.value === 'TEAM') return '例如：T001,T002 或每行一个团队编号'
  if (bindTargetMode.value === 'PERSON') return '例如：1001,1002 或每行一个报名成员ID'
  return '请输入对象标识'
})
const selectedReviewSessionLabel = computed(() => {
  const session = selectedReviewSession.value
  if (!session) return ''
  return `${session.sessionName || '未命名场次'}（ID：${session.id}）`
})

function formatRange(start, end) {
  if (!start && !end) return '-'
  return `${start || '-'} ~ ${end || '-'}`
}

function getUserDisplayName(row) {
  return row?.authInfo?.realName || row?.realName || row?.nickName || row?.userName || '-'
}

function getUserPhone(row) {
  return row?.phonenumber || row?.phoneNumber || row?.phone || ''
}

function getUserSchoolName(row) {
  return row?.schoolName || row?.identityInfoList?.[0]?.schoolName || ''
}

function getUserId(row) {
  return row?.userId === undefined || row?.userId === null ? '' : String(row.userId)
}

function normalizePhoneValue(value) {
  return value === undefined || value === null ? '' : String(value).replace(/\s/g, '')
}

function parseBatchPhoneList(value) {
  const uniquePhones = []
  const phoneSet = new Set()
  String(value || '').split(/[\r\n]+/).forEach(item => {
    const phone = normalizePhoneValue(item)
    if (!phone || phoneSet.has(phone)) return
    phoneSet.add(phone)
    uniquePhones.push(phone)
  })
  return uniquePhones
}

function getAuthStatusLabel(value) {
  const statusMap = {
    1: '未实名',
    2: '审核中',
    3: '已驳回',
    4: '待审核',
    5: '已认证',
    6: '认证失败'
  }
  return statusMap[value] || value || '-'
}

function getAuthStatusType(value) {
  if (value === '5' || value === 5) return 'success'
  if (value === '6' || value === 6 || value === '3' || value === 3) return 'danger'
  if (value === '2' || value === 2 || value === '4' || value === 4) return 'warning'
  return 'info'
}

const userDialogBatchMode = computed(() => !targetForm.value?.targetId)
const hasSelectedTargetUsers = computed(() => {
  return targetSelectedUsers.value.length > 0 || !!targetForm.value?.userId
})
const isUserConfirmDisabled = computed(() => {
  if (userDialogBatchMode.value) {
    return userDialogSelectedRows.value.length === 0
  }
  return !userCurrentRow.value
})

function formatSelectedTargetUserLabel() {
  if (targetSelectedUsers.value.length > 0) {
    const names = targetSelectedUsers.value.slice(0, 3).map(getUserDisplayName).join('、')
    const suffix = targetSelectedUsers.value.length > 3 ? `等 ${targetSelectedUsers.value.length} 人` : `${targetSelectedUsers.value.length} 人`
    return `${names}（${suffix}）`
  }
  if (!targetForm.value?.userId) return ''
  const name = targetForm.value.userName || ''
  return name ? `${name}（ID：${targetForm.value.userId}）` : `ID：${targetForm.value.userId}`
}

function formatSelectedUserGroupLabel() {
  if (!selectedUserGroup.value) return ''
  const count = selectedUserGroup.value.userIdCount || selectedUserGroup.value.userList?.length || 0
  return count ? `${selectedUserGroup.value.name}（${count} 人）` : selectedUserGroup.value.name
}

function formatUserDialogFooterText() {
  if (userDialogBatchMode.value) {
    return userDialogSelectedRows.value.length ? `已选择 ${userDialogSelectedRows.value.length} 人` : '未选择用户'
  }
  return userCurrentRow.value ? `当前选择：${getUserDisplayName(userCurrentRow.value)}` : '未选择用户'
}

function normalizeSelectValue(value) {
  return value === undefined || value === null ? '' : String(value)
}

function normalizeExistingRelationValue(value) {
  return value === undefined || value === null ? value : String(value)
}

function isSameSelectValue(left, right) {
  return normalizeSelectValue(left) === normalizeSelectValue(right)
}

function findScheduleCompetitionOption(competitionSeriesId) {
  return competitionOptions.value.find(item => isSameSelectValue(item.competitionSeriesId, competitionSeriesId))
}

function normalizeStageOptions(list) {
  return (Array.isArray(list) ? list : [])
    .filter(item => item?.stageId !== undefined && item?.stageId !== null)
    .map(item => ({
      ...item,
      stageId: normalizeSelectValue(item.stageId),
      stageName: item.stageName || ''
    }))
}

function normalizeTrackOptions(trackList) {
  return (Array.isArray(trackList) ? trackList : [])
    .map(item => ({
      id: normalizeSelectValue(item.competitionTrackId),
      label: item.competitionTrackName || '',
      raw: item
    }))
    .filter(item => item.id)
}

function clearScheduleCompetitionRelationFields() {
  scheduleForm.value.competitionStageId = ''
  scheduleForm.value.competitionStageName = ''
  scheduleForm.value.competitionTrackId = ''
  scheduleForm.value.competitionTrackName = ''
  scheduleForm.value.secondLevelCode = ''
  scheduleForm.value.secondLevelName = ''
}

function resetScheduleCompetitionOptions() {
  competitionDetailRequestIndex++
  scheduleTrackRequestIndex++
  competitionDetailLoading.value = false
  scheduleTrackLoading.value = false
  scheduleStageOptions.value = []
  scheduleTrackOptions.value = []
}

function syncScheduleSelectedNames() {
  const selectedStage = scheduleStageOptions.value.find(item => isSameSelectValue(item.stageId, scheduleForm.value.competitionStageId))
  if (selectedStage) {
    scheduleForm.value.competitionStageId = selectedStage.stageId
    scheduleForm.value.competitionStageName = selectedStage.stageName
  }

  const selectedTrack = scheduleTrackOptions.value.find(item => isSameSelectValue(item.id, scheduleForm.value.competitionTrackId))
  if (selectedTrack) {
    scheduleForm.value.competitionTrackId = selectedTrack.id
    scheduleForm.value.competitionTrackName = selectedTrack.label
  }

  scheduleForm.value.secondLevelCode = ''
}

function buildCompetitionDetailParams(selected) {
  const competitionSeriesId = selected?.competitionSeriesId ?? scheduleForm.value.competitionSeriesId
  if (competitionSeriesId === undefined || competitionSeriesId === null || competitionSeriesId === '') return null
  const params = { competitionSeriesId }
  const competitionId = selected?.competitionId ?? selected?.raw?.competitionId
  if (competitionId !== undefined && competitionId !== null && competitionId !== '') {
    params.competitionId = competitionId
  }
  return params
}

function applyCompetitionDetailOptions(detail, selected) {
  scheduleStageOptions.value = normalizeStageOptions(detail?.competitionStageList)
}

async function loadScheduleCompetitionDetail(selected, options = {}) {
  const { autoSelectSingleStage = false, keepSelected = false } = options
  const params = buildCompetitionDetailParams(selected)
  if (!params) {
    resetScheduleCompetitionOptions()
    return
  }

  const requestIndex = ++competitionDetailRequestIndex
  competitionDetailLoading.value = true
  try {
    const response = await getCompetition(params)
    if (requestIndex !== competitionDetailRequestIndex) return
    const detail = response.data || {}
    applyCompetitionDetailOptions(detail, selected)

    if (keepSelected) {
      syncScheduleSelectedNames()
    } else if (autoSelectSingleStage && scheduleStageOptions.value.length === 1) {
      scheduleForm.value.competitionStageId = scheduleStageOptions.value[0].stageId
      handleScheduleStageChange(scheduleStageOptions.value[0].stageId)
    }

    if (scheduleStageOptions.value.length === 0) {
      modal.msgWarning('当前赛事暂无阶段配置')
    }
  } catch (error) {
    if (requestIndex !== competitionDetailRequestIndex) return
    scheduleStageOptions.value = []
    modal.msgWarning('赛事详情获取失败，请稍后重试')
  } finally {
    if (requestIndex === competitionDetailRequestIndex) {
      competitionDetailLoading.value = false
    }
  }
}

async function loadScheduleTrackOptions(selected, options = {}) {
  const { keepSelected = false } = options
  const competitionSeriesId = selected?.competitionSeriesId ?? scheduleForm.value.competitionSeriesId
  if (competitionSeriesId === undefined || competitionSeriesId === null || competitionSeriesId === '') {
    scheduleTrackOptions.value = []
    return
  }

  const requestIndex = ++scheduleTrackRequestIndex
  scheduleTrackLoading.value = true
  try {
    const response = await listCompetitionTracks({
      pageNum: 1,
      pageSize: 9999,
      competitionSeriesId,
      checkStatus: '4'
    })
    if (requestIndex !== scheduleTrackRequestIndex) return
    scheduleTrackOptions.value = normalizeTrackOptions(response.rows || [])
    if (keepSelected) {
      syncScheduleSelectedNames()
    }
  } catch (error) {
    if (requestIndex !== scheduleTrackRequestIndex) return
    scheduleTrackOptions.value = []
    modal.msgWarning('赛道列表获取失败，请稍后重试')
  } finally {
    if (requestIndex === scheduleTrackRequestIndex) {
      scheduleTrackLoading.value = false
    }
  }
}

function resetScheduleForm() {
  resetScheduleCompetitionOptions()
  scheduleForm.value = {
    scheduleId: undefined,
    scheduleName: '',
    competitionSeriesId: undefined,
    competitionName: '',
    competitionStageId: '',
    competitionStageName: '',
    competitionTrackId: '',
    competitionTrackName: '',
    secondLevelCode: '',
    secondLevelName: '',
    reportStartTime: undefined,
    reportEndTime: undefined,
    reportLocation: '',
    contestStartTime: undefined,
    contestEndTime: undefined,
    contestLocation: '',
    contestRoom: '',
    waitingStartTime: undefined,
    waitingEndTime: undefined,
    waitingLocation: '',
    waitingGroupName: '',
    materialLocation: '',
    notice: '',
    status: '0'
  }
}

function resetTargetForm() {
  targetSelectedUsers.value = []
  selectedUserGroup.value = null
  targetForm.value = {
    targetId: undefined,
    scheduleId: selectedSchedule.value?.scheduleId,
    targetSource: 'MANUAL',
    targetType: 'USER',
    reviewObjectId: undefined,
    targetName: '',
    teamCode: '',
    teamName: '',
    memberId: undefined,
    certificateCode: '',
    sequenceNo: undefined,
    userId: undefined,
    userName: '',
    phone: '',
    email: '',
    idCardType: '',
    idCard: '',
    idCardSuffix: '',
    schoolName: '',
    orgName: '',
    credentialType: '',
    competitionRoleName: '',
    seatNo: '',
    waitingGroupCode: selectedSchedule.value?.waitingGroupCode || '',
    waitingGroupName: selectedSchedule.value?.waitingGroupName || '',
    status: '0'
  }
}

function getScheduleList() {
  scheduleLoading.value = true
  listSceneSchedule(queryParams.value).then(response => {
    scheduleList.value = response.rows || []
    scheduleTotal.value = response.total || 0
    if (!selectedSchedule.value && scheduleList.value.length > 0) {
      setSelectedSchedule(scheduleList.value[0], false)
    }
  }).finally(() => {
    scheduleLoading.value = false
  })
}

function getTargetList() {
  targetLoading.value = true
  listSceneTarget(targetQuery.value).then(response => {
    targetList.value = response.rows || []
    targetTotal.value = response.total || 0
  }).finally(() => {
    targetLoading.value = false
  })
}

function getCredentialList() {
  credentialLoading.value = true
  listSceneCredential(credentialQuery.value).then(response => {
    credentialList.value = response.rows || []
    credentialTotal.value = response.total || 0
  }).finally(() => {
    credentialLoading.value = false
  })
}

function getLogList() {
  logLoading.value = true
  listSceneVerifyLog(logQuery.value).then(response => {
    logList.value = response.rows || []
    logTotal.value = response.total || 0
  }).finally(() => {
    logLoading.value = false
  })
}

function getUserList() {
  userLoading.value = true
  listUser(userQuery.value).then(response => {
    userList.value = response.rows || []
    userTotal.value = response.total || 0
    nextTick(() => {
      if (!userTableRef.value) return
      if (userDialogBatchMode.value) {
        syncUserTableSelection()
        return
      }
      if (!targetForm.value?.userId) return
      const selected = userList.value.find(item => String(item.userId) === String(targetForm.value.userId))
      if (selected) {
        userTableRef.value.setCurrentRow(selected)
      }
    })
  }).finally(() => {
    userLoading.value = false
  })
}

function getGroupList() {
  groupLoading.value = true
  systemUserGroupMangerList(groupQuery.value).then(response => {
    groupList.value = response.rows || []
    groupTotal.value = response.total || 0
  }).finally(() => {
    groupLoading.value = false
  })
}

function resetUserQuery() {
  userQueryRef.value?.resetFields()
  userBatchPhoneText.value = ''
  userBatchQuerySummary.value = ''
  userBatchNotFoundPhones.value = []
  userQuery.value = {
    pageNum: 1,
    pageSize: 10,
    userName: undefined,
    nickName: undefined,
    phonenumber: undefined,
    email: undefined,
    schoolName: undefined,
    status: '0'
  }
  getUserList()
}

function handleGroupQuery() {
  groupQuery.value.pageNum = 1
  getGroupList()
}

function resetGroupQuery() {
  groupQueryRef.value?.resetFields()
  groupQuery.value = {
    pageNum: 1,
    pageSize: 10,
    name: undefined,
    groupManager: undefined,
    identifyType: undefined
  }
  getGroupList()
}

function loadCompetitionOptions() {
  getSelectCompetitionList().then(response => {
    const data = response.data || []
    competitionOptions.value = data.map(item => ({
      competitionId: item.competitionId,
      competitionSeriesId: item.competitionSeriesId,
      competitionSeriesName: item.competitionSeriesName,
      competitionName: `${item.competitionSeriesName || ''}${item.competitionName || ''}`,
      raw: item
    }))
  })
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getScheduleList()
}

function openAnnouncementManager() {
  noticeManagerMode.value = 'ANNOUNCEMENT'
  noticeManagerTarget.value = null
  noticeManagerTargets.value = []
  noticeManagerSeriesId.value = queryParams.value.competitionSeriesId || selectedSchedule.value?.competitionSeriesId
  noticeManagerOpen.value = true
}

function openPersonalNoticeManager(row) {
  if (!row?.targetId) {
    modal.msgWarning('当前绑定对象缺少对象ID，无法添加个人通知')
    return
  }
  if (!row.userId && !row.memberId) {
    modal.msgWarning('当前绑定对象未关联平台用户或报名成员，用户端无法安全接收通知，请先完善人员绑定关系')
    return
  }
  noticeManagerMode.value = 'PERSONAL'
  noticeManagerTarget.value = { ...row }
  noticeManagerTargets.value = []
  noticeManagerSeriesId.value = row.competitionSeriesId
  noticeManagerOpen.value = true
}

function openBatchPersonalNoticeManager() {
  if (selectedTargetRows.value.length === 0) {
    modal.msgWarning('请先选择绑定对象下的人员')
    return
  }
  const validTargets = selectedTargetRows.value.filter(isReceivableNoticeTarget)
  const invalidCount = selectedTargetRows.value.length - validTargets.length
  if (validTargets.length === 0) {
    modal.msgWarning('已选人员均缺少平台用户或报名成员标识，无法创建个人通知')
    return
  }
  if (invalidCount > 0) {
    modal.msgWarning(`已忽略 ${invalidCount} 个缺少接收标识的人员`)
  }
  if (validTargets.length === 1) {
    openPersonalNoticeManager(validTargets[0])
    return
  }
  noticeManagerMode.value = 'PERSONAL'
  noticeManagerTarget.value = null
  noticeManagerTargets.value = validTargets.map(item => ({ ...item }))
  noticeManagerSeriesId.value = validTargets[0]?.competitionSeriesId
  noticeManagerOpen.value = true
}

function isReceivableNoticeTarget(row) {
  return !!(row?.targetId && (row.userId || row.memberId))
}

function resetQuery() {
  queryRef.value?.resetFields()
  queryParams.value = {
    pageNum: 1,
    pageSize: 10,
    scheduleName: undefined,
    competitionSeriesId: undefined,
    status: undefined
  }
  getScheduleList()
}

function refreshActiveTab() {
  if (activeTab.value === 'schedule') getScheduleList()
  if (activeTab.value === 'target') getTargetList()
  if (activeTab.value === 'credential') getCredentialList()
  if (activeTab.value === 'resource') resourceTabRef.value?.refresh()
  if (activeTab.value === 'log') getLogList()
}

function handleTabChange(name) {
  if (name === 'target') getTargetList()
  if (name === 'credential') getCredentialList()
  if (name === 'resource') resourceTabRef.value?.refresh()
  if (name === 'log') getLogList()
}

function setSelectedSchedule(row, syncTabQueries = true) {
  selectedSchedule.value = row
  if (syncTabQueries && row) {
    targetQuery.value.scheduleId = row.scheduleId
    credentialQuery.value.scheduleId = row.scheduleId
    logQuery.value.scheduleId = row.scheduleId
  }
}

function handleCurrentScheduleChange(row) {
  if (row) {
    setSelectedSchedule(row)
  }
}

function handleViewTargets(row) {
  setSelectedSchedule(row)
  activeTab.value = 'target'
  getTargetList()
}

function handleViewCredentials(row) {
  setSelectedSchedule(row)
  activeTab.value = 'credential'
  getCredentialList()
}

function handleAddSchedule() {
  resetScheduleForm()
  scheduleDialogTitle.value = '新增赛场安排'
  scheduleDialogOpen.value = true
  nextTick(() => scheduleFormRef.value?.clearValidate())
}

function handleUpdateSchedule(row) {
  resetScheduleCompetitionOptions()
  scheduleForm.value = {
    ...row,
    competitionStageId: normalizeExistingRelationValue(row.competitionStageId),
    competitionTrackId: normalizeExistingRelationValue(row.competitionTrackId),
    secondLevelCode: normalizeExistingRelationValue(row.secondLevelCode)
  }
  scheduleDialogTitle.value = '修改赛场安排'
  scheduleDialogOpen.value = true
  nextTick(() => scheduleFormRef.value?.clearValidate())
  const selected = findScheduleCompetitionOption(scheduleForm.value.competitionSeriesId) || {
    competitionSeriesId: scheduleForm.value.competitionSeriesId,
    competitionName: scheduleForm.value.competitionName,
    raw: {}
  }
  if (selected?.competitionSeriesId) {
    if (!scheduleForm.value.competitionName && selected.competitionName) {
      scheduleForm.value.competitionName = selected.competitionName
    }
    loadScheduleCompetitionDetail(selected, { keepSelected: true })
    loadScheduleTrackOptions(selected, { keepSelected: true })
  }
}

function handleScheduleCompetitionChange(value) {
  const selected = findScheduleCompetitionOption(value)
  scheduleForm.value.competitionName = selected ? selected.competitionName : ''
  clearScheduleCompetitionRelationFields()
  resetScheduleCompetitionOptions()
  if (selected) {
    loadScheduleCompetitionDetail(selected, { autoSelectSingleStage: true })
    loadScheduleTrackOptions(selected)
  }
}

function handleScheduleStageChange(value) {
  const selected = scheduleStageOptions.value.find(item => isSameSelectValue(item.stageId, value))
  if (!selected) {
    scheduleForm.value.competitionStageId = ''
    scheduleForm.value.competitionStageName = ''
    return
  }
  scheduleForm.value.competitionStageId = selected.stageId
  scheduleForm.value.competitionStageName = selected.stageName
  if (selected.stageStartTime) {
    scheduleForm.value.contestStartTime = selected.stageStartTime
  }
  if (selected.stageEndTime) {
    scheduleForm.value.contestEndTime = selected.stageEndTime
  }
}

function handleScheduleTrackChange(value) {
  const selected = scheduleTrackOptions.value.find(item => isSameSelectValue(item.id, value))
  if (!selected) {
    scheduleForm.value.competitionTrackId = ''
    scheduleForm.value.competitionTrackName = ''
    return
  }
  scheduleForm.value.competitionTrackId = selected.id
  scheduleForm.value.competitionTrackName = selected.label
}

function handleScheduleSecondLevelNameInput() {
  scheduleForm.value.secondLevelCode = ''
}

function buildSchedulePayload() {
  return {
    scheduleId: scheduleForm.value.scheduleId,
    scheduleName: scheduleForm.value.scheduleName,
    competitionSeriesId: scheduleForm.value.competitionSeriesId,
    competitionName: scheduleForm.value.competitionName,
    competitionStageId: scheduleForm.value.competitionStageId,
    competitionStageName: scheduleForm.value.competitionStageName,
    competitionTrackId: scheduleForm.value.competitionTrackId,
    competitionTrackName: scheduleForm.value.competitionTrackName,
    secondLevelCode: scheduleForm.value.secondLevelCode,
    secondLevelName: scheduleForm.value.secondLevelName,
    reportStartTime: scheduleForm.value.reportStartTime,
    reportEndTime: scheduleForm.value.reportEndTime,
    reportLocation: scheduleForm.value.reportLocation,
    contestStartTime: scheduleForm.value.contestStartTime,
    contestEndTime: scheduleForm.value.contestEndTime,
    contestLocation: scheduleForm.value.contestLocation,
    contestRoom: scheduleForm.value.contestRoom,
    waitingStartTime: scheduleForm.value.waitingStartTime,
    waitingEndTime: scheduleForm.value.waitingEndTime,
    waitingLocation: scheduleForm.value.waitingLocation,
    waitingGroupName: scheduleForm.value.waitingGroupName,
    materialLocation: scheduleForm.value.materialLocation,
    notice: scheduleForm.value.notice,
    status: scheduleForm.value.status,
    remark: scheduleForm.value.remark
  }
}

function submitScheduleForm() {
  scheduleFormRef.value.validate(valid => {
    if (!valid) return
    scheduleForm.value.secondLevelCode = ''
    const payload = buildSchedulePayload()
    const request = scheduleForm.value.scheduleId ? updateSceneSchedule : addSceneSchedule
    request(payload).then(() => {
      modal.msgSuccess(scheduleForm.value.scheduleId ? '修改成功' : '新增成功')
      scheduleDialogOpen.value = false
      getScheduleList()
    })
  })
}

function handleDeleteSchedule(row) {
  modal.confirm(`确定删除安排“${row.scheduleName}”吗？`).then(() => {
    return delSceneSchedule(row.scheduleId)
  }).then(() => {
    modal.msgSuccess('删除成功')
    if (selectedSchedule.value?.scheduleId === row.scheduleId) {
      selectedSchedule.value = null
    }
    getScheduleList()
  }).catch(() => {})
}

function handleMatchSelected() {
  if (!selectedSchedule.value) return
  handleMatch(selectedSchedule.value)
}

function handleMatch(row) {
  modal.confirm(`将当前比赛已报名成功人员全部自动导入，确定继续吗？`).then(() => {
    return matchSceneSchedule(row.scheduleId)
  }).then(response => {
    const data = response.data || {}
    modal.msgSuccess(`匹配完成，新增 ${data.matchedCount || 0} 条，跳过 ${data.skippedCount || 0} 条`)
    setSelectedSchedule(row)
    activeTab.value = 'target'
    getTargetList()
  }).catch(() => {})
}

function handleGenerateSelectedSchedule() {
  if (!selectedSchedule.value) return
  handleGenerateSchedule(selectedSchedule.value)
}

function handleGenerateSchedule(row) {
  modal.confirm(`确定为安排“${row.scheduleName}”下的有效人员生成证件吗？`).then(() => {
    return generateSceneCredential({
      scheduleId: row.scheduleId,
      regenerate: false
    })
  }).then(response => {
    modal.msgSuccess(`生成成功，新增 ${response.data || 0} 张证件`)
    setSelectedSchedule(row)
    activeTab.value = 'credential'
    getCredentialList()
  }).catch(() => {})
}

function handleTargetQuery() {
  targetQuery.value.pageNum = 1
  getTargetList()
}

function resetTargetQuery() {
  targetQuery.value = {
    pageNum: 1,
    pageSize: 10,
    scheduleId: selectedSchedule.value?.scheduleId,
    targetType: undefined,
    reviewObjectId: undefined,
    teamName: undefined,
    userName: undefined,
    certificateCode: undefined,
    credentialType: undefined,
    competitionRoleName: undefined
  }
  getTargetList()
}

function handleTargetSelectionChange(rows) {
  selectedTargetRows.value = rows
}

function handleCredentialSelectionChange(rows) {
  selectedCredentialRows.value = rows
}

function handleUserQuery() {
  userBatchQuerySummary.value = ''
  userBatchNotFoundPhones.value = []
  userQuery.value.pageNum = 1
  getUserList()
}

async function handleBatchPhoneQuery() {
  const phones = parseBatchPhoneList(userBatchPhoneText.value)
  if (phones.length === 0) {
    modal.msgWarning('请输入需要查询的手机号')
    return
  }
  userBatchQueryLoading.value = true
  userBatchQuerySummary.value = ''
  userBatchNotFoundPhones.value = []
  const matchedUsers = []
  const unmatchedPhones = []
  const failedPhones = []
  try {
    for (const phone of phones) {
      try {
        const response = await listUser({
          pageNum: 1,
          pageSize: 20,
          phonenumber: phone,
          status: userQuery.value.status
        })
        const rows = response.rows || []
        const exactRows = rows.filter(row => normalizePhoneValue(getUserPhone(row)) === phone)
        if (exactRows.length > 0) {
          matchedUsers.push(...exactRows)
        } else {
          unmatchedPhones.push(phone)
        }
      } catch (error) {
        failedPhones.push(phone)
      }
    }
    const uniqueMatchedUsers = mergeUsers(matchedUsers)
    userList.value = uniqueMatchedUsers
    userTotal.value = uniqueMatchedUsers.length
    userDialogSelectedRows.value = mergeUsers([...userDialogSelectedRows.value, ...uniqueMatchedUsers])
    userBatchNotFoundPhones.value = unmatchedPhones
    await nextTick()
    syncUserTableSelection()
    const summaryParts = [`匹配 ${uniqueMatchedUsers.length} 人`]
    if (unmatchedPhones.length > 0) summaryParts.push(`未匹配 ${unmatchedPhones.length} 个`)
    if (failedPhones.length > 0) summaryParts.push(`查询失败 ${failedPhones.length} 个`)
    userBatchQuerySummary.value = summaryParts.join('，')
    if (uniqueMatchedUsers.length > 0) {
      modal.msgSuccess(`批量查询完成，${userBatchQuerySummary.value}`)
    } else if (failedPhones.length > 0) {
      modal.msgWarning('批量查询失败，请稍后重试')
    } else {
      modal.msgWarning('未查询到匹配用户')
    }
  } finally {
    userBatchQueryLoading.value = false
  }
}

function openUserSelectDialog() {
  userDialogOpen.value = true
  userCurrentRow.value = null
  userDialogSelectedRows.value = userDialogBatchMode.value ? [...targetSelectedUsers.value] : []
  resetUserQuery()
}

function handleUserCurrentChange(row) {
  userCurrentRow.value = row
}

function handleUserRowDblClick(row) {
  if (userDialogBatchMode.value) {
    const selected = userDialogSelectedRows.value.some(item => getUserId(item) === getUserId(row))
    userTableRef.value?.toggleRowSelection(row, !selected)
    return
  }
  userCurrentRow.value = row
  confirmUserSelection()
}

function syncUserTableSelection() {
  if (!userTableRef.value) return
  syncingUserSelection.value = true
  userTableRef.value.clearSelection()
  userList.value.forEach(row => {
    const selected = userDialogSelectedRows.value.some(item => getUserId(item) === getUserId(row))
    if (selected) {
      userTableRef.value.toggleRowSelection(row, true)
    }
  })
  nextTick(() => {
    syncingUserSelection.value = false
  })
}

function mergeUsers(users) {
  const uniqueMap = new Map()
  users.filter(item => getUserId(item)).forEach(item => {
    uniqueMap.set(getUserId(item), item)
  })
  return Array.from(uniqueMap.values())
}

function handleUserSelectionChange(rows) {
  if (!userDialogBatchMode.value || syncingUserSelection.value) return
  const currentPageIds = new Set(userList.value.map(getUserId).filter(Boolean))
  const preservedRows = userDialogSelectedRows.value.filter(item => !currentPageIds.has(getUserId(item)))
  userDialogSelectedRows.value = mergeUsers([...preservedRows, ...rows])
}

function confirmUserSelection() {
  if (userDialogBatchMode.value) {
    applySelectedTargetUsers(userDialogSelectedRows.value)
    userDialogOpen.value = false
    return
  }
  if (!userCurrentRow.value) return
  const row = userCurrentRow.value
  targetForm.value.userId = row.userId
  targetForm.value.userName = getUserDisplayName(row)
  targetForm.value.phone = getUserPhone(row)
  targetForm.value.email = row.email || ''
  targetForm.value.schoolName = getUserSchoolName(row)
  targetForm.value.orgName = row.orgName || ''
  userDialogOpen.value = false
}

function applySelectedTargetUsers(users) {
  targetSelectedUsers.value = mergeUsers(users)
  const firstUser = targetSelectedUsers.value[0]
  if (firstUser) {
    targetForm.value.userId = firstUser.userId
    targetForm.value.userName = getUserDisplayName(firstUser)
    targetForm.value.phone = getUserPhone(firstUser)
    targetForm.value.email = firstUser.email || ''
    targetForm.value.schoolName = getUserSchoolName(firstUser)
    targetForm.value.orgName = firstUser.orgName || ''
  } else {
    clearTargetUsers()
  }
}

function removeSelectedTargetUser(user) {
  targetSelectedUsers.value = targetSelectedUsers.value.filter(item => getUserId(item) !== getUserId(user))
  if (targetSelectedUsers.value.length === 0 || getUserId(targetForm.value) === getUserId(user)) {
    const firstUser = targetSelectedUsers.value[0]
    if (firstUser) {
      applySelectedTargetUsers(targetSelectedUsers.value)
    } else {
      clearTargetUsers()
    }
  }
}

function clearTargetUsers() {
  targetSelectedUsers.value = []
  selectedUserGroup.value = null
  targetForm.value.userId = undefined
  targetForm.value.userName = ''
  targetForm.value.phone = ''
  targetForm.value.email = ''
  targetForm.value.schoolName = ''
  targetForm.value.orgName = ''
}

function openGroupSelectDialog() {
  groupDialogOpen.value = true
  groupCurrentRow.value = null
  resetGroupQuery()
}

function handleGroupCurrentChange(row) {
  groupCurrentRow.value = row
}

function handleGroupRowDblClick(row) {
  groupCurrentRow.value = row
  confirmGroupSelection()
}

function confirmGroupSelection() {
  if (!groupCurrentRow.value) return
  groupImporting.value = true
  systemUserGroupDetail(groupCurrentRow.value.id).then(response => {
    const groupDetail = response.data || {}
    const whitelistUsers = groupDetail.userList || []
    if (whitelistUsers.length === 0) {
      modal.msgWarning('该用户组暂无白名单成员')
      return
    }
    selectedUserGroup.value = {
      ...groupCurrentRow.value,
      ...groupDetail,
      userIdCount: whitelistUsers.length
    }
    applySelectedTargetUsers([...targetSelectedUsers.value, ...whitelistUsers])
    modal.msgSuccess(`已添加 ${whitelistUsers.length} 名白名单成员`)
    groupDialogOpen.value = false
  }).finally(() => {
    groupImporting.value = false
  })
}

async function loadSceneScheduleForTarget(scheduleId) {
  if (!scheduleId) return null
  if (String(selectedSchedule.value?.scheduleId || '') === String(scheduleId)) {
    return selectedSchedule.value
  }
  const response = await getSceneSchedule(scheduleId)
  return response.data || null
}

function applyTargetWaitingGroupFromSchedule(schedule) {
  targetForm.value.waitingGroupCode = schedule?.waitingGroupCode || ''
  targetForm.value.waitingGroupName = schedule?.waitingGroupName || ''
}

async function handleAddTarget() {
  resetTargetForm()
  try {
    const schedule = await loadSceneScheduleForTarget(targetForm.value.scheduleId)
    if (schedule) {
      selectedSchedule.value = schedule
      applyTargetWaitingGroupFromSchedule(schedule)
    }
  } catch (error) {
    modal.msgWarning('赛场安排候场组信息获取失败，请稍后重试')
  }
  targetDialogTitle.value = '新增人员'
  targetDialogOpen.value = true
  nextTick(() => targetFormRef.value?.clearValidate())
}

function handleUpdateTarget(row) {
  targetSelectedUsers.value = []
  selectedUserGroup.value = null
  targetForm.value = {
    ...row,
    credentialType: normalizeCredentialType(row.credentialType),
    competitionRoleName: normalizeTargetRole(row.competitionRoleName),
    idCard: ''
  }
  targetDialogTitle.value = '修改人员'
  targetDialogOpen.value = true
  nextTick(() => targetFormRef.value?.clearValidate())
}

function buildBatchTargetPayload(user) {
  return {
    scheduleId: targetForm.value.scheduleId,
    targetSource: targetForm.value.targetSource || 'MANUAL',
    targetType: targetForm.value.targetType || 'USER',
    credentialType: normalizeCredentialType(targetForm.value.credentialType),
    userId: user.userId,
    userName: getUserDisplayName(user),
    phone: getUserPhone(user),
    email: user.email || '',
    schoolName: getUserSchoolName(user),
    orgName: user.orgName || '',
    competitionRoleName: targetForm.value.competitionRoleName || '',
    seatNo: targetForm.value.seatNo || '',
    status: targetForm.value.status || '0'
  }
}

function buildUpdateTargetPayload() {
  return {
    targetId: targetForm.value.targetId,
    scheduleId: targetForm.value.scheduleId,
    targetSource: targetForm.value.targetSource,
    targetType: targetForm.value.targetType,
    reviewObjectId: targetForm.value.reviewObjectId,
    targetName: targetForm.value.targetName,
    credentialType: normalizeCredentialType(targetForm.value.credentialType),
    teamCode: targetForm.value.teamCode,
    teamName: targetForm.value.teamName,
    memberId: targetForm.value.memberId,
    certificateCode: targetForm.value.certificateCode,
    sequenceNo: targetForm.value.sequenceNo,
    userId: targetForm.value.userId,
    userName: targetForm.value.userName,
    phone: targetForm.value.phone,
    email: targetForm.value.email,
    idCardType: targetForm.value.idCardType,
    idCard: targetForm.value.idCard,
    idCardSuffix: targetForm.value.idCardSuffix,
    schoolName: targetForm.value.schoolName,
    orgName: targetForm.value.orgName,
    competitionRoleName: targetForm.value.competitionRoleName,
    seatNo: targetForm.value.seatNo,
    status: targetForm.value.status,
    remark: targetForm.value.remark
  }
}

function submitTargetForm() {
  targetFormRef.value.validate(async valid => {
    if (!valid) return
    targetSubmitting.value = true
    if (!targetForm.value.targetId && targetSelectedUsers.value.length > 0) {
      const payload = targetSelectedUsers.value.map(buildBatchTargetPayload)
      let successCount = 0
      let skippedCount = 0
      let failCount = 0
      for (const item of payload) {
        try {
          const response = await addSceneTarget(item)
          const data = response.data || {}
          const hasResultCount = data.matchedCount !== undefined || data.skippedCount !== undefined
          successCount += hasResultCount ? (data.matchedCount || 0) : 1
          skippedCount += data.skippedCount || 0
        } catch (error) {
          failCount++
        }
      }
      if (successCount > 0) {
        const skippedText = skippedCount > 0 ? `，已存在 ${skippedCount} 条` : ''
        const failText = failCount > 0 ? `，失败 ${failCount} 条` : ''
        modal.msgSuccess(`新增成功 ${successCount} 条${skippedText}${failText}`)
        targetDialogOpen.value = false
        getTargetList()
      } else if (skippedCount > 0 && failCount === 0) {
        modal.msgWarning(`所选用户均已在当前安排中，无需重复添加`)
        targetDialogOpen.value = false
        getTargetList()
      } else if (skippedCount > 0) {
        modal.msgWarning(`已存在 ${skippedCount} 条，失败 ${failCount} 条`)
        getTargetList()
      } else if (failCount > 0) {
        modal.msgWarning('新增失败，请检查所选用户后重试')
      }
      targetSubmitting.value = false
      return
    }
    if (!targetForm.value.targetId) {
      modal.msgWarning('请选择用户')
      targetSubmitting.value = false
      return
    }
    updateSceneTarget(buildUpdateTargetPayload()).then(() => {
      modal.msgSuccess('修改成功')
      targetDialogOpen.value = false
      getTargetList()
    }).finally(() => {
      targetSubmitting.value = false
    })
  })
}

function handleDeleteTarget(row) {
  modal.confirm('确定删除该安排人员吗？').then(() => {
    return delSceneTarget(row.targetId)
  }).then(() => {
    modal.msgSuccess('删除成功')
    selectedTargetRows.value = []
    getTargetList()
    getCredentialList()
  }).catch(() => {})
}

function handleDeleteTargets() {
  if (selectedTargetRows.value.length === 0) return
  const targetIds = selectedTargetRows.value.map(item => item.targetId)
  modal.confirm(`确定删除已选 ${targetIds.length} 个人员吗？相关证件将同步删除。`).then(() => {
    return delSceneTarget(targetIds.join(','))
  }).then(() => {
    modal.msgSuccess('删除成功')
    selectedTargetRows.value = []
    getTargetList()
    getCredentialList()
  }).catch(() => {})
}

function handleGenerateTargets() {
  if (!selectedSchedule.value || selectedTargetRows.value.length === 0) return
  const targetIds = selectedTargetRows.value.map(item => item.targetId)
  modal.confirm(`确定为已选 ${targetIds.length} 个人员生成证件吗？`).then(() => {
    return generateSceneCredential({
      scheduleId: selectedSchedule.value.scheduleId,
      targetIds,
      regenerate: false
    })
  }).then(response => {
    modal.msgSuccess(`生成成功，新增 ${response.data || 0} 张证件`)
    activeTab.value = 'credential'
    getCredentialList()
  }).catch(() => {})
}

function splitInputValues(rawValues) {
  return String(rawValues || '')
    .split(/[\s,，、;；\n\r\t]+/)
    .map(item => item.trim())
    .filter(Boolean)
}

function normalizeSceneResult(data) {
  return {
    totalCount: data?.totalCount ?? 0,
    matchedCount: data?.matchedCount ?? data?.successCount ?? 0,
    skippedCount: data?.skippedCount ?? 0,
    failedCount: data?.failedCount ?? 0,
    warnings: data?.warnings || []
  }
}

function showSceneActionResult(data, fallback = '操作完成') {
  const result = normalizeSceneResult(data || {})
  const detail = `成功 ${result.matchedCount} 条，跳过 ${result.skippedCount} 条，失败 ${result.failedCount} 条`
  if (result.failedCount > 0) {
    modal.msgWarning(`${fallback}：${detail}`)
  } else {
    modal.msgSuccess(`${fallback}：${detail}`)
  }
  if (result.warnings.length > 0) {
    modal.msgWarning(result.warnings.slice(0, 3).join('；'))
  }
}

function ensureSelectedSchedule() {
  if (selectedSchedule.value?.scheduleId) return true
  modal.msgWarning('请先选择一个赛场安排')
  return false
}

function openBindTargetDialog(mode) {
  if (!ensureSelectedSchedule()) return
  bindTargetMode.value = mode
  bindTargetForm.value = { rawValues: '' }
  bindTargetDialogOpen.value = true
}

async function submitBindTargetDialog() {
  if (!ensureSelectedSchedule()) return
  const values = splitInputValues(bindTargetForm.value.rawValues)
  if (values.length === 0) {
    modal.msgWarning(`请输入${bindTargetInputLabel.value}`)
    return
  }

  targetActionLoading.value = true
  const scheduleId = selectedSchedule.value.scheduleId
  try {
    let response
    if (bindTargetMode.value === 'REVIEW_OBJECT') {
      const reviewObjectIds = values.map(item => Number(item)).filter(item => Number.isInteger(item) && item > 0)
      if (reviewObjectIds.length !== values.length) {
        modal.msgWarning('评审对象ID必须是正整数')
        return
      }
      response = await bindSceneReviewObjects(scheduleId, { reviewObjectIds })
    } else if (bindTargetMode.value === 'TEAM') {
      response = await bindSceneTeams(scheduleId, { teamCodes: values })
    } else if (bindTargetMode.value === 'PERSON') {
      response = await bindScenePersons(scheduleId, { memberIds: values })
    } else {
      modal.msgWarning('暂不支持该对象类型')
      return
    }
    showSceneActionResult(response.data, '绑定完成')
    bindTargetDialogOpen.value = false
    getTargetList()
  } finally {
    targetActionLoading.value = false
  }
}

function resetManualTargetForm() {
  manualTargetForm.value = {
    targetName: '',
    orgName: '',
    contactPhone: '',
    remark: ''
  }
}

function openManualTargetDialog() {
  if (!ensureSelectedSchedule()) return
  resetManualTargetForm()
  manualTargetDialogOpen.value = true
  nextTick(() => manualTargetFormRef.value?.clearValidate())
}

function submitManualTargetDialog() {
  if (!ensureSelectedSchedule()) return
  manualTargetFormRef.value?.validate(async valid => {
    if (!valid) return
    targetActionLoading.value = true
    try {
      const response = await addSceneManualTarget(selectedSchedule.value.scheduleId, manualTargetForm.value)
      showSceneActionResult(response.data, '新增完成')
      manualTargetDialogOpen.value = false
      getTargetList()
    } finally {
      targetActionLoading.value = false
    }
  })
}

function normalizeSequenceValue(value) {
  if (value === undefined || value === null || value === '') return null
  const num = Number(value)
  return Number.isInteger(num) && num > 0 ? num : value
}

function handleSaveTargetSequence() {
  if (!ensureSelectedSchedule()) return
  const invalid = targetList.value.find(item => {
    const value = normalizeSequenceValue(item.sequenceNo)
    return value !== null && (!Number.isInteger(value) || value <= 0)
  })
  if (invalid) {
    modal.msgWarning('顺序号必须为空或正整数')
    return
  }
  const payload = targetList.value.map(item => ({
    targetId: item.targetId,
    sequenceNo: normalizeSequenceValue(item.sequenceNo)
  }))
  targetActionLoading.value = true
  saveSceneTargetSequence(selectedSchedule.value.scheduleId, payload).then(response => {
    showSceneActionResult(response.data, '顺序保存完成')
    getTargetList()
  }).finally(() => {
    targetActionLoading.value = false
  })
}

function handleAutoGenerateSequence(overwriteExisting = false) {
  if (!ensureSelectedSchedule()) return
  const text = overwriteExisting ? '确定重新生成全部顺序吗？已有顺序会被覆盖。' : '确定为未排序对象自动补齐顺序吗？'
  modal.confirm(text).then(() => {
    targetActionLoading.value = true
    return autoGenerateSceneTargetSequence(selectedSchedule.value.scheduleId, { overwriteExisting })
  }).then(response => {
    showSceneActionResult(response.data, overwriteExisting ? '重排完成' : '自动顺序完成')
    getTargetList()
  }).finally(() => {
    targetActionLoading.value = false
  }).catch(() => {})
}

function openNameSequenceDialog() {
  if (!ensureSelectedSchedule()) return
  nameSequenceForm.value = { namesText: '' }
  nameSequenceDialogOpen.value = true
}

function submitNameSequenceDialog() {
  if (!ensureSelectedSchedule()) return
  const names = splitInputValues(nameSequenceForm.value.namesText)
  if (names.length === 0) {
    modal.msgWarning('请输入姓名名单')
    return
  }
  targetActionLoading.value = true
  sortSceneTargetSequenceByNames(selectedSchedule.value.scheduleId, {
    namesText: nameSequenceForm.value.namesText
  }).then(response => {
    showSceneActionResult(response.data, '按姓名排序完成')
    nameSequenceDialogOpen.value = false
    getTargetList()
  }).finally(() => {
    targetActionLoading.value = false
  })
}

function openSyncReviewSessionDialog() {
  if (!ensureSelectedSchedule()) return
  syncReviewSessionForm.value = { sessionId: undefined }
  selectedReviewSession.value = null
  resetReviewSessionQuery(false)
  syncReviewSessionDialogOpen.value = true
  getReviewSessionList()
}

function getReviewSessionList() {
  reviewSessionLoading.value = true
  listReviewSession(reviewSessionQuery.value).then(response => {
    reviewSessionList.value = response.rows || []
    reviewSessionTotal.value = response.total || 0
  }).finally(() => {
    reviewSessionLoading.value = false
  })
}

function handleReviewSessionQuery() {
  reviewSessionQuery.value.pageNum = 1
  getReviewSessionList()
}

function resetReviewSessionQuery(refresh = true) {
  reviewSessionQuery.value = {
    pageNum: 1,
    pageSize: 5,
    sessionName: undefined,
    sessionCode: undefined,
    activityId: undefined
  }
  if (refresh) {
    getReviewSessionList()
  }
}

function selectReviewSession(row) {
  if (!row) return
  selectedReviewSession.value = row
  syncReviewSessionForm.value.sessionId = row.id
}

function submitSyncReviewSessionDialog() {
  if (!ensureSelectedSchedule()) return
  if (!syncReviewSessionForm.value.sessionId) {
    modal.msgWarning('请选择评审场次')
    return
  }
  targetActionLoading.value = true
  syncSceneTargetsToReviewSession(selectedSchedule.value.scheduleId, {
    sessionId: syncReviewSessionForm.value.sessionId
  }).then(response => {
    showSceneActionResult(response.data, '同步完成')
    syncReviewSessionDialogOpen.value = false
  }).finally(() => {
    targetActionLoading.value = false
  })
}

function handleCredentialQuery() {
  credentialQuery.value.pageNum = 1
  getCredentialList()
}

function resetCredentialQuery() {
  credentialQuery.value = {
    pageNum: 1,
    pageSize: 10,
    scheduleId: selectedSchedule.value?.scheduleId,
    credentialNo: undefined,
    scopeType: undefined,
    teamName: undefined,
    userName: undefined,
    credentialStatus: undefined
  }
  getCredentialList()
}

function handleUpdateCredential(row) {
  credentialForm.value = {
    credentialId: row.credentialId,
    credentialNo: row.credentialNo,
    credentialStatus: row.credentialStatus,
    remark: row.remark
  }
  credentialDialogOpen.value = true
}

function submitCredentialForm() {
  updateSceneCredential(credentialForm.value).then(() => {
    modal.msgSuccess('保存成功')
    credentialDialogOpen.value = false
    getCredentialList()
  })
}

function handleDeleteCredential(row) {
  deleteCredentials([row.credentialId], `确定删除证件“${row.credentialNo || ''}”吗？`)
}

function handleDeleteCredentials() {
  if (selectedCredentialRows.value.length === 0) return
  const credentialIds = selectedCredentialRows.value.map(item => item.credentialId)
  deleteCredentials(credentialIds, `确定删除已选 ${credentialIds.length} 张证件吗？`)
}

function deleteCredentials(credentialIds, message) {
  modal.confirm(message).then(() => {
    return delSceneCredential(credentialIds.join(','))
  }).then(() => {
    modal.msgSuccess('删除成功')
    selectedCredentialRows.value = []
    getCredentialList()
  }).catch(() => {})
}

function handleLogQuery() {
  logQuery.value.pageNum = 1
  getLogList()
}

function resetLogQuery() {
  logQuery.value = {
    pageNum: 1,
    pageSize: 10,
    scheduleId: selectedSchedule.value?.scheduleId,
    credentialNo: undefined,
    operationType: undefined,
    operationResult: undefined
  }
  getLogList()
}

onMounted(() => {
  loadCompetitionOptions()
  getScheduleList()
})
</script>

<style scoped lang="scss">
.scene-schedule-page {
  .selected-line {
    display: flex;
    align-items: center;
    gap: 8px;
    min-height: 34px;
    margin: 4px 0 10px;
    color: #303133;
  }

  .pane-toolbar {
    margin-bottom: 10px;
  }

  .target-user-picker {
    display: flex;
    align-items: center;
    width: 100%;
    gap: 8px;
  }

  .selected-user-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
    min-height: 32px;
    align-items: center;
  }

  .user-batch-query {
    display: flex;
    align-items: flex-start;
    gap: 10px;
    margin: 0 0 12px;
  }

  .user-batch-query__input {
    flex: 1;
  }

  .user-batch-query__actions {
    display: flex;
    flex-direction: column;
    gap: 6px;
    min-width: 150px;
  }

  .user-batch-not-found {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 6px;
    margin: 0 0 12px;
  }

  .user-batch-not-found__label {
    color: #e6a23c;
    font-size: 12px;
  }

  .user-dialog-footer {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  .muted {
    color: #909399;
    font-size: 12px;
    line-height: 20px;
  }

  :deep(.el-tag + .el-tag) {
    margin-left: 4px;
  }
}
</style>
