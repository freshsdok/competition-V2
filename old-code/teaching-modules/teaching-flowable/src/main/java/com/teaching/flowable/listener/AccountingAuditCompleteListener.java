package com.teaching.flowable.listener;

import cn.hutool.core.map.MapUtil;
import com.teaching.common.core.constant.SecurityConstants;
import com.teaching.common.core.utils.SpringUtils;
import com.teaching.system.api.RemoteNotificationService;
import com.teaching.system.api.domain.NotificationSendDTO;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 会计审核完成站内信监听器
 * 在流程中配置到「会计审核」节点的完成事件（complete）后，任务完成时向指定用户发送站内信
 *
 * @author teaching
 */
@Slf4j
@Component(value = "accountingAuditCompleteListener")
public class AccountingAuditCompleteListener implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {
        // 仅在任务完成事件时发送站内信
        if (!"complete".equals(delegateTask.getEventName())) {
            return;
        }

        Map<String, Object> variables = delegateTask.getVariables();
        // 收件人：优先使用流程变量 receiverUserIds（逗号分隔），若无则通知发起人 starterUserId
        String receiverUserIds = MapUtil.getStr(variables, "receiverUserIds");
        if (receiverUserIds == null || receiverUserIds.trim().isEmpty()) {
            Long starterUserId = MapUtil.getLong(variables, "starterUserId");
            if (starterUserId == null) {
                log.warn("会计审核完成站内信：未配置 receiverUserIds 或 starterUserId，跳过发送");
                return;
            }
            receiverUserIds = String.valueOf(starterUserId);
        }

        NotificationSendDTO dto = new NotificationSendDTO();
        dto.setTitle("会计审核已完成");
        dto.setContent("任务「" + delegateTask.getName() + "」已通过会计审核。");
        dto.setMessageType("2"); // 2-系统通知
        dto.setRelatedType("FLOWABLE_TASK");
        dto.setReceiverUserIds(receiverUserIds);

        Long orgId = MapUtil.getLong(variables, "orgId");
        Long starterUserId = MapUtil.getLong(variables, "starterUserId");
        dto.setOrgId(orgId);
        dto.setSenderUserId(starterUserId != null ? starterUserId : 0L);

        try {
            RemoteNotificationService remoteNotificationService = SpringUtils.getBean(RemoteNotificationService.class);
            remoteNotificationService.send(dto, SecurityConstants.INNER);
        } catch (Exception e) {
            log.error("会计审核完成站内信发送失败, taskId={}", delegateTask.getId(), e);
        }
    }
}
