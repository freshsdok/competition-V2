package com.teaching.flowable.listener;

import cn.hutool.core.map.MapUtil;
import com.teaching.common.core.constant.SecurityConstants;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.core.utils.SpringUtils;
import com.teaching.flowable.service.IOperationFlowService;
import com.teaching.system.api.CompetitionService;
import com.teaching.system.api.RemoteNotificationService;
import com.teaching.system.api.domain.ChangeLog;
import com.teaching.system.api.domain.NotificationSendDTO;
import com.teaching.system.api.domain.OperationFlow;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

/**
 * 需要在流程中配置任务监听才能被调用
 *
 * @author Administrator
 */
@Slf4j
@Component(value = "changeLogListener")
public class ChangeLogListener implements TaskListener {


    @Override
    public void notify(DelegateTask delegateTask) {
        System.out.println("进入任务监听器...");
        // 获取任务的变量
        CompetitionService competitionService = SpringUtils.getBean(CompetitionService.class);
        Map<String, Object> variables = delegateTask.getVariables();
        //操作类型（change人员变更，repayment退费重缴费,retired退赛）
        String operationType = MapUtil.getStr(variables, "operationType");
        ChangeLog changeLog = new ChangeLog();
        Date nowDate = DateUtils.getNowDate();
        changeLog.setChangeType(operationType);
        //ApplicantId ApplicantName
        //操作人信息只有退赛申请使用userid和nickName，其他操作使用ApplicantId和ApplicantName
        Long userId = MapUtil.getLong(variables, "ApplicantId");
        String nickName = MapUtil.getStr(variables, "ApplicantName");
        changeLog.setIpAddress(MapUtil.getStr(variables, "ipAddress"));
        changeLog.setOperatorUserId(userId);
        changeLog.setUserId(userId);
        changeLog.setChangeTime(nowDate);
        changeLog.setCreateBy(nickName);
        changeLog.setCreateTime(nowDate);
        changeLog.setTeamId(MapUtil.getStr(variables, "teamCode"));
        changeLog.setResult("审批通过");
        changeLog.setOldData(MapUtil.getStr(variables, "oldData"));
        changeLog.setNewData(MapUtil.getStr(variables, "newData", null));
        String teamInfo = MapUtil.getStr(variables, "teamInfo");
        String changeDetails = String.format("%s，%s申请了，" + teamInfo + "团队的", DateUtils.dateTimeNow("yyyy年MM月dd日"), nickName);
        String details = "retired".equals(operationType) ? "【退赛退费】" : ("repayment".equals(operationType) ? "【退费重缴】" : String.format("【人员变更】，删除了成员%s", MapUtil.getStr(variables, "delUserNames")));
        String content = changeDetails + details;
        changeLog.setChangeDetails(content);
        competitionService.insertChangeLog(changeLog, SecurityConstants.INNER);
        // 更新流程团队关联表 状态 completed
        IOperationFlowService operationFlowService = SpringUtils.getBean(IOperationFlowService.class);
        operationFlowService.updateStatusByFlowId(new OperationFlow(MapUtil.getStr(variables, "traceabilityCode"), "completed"));

        // 发送站内信通知申请人：变更/退赛/退费审批通过
        try {
            RemoteNotificationService notificationService = SpringUtils.getBean(RemoteNotificationService.class);
            NotificationSendDTO dto = new NotificationSendDTO();
            dto.setTitle("变更审批已通过");
            dto.setContent(content);
            // 2-系统通知（notification_message_type 字典）
            dto.setMessageType("2");
            // 收件人：申请人
            dto.setReceiverUserIds(String.valueOf(userId));
            // 机构、发送人（0 表示系统）
            Long orgId = MapUtil.getLong(variables, "orgId");
            dto.setOrgId(orgId);
            dto.setSenderUserId(0L);

            notificationService.send(dto, SecurityConstants.INNER);
        } catch (Exception e) {
            log.error("变更审批通过站内信发送失败, traceabilityCode={}", MapUtil.getStr(variables, "traceabilityCode"), e);
        }
    }
}
