package com.teaching.flowable.listener;

import cn.hutool.core.map.MapUtil;
import com.teaching.common.core.constant.SecurityConstants;
import com.teaching.system.api.RemoteNotificationService;
import com.teaching.system.api.domain.NotificationSendDTO;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.identitylink.api.IdentityLink;
import org.flowable.task.service.delegate.DelegateTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Set;
import java.util.Map;
import java.util.stream.Collectors;
/**
 * 用户任务监听器
 * 需要在流程中配置任务监听才能被调用
 *
 * @author KonBAI
 * @since 2023/5/13
 */
 @Component(value = "userTaskListener")
 public class UserTaskListener implements TaskListener {
     @Override
     public void notify(DelegateTask delegateTask) {
         System.out.println("进入执行任务监听器...");

         Map<String, Object> variables = delegateTask.getVariables();
         String treatmentType22 = MapUtil.getStr(variables, "treatmentType22");
         //转办 摇人
         if("5".equals(treatmentType22)){
             // 强制清空所有候选组
             delegateTask.getCandidates().stream()
                     .map(IdentityLink::getGroupId)
                     .filter(Objects::nonNull)
                     .forEach(delegateTask::deleteCandidateGroup);

             // 事务提交后验证（需通过ServiceTask后续检查）
             delegateTask.setVariable("REFRESH_CANDIDATES", true);
             String sponsor = MapUtil.getStr(variables, "sponsor");
             delegateTask.addCandidateUser(sponsor);
         }
     }


/*@Component(value = "userTaskListener")
public class UserTaskListener implements TaskListener {

    @Autowired
    private RemoteNotificationService remoteNotificationService;

    @Override
    public void notify(DelegateTask delegateTask) {
        System.out.println("进入执行任务监听器...");

        Map<String, Object> variables = delegateTask.getVariables();
        String treatmentType22 = MapUtil.getStr(variables, "treatmentType22");
        // 转办 摇人
        if ("5".equals(treatmentType22)) {
            // 强制清空所有候选组
            delegateTask.getCandidates().stream()
                    .map(IdentityLink::getGroupId)
                    .filter(Objects::nonNull)
                    .forEach(delegateTask::deleteCandidateGroup);

            // 事务提交后验证（需通过ServiceTask后续检查）
            delegateTask.setVariable("REFRESH_CANDIDATES", true);
            String sponsor = MapUtil.getStr(variables, "sponsor");
            delegateTask.addCandidateUser(sponsor);
        }

        // 只在任务创建事件时发送站内信，避免每个事件都推一次
        if (!"create".equals(delegateTask.getEventName())) {
            return;
        }

        // 收件人：所有候选用户（按你实际业务也可以改成 assignee）
        Set<String> candidateUserIds = delegateTask.getCandidates().stream()
                .map(IdentityLink::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (candidateUserIds.isEmpty()) {
            return;
        }
        String receiverUserIds = String.join(",", candidateUserIds);

        NotificationSendDTO dto = new NotificationSendDTO();
        dto.setTitle("您有新的待办任务");
        dto.setContent("任务：" + delegateTask.getName());
        dto.setMessageType("1");
        // 如果 executionId 不是纯数字，可以不用 relatedId，只用 relatedType
        dto.setRelatedType("FLOWABLE_TASK");
        dto.setReceiverUserIds(receiverUserIds);

        // 机构、发起人从流程变量中取（请按你实际变量名调整）
        Long orgId = MapUtil.getLong(variables, "orgId");
        Long starterUserId = MapUtil.getLong(variables, "starterUserId");
        dto.setOrgId(orgId);
        dto.setSenderUserId(starterUserId != null ? starterUserId : 0L);

        // FROM_SOURCE 标记为内部调用
        remoteNotificationService.send(dto, SecurityConstants.INNER);
    }
}*/

    /**
     * 将消息推送给发起人
     *
     * @param deptId  部门ID
     * @param userIds 候选人ID列表
     * @param starter 发起人ID
     */
   /* private void pushMessToStarter(String deptId, List<String> userIds, Long starter) {
        if (CollectionUtils.isEmpty(userIds)) {
            System.out.println("没有候选人，给发起人发消息");
            if (StringUtils.isNotNull(starter)) {
                List<String> userPhoneList = new ArrayList<>();
                Set<Long> starterIdSet = new HashSet<>();
                Set<String> userNameSet = new HashSet<>();

                SysUser sysUser = SpringUtils.getBean(UserService.class).selectUserById(starter);
                userPhoneList.add(sysUser.getPhonenumber());
                userNameSet.add(sysUser.getNickName());
                starterIdSet.add(sysUser.getUserId());

                String dingTalkUserIds = DingTalkUtil.getDingtalkUidByUserList(userPhoneList);
                if (StringUtils.isNotBlank(dingTalkUserIds)) {
                    String taskId = "";
                    String errCode = "";
                    String errMsg = "";
                    try {
                        String resultMsg = DingTalkMessageUtil.sendOaMessage(dingTalkUserIds,
                                "流程无审核人提醒",
                                "您有流程节点没有审核人，请联系管理员处理",
                                "5");
                        com.alibaba.fastjson2.JSONObject jsonObject = com.alibaba.fastjson2.JSONObject.parseObject(resultMsg);
                        taskId = jsonObject.getString("task_id");
                        errCode = jsonObject.getString("errcode");
                        errMsg = jsonObject.getString("errmsg");
                    } catch (Exception e) {
                        System.err.println("流程无审核人消息推送失败, userDingCode: {" + dingTalkUserIds + "}");
                    } finally {
                        Map<String, Object> map = new HashMap<>();
                        map.put("deptId", deptId);
                        map.put("messageType", "7");
                        map.put("pushType", "0".equals(errCode) ? "2" : "1");
                        map.put("recipient", StringUtils.join(starterIdSet, ","));
                        map.put("recipientName", StringUtils.join(userNameSet, ","));
                        map.put("pushBy", "流程消息推送");
                        Date nowDate = DateUtils.getNowDate();
                        map.put("createTime", nowDate);
                        map.put("pushTime", nowDate);
                        map.put("interfaceType", "1");
                        map.put("processQueryKey", taskId);
                        map.put("pushContent", "您有流程节点没有审核人，请联系管理员处理");
                        map.put("remark", errCode + ":" + errMsg);
                        SpringUtils.getBean(UserService.class).insertYqfMessagePush(map);
                    }
                }
            }
        }
    }*/

}
