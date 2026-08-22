<template>
  <div class="bg">
    <div class="container-custom">
      <Breadcrumbar />
      <el-card class="card">
        <div class="title">
          {{ queryParams?.competitionName }}-
          {{ queryParams?.competitionTrackName }}-{{
            queryParams?.secondLevelName
          }}
        </div>
        <div class="tuandui">
          <div>团队名：{{ queryParams?.teamName }}</div>
        </div>

         <div v-if="queryParams.orderPayFlag == 1" style="margin-top: 10px;">
                <el-alert type="error" show-icon>
                  <div >
                    <div>当前团队存在《待支付》或《待审核》订单，
                      <span class="quzhifu" @click="isquzhifu">
                        去查看
                      </span>
                      </div>
                  </div>
                </el-alert>
              </div>
        <div style="padding: 1.25rem; background: #fafafa; margin-top: 1.25rem">
          <div class="biaotou">参赛信息修改</div>
          <div class="cansaixinxi">
            <!-- 左侧富文本渲染详情以及附件 getOperationConfig返回对应富文本以及时间操作项 -->
            <isxiangqing
              :hintText1="getOperationConfig(1)?.hintText1"
              :fujian="getOperationConfig(1)"
            ></isxiangqing>
            <!-- 右侧表格以及顶部更换组别 -->
            <div class="caisaibd">
              <!-- 判定是否存在学员 并渲染第一个学员所属学校 -->
              <div
                v-if="queryParams?.competitionApplyInfoList?.length > 0"
                class="xuexiao"
              >
                所属学校：{{
                  queryParams?.competitionApplyInfoList[0].schoolName
                }}
              </div>
              <!-- 修改组别部分 -->
              <div class="header-actions">
                <!-- isEditingGroup判定组别处于编辑状态  -->
                <div v-if="!isEditingGroup">
                  <!-- isCurrentTimeInRange(getOperationConfig(1)?.jsonallowedTimeRanges)判定是否处于可编辑时间内 -->
                  <!-- queryParams.secondLevelOperateCount组别可修改次数 -->
                  <!-- queryParams.retiredAuditInfo?.status != 'COMPLETED' 判定该团队是否退赛通过 -->
                  <!-- permissionStatus[0].isshow判定该用户身份是否可编辑 -->
                  <!-- orderPayFlag判定是否处于退费重缴待付费状态 -->
                  <!-- queryParams.flag判定是否有已存在流程 -->
                  <!--  是否显示学生提交按钮 -->
                  <!-- 计算学生修改项数量 -->
                  <el-button
                    :type="
                      !isCurrentTimeInRange(
                        getOperationConfig(1)?.jsonallowedTimeRanges
                      ) || queryParams.secondLevelOperateCount == 0
                        ? 'info'
                        : 'primary'
                    "
                    :class="
                      !isCurrentTimeInRange(
                        getOperationConfig(1)?.jsonallowedTimeRanges
                      ) || queryParams.secondLevelOperateCount == 0 ||
                      !queryParams.flag ||
                      modifyScope == '2'
                        ? 'disabled-btn'
                        : 'search-btn'
                    "
                    v-if="
                      permissionStatus[0].isshow &&
                      queryParams.retiredAuditInfo?.status != 'COMPLETED' &&
                      queryParams.orderPayFlag == 0
                    "
                    :disabled="
                      !isCurrentTimeInRange(
                        getOperationConfig(1)?.jsonallowedTimeRanges
                      ) ||
                      queryParams.secondLevelOperateCount == 0 ||
                      !queryParams.flag ||
                      modifyScope == '2'
                    "
                    @click="handleEditGroup"
                    >更换组别
                    <span
                      v-if="
                        queryParams.secondLevelOperateCount != -1 &&
                        isCurrentTimeInRange(
                          getOperationConfig(1)?.jsonallowedTimeRanges
                        )
                      "
                      >({{ queryParams.secondLevelOperateCount }}次)</span
                    >
                  </el-button>
                  <span style="display: inline-block; margin-left: 1.25rem"
                    >当前组别：
                    <span style="font-weight: bold">{{
                      queryParams.secondLevelName
                    }}</span></span
                  >
                  <el-badge
                    :value="changeCount"
                    class="item"
                    v-if="isxueshengtijiao && changeCount > 0"
                  >
                    <el-button
                      type="primary"
                      @click="tijiao(1)"
                      class="search-btn"
                      :loading="grouploading"
                      style="margin-left: 1.25rem"
                    >
                      提交</el-button
                    >
                  </el-badge>
                </div>
                <div v-else class="group-edit-actions">
                  <el-select
                    v-model="selectedGroup"
                    placeholder="请选择组别"
                    style="width: 12.5rem"
                  >
                    <el-option
                      :label="item.secondLevelName"
                      :value="item.secondLevelCode"
                      v-for="(item, index) in competitionTrackConfigInfoList"
                      :key="index"
                    />
                  </el-select>
                  <el-button
                    type="primary"
                    size="small"
                    class="search-btn"
                    :loading="zubieloading"
                    @click="handleSaveGroup"
                    >暂存</el-button
                  >
                  <el-button size="small" @click="handleCancelGroup"
                    >取消</el-button
                  >
                </div>
              </div>
              <!-- 时间提示 -->
              <div
                v-if="
                  !isCurrentTimeInRange(
                    getOperationConfig(1)?.jsonallowedTimeRanges
                  )
                "
              >
                <el-alert type="primary" show-icon>
                  <div style="display: flex">
                    <div>当前时间未处于可修改时间，可修改时间为：</div>
                    <div>
                      <p
                        v-for="(x, i) in getOperationConfig(1)
                          ?.jsonallowedTimeRanges"
                        :key="i"
                      >
                        {{ x.start }}至{{ x.end }}
                      </p>
                    </div>
                  </div>
                </el-alert>
              </div>
              <!-- 退费存在提示 -->
              <div v-else-if="!queryParams.flag" style="margin-top: 0.625rem">
                <el-alert type="warning" show-icon>
                  当前正在处理退费,流程结束后再操作
                </el-alert>
              </div>
              <!-- 组别校验问题 -->
              <div v-if="isxiugaizubie" style="margin-top: 0.625rem">
                <el-alert type="error" show-icon @close="isxiugaizubie = ''">
                  {{ isxiugaizubie }}
                </el-alert>
              </div>

              <el-table
                :data="queryParams.competitionApplyInfoList"
                style="width: 100%"
              >
                <el-table-column
                  prop="userName"
                  label="姓名"
                  min-width="80"
                  align="center"
                >
                  <template #default="{ row }">
                    <el-input
                      v-if="row.isEditing && row.isNew"
                      v-model="row.ADDuserName"
                      placeholder="请输入姓名"
                      size="small"
                    />
                    <span
                      v-else
                      :class="row.delFlag == '1' ? 'truedel' : 'falsedel'"
                      >{{ row.userName }}</span
                    >
                  </template>
                </el-table-column>
                <el-table-column
                  prop="competitionRoleName"
                  label="身份"
                  min-width="70"
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
                    <span
                      v-else
                      :class="row.delFlag == '1' ? 'truedel' : 'falsedel'"
                      >{{ row.competitionRoleName }}</span
                    >
                  </template>
                </el-table-column>
                <el-table-column
                  prop="idCard"
                  label="身份证号"
                  min-width="180"
                  align="center"
                >
                  <template #default="{ row }">
                    <el-input
                      v-if="row.isEditing && row.isNew"
                      v-model="row.ADDidCard"
                      placeholder="请输入身份证号"
                      size="small"
                      @blur="validateADDidCard(row)"
                    />
                    <span
                      v-else
                      :class="row.delFlag == '1' ? 'truedel' : 'falsedel'"
                      >{{ formatIdCard(row.idCard) }}</span
                    >
                  </template>
                </el-table-column>
                <el-table-column
                  prop="phone"
                  label="手机号"
                  min-width="160"
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

                    <div
                      v-else
                      style="display: flex; justify-content: center"
                      :class="row.delFlag == '1' ? 'truedel' : 'falsedel'"
                    >
                      {{ formatphone(row.phone) }}
                      <el-popover title="" placement="top-start">
                        <div>
                          该队员报名手机号信息同本人注册账号不一致,注册手机号为
                          <p>
                            {{ row.userInfoDateList?.phone }}
                          </p>
                        </div>
                        <template #reference>
                          <Warning
                            style="
                              width: 20px;
                              color: #ffc400;
                              margin-left: 5px;
                            "
                            v-if="
                              row.userInfoFlag?.split(',')?.indexOf('2') != -1
                            "
                          /> </template
                      ></el-popover>
                    </div>
                  </template>
                </el-table-column>
                <el-table-column
                  prop="email"
                  label="邮箱"
                  min-width="180"
                  align="center"
                >
                  <template #default="{ row }">
                    <div v-if="row.isEditing">
                      <el-input
                        v-model="row.ADDemail"
                        placeholder="请输入邮箱"
                        size="small"
                        @blur="validateEmail(row)"
                      />
                    </div>
                    <div
                      v-else
                      style="display: flex; justify-content: center"
                      :class="row.delFlag == '1' ? 'truedel' : 'falsedel'"
                    >
                      {{ formatemail(row.email) }}
                      <el-popover title="" placement="top-start">
                        <div>
                          该队员报名邮箱信息同本人注册账号不一致, 注册邮箱为
                          <p>
                            {{ row.userInfoDateList?.email }}
                          </p>
                        </div>
                        <template #reference>
                          <Warning
                            style="
                              width: 20px;
                              color: #ffc400;
                              margin-left: 5px;
                            "
                            v-if="
                              row.userInfoFlag?.split(',')?.indexOf('1') != -1
                            "
                          /> </template
                      ></el-popover>
                    </div>
                  </template>
                </el-table-column>
                <el-table-column
                  prop="userName"
                  label="调整顺序"
                  width="120"
                  align="left"
                >
                  <template #default="{ row,$index }">
                    <div class="flex justify-around w-full">
                      <el-button
                        type="primary"
                        size="small"
                        v-if="($index !== 0) && modifyScope"
                        @click="handleMoveUp(row)"
                        :disabled="
                          !isCurrentTimeInRange(
                            getOperationConfig(1)?.jsonallowedTimeRanges
                          ) ||
                          row.delFlag == '1'
                        "
                        plain>上移</el-button>
                      <el-button
                        type="primary"
                        size="small"
                        v-if="($index !== queryParams?.competitionApplyInfoList?.length - 1) && modifyScope"
                        :disabled="
                          !isCurrentTimeInRange(
                            getOperationConfig(1)?.jsonallowedTimeRanges
                          ) ||
                          row.delFlag == '1'
                        "
                        @click="handleMoveDown(row)"
                        plain>下移</el-button>
                    </div>
                  </template>
                </el-table-column>
                <el-table-column
                  label="操作"
                  fixed="right"
                  width="160"
                  align="center"
                >
                  <template #default="{ row }">
                    <div
                      :class="row.isxuesheng ? 'gaodu2' : 'gaodu'"
                      v-if="
                        queryParams.retiredAuditInfo?.status != 'COMPLETED' &&
                        queryParams.orderPayFlag == 0
                      "
                    >
                      <div v-if="chengyuan">
                        <el-button
                          type="success"
                          size="mini"
                          @click="handleSave(row, '学生', 'change')"
                          :loading="row.xueshengloading"
                          link
                          v-if="row.isEditing"
                          >暂存</el-button
                        >
                        <el-button
                          size="mini"
                          type="danger"
                          link
                          v-if="row.delFlag == 0 || row.isEditing"
                          :disabled="
                            (!queryParams.flag || row.delFlag == '1') &&
                            row.memberId
                          "
                          @click="handleDelete(row, 'competitionApplyInfoList')"
                          >删除</el-button
                        >
                        <el-button
                          size="mini"
                          type="danger"
                          link
                          v-if="row.delFlag == 1"
                          @click="enddleDelete(row, 'competitionApplyInfoList')"
                          >取消删除</el-button
                        >
                      </div>
                      <div v-else>
                        <div v-if="row.isEditing" style="position: relative">
                          <el-button
                            type="success"
                            size="mini"
                            @click="handleSave(row, '学生', 'info')"
                            :loading="row.xueshengloading"
                            link
                            >暂存</el-button
                          >
                          <el-button
                            type="danger"
                            size="mini"
                            @click="handleCancel(row)"
                            link
                          >
                            取消
                          </el-button>
                        </div>
                        <el-button
                          v-else-if="permissionStatus[0].isshow"
                          type="primary"
                          size="mini"
                          @click="handleEdit(row, 1)"
                          :disabled="
                            !isCurrentTimeInRange(
                              getOperationConfig(1)?.jsonallowedTimeRanges
                            ) ||
                            row.applyInfoChangeOperateCount == 0 ||
                            row.delFlag == '1' ||
                            !queryParams.flag ||
                            modifyScope == '2'
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
                      <div class="danghang" v-if="row.isxuesheng">
                        <CircleCloseFilled class="icon" />
                        {{ row.isxuesheng }}
                      </div>
                    </div>
                  </template>
                </el-table-column>
              </el-table>
              <div v-if="cybgjy" style="margin-top: 0.625rem">
                <el-alert type="error" show-icon @close="cybgjy = ''">
                  {{ cybgjy }}
                </el-alert>
              </div>

              <div v-if="istijiao[1]" style="margin-top: 0.625rem">
                <el-alert type="error" show-icon @close="closeAlert(1)">
                  {{ istijiao[1] }}
                </el-alert>
              </div>
              <div
                style="margin-top: 1.25rem"
                v-if="queryParams.retiredAuditInfo?.status != 'COMPLETED'"
              >
                <div
                  v-if="
                    !chengyuan &&
                    permissionStatus[0].isshow &&
                    queryParams.orderPayFlag == 0
                  "
                >
                  <el-button
                    type="warning"
                    @click="cybgeng"
                    :disabled="
                      queryParams.memberOperateCount == 0 ||
                      !isCurrentTimeInRange(
                        getOperationConfig(1)?.jsonallowedTimeRanges
                      ) ||
                      !queryParams.flag ||
                      modifyScope == '2'
                    "
                    >成员变更<span
                      v-if="
                        queryParams.memberOperateCount != -1 &&
                        isCurrentTimeInRange(
                          getOperationConfig(1)?.jsonallowedTimeRanges
                        )
                      "
                      >({{ queryParams.memberOperateCount }}次)</span
                    >
                  </el-button>
                </div>
                <div v-if="chengyuan">
                  <el-button type="success" @click="handleAddMember"
                    >新增成员</el-button
                  >
                  <el-button type="primary" plain @click="handleSaveMember"
                    >暂存</el-button
                  >
                </div>
              </div>
              <div
                v-if="isxueshengtijiao && changeCount > 0"
                class="tijiao-btn"
              >
                <div class="tijiao-btn-content">
                  <el-badge :value="changeCount" class="item">
                    <el-button
                      type="primary"
                      @click="tijiao(1)"
                      class="search-btn"
                      :loading="grouploading"
                      style="margin-left: 1.25rem"
                    >
                      提交</el-button
                    >
                  </el-badge>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div style="padding: 1.25rem; background: #fafafa; margin-top: 1.25rem">
          <div class="biaotou">指导教师修改</div>
          <div class="cansaixinxi">
            <!-- 左侧富文本渲染详情以及附件 getOperationConfig返回对应富文本以及时间操作项 -->
            <isxiangqing
              :hintText1="getOperationConfig(2)?.hintText1"
              :fujian="getOperationConfig(2)"
            ></isxiangqing>
            <div class="caisaibd">
              <div
                v-if="
                  !isCurrentTimeInRange(
                    getOperationConfig(2)?.jsonallowedTimeRanges
                  )
                "
              >
                <el-alert type="primary" show-icon>
                  <div style="display: flex">
                    <div>当前时间未处于可修改时间，可修改时间为：</div>
                    <div>
                      <p
                        v-for="(x, i) in getOperationConfig(2)
                          ?.jsonallowedTimeRanges"
                        :key="i"
                      >
                        {{ x.start }}至{{ x.end }}
                      </p>
                    </div>
                  </div>
                </el-alert>
              </div>
              <div v-else-if="!queryParams.flag" style="margin-top: 0.625rem">
                <el-alert type="warning" show-icon>
                  当前正在处理退费,流程结束后再操作
                </el-alert>
              </div>
              <el-table
                :data="queryParams.guideTeacherApplyInfoList"
                style="width: 100%"
              >
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
                    <span
                      v-else
                      :class="row.delFlag == '1' ? 'truedel' : 'falsedel'"
                      >{{ row.userName }}</span
                    >
                  </template>
                </el-table-column>
                <el-table-column
                  prop="competitionRoleName"
                  label="身份"
                  min-width="80"
                  align="center"
                >
                  <template #default="{ row }">
                    <span
                      :class="row.delFlag == '1' ? 'truedel' : 'falsedel'"
                      >{{ row.competitionRoleName }}</span
                    >
                  </template>
                </el-table-column>
                <el-table-column
                  prop="phone"
                  label="手机号"
                  min-width="180"
                  align="center"
                >
                  <template #default="{ row }">
                    <el-input
                      v-if="row.isEditing"
                      v-model="row.ADDphone"
                      placeholder="请输入手机号"
                      size="small"
                      @blur="validatePhone(row)"
                    />
                    <div
                      v-else
                      style="display: flex; justify-content: center"
                      :class="row.delFlag == '1' ? 'truedel' : 'falsedel'"
                    >
                      {{ formatphone(row.phone) }}
                    </div>
                  </template>
                </el-table-column>
                <el-table-column
                  prop="email"
                  label="邮箱"
                  min-width="200"
                  align="center"
                >
                  <template #default="{ row }">
                    <div v-if="row.isEditing">
                      <el-input
                        v-model="row.ADDemail"
                        placeholder="请输入邮箱"
                        size="small"
                        @blur="validateEmail(row)"
                      />
                    </div>
                    <div
                      v-else
                      style="display: flex; justify-content: center"
                      :class="row.delFlag == '1' ? 'truedel' : 'falsedel'"
                    >
                      {{ formatemail(row.email) }}
                    </div>
                  </template>
                </el-table-column>
                <el-table-column
                  label="操作"
                  fixed="right"
                  width="200"
                  align="center"
                >
                  <template #default="{ row }">
                    <div
                      style="position: relative"
                      :class="row.isxuesheng ? 'gaodu2' : 'gaodu'"
                      v-if="
                        queryParams.retiredAuditInfo?.status != 'COMPLETED' &&
                        queryParams.orderPayFlag == 0
                      "
                    >
                      <div v-if="Teacher">
                        <el-button
                          type="success"
                          size="mini"
                          @click="handleSave(row, '指导教师', 'change')"
                          link
                          v-if="row.isEditing"
                          >暂存</el-button
                        >
                        <el-button
                          size="mini"
                          type="danger"
                          link
                          v-if="row.delFlag == 0 || row.isEditing"
                          :disabled="
                            (!queryParams.flag || row.delFlag == '1') &&
                            row.memberId
                          "
                          @click="
                            handleDelete(row, 'guideTeacherApplyInfoList')
                          "
                          >删除</el-button
                        >
                        <el-button
                          size="mini"
                          type="danger"
                          link
                          v-if="row.delFlag == 1"
                          @click="
                            enddleDelete(row, 'guideTeacherApplyInfoList')
                          "
                          >取消删除</el-button
                        >
                      </div>
                      <div v-else>
                        <div v-if="row.isEditing">
                          <el-button
                            type="success"
                            size="mini"
                            @click="handleSave(row, '指导教师', 'info')"
                            link
                            >暂存</el-button
                          >
                          <el-button
                            type="danger"
                            size="mini"
                            @click="handleCancel(row)"
                            link
                            >取消</el-button
                          >
                        </div>

                        <el-button
                          v-else-if="permissionStatus[1].isshow"
                          type="primary"
                          size="mini"
                          link
                          @click="handleEdit(row, 2)"
                          :disabled="
                            !isCurrentTimeInRange(
                              caozuoyaoqiu.find(
                                (item) => item.operationType == 2
                              )?.jsonallowedTimeRanges
                            ) ||
                            row.applyInfoChangeOperateCount == 0 ||
                            !queryParams.flag
                          "
                          >编辑
                          <span
                            v-if="
                              row.applyInfoChangeOperateCount != -1 &&
                              isCurrentTimeInRange(
                                caozuoyaoqiu.find(
                                  (item) => item.operationType == 2
                                )?.jsonallowedTimeRanges
                              )
                            "
                            >({{ row.applyInfoChangeOperateCount }}次)</span
                          >
                        </el-button>
                      </div>
                      <div class="danghang" v-if="row.isxuesheng">
                        <CircleCloseFilled class="icon" />
                        {{ row.isxuesheng }}
                      </div>
                    </div>
                  </template>
                </el-table-column>
              </el-table>
              <div v-if="zhidaolaosjy" style="margin-top: 0.625rem">
                <el-alert type="error" show-icon @close="zhidaolaosjy = ''">
                  {{ zhidaolaosjy }}
                </el-alert>
              </div>
              <div v-if="istijiao[2]" style="margin-top: 0.625rem">
                <el-alert type="error" show-icon @close="closeAlert(1)">
                  {{ istijiao[2] }}
                </el-alert>
              </div>
              <div
                style="margin-top: 1.25rem"
                v-if="queryParams.retiredAuditInfo?.status != 'COMPLETED'"
              >
                <div
                  v-if="
                    !Teacher &&
                    permissionStatus[1].isshow &&
                    queryParams.orderPayFlag == 0
                  "
                >
                  <el-button
                    type="warning"
                    @click="isnumTeacher"
                    :disabled="
                      queryParams.guideTeacherOperateCount == 0 ||
                      !isCurrentTimeInRange(
                        getOperationConfig(2)?.jsonallowedTimeRanges
                      ) ||
                      !queryParams.flag
                    "
                    >成员变更
                    <span
                      v-if="
                        queryParams.guideTeacherOperateCount != -1 &&
                        isCurrentTimeInRange(
                          getOperationConfig(2)?.jsonallowedTimeRanges
                        )
                      "
                      >({{ queryParams.guideTeacherOperateCount }}次)</span
                    >
                  </el-button>
                </div>
                <div v-if="Teacher">
                  <el-button type="success" @click="handleAddTeacher"
                    >新增指导教师</el-button
                  >
                  <el-button type="primary" plain @click="handleSaveTeacher"
                    >暂存</el-button
                  >
                </div>
              </div>
              <div
                v-if="iszhidaolaoshi && jiaoshichangeCount > 0"
                class="tijiao-btn"
              >
                <div class="tijiao-btn-content">
                  <el-badge :value="jiaoshichangeCount" class="item">
                    <el-button
                      type="primary"
                      @click="tijiao(2)"
                      class="search-btn"
                      >提交
                    </el-button>
                  </el-badge>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div
          class="tuifei-container"
          style="padding: 1.25rem; background: #fafafa; margin-top: 1.25rem"
        >
          <div class="tuifeichongjiao">
            <div class="biaotou">退费重缴</div>
            <div style="margin-top: 1.25rem" class="tuifei-content">
              <!-- 左侧富文本渲染详情以及附件 getOperationConfig返回对应富文本以及时间操作项 -->
              <isxiangqing
                :hintText1="getOperationConfig(3)?.hintText1"
                :fujian="getOperationConfig(3)"
              ></isxiangqing>
              <div class="tuifei-btn">
                <div v-if="queryParams.repaymentAuditInfo">
                  <div class="audit-info-item">
                    <span class="audit-info-label"> 退费金额：</span>
                    <span class="audit-info-value"
                      >{{
                        queryParams.repaymentAuditInfo.formData.amount
                      }}元</span
                    >
                  </div>

                  <div
                    v-for="(x, i) in queryParams.repaymentAuditInfo
                      .approvalHistory"
                    :key="i"
                  >
                    <div
                      v-if="x.status == 'REJECTED' && x.comments.length > 0"
                      class="audit-info-item"
                    >
                      <span class="audit-info-label">审核意见：</span>
                      <span class="audit-info-value">
                        <span v-for="(xx, ii) in x.comments" :key="ii">
                          {{ xx }}
                        </span>
                      </span>
                    </div>
                  </div>
                  <div class="audit-info-item">
                    <span class="audit-info-label">审核状态：</span>
                    <span
                      class="audit-info-value"
                      :class="{
                        'status-running':
                          queryParams.repaymentAuditInfo.status === 'RUNNING',
                        'status-rejected':
                          queryParams.repaymentAuditInfo.status === 'REJECTED',
                        'status-completed':
                          queryParams.repaymentAuditInfo.status === 'COMPLETED',
                      }"
                    >
                      {{
                        shenhe.find(
                          (item) =>
                            item.value == queryParams.repaymentAuditInfo.status
                        )?.label || "未知"
                      }}
                    </span>
                  </div>
                </div>
                <div
                  v-if="
                    permissionStatus[2].isshow &&
                    queryParams.retiredAuditInfo?.status != 'COMPLETED' &&
                    queryParams.orderPayFlag == 0
                  "
                >
                  <!-- orderPayFlag检查有没有进行中的支付 返回值 0-没有，1-有 -->
                  <el-button
                    type="primary"
                    size="mini"
                    class="tuisai-btn"
                    v-if="
                      queryParams.repaymentAuditInfo?.status !== 'RUNNING' ||
                      !queryParams.repaymentAuditInfo
                    "
                    @click="tuifeicongjiao"
                    :disabled="
                      queryParams.repaymentOperateCount == 0 ||
                      !queryParams.flag ||
                      !isCurrentTimeInRange(
                        getOperationConfig(3)?.jsonallowedTimeRanges
                      )
                    "
                    >申请退费重缴
                    <span
                      v-if="
                        queryParams.repaymentOperateCount != -1 &&
                        isCurrentTimeInRange(
                          getOperationConfig(3)?.jsonallowedTimeRanges
                        )
                      "
                      >({{ queryParams.repaymentOperateCount }}次)</span
                    >
                  </el-button>
                </div>
                <!-- 提示 -->
                <div
                  v-if="
                    !isCurrentTimeInRange(
                      getOperationConfig(3)?.jsonallowedTimeRanges
                    )
                  "
                  style="margin-top: 10px; margin-right: 10px"
                >
                  <el-alert type="primary" show-icon>
                    <div>
                      <div>当前时间未处于可修改时间，可修改时间为：</div>
                      <div>
                        <p
                          v-for="(x, i) in getOperationConfig(3)
                            ?.jsonallowedTimeRanges"
                          :key="i"
                        >
                          {{ x.start }}至{{ x.end }}
                        </p>
                      </div>
                    </div>
                  </el-alert>
                </div>
                <div v-else-if="!queryParams.flag" style="margin-top: 0.625rem">
                  <el-alert type="warning" show-icon>
                    当前正在处理退费,流程结束后再操作
                  </el-alert>
                </div>
              </div>
            </div>
          </div>
          <div class="tuisai">
            <div class="biaotou">退赛申请</div>
            <div style="margin-top: 1.25rem" class="tuifei-content">
              <!-- 左侧富文本渲染详情以及附件 getOperationConfig返回对应富文本以及时间操作项-->
              <isxiangqing
                :hintText1="getOperationConfig(4)?.hintText1"
                :fujian="getOperationConfig(4)"
              ></isxiangqing>

              <withdraw
                v-if="queryParams"
                :hintText1="getOperationConfig(4)"
                :queryParams="queryParams"
                :isshow="permissionStatus[3].isshow"
              ></withdraw>
            </div>
          </div>
        </div>

        <div class="fanhuishangji">
          <el-button type="primary" size="mini" @click="handleBack" plain
            >返回队伍列表</el-button
          >
        </div>
      </el-card>
    </div>
    <el-dialog
      v-model="tusaiVisible"
      title="提示信息"
      width="1000"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :show-close="false"
    >
      <div class="ql-container ql-snow">
        <div class="rich-content ql-editor" v-html="xiangqing.hintText2"></div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="quxiaotuifei">取消</el-button>
          <el-button
            type="primary"
            @click="tuifei"
            :disabled="xiangqing.forceReadSeconds > 0"
            :loading="grouploading"
            class="search-btn"
          >
            {{
              xiangqing.forceReadSeconds > 0
                ? xiangqing.forceReadSeconds + "秒后可提交"
                : "我已阅读，确认提交"
            }}
          </el-button>
          <div v-if="xiangqingloading" style="margin-top: 0.625rem">
            <el-alert type="error" show-icon @close="xiangqingloading = ''">
              {{ xiangqingloading }}
            </el-alert>
          </div>
        </div>
      </template>
    </el-dialog>
    <el-dialog
      v-model="xiangqingdialogVisible"
      title="提示信息"
      width="1000"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :show-close="false"
    >
      <div class="ql-container ql-snow">
        <div class="rich-content ql-editor" v-html="xiangqing.hintText2"></div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="quxiaodialog">取消</el-button>
          <el-button
            type="primary"
            @click="xiangqinghandleSave(xiangqing.operationType)"
            :disabled="xiangqing.forceReadSeconds > 0"
            :loading="grouploading"
            class="search-btn"
          >
            {{
              xiangqing.forceReadSeconds > 0
                ? xiangqing.forceReadSeconds + "秒后可提交"
                : "我已阅读，确认提交"
            }}
          </el-button>
          <div v-if="xiangqingloading" style="margin-top: 0.625rem">
            <el-alert type="error" show-icon @close="xiangqingloading = ''">
              {{ xiangqingloading }}
            </el-alert>
          </div>
        </div>
      </template>
    </el-dialog>
    <el-dialog
      v-model="xiudialogVisible"
      title="提示"
      width="500"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :show-close="false"
    >
      <div>您的修改尚未提交，返回列表后修改数据将不能生效，确认返回列表</div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="xiudialogVisible = false">取消</el-button>
          <el-button type="primary" @click="querenback">确认返回</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import Breadcrumbar from "@/components/breadcrumbar.vue";
import { ElMessageBox } from "element-plus";
import {
  getUserCompetitionApplyInfo,
  changeCompetitionApplyInfo,
  selectCompetitionTrackConfigInfo,
  selectCompetitionOperationConfigInfo,
  checkChangeOperator,
  checkChangeCompetitionApplyInfo,
  createPayOrderByTeamChange,
  updateApplyInfoSequence,
} from "@/api/team";
import "@vueup/vue-quill/dist/vue-quill.snow.css";
import { useRoute, useRouter } from "vue-router";
import { onMounted, ref, computed } from "vue";
import { ElMessage, ElNotification } from "element-plus";
import { forEach } from "lodash";
import isxiangqing from "@/views/personal/teams/xiangqing.vue";
import withdraw from "@/views/personal/teams/withdraw.vue";
import { getSystemDate } from "@/api/index";

const shenhe = ref([
  {
    label: "审核中",
    value: "RUNNING",
  },
  {
    label: "驳回",
    value: "REJECTED",
  },
  {
    label: "通过",
    value: "COMPLETED",
  },
]);
// 存储原始数据
const chunchuyuanbenshujv = ref({});
const route = useRoute();
const router = useRouter();
const queryParams = ref({});
const isEditingGroup = ref(false);
const selectedGroup = ref("");
const originalGroupName = ref("");
const isxueshengtijiao = ref(false);
const iszhidaolaoshi = ref(false);

// 编辑成员信息
const handleEdit = (row, i) => {
  // if (row.applyInfoChangeOperateCount == 0) {
  //   ElMessage.warning("成员暂无修改次数");
  //   return;
  // }

  row.isEditing = true;
  row.ADDuserName = row.userName;
  row.ADDphone = row.phone;
  row.ADDemail = row.email;
  row.ADDcompetitionRoleName = row.competitionRoleName;
  row.ADDidCard = row.idCard;
};
const permissionStatus = ref([
  {
    operationFlag: 1,
    isshow: false,
  },
  {
    operationFlag: 2,
    isshow: false,
  },
  {
    operationFlag: 3,
    isshow: false,
  },
  {
    operationFlag: 4,
    isshow: false,
  },
]);
let modifyScope = ref("");
const isshowcheckChangeOperator = (i) => {
  const params = {
    competitionSeriesId: queryParams.value.competitionSeriesId,
    operationFlag: i,
  };
  checkChangeOperator(params).then((res) => {
    permissionStatus.value[i - 1].isshow = res.data;
    if(i == 1){
      modifyScope.value = res.modifyScope;
    }
  });
};

// 编辑组别
const handleEditGroup = () => {
  // if (queryParams.value.secondLevelOperateCount == 0) {
  //   ElMessage.warning("组别暂无修改次数");
  //   return;
  // }

  originalGroupName.value = queryParams.value.secondLevelName;
  selectedGroup.value = queryParams.value.secondLevelCode;
  isEditingGroup.value = true;
};

// 保存组别修改
// 组别后台校验问题
const isxiugaizubie = ref("");

const zubieloading = ref(false);
// 组别暂存
const handleSaveGroup = () => {
  isxiugaizubie.value = "";
  zubieloading.value = true;
  // 竞赛赛道配置信息列表competitionTrackConfigInfoList
  let secondLevelName = competitionTrackConfigInfoList.value.find(
    (item) => item.secondLevelCode == selectedGroup.value
  ).secondLevelName;

  const params = {
    competitionSeriesId: queryParams.value.competitionSeriesId,
    competitionName: queryParams.value.competitionName,
    competitionTrackName: queryParams.value.competitionTrackName,
    competitionTrackId: queryParams.value.competitionTrackId,
    secondLevelCode: selectedGroup.value,
    secondLevelName: secondLevelName,
    teamCode: queryParams.value.teamCode,
    teamName: queryParams.value.teamName,
    changeType: "group",
  };
  checkChangeCompetitionApplyInfo(params)
    .then((res) => {
      zubieloading.value = false;
      if (res.code == 200) {
        queryParams.value.secondLevelName = secondLevelName;
        queryParams.value.secondLevelCode = selectedGroup.value;
        // 判定是否可编辑
        isEditingGroup.value = false;

        // 判定组别是否修改
        if (
          chunchuyuanbenshujv.value.secondLevelCode ==
          queryParams.value.secondLevelCode
        ) {
          queryParams.value.iszubie = false;
          return;
        }
        //   记录组别是否修改过
        queryParams.value.iszubie = true;
        // 是否显示学生提交按钮
        isxueshengtijiao.value = true;
      }
    })
    .catch((res) => {
      zubieloading.value = false;
      isxiugaizubie.value = res.msg;
    });
};

// 取消组别编辑 并复原组别
const handleCancelGroup = () => {
  selectedGroup.value = originalGroupName.value;
  isEditingGroup.value = false;
  isxiugaizubie.value = "";
};
const iszhidao = ref("");
// 保存成员信息修改
const handleSave = (row, type, changeType) => {
  // 姓名校验
  row.xueshengloading = true;
  row.isxuesheng = "";
  iszhidao.value = "";
  var dellist = [];
  if (!row.ADDuserName) {
    row.isxuesheng = "请输入姓名";
    row.xueshengloading = false;
    return;
  }
  let addrow = JSON.parse(JSON.stringify(row));
  // 学生特有校验
  if (type === "学生") {
    // 身份证号校验
    if (!validateADDidCard(row)) {
      row.isxuesheng = "请输入证件号";
      row.xueshengloading = false;
      return;
    }
    // 身份选择校验
    if (!row.ADDcompetitionRoleName) {
      row.isxuesheng = "请选择身份";
      row.xueshengloading = false;
      return;
    }

    // 手机号校验
    if (!validatePhone(row)) {
      row.xueshengloading = false;
      return;
    }
    // 邮箱校验
    if (!validateEmail(row)) {
      row.xueshengloading = false;
      return;
    }

    addrow.idCard = row.ADDidCard;
    if (queryParams.value.competitionApplyInfoList.length > 0) {
      queryParams.value.competitionApplyInfoList.forEach((item) => {
        if (item.delFlag == 1) {
          dellist.push(item);
        }
      });
    }
  }

  // 指导教师校验
  if (type === "指导教师") {
    // // 邮箱或手机号至少填写一个
    // if (!row.ADDemail && !row.ADDphone) {
    //   row.isxuesheng = "请输入邮箱或手机号";
    //   return;
    // }
    // 邮箱格式校验（如果填写了）
    if (row.ADDemail && !validateEmail(row)) {
      return;
    }

    // 手机号格式校验（如果填写了）
    if (row.ADDphone && !validatePhone(row)) {
      return;
    }
    if (queryParams.value.guideTeacherApplyInfoList.length > 0) {
      queryParams.value.guideTeacherApplyInfoList.forEach((item) => {
        if (item.delFlag == 1) {
          dellist.push(item);
        }
      });
    }
  }

  addrow.userName = row.ADDuserName;
  addrow.phone = row.ADDphone;
  addrow.email = row.ADDemail;

  addrow.competitionRoleName = row.ADDcompetitionRoleName;
  const params = {
    competitionSeriesId: queryParams.value.competitionSeriesId,
    competitionName: queryParams.value.competitionName,
    competitionTrackName: queryParams.value.competitionTrackName,
    competitionTrackId: queryParams.value.competitionTrackId,
    secondLevelCode: chunchuyuanbenshujv.value.secondLevelCode,
    secondLevelName: chunchuyuanbenshujv.value.secondLevelName,
    teamCode: queryParams.value.teamCode,
    teamName: queryParams.value.teamName,
    changeType: changeType,
    competitionApplyInfoList: [addrow],
    // 0 是单行操作校验 1或者不穿任何值是总行操作校验oneLineFlag;
    oneLineFlag: 0,
  };

  checkChangeCompetitionApplyInfo(params)
    .then((res) => {
      if (res.code == 200) {
        // 更新数据
        row.userName = row.ADDuserName;
        row.phone = row.ADDphone;
        row.email = row.ADDemail;

        // 学生特有字段更新
        if (type === "学生") {
          row.competitionRoleName = row.ADDcompetitionRoleName;
          row.idCard = row.ADDidCard;
        }
        row.xueshengloading = false;
        // 标记编辑状态
        row.isEditing = false;
        row.isNew = false;

        // 记录成员是否修改过
        if (type === "学生") {
          //   记录学生是否修改过

          // 初始数据
          let chushi = chunchuyuanbenshujv.value.competitionApplyInfoList.find(
            (item) => item.memberId == row.memberId
          );
          if (chushi.phone == row.phone && chushi.email == row.email) {
            row.isChengyuan = false;
            return;
          }
          row.isChengyuan = true;
          // 是否显示学生提交按钮
          isxueshengtijiao.value = true;
        } else if (type === "指导教师") {
          // 初始数据
          let chushi = chunchuyuanbenshujv.value.guideTeacherApplyInfoList.find(
            (item) => item.memberId == row.memberId
          );
          if (chushi.phone == row.phone && chushi.email == row.email) {
            row.isChengyuan = false;
            return;
          }
          //   记录指导教师是否修改过
          row.isChengyuan = true;
          // 是否显示指导教师提交按钮
          iszhidaolaoshi.value = true;
        }
      }
    })
    .catch((res) => {
      row.xueshengloading = false;
      if (type === "学生") {
        row.isxuesheng = res.msg;
      } else if (type === "指导教师") {
        row.isxuesheng = res.msg;
      }
    });
};
const tusaiVisible = ref(false);
const tuifeicongjiao = () => {
  if (!queryParams.value.flag) {
    return;
  }

  xiangqing.value = JSON.parse(
    JSON.stringify(
      caozuoyaoqiu.value.find((item) => item.operationType == 3) || {}
    )
  );
  if (xiangqing.value.hintText2 == "" || xiangqing.value.hintText2 == null) {
    tuifei();
    return;
  }

  qiangzhitime.value = setInterval(() => {
    if (xiangqing.value.forceReadSeconds > 0) {
      xiangqing.value.forceReadSeconds--;
    }
    if (xiangqing.value.forceReadSeconds == 0) {
      clearInterval(qiangzhitime.value);
    }
  }, 1000);
  xiangqingloading.value = "";
  tusaiVisible.value = true;
};
const tuifei = () => {
  grouploading.value = true;
  const params = {
    changeType: "repayment",
    commodityType: "competition",
    teamCode: queryParams.value.teamCode,
    competitionSeriesId: queryParams.value.competitionSeriesId,
    teamInfo: `${queryParams.value.competitionName}-${queryParams.value.competitionTrackName}-${queryParams.value?.secondLevelName}`,
    secondLevelCode: queryParams.value.secondLevelCode,
    userIds: queryParams.value.competitionApplyInfoList
      .map((item) => item.userId)
      .join(","),
    userNum: queryParams.value.competitionApplyInfoList.length,
    eventId: queryParams.value.competitionSeriesId,
  };
  // console.log(params,1234);
  createPayOrderByTeamChange(params)
    .then((res) => {
      grouploading.value = false;
      router.push({
        path: "/personal/paymentrecords/payment",
        query: {
          id: res.data.id,
        },
      });
    })
    .catch((res) => {
      xiangqingloading.value = res.msg;
    });
};
const quxiaotuifei = () => {
  clearInterval(qiangzhitime.value);
  tusaiVisible.value = false;
  xiangqingloading.value = "";
};
const closeAlert = (i) => {
  istijiao.value[i] = "";
};
// 取消成员信息编辑
const handleCancel = (row) => {
  row.isEditing = false;
  row.isNew = false;
  row.isxuesheng = "";
  row.ADDemail = "";
  row.ADDphone = "";
  row.ADDcompetitionRoleName = "";
  row.ADDidCard = "";
  row.ADDuserName = "";
};

const chengyuan = ref(false);

const cybgeng = () => {
  if (
    queryParams.value.memberOperateCount > 0 ||
    queryParams.value.memberOperateCount == -1
  ) {
    chengyuan.value = true;
  }
};
// 添加新成员
const handleAddMember = () => {
  queryParams.value.competitionApplyInfoList.push({
    userName: "",
    phone: "",
    competitionRoleName: "队员",
    email: "",
    isEditing: true,
    isNew: true,
    applyInfoChangeOperateCount: -1,
    delFlag: 0,
    userInfoFlag: "0",
  });
};

// 保存成员变更
// 储存成员变更信息
// 成员变更校验
const cybgjy = ref("");
const chucunchengyuanbiangeng = ref([]);
const handleSaveMember = () => {
  chucunchengyuanbiangeng.value = [];
  for (const item of queryParams.value.competitionApplyInfoList) {
    if (item.isNew || item.isEditing) {
      if (!item.ADDuserName) {
        item.isxuesheng = "请输入姓名";

        return;
      }
      if (!item.ADDcompetitionRoleName) {
        item.isxuesheng = "请选择身份";
        return;
      }
      // 身份证校验
      if (!validateADDidCard(item)) {
        item.isxuesheng = "请输入证件号";
        return;
      }

      // 手机号校验
      if (!validatePhone(item)) {
        return;
      }

      // 邮箱校验
      if (!validateEmail(item)) {
        return;
      }
    }
  }

  queryParams.value.competitionApplyInfoList.forEach((item) => {
    if (!item.memberId) {
      item.isEditing = false;
      item.isNew = false;
      item.email = item.ADDemail || item.email;
      item.phone = item.ADDphone || item.phone;
      item.userName = item.ADDuserName || item.userName;
      item.competitionRoleName =
        item.ADDcompetitionRoleName || item.competitionRoleName;
      item.idCard = item.ADDidCard || item.idCard;
      item.delFlag = 0;
      chucunchengyuanbiangeng.value.push(item);
    }
  });
  // 已删除的成员
  yishanchu.value.forEach((item) => {
    item.delFlag = 1;
  });

  chucunchengyuanbiangeng.value.push(...yishanchu.value);
  if (chucunchengyuanbiangeng.value.length > 0) {
    // isxueshengtijiao.value = true;
    const params = {
      competitionSeriesId: queryParams.value.competitionSeriesId,
      competitionName: queryParams.value.competitionName,
      competitionTrackName: queryParams.value.competitionTrackName,
      competitionTrackId: queryParams.value.competitionTrackId,
      secondLevelCode: chunchuyuanbenshujv.value.secondLevelCode,
      secondLevelName: chunchuyuanbenshujv.value.secondLevelName,
      teamCode: queryParams.value.teamCode,
      teamName: queryParams.value.teamName,
      changeType: "change",
      competitionApplyInfoList: [...chucunchengyuanbiangeng.value],
    };
    checkChangeCompetitionApplyInfo(params)
      .then((res) => {
        if (res.code === 200) {
          // 是否显删除按钮
          cybgjy.value = "";
          chengyuan.value = false;
          isxueshengtijiao.value = true;
        }
      })
      .catch((res) => {
        cybgjy.value = res.msg;
        isxueshengtijiao.value = false;
      });
  } else {
    cybgjy.value = "";
    chengyuan.value = false;
    isxueshengtijiao.value = true;
  }
};

const Teacher = ref(false);

// 计算学生修改项数量
const changeCount = computed(() => {
  let count = 0;

  // 如果组别已修改，加1
  if (queryParams.value.iszubie) {
    count++;
  }

  // 学生列表中每有一个 isChengyuan=true 时加1
  if (queryParams.value.competitionApplyInfoList) {
    queryParams.value.competitionApplyInfoList.forEach((item) => {
      if (item.isChengyuan) {
        count++;
      }
    });
  }
  for (let i = 0; i < chucunchengyuanbiangeng.value.length; i++) {
    count++;
  }

  return count;
});
// 计算修改项数量
const jiaoshichangeCount = computed(() => {
  let count = 0;

  // 学生列表中每有一个 isChengyuan=true 时加1
  if (queryParams.value.guideTeacherApplyInfoList) {
    queryParams.value.guideTeacherApplyInfoList.forEach((item) => {
      if (item.isChengyuan) {
        count++;
      }
    });
  }
  for (let i = 0; i < chucunjiaoshibiangeng.value.length; i++) {
    count++;
  }

  return count;
});

// 已删除成员
const yishanchu = ref([]);
const yishanchuzhidaolaoshi = ref([]);
// 删除成员或指导教师
const handleDelete = (row, listName) => {
  const index = queryParams.value[listName].findIndex((item) => item === row);
  if (index !== -1) {
    if (row.memberId && listName == "competitionApplyInfoList") {
      yishanchu.value.push(row);
      queryParams.value[listName][index].delFlag = "1";
    } else if (row.memberId && listName == "guideTeacherApplyInfoList") {
      yishanchuzhidaolaoshi.value.push(row);
      queryParams.value[listName][index].delFlag = "1";
    } else {
      queryParams.value[listName].splice(index, 1);
    }
  }
};
const enddleDelete = (row, listName) => {
  if (listName == "competitionApplyInfoList") {
    const index = yishanchu.value.findIndex(
      (item) => item.memberId === row.memberId
    );
    if (index !== -1) {
      yishanchu.value.splice(index, 1);
      row.delFlag = 0;
    }
  } else if (listName == "guideTeacherApplyInfoList") {
    const index = yishanchuzhidaolaoshi.value.findIndex(
      (item) => item.memberId === row.memberId
    );
    if (index !== -1) {
      yishanchuzhidaolaoshi.value.splice(index, 1);
      row.delFlag = 0;
    }
  }
};
// 检查指导教师修改次数
const isnumTeacher = () => {
  if (
    queryParams.value.guideTeacherOperateCount > 0 ||
    queryParams.value.guideTeacherOperateCount == -1
  ) {
    Teacher.value = true;
  }
};

// 添加新指导教师
const handleAddTeacher = () => {
  queryParams.value.guideTeacherApplyInfoList.push({
    userName: "",
    competitionRoleName: "指导教师",
    ADDcompetitionRoleName: "指导教师",
    applyInfoChangeOperateCount: -1,
    phone: "",
    email: "",
    isEditing: true,
    isNew: true,
    delFlag: 0,
  });
};

// 保存指导教师变更
const chucunjiaoshibiangeng = ref([]);
const zhidaolaosjy = ref("");
const handleSaveTeacher = () => {
  chucunjiaoshibiangeng.value = [];
  for (const item of queryParams.value.guideTeacherApplyInfoList) {
    if (item.isNew || item.isEditing) {
      if (!item.ADDuserName) {
        item.isxuesheng = "请输入姓名";
        return;
      }
      // 手机号选填，填写后才校验格式
      if (item.ADDphone && !validatePhone(item)) {
        return;
      }

      // 邮箱选填，填写后才校验格式
      if (item.ADDemail && !validateEmail(item)) {
        return;
      }
    }
  }

  queryParams.value.guideTeacherApplyInfoList.forEach((item) => {
    if (!item.memberId) {
      item.isEditing = false;
      item.isNew = false;
      item.userName = item.ADDuserName || item.userName;
      item.email = item.ADDemail || item.email;
      item.phone = item.ADDphone || item.phone;
      item.delFlag = 0;
      chucunjiaoshibiangeng.value.push(item);
    }
  });
  yishanchuzhidaolaoshi.value.forEach((item) => {
    item.delFlag = 1;
  });
  chucunjiaoshibiangeng.value.push(...yishanchuzhidaolaoshi.value);
  if (chucunjiaoshibiangeng.value.length > 0) {
    const params = {
      competitionSeriesId: queryParams.value.competitionSeriesId,
      competitionName: queryParams.value.competitionName,
      competitionTrackName: queryParams.value.competitionTrackName,
      competitionTrackId: queryParams.value.competitionTrackId,
      secondLevelCode: chunchuyuanbenshujv.value.secondLevelCode,
      secondLevelName: chunchuyuanbenshujv.value.secondLevelName,
      teamCode: queryParams.value.teamCode,
      teamName: queryParams.value.teamName,
      changeType: "change",
      competitionApplyInfoList: [...chucunjiaoshibiangeng.value],
    };
    checkChangeCompetitionApplyInfo(params)
      .then((res) => {
        if (res.code === 200) {
          // 是否显示删除按钮
          Teacher.value = false;
          iszhidaolaoshi.value = true;
          zhidaolaosjy.value = "";
        }
      })
      .catch((res) => {
        zhidaolaosjy.value = res.msg || "指导教师变更失败";
        iszhidaolaoshi.value = false;
      });
  } else {
    // 是否显示删除按钮
    zhidaolaosjy.value = "";
    Teacher.value = false;
    iszhidaolaoshi.value = true;
  }
};

// 获取操作配置信息 返回对应富文本以及时间操作项
const getOperationConfig = (operationType) => {
  return caozuoyaoqiu.value.find((item) => item.operationType == operationType);
};

// 处理用户信息标志
const handleUserInfoFlags = (list) => {
  if (list) {
    list.forEach((item) => {
      item.userInfoFlags = item.userInfoFlag?.split(",");
    });
  }
};

// 获取参赛信息列表
const getlist = () => {
  getUserCompetitionApplyInfo({
    teamCode: route.query.teamCode,
  })
    .then((res) => {
      if (res.data) {
        queryParams.value = res.data[0];
        // 处理用户信息标志
        handleUserInfoFlags(queryParams.value.competitionApplyInfoList);
        handleUserInfoFlags(queryParams.value.guideTeacherApplyInfoList);

        chunchuyuanbenshujv.value = JSON.parse(JSON.stringify(res.data[0]));

        const paramsTrackConfig = {
          competitionSeriesId: queryParams.value.competitionSeriesId,
          competitionTrackId: queryParams.value.competitionTrackId,
        };
        selectCompetitionTrackConfigInfolist(paramsTrackConfig);
        selectCompetitionOperationConfigInfolist(
          queryParams.value.competitionSeriesId
        );
      }
    })
    .then(() => {
      for (let i = 0; i < permissionStatus.value.length; i++) {
        isshowcheckChangeOperator(permissionStatus.value[i].operationFlag);
      }
    });
};
// 内容.
const xiangqing = ref({});
const xiangqingdialogVisible = ref(false);
const qiangzhitime = ref(null);

const isCurrentTimeInRange = (timeRangesStr) => {
  if (!timeRangesStr) return true;
  try {
    const timeRanges = timeRangesStr;
    if (!Array.isArray(timeRanges) || timeRanges.length === 0) return false;

    const currentTime = systemDate.value ? new Date(systemDate.value) : new Date();
    for (const range of timeRanges) {
      if (range.start && range.end) {
        const startTime = new Date(range.start);
        const endTime = new Date(range.end);
        if (currentTime >= startTime && currentTime <= endTime) {
          return true;
        }
      }
    }
    return false;
  } catch (error) {
    console.error("解析时间范围失败:", error);
    return false;
  }
};
let systemDate = ref(null);
const getSysTime = async () => {
  getSystemDate().then((res) => {
    systemDate.value = res.data || null;
  })
}
getSysTime()

const istijiao = ref([]);
const tijiao = (i) => {
  // 检查是否存在未保存的项
  let hasUnsavedItems = false;

  // 检查学生信息是否有未保存的项
  if (queryParams.value.competitionApplyInfoList) {
    for (const item of queryParams.value.competitionApplyInfoList) {
      if (item.isEditing) {
        hasUnsavedItems = true;
        break;
      }
    }
  }

  // 检查指导教师信息是否有未保存的项
  if (!hasUnsavedItems && queryParams.value.guideTeacherApplyInfoList) {
    for (const item of queryParams.value.guideTeacherApplyInfoList) {
      if (item.isEditing) {
        hasUnsavedItems = true;
        break;
      }
    }
  }

  // 如果存在未保存的项，直接跳出事件
  if (hasUnsavedItems) {
    istijiao.value[i] = "请先保存所有修改项";
    return;
  } else {
    istijiao.value[i] = "";
  }

  xiangqing.value = JSON.parse(
    JSON.stringify(caozuoyaoqiu.value.find((item) => item.operationType == i))
  );

  if (xiangqing.value.hintText2 == "" || xiangqing.value.hintText2 == null) {
    xiangqinghandleSave(i);
    return;
  }
  qiangzhitime.value = setInterval(() => {
    if (xiangqing.value.forceReadSeconds > 0) {
      xiangqing.value.forceReadSeconds--;
    }
    if (xiangqing.value.forceReadSeconds == 0) {
      clearInterval(qiangzhitime.value);
    }
  }, 1000);

  xiangqingloading.value = "";
  xiangqingdialogVisible.value = true;
};

const quxiaodialog = () => {
  clearInterval(qiangzhitime.value);
  xiangqingloading.value = "";
  xiangqingdialogVisible.value = false;
};
onBeforeUnmount(() => {
  clearInterval(qiangzhitime.value);
});
const competitionTrackConfigInfoList = ref([]);
// 获取竞赛赛道配置信息列表
const selectCompetitionTrackConfigInfolist = (paramsTrackConfig) => {
  selectCompetitionTrackConfigInfo(paramsTrackConfig).then((res) => {
    if (res.code == 200) {
      competitionTrackConfigInfoList.value = res.data;
    }
  });
};

const caozuoyaoqiu = ref([]);
// 获取竞赛操作配置信息列表
const selectCompetitionOperationConfigInfolist = (competitionSeriesId) => {
  selectCompetitionOperationConfigInfo(competitionSeriesId).then((res) => {
    if (res.code == 200) {
      caozuoyaoqiu.value = res.data;
      caozuoyaoqiu.value.forEach((item) => {
        item.jsonallowedTimeRanges = JSON.parse(item.allowedTimeRanges);
      });
    }
  });
};
const grouploading = ref(false);
const xiangqingloading = ref("");

const xiangqinghandleSave = (operationType) => {
  grouploading.value = true;
  const params = [];
  if (operationType == 1) {
    if (queryParams.value.iszubie) {
      // 组别已修改
      params.push({
        competitionSeriesId: queryParams.value.competitionSeriesId,
        competitionName: queryParams.value.competitionName,
        competitionTrackName: queryParams.value.competitionTrackName,
        competitionTrackId: queryParams.value.competitionTrackId,
        secondLevelCode: queryParams.value.secondLevelCode,
        secondLevelName: queryParams.value.secondLevelName,
        teamCode: queryParams.value.teamCode,
        teamName: queryParams.value.teamName,
        changeType: "group",
      });
    }
    let ss = {
      competitionSeriesId: queryParams.value.competitionSeriesId,
      competitionName: queryParams.value.competitionName,
      competitionTrackName: queryParams.value.competitionTrackName,
      competitionTrackId: queryParams.value.competitionTrackId,
      secondLevelCode: chunchuyuanbenshujv.value.secondLevelCode,
      secondLevelName: chunchuyuanbenshujv.value.secondLevelName,
      teamCode: queryParams.value.teamCode,
      teamName: queryParams.value.teamName,
      competitionRoleName: "队员",
      changeType: "info",
      competitionApplyInfoList: [],
    };
    let sb = {
      competitionSeriesId: queryParams.value.competitionSeriesId,
      competitionName: queryParams.value.competitionName,
      competitionTrackName: queryParams.value.competitionTrackName,
      competitionTrackId: queryParams.value.competitionTrackId,
      secondLevelCode: chunchuyuanbenshujv.value.secondLevelCode,
      secondLevelName: chunchuyuanbenshujv.value.secondLevelName,
      teamCode: queryParams.value.teamCode,
      teamName: queryParams.value.teamName,
      competitionRoleName: "队员",
      changeType: "change",
      competitionApplyInfoList: [],
    };
    forEach(queryParams.value.competitionApplyInfoList, (item) => {
      if (item.isChengyuan) {
        ss.competitionApplyInfoList.push(item);
      }
    });
    forEach(chucunchengyuanbiangeng.value, (item) => {
      sb.competitionApplyInfoList.push(item);
    });
    if (ss.competitionApplyInfoList.length > 0) {
      params.push(ss);
    }
    if (sb.competitionApplyInfoList.length > 0) {
      params.push(sb);
    }
  }
  if (operationType == 2) {
    let ss = {
      competitionSeriesId: queryParams.value.competitionSeriesId,
      competitionName: queryParams.value.competitionName,
      competitionTrackName: queryParams.value.competitionTrackName,
      competitionTrackId: queryParams.value.competitionTrackId,
      secondLevelCode: chunchuyuanbenshujv.value.secondLevelCode,
      secondLevelName: chunchuyuanbenshujv.value.secondLevelName,
      teamCode: queryParams.value.teamCode,
      teamName: queryParams.value.teamName,
      competitionRoleName: "指导教师",
      changeType: "info",
      competitionApplyInfoList: [],
    };
    let sb = {
      competitionSeriesId: queryParams.value.competitionSeriesId,
      competitionName: queryParams.value.competitionName,
      competitionTrackName: queryParams.value.competitionTrackName,
      competitionTrackId: queryParams.value.competitionTrackId,
      secondLevelCode: chunchuyuanbenshujv.value.secondLevelCode,
      secondLevelName: chunchuyuanbenshujv.value.secondLevelName,
      teamCode: queryParams.value.teamCode,
      teamName: queryParams.value.teamName,
      competitionRoleName: "指导教师",
      changeType: "changeTeacher",
      competitionApplyInfoList: [],
    };
    forEach(queryParams.value.guideTeacherApplyInfoList, (item) => {
      if (item.isChengyuan) {
        ss.competitionApplyInfoList.push(item);
      }
    });
    forEach(chucunjiaoshibiangeng.value, (item) => {
      sb.competitionApplyInfoList.push(item);
    });
    if (ss.competitionApplyInfoList.length > 0) {
      params.push(ss);
    }
    if (sb.competitionApplyInfoList.length > 0) {
      params.push(sb);
    }
  }

  xiangqingloading.value = "";
  changeCompetitionApplyInfo(params)
    .then((res) => {
      if (res.code == 200) {
        // ElMessage.success("修改成功");
        xiangqingdialogVisible.value = false;
        if (operationType == 1) {
          isxueshengtijiao.value = false;
        }
        if (operationType == 2) {
          iszhidaolaoshi.value = false;
        }
        // getlist();
        location.reload();
      }
      grouploading.value = false;
    })
    .catch((res) => {
      grouploading.value = false;
      xiangqingloading.value = res.msg;
    });
};
// 判定无修改返回上一页
const xiudialogVisible = ref(false);
const handleBack = () => {
  if (changeCount.value > 0 || jiaoshichangeCount.value > 0) {
    xiudialogVisible.value = true;
  } else {
    router.push({
      path: "/personal/list",
      query: {
        lefttabs: "我的团队",
      },
    });
  }
};

// 强制返回上一页
const querenback = () => {
  router.push({
    path: "/personal/list",
    query: {
      lefttabs: "我的团队",
    },
  });
};

// 手机号校验
const validatePhone = (row) => {
  row.isxuesheng = "";
  const phone = row.ADDphone;
  if (!phone) {
    row.isxuesheng = "请输入手机号";
    return false;
  }

  const phoneReg = /^1[3-9]\d{9}$/;
  if (!phoneReg.test(phone)) {
    row.isxuesheng = "请输入正确的手机号";
    return false;
  }

  return true;
};

// 证件号校验
const validateADDidCard = (row) => {
  row.isxuesheng = "";
  const ADDidCard = row.ADDidCard;
  if (!ADDidCard) {
    row.isxuesheng = "请输入证件号";
    return false;
  }
  return true;
};

// 邮箱校验
const validateEmail = (row) => {
  row.isxuesheng = "";
  const email = row.ADDemail;
  if (!email) {
    row.isxuesheng = "请输入邮箱";
    return false;
  }

  const emailReg = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
  if (!emailReg.test(email)) {
    row.isxuesheng = "请输入正确的邮箱";
    return false;
  }

  return true;
};

// 加密
const formatIdCard = (idCard) => {
  if (!idCard) return "";
  return idCard.substring(0, 6) + "********" + idCard.substring(14);
};

const formatphone = (phone) => {
  if (!phone) return "";
  return phone.substring(0, 3) + "****" + phone.substring(7);
};
const formatemail = (email) => {
  if (!email) return "";
  return email.substring(0, 3) + "****" + email.substring(7);
};
const isquzhifu = () => {
  router.push({
    path: "/personal/paymentrecords",
    query: {
      status: "pending",
    },
  });
};

// 调整顺序 loading
const sequenceLoading = ref(false);
// 调整顺序（上移/下移）
const handleMove = (row, direction) => {
  if (sequenceLoading.value) return;
  
  const directionText = direction === 'up' ? '上移' : '下移';
  
  ElMessageBox.confirm(
    `移动后将直接生效，是否确认${directionText}位置？`,
    '确认移动',
    {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
    .then(() => {
      sequenceLoading.value = true;
      const params = {
        competitionSeriesId: queryParams.value.competitionSeriesId,
        teamCode: queryParams.value.teamCode,
        memberId: row.memberId,
        change: direction
      };
      updateApplyInfoSequence(params)
        .then((res) => {
          if (res.code === 200) {
            ElMessage.success(`${directionText}成功`);
            getlist();
          } else {
            ElMessage.error(res.msg || `${directionText}失败`);
          }
        })
        .catch((res) => {
          ElMessage.error(res.msg || `${directionText}失败`);
        })
        .finally(() => {
          sequenceLoading.value = false;
        });
    })
    .catch(() => {
      // 用户取消，不做任何操作
    });
};

// 上移
const handleMoveUp = (row) => {
  handleMove(row, 'up');
};

// 下移
const handleMoveDown = (row) => {
  handleMove(row, 'down');
};

onMounted(() => {
  getlist();
});
</script>

<style scoped lang="scss">
:deep(.el-card__body) {
  padding: 0;
}

.bg {
  width: 100%;
}

.card {
  width: 100%;
  margin-top: 2.5rem;
  padding: 1.25rem 1.5625rem;
  min-height: 43.75rem;
  margin-bottom: 1.875rem;
}

.title {
  font-family: Source Han Sans CN, Source Han Sans CN;
  font-weight: bold;
  font-size: 1.25rem;
  color: #333333;
  line-height: 1.8125rem;
  text-align: left;
  font-style: normal;
  text-transform: none;
}

.tuandui {
  font-family: Source Han Sans CN, Source Han Sans CN;
  font-weight: 400;
  font-size: 0.875rem;
  color: #333333;
  line-height: 1.25rem;
  text-align: left;
  font-style: normal;
  text-transform: none;
  margin-top: 0.9375rem;
  display: flex;
}
.xuexiao {
  font-size: 1.125rem;
  margin: 0.625rem 0;
}
.biaotou {
  border-left: 0.25rem solid #3169f8;
  height: 1.25rem;
  padding-left: 0.625rem;
  font-family: Source Han Sans CN, Source Han Sans CN;
  font-weight: bold;
  font-size: 1.25rem;
  color: #333333;
  line-height: 1.8125rem;
  text-align: left;
  font-style: normal;
  text-transform: none;
  display: flex;
  align-items: center;
  margin-top: 0.9375rem;
}

.cansaixinxi {
  display: flex;
  align-items: center;
  margin-top: 1.5625rem;
}

.caisaibd {
  min-height: 18.75rem;
  padding-left: 1.25rem;
  width: calc(100% - 18.75rem);
}

.header-actions {
  display: flex;
  gap: 0.625rem;
  margin-bottom: 0.9375rem;
  align-items: center;
}

.group-edit-actions {
  display: flex;
  gap: 0.625rem;
  align-items: center;
}

/* 退费相关样式 */
.tuifei-container {
  display: flex;
  margin-top: 1.5625rem;
  background: #fafafa;
}

.tuifeichongjiao {
  width: 50%;
}

.tuisai {
  width: 50%;
}

.tuifei-content {
  margin-top: 1.25rem;
  display: flex;
}

// 开始
.tuifei-btn {
  height: 300px;
  margin-left: 1.25rem;
  display: flex;
  flex-direction: column;
}
.fanhuishangji {
  margin-top: 3.75rem;
  display: flex;
  justify-content: center;
}
.tijiao-btn {
  margin-top: 1.25rem;
  display: flex;
  justify-content: end;
  .tijiao-btn-content {
    width: 12.5rem;
    display: flex;
    justify-content: center;
  }
}
.search-btn {
  background: #3169f8;
  border-radius: 0.375rem 0.375rem 0.375rem 0.375rem;
  border: 0;
}

.disabled-btn {
  background: #b2b4bb;
  border-radius: 0.375rem 0.375rem 0.375rem 0.375rem;
  border: 0;
}
.tuisai-btn {
  width: 12.5rem;
  height: 3.125rem;
  background: #3169f8;
  border-radius: 0.375rem 0.375rem 0.375rem 0.375rem;
  font-family: Source Han Sans CN, Source Han Sans CN;
  font-weight: 400;
  font-size: 1rem;
  color: #ffffff;
  line-height: 1.375rem;
  text-align: center;
  font-style: normal;
  text-transform: none;
}
.gaodu {
  height: 1.5625rem;
}
.gaodu2 {
  height: 3.75rem;
}
:deep(.el-table .el-table__cell) {
  vertical-align: top;
}
:deep(.el-table .cell) {
  overflow: visible;
}
.danghang {
  line-height: 2.1875rem;
  width: 62.5rem;
  color: #f56c6c;
  position: absolute;
  top: 2.1875rem;
  right: 0rem;
  border-top: 0.0625rem solid #e5e5e5;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.875rem;
  .icon {
    height: 1rem;
    margin-right: 0.3125rem;
  }
}
:deep(.el-icon svg) {
  width: 1.25rem;
  height: 1.25rem;
}
:deep(.el-alert .el-alert__icon.is-big) {
  margin-right: 0.3125rem;
}
.truedel {
  color: #999797;
}
.falsedel {
  color: #333333;
}
.audit-info-item {
  display: flex;
  margin-bottom: 10px;
}

.audit-info-label {
  width: 100px;
  text-align: right;
  margin-right: 10px;
  font-weight: 500;
}

.audit-info-value {
  flex: 1;
  text-align: left;
  overflow: auto;
  max-height: 100px;
}

.status-running {
  color: #ffc400;
  font-weight: 500;
}

.status-rejected {
  color: #f56c6c;
  font-weight: 500;
}

.status-completed {
  color: #67c23a;
  font-weight: 500;
}
:deep(.el-alert) {
  align-items: flex-start;
  padding-right: 50px;
}
:deep(.el-icon) {
  height: 20px;
}
:deep(.el-alert .el-alert__description) {
  line-height: 20px;
}
:deep(.el-alert .el-alert__close-btn) {
  top: 8px;
}
.quzhifu{
  color: #3169f8;
  font-weight: 500;
  cursor: pointer;
}
</style>
