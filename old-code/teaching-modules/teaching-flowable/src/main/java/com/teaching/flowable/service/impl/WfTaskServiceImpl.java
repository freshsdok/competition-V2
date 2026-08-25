package com.teaching.flowable.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.teaching.common.core.JsonUtils;
import com.teaching.common.core.constant.SecurityConstants;
import com.teaching.common.core.exception.ServiceException;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.core.utils.SpringUtils;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.flowable.common.constant.ProcessConstants;
import com.teaching.flowable.common.constant.TaskConstants;
import com.teaching.flowable.common.enums.FlowComment;
import com.teaching.flowable.common.enums.ProcessStatus;
import com.teaching.flowable.core.FormConf;
import com.teaching.flowable.domain.WfDeployForm;
import com.teaching.flowable.domain.bo.WfTaskBo;
import com.teaching.flowable.domain.vo.WfDeployFormVo;
import com.teaching.flowable.factory.FlowServiceFactory;
import com.teaching.flowable.flow.CustomProcessDiagramGenerator;
import com.teaching.flowable.flow.FlowableUtils;
import com.teaching.flowable.mapper.OperationFlowMapper;
import com.teaching.flowable.mapper.WfDeployFormMapper;
import com.teaching.flowable.service.IWfCopyService;
import com.teaching.flowable.service.IWfTaskService;
import com.teaching.flowable.utils.ModelUtils;
import com.teaching.flowable.utils.TaskUtils;
import com.teaching.system.api.CompetitionService;
import com.teaching.system.api.OrderService;
import com.teaching.system.api.RemoteNotificationService;
import com.teaching.system.api.domain.ChangeLog;
import com.teaching.system.api.domain.NotificationSendDTO;
import com.teaching.system.api.domain.OperationFlow;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.flowable.bpmn.constants.BpmnXMLConstants;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.*;
import org.flowable.common.engine.api.FlowableException;
import org.flowable.common.engine.api.FlowableObjectNotFoundException;
import org.flowable.common.engine.impl.identity.Authentication;
import org.flowable.engine.ProcessEngineConfiguration;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.task.Comment;
import org.flowable.image.ProcessDiagramGenerator;
import org.flowable.task.api.DelegationState;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.variable.api.history.HistoricVariableInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author KonBAI
 * @createTime 2022/3/10 00:12
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class WfTaskServiceImpl extends FlowServiceFactory implements IWfTaskService {

//    private final UserService sysUserService;

    private final IWfCopyService copyService;

    private final WfDeployFormMapper deployFormMapper;
    private final OperationFlowMapper operationFlowMapper;

//    private final SysUserMapper userMapper;
//    private final ISysMessagePushService messagePushService;

    /**
     * 完成任务
     *
     * @param taskBo 请求实体参数
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void complete(WfTaskBo taskBo) {
        taskBo.setComment(taskBo.getComment() + "   " + DateUtil.now());
        Task task = taskService.createTaskQuery().taskId(taskBo.getTaskId()).singleResult();
        if (Objects.isNull(task)) {
            throw new ServiceException("任务不存在");
        }
        if (task.isSuspended()) {
            throw new RuntimeException("任务处于挂起状态");
        }
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(task.getProcessInstanceId())
                .active()
                .singleResult();
        if (ObjectUtil.isNull(processInstance)) {
            throw new RuntimeException("流程已结束或已挂起，无法执行此操作");
        }
        // 获取 bpmn 模型
        BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
        if (DelegationState.PENDING.equals(task.getDelegationState())) {
            taskService.addComment(taskBo.getTaskId(), taskBo.getProcInsId(), FlowComment.DELEGATE.getType(), taskBo.getComment());
            taskService.resolveTask(taskBo.getTaskId());
        } else {
            String treatmentType = FlowComment.NORMAL.getType();
            taskService.addComment(taskBo.getTaskId(), taskBo.getProcInsId(), treatmentType, taskBo.getComment());
            taskService.setAssignee(taskBo.getTaskId(), TaskUtils.getUserId());
            if (ObjectUtil.isNotEmpty(taskBo.getVariables())) {
                // 获取模型信息
                String localScopeValue = ModelUtils.getUserTaskAttributeValue(bpmnModel, task.getTaskDefinitionKey(), ProcessConstants.PROCESS_FORM_LOCAL_SCOPE);
                boolean localScope = Convert.toBool(localScopeValue, false);
                taskService.complete(taskBo.getTaskId(), taskBo.getVariables(), localScope);
            } else {
                taskService.complete(taskBo.getTaskId());
            }
        }
        // 设置任务节点名称
        taskBo.setTaskName(task.getName());
        // 处理下一级审批人
        if (StringUtils.isNotBlank(taskBo.getNextUserIds())) {
            this.assignNextUsers(bpmnModel, taskBo.getProcInsId(), taskBo.getNextUserIds());
        }
        // 处理抄送用户
        if (!copyService.makeCopy(taskBo)) {
            throw new RuntimeException("抄送任务失败");
        }
        //saveInfo(task, bpmnModel);
        //推送完成消息
//        processEndToMessage(task, bpmnModel);
    }

    /**
     * 获取type
     *
     * @param variables
     */
    public String getLastTreatmentType(Map<String, Object> variables) {
        Set<String> keys = variables.keySet();
        Iterator<String> iterator = keys.iterator();
        if (iterator.hasNext()) {
            String key = iterator.next();
            if (key.startsWith("treatmentType")) {
                return MapUtil.getStr(variables, key);
            }
        }
        return null;
    }

    /**
     * 流程审批结束给发起人推消息
     *
     * @param task
     * @param bpmnModel
     */
    public void processEndToMessage(Task task, BpmnModel bpmnModel) {
        HistoricProcessInstance historicProcIns = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(task.getProcessInstanceId())
                .includeProcessVariables()
                .singleResult();
        if (historicProcIns != null && historicProcIns.getEndTime() != null) {
            System.out.println("流程实例已结束");
            //发起人id
            Long startUserId = Long.valueOf(historicProcIns.getStartUserId());
            /*SysUser sysUsers = userMapper.selectUserById(startUserId);
            String nickName = sysUsers.getNickName();
            String phonenumber = sysUsers.getPhonenumber();
            Long deptId = sysUsers.getDeptId();
            messagePushService.sendSysMessagePush(Collections.singletonList(deptId + ""), Collections.singletonList(phonenumber), Collections.singleton(startUserId), Collections.singleton(nickName), "finished");*/
        }
    }


    /**
     * 获取表单中的数据
     *
     * @param bpmnModel
     * @param historicProcIns
     * @return
     */
    private List<FormConf> processFormList(BpmnModel bpmnModel, HistoricProcessInstance historicProcIns) {
        List<FormConf> procFormList = new ArrayList<>();

        List<HistoricActivityInstance> activityInstanceList = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(historicProcIns.getId()).finished()
                .activityTypes(CollUtil.newHashSet(BpmnXMLConstants.ELEMENT_EVENT_START, BpmnXMLConstants.ELEMENT_TASK_USER))
                .orderByHistoricActivityInstanceStartTime().asc()
                .list();
        List<String> processFormKeys = new ArrayList<>();
        for (HistoricActivityInstance activityInstance : activityInstanceList) {
            // 获取当前节点流程元素信息
            FlowElement flowElement = ModelUtils.getFlowElementById(bpmnModel, activityInstance.getActivityId());
            // 获取当前节点表单Key
            String formKey = ModelUtils.getFormKey(flowElement);
            if (formKey == null) {
                continue;
            }
            boolean localScope = Convert.toBool(ModelUtils.getElementAttributeValue(flowElement, ProcessConstants.PROCESS_FORM_LOCAL_SCOPE), false);
            Map<String, Object> variables;
            if (localScope) {
                variables = historyService.createHistoricVariableInstanceQuery()
                        .processInstanceId(historicProcIns.getId())
                        .taskId(activityInstance.getTaskId())
                        .list()
                        .stream()
                        .collect(Collectors.toMap(HistoricVariableInstance::getVariableName, i -> Optional.ofNullable(i.getValue()).orElse(""), (v1, v2) -> v1));

            } else {
                if (processFormKeys.contains(formKey)) {
                    continue;
                }
                variables = historicProcIns.getProcessVariables();
                processFormKeys.add(formKey);
            }
            // 非节点表单此处查询结果可能有多条，只获取第一条信息
            List<WfDeployFormVo> formInfoList = deployFormMapper.selectVoList(new LambdaQueryWrapper<WfDeployForm>()
                    .eq(WfDeployForm::getDeployId, historicProcIns.getDeploymentId())
                    .eq(WfDeployForm::getFormKey, formKey)
                    .eq(localScope, WfDeployForm::getNodeKey, flowElement.getId()));

            //@update by Brath：避免空集合导致的NULL空指针
            WfDeployFormVo formInfo = formInfoList.stream().findFirst().orElse(null);

            if (ObjectUtil.isNotNull(formInfo)) {
                // 旧数据 formInfo.getFormName() 为 null
                String formName = Optional.ofNullable(formInfo.getFormName()).orElse(StringUtils.EMPTY);
                String title = localScope ? formName.concat("(" + flowElement.getName() + ")") : formName;
                FormConf formConf = new FormConf();
                Map<String, Object> formModel = JsonUtils.parseObject(formInfo.getContent(), Map.class);
                if (null != formModel && !formModel.isEmpty()) {
                    formConf.setTitle(title);
                    formConf.setDisabled(true);
                    formConf.setFormBtns(false);
                    formConf.setFormModel(formModel);
                    formConf.setFormData(variables);
                    procFormList.add(formConf);
                }
            }
        }
        return procFormList;
    }

    /**
     * 拒绝任务
     *
     * @param taskBo
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void taskReject(WfTaskBo taskBo) {
        // 当前任务 task
        Task task = taskService.createTaskQuery().taskId(taskBo.getTaskId()).singleResult();
        if (ObjectUtil.isNull(task)) {
            throw new RuntimeException("获取任务信息异常！");
        }
        if (task.isSuspended()) {
            throw new RuntimeException("任务处于挂起状态");
        }
       /* // 获取流程实例
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
            .processInstanceId(taskBo.getProcInsId())
            .singleResult();
        if (processInstance == null) {
            throw new RuntimeException("流程实例不存在，请确认！");
        }*/
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(task.getProcessInstanceId())
                .active()
                .singleResult();
        if (ObjectUtil.isNull(processInstance)) {
            throw new RuntimeException("流程已结束或已挂起，无法执行此操作");
        }
        // 获取流程定义信息
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(task.getProcessDefinitionId())
                .singleResult();

        // 添加审批意见
        taskService.addComment(taskBo.getTaskId(), taskBo.getProcInsId(), FlowComment.REJECT.getType(), taskBo.getComment());
        taskService.setAssignee(taskBo.getTaskId(), TaskUtils.getUserId());
        // 设置流程状态为已终结
        runtimeService.setVariable(processInstance.getId(), ProcessConstants.PROCESS_STATUS_KEY, ProcessStatus.TERMINATED.getStatus());
        // 获取所有节点信息
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinition.getId());
        EndEvent endEvent = ModelUtils.getEndEvent(bpmnModel);
        // 终止流程
        List<Execution> executions = runtimeService.createExecutionQuery().parentId(task.getProcessInstanceId()).list();
        List<String> executionIds = executions.stream().map(Execution::getId).collect(Collectors.toList());
        runtimeService.createChangeActivityStateBuilder()
                .processInstanceId(task.getProcessInstanceId())
                .moveExecutionsToSingleActivityId(executionIds, endEvent.getId())
                .changeState();
        // 处理抄送用户
        if (!copyService.makeCopy(taskBo)) {
            throw new RuntimeException("抄送任务失败");
        }
        handleIntermediateTables(task, bpmnModel, "rejected");
    }

    /**
     * 拒绝/终止流程时
     *
     * @param task
     * @param bpmnModel
     */
    private void handleIntermediateTables(Task task, BpmnModel bpmnModel, String type) {
        System.out.println("审核拒绝/终止！！");
        HistoricProcessInstance historicProcIns = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(task.getProcessInstanceId())
                .includeProcessVariables()
                .singleResult();
        if (historicProcIns != null && historicProcIns.getEndTime() != null) {
            List<FormConf> formConfs = processFormList(bpmnModel, historicProcIns);
            Map<String, Object> formDataEnd = new HashMap<>();
            for (FormConf formConf : formConfs) {
                Map<String, Object> formData = formConf.getFormData();
                formDataEnd.putAll(formData);
            }
            String operationType = MapUtil.getStr(formDataEnd, "operationType");
            CompetitionService competitionService = SpringUtils.getBean(CompetitionService.class);
            OrderService orderService = SpringUtils.getBean(OrderService.class);
            //处理订单信息 修改订单状态
            orderService.updateRefundCancelStatus(MapUtil.getLong(formDataEnd, "refundOrderId"), SecurityConstants.INNER);

            ChangeLog changeLog = new ChangeLog();
            Date nowDate = DateUtils.getNowDate();
            changeLog.setChangeType(operationType);
            Long userId = MapUtil.getLong(formDataEnd, "ApplicantId");
            String nickName = MapUtil.getStr(formDataEnd, "ApplicantName");
            changeLog.setIpAddress(MapUtil.getStr(formDataEnd, "ipAddress"));
            changeLog.setOperatorUserId(userId);
            changeLog.setUserId(userId);
            changeLog.setChangeTime(nowDate);
            changeLog.setResult("rejected".equals(type) ? "审批驳回" : "审批被终止");
            changeLog.setCreateBy(nickName);
            changeLog.setCreateTime(nowDate);
            // teamId 前端传过来
            changeLog.setTeamId(MapUtil.getStr(formDataEnd, "teamCode"));
            // 新数据 前端传过来
            changeLog.setNewData(MapUtil.getStr(formDataEnd, "newData", null));
            // 旧数据 查出来 or 传过来
            changeLog.setOldData(MapUtil.getStr(formDataEnd, "oldData"));
            String teamInfo = MapUtil.getStr(formDataEnd, "teamInfo", null);
            String changeDetails = String.format("%s，%s申请了，" + teamInfo + "团队的", DateUtils.dateTimeNow("yyyy年MM月dd日"), nickName);
            String details = "retired".equals(operationType)
                    ? "【退赛】"
                    : ("repayment".equals(operationType) ? "【退费重缴费】" : String.format("【人员变更】，删除/添加了%s成员", nickName));
            String content = changeDetails + details;
            changeLog.setChangeDetails(content);
            //新增日志信息
            competitionService.insertChangeLog(changeLog, SecurityConstants.INNER);
            // 处理流程和团队操作中间表   修改操作流程管理表状态
            operationFlowMapper.updateStatusByFlowId(new OperationFlow(MapUtil.getStr(formDataEnd, "traceabilityCode"), type));
            // 审批驳回时发送站内信给申请人
            try {
                RemoteNotificationService notificationService = SpringUtils.getBean(RemoteNotificationService.class);
                NotificationSendDTO dto = new NotificationSendDTO();
                dto.setTitle("rejected".equals(type) ? "变更审批已驳回" : "变更审批已被终止");
                dto.setContent(content);
                // 2-系统通知
                dto.setMessageType("2");
                // 收件人：申请人
                dto.setReceiverUserIds(String.valueOf(userId));
                // 机构、发送人（0 表示系统）
                Long orgId = MapUtil.getLong(formDataEnd, "orgId");
                dto.setOrgId(orgId);
                dto.setSenderUserId(0L);

                notificationService.send(dto, SecurityConstants.INNER);
            } catch (Exception e) {
                log.error("变更审批驳回站内信发送失败, traceabilityCode={}", MapUtil.getStr(formDataEnd, "traceabilityCode"), e);
            }
        }
    }

    /**
     * 终止
     *
     * @param taskBo
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void taskStop(WfTaskBo taskBo) {
        // 当前任务 task
        Task task = taskService.createTaskQuery().taskId(taskBo.getTaskId()).singleResult();
        if (ObjectUtil.isNull(task)) {
            throw new RuntimeException("获取任务信息异常！");
        }
        if (task.isSuspended()) {
            throw new RuntimeException("任务处于挂起状态");
        }
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(task.getProcessInstanceId())
                .active()
                .singleResult();
        if (ObjectUtil.isNull(processInstance)) {
            throw new RuntimeException("流程已结束或已挂起，无法执行此操作");
        }
        // 获取流程定义信息
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(task.getProcessDefinitionId())
                .singleResult();

        // 添加审批意见
        taskService.addComment(taskBo.getTaskId(), taskBo.getProcInsId(), FlowComment.STOP.getType(), taskBo.getComment());
        // 设置流程状态为已终结
        runtimeService.setVariable(processInstance.getId(), ProcessConstants.PROCESS_STATUS_KEY, ProcessStatus.TERMINATED.getStatus());
        // 获取所有节点信息
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinition.getId());
        EndEvent endEvent = ModelUtils.getEndEvent(bpmnModel);
        // 终止流程
        List<Execution> executions = runtimeService.createExecutionQuery().parentId(task.getProcessInstanceId()).list();
        List<String> executionIds = executions.stream().map(Execution::getId).collect(Collectors.toList());
        runtimeService.createChangeActivityStateBuilder()
                .processInstanceId(task.getProcessInstanceId())
                .moveExecutionsToSingleActivityId(executionIds, endEvent.getId())
                .changeState();
        handleIntermediateTables(task, bpmnModel, "terminated");
    }

    /**
     * 退回任务
     *
     * @param bo 请求实体参数
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void taskReturn(WfTaskBo bo) {
        // 当前任务 task
        Task task = taskService.createTaskQuery().taskId(bo.getTaskId()).singleResult();
        if (ObjectUtil.isNull(task)) {
            throw new RuntimeException("获取任务信息异常！");
        }
        if (task.isSuspended()) {
            throw new RuntimeException("任务处于挂起状态");
        }
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(task.getProcessInstanceId())
                .active()
                .singleResult();
        if (ObjectUtil.isNull(processInstance)) {
            throw new RuntimeException("流程已结束或已挂起，无法执行此操作");
        }
        // 获取流程定义信息
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(task.getProcessDefinitionId()).singleResult();
        // 获取流程模型信息
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinition.getId());
        // 获取当前任务节点元素
        FlowElement source = ModelUtils.getFlowElementById(bpmnModel, task.getTaskDefinitionKey());
        // 获取跳转的节点元素
        FlowElement target = ModelUtils.getFlowElementById(bpmnModel, bo.getTargetKey());
        // 从当前节点向前扫描，判断当前节点与目标节点是否属于串行，若目标节点是在并行网关上或非同一路线上，不可跳转
        boolean isSequential = ModelUtils.isSequentialReachable(source, target, new HashSet<>());
        if (!isSequential) {
            throw new RuntimeException("当前节点相对于目标节点，不属于串行关系，无法回退");
        }

        // 获取所有正常进行的任务节点 Key，这些任务不能直接使用，需要找出其中需要撤回的任务
        List<Task> runTaskList = taskService.createTaskQuery().processInstanceId(task.getProcessInstanceId()).list();
        List<String> runTaskKeyList = new ArrayList<>();
        runTaskList.forEach(item -> runTaskKeyList.add(item.getTaskDefinitionKey()));
        // 需退回任务列表
        List<String> currentIds = new ArrayList<>();
        // 通过父级网关的出口连线，结合 runTaskList 比对，获取需要撤回的任务
        List<UserTask> currentUserTaskList = FlowableUtils.iteratorFindChildUserTasks(target, runTaskKeyList, null, null);
        currentUserTaskList.forEach(item -> currentIds.add(item.getId()));

        // 循环获取那些需要被撤回的节点的ID，用来设置驳回原因
        Set<String> currentTaskIds = new HashSet<>();
        currentIds.forEach(currentId -> runTaskList.forEach(runTask -> {
            if (currentId.equals(runTask.getTaskDefinitionKey())) {
                currentTaskIds.add(runTask.getId());
            }
        }));
        // 设置回退意见
        for (String currentTaskId : currentTaskIds) {
            taskService.addComment(currentTaskId, task.getProcessInstanceId(), FlowComment.REBACK.getType(), bo.getComment());
            taskService.setAssignee(task.getId(), TaskUtils.getUserId());
        }

        try {
            // 1 对 1 或 多 对 1 情况，currentIds 当前要跳转的节点列表(1或多)，targetKey 跳转到的节点(1)
            runtimeService.createChangeActivityStateBuilder()
                    .processInstanceId(task.getProcessInstanceId())
                    .moveActivityIdsToSingleActivityId(currentIds, bo.getTargetKey()).changeState();
        } catch (FlowableObjectNotFoundException e) {
            throw new RuntimeException("未找到流程实例，流程可能已发生变化");
        } catch (FlowableException e) {
            throw new RuntimeException("无法取消或开始活动");
        }
        // 设置任务节点名称
        bo.setTaskName(task.getName());
        // 处理抄送用户
        if (!copyService.makeCopy(bo)) {
            throw new RuntimeException("抄送任务失败");
        }
    }


    /**
     * 获取所有可回退的节点
     *
     * @param bo
     * @return
     */
    @Override
    public List<FlowElement> findReturnTaskList(WfTaskBo bo) {
        // 当前任务 task
        Task task = taskService.createTaskQuery().taskId(bo.getTaskId()).singleResult();
        // 获取流程定义信息
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(task.getProcessDefinitionId()).singleResult();
        // 获取流程模型信息
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinition.getId());
        // 查询历史节点实例
        List<HistoricActivityInstance> activityInstanceList = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(task.getProcessInstanceId())
                .activityType(BpmnXMLConstants.ELEMENT_TASK_USER)
                .finished()
                .orderByHistoricActivityInstanceEndTime().asc()
                .list();
        List<String> activityIdList = activityInstanceList.stream()
                .map(HistoricActivityInstance::getActivityId)
                .filter(activityId -> !StringUtils.equals(activityId, task.getTaskDefinitionKey()))
                .distinct()
                .collect(Collectors.toList());
        // 获取当前任务节点元素
        FlowElement source = ModelUtils.getFlowElementById(bpmnModel, task.getTaskDefinitionKey());
        List<FlowElement> elementList = new ArrayList<>();
        for (String activityId : activityIdList) {
            FlowElement target = ModelUtils.getFlowElementById(bpmnModel, activityId);
            boolean isSequential = ModelUtils.isSequentialReachable(source, target, new HashSet<>());
            if (isSequential) {
                elementList.add(target);
            }
        }
        return elementList;
    }

    /**
     * 删除任务
     *
     * @param bo 请求实体参数
     */
    @Override
    public void deleteTask(WfTaskBo bo) {
        taskService.deleteTask(bo.getTaskId(), bo.getComment());
    }

    /**
     * 认领/签收任务
     *
     * @param taskBo 请求实体参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void claim(WfTaskBo taskBo) {
        Task task = taskService.createTaskQuery().taskId(taskBo.getTaskId()).singleResult();
        if (Objects.isNull(task)) {
            throw new ServiceException("任务不存在");
        }
        if (task.isSuspended()) {
            throw new RuntimeException("任务处于挂起状态");
        }
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(task.getProcessInstanceId())
                .active()
                .singleResult();
        if (ObjectUtil.isNull(processInstance)) {
            throw new RuntimeException("流程已结束或已挂起，无法执行此操作");
        }
        taskService.claim(taskBo.getTaskId(), TaskUtils.getUserId());
    }

    /**
     * 取消认领/签收任务
     *
     * @param bo 请求实体参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unClaim(WfTaskBo bo) {
        taskService.unclaim(bo.getTaskId());
    }

    /**
     * 委派任务
     *
     * @param bo 请求实体参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delegateTask(WfTaskBo bo) {
        // 当前任务 task
        Task task = taskService.createTaskQuery().taskId(bo.getTaskId()).singleResult();
        if (ObjectUtil.isEmpty(task)) {
            throw new ServiceException("获取任务失败！");
        }
        if (task.isSuspended()) {
            throw new RuntimeException("任务处于挂起状态");
        }
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(task.getProcessInstanceId())
                .active()
                .singleResult();
        if (ObjectUtil.isNull(processInstance)) {
            throw new RuntimeException("流程已结束或已挂起，无法执行此操作");
        }
       /* StringBuilder commentBuilder = new StringBuilder(LoginHelper.getNickName())
                .append("->");
        String nickName = sysUserService.selectNickNameById(Long.parseLong(bo.getUserId()));
        if (StringUtils.isNotBlank(nickName)) {
            commentBuilder.append(nickName);
        } else {
            commentBuilder.append(bo.getUserId());
        }
        if (StringUtils.isNotBlank(bo.getComment())) {
            commentBuilder.append(": ").append(bo.getComment());
        }*/
        // 添加审批意见
//        taskService.addComment(bo.getTaskId(), task.getProcessInstanceId(), FlowComment.DELEGATE.getType(), commentBuilder.toString());
        // 设置办理人为当前登录人
        taskService.setOwner(bo.getTaskId(), TaskUtils.getUserId());
        // 执行委派
        taskService.delegateTask(bo.getTaskId(), bo.getUserId());
        // 设置任务节点名称
        bo.setTaskName(task.getName());
        // 处理抄送用户
        if (!copyService.makeCopy(bo)) {
            throw new RuntimeException("抄送任务失败");
        }
    }


    /**
     * 转办任务
     *
     * @param bo 请求实体参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void transferTask(WfTaskBo bo) {
        // 当前任务 task
        Task task = taskService.createTaskQuery().taskId(bo.getTaskId()).singleResult();
        if (ObjectUtil.isEmpty(task)) {
            throw new ServiceException("获取任务失败！");
        }
        if (task.isSuspended()) {
            throw new RuntimeException("任务处于挂起状态");
        }
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(task.getProcessInstanceId())
                .active()
                .singleResult();
        if (ObjectUtil.isNull(processInstance)) {
            throw new RuntimeException("流程已结束或已挂起，无法执行此操作");
        }
        runtimeService.setVariable(processInstance.getId(), "actionType", "transfer");
        /*StringBuilder commentBuilder = new StringBuilder(LoginHelper.getNickName())
                .append("->");
        String nickName = sysUserService.selectNickNameById(Long.parseLong(bo.getUserId()));
        if (StringUtils.isNotBlank(nickName)) {
            commentBuilder.append(nickName);
        } else {
            commentBuilder.append(bo.getUserId());
        }
        if (StringUtils.isNotBlank(bo.getComment())) {
            commentBuilder.append(": ").append(bo.getComment());
        }
        // 添加审批意见
        taskService.addComment(bo.getTaskId(), task.getProcessInstanceId(), FlowComment.TRANSFER.getType(), commentBuilder.toString());*/
        // 设置拥有者为当前登录人
        taskService.setOwner(bo.getTaskId(), TaskUtils.getUserId());
        // 转办任务
        taskService.setAssignee(bo.getTaskId(), bo.getUserId());
        // 设置任务节点名称
        bo.setTaskName(task.getName());
        // 处理抄送用户
        if (!copyService.makeCopy(bo)) {
            throw new RuntimeException("抄送任务失败");
        }
    }

    /**
     * 取消申请
     *
     * @param bo
     * @return
     */
    @Override
    public void stopProcess(WfTaskBo bo) {
        List<Task> taskList = taskService.createTaskQuery().processInstanceId(bo.getProcInsId()).list();
        if (CollectionUtils.isEmpty(taskList)) {
            throw new RuntimeException("流程未启动或已执行完成，取消申请失败");
        }
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(bo.getProcInsId())
                .active()
                .singleResult();
        if (ObjectUtil.isNull(processInstance)) {
            throw new RuntimeException("流程已结束或已挂起，无法执行此操作");
        }
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processInstance.getProcessDefinitionId());
        if (Objects.nonNull(bpmnModel)) {
            Process process = bpmnModel.getMainProcess();
            List<EndEvent> endNodes = process.findFlowElementsOfType(EndEvent.class, false);
            if (CollectionUtils.isNotEmpty(endNodes)) {
                Authentication.setAuthenticatedUserId(TaskUtils.getUserId());
                runtimeService.setVariable(processInstance.getId(), ProcessConstants.PROCESS_STATUS_KEY, ProcessStatus.CANCELED.getStatus());
                for (Task task : taskList) {
                    if (task.isSuspended()) {
                        throw new RuntimeException("任务处于挂起状态");
                    }
                    taskService.addComment(task.getId(), processInstance.getProcessInstanceId(), FlowComment.STOP.getType(), "取消流程");
                    taskService.setAssignee(task.getId(), TaskUtils.getUserId());
                    HistoricProcessInstance historicProcIns = historyService.createHistoricProcessInstanceQuery()
                            .processInstanceId(task.getProcessInstanceId())
                            .includeProcessVariables()
                            .singleResult();
                    if (historicProcIns != null) {
                        List<FormConf> formConfs = processFormList(bpmnModel, historicProcIns);
                        Map<String, Object> formDataEnd = new HashMap<>();
                        for (FormConf formConf : formConfs) {
                            Map<String, Object> formData = formConf.getFormData();
                            formDataEnd.putAll(formData);
                        }
                    }
                }
                // 获取当前流程最后一个节点
                String endId = endNodes.get(0).getId();
                List<Execution> executions = runtimeService.createExecutionQuery()
                        .parentId(processInstance.getProcessInstanceId()).list();
                List<String> executionIds = new ArrayList<>();
                executions.forEach(execution -> executionIds.add(execution.getId()));
                // 变更流程为已结束状态
                runtimeService.createChangeActivityStateBuilder()
                        .moveExecutionsToSingleActivityId(executionIds, endId).changeState();
            }
        }
    }

    /**
     * 撤回流程
     *
     * @param taskBo 请求实体参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void revokeProcess(WfTaskBo taskBo) {
        String procInsId = taskBo.getProcInsId();
        String taskId = taskBo.getTaskId();
        // 校验流程是否结束
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(procInsId)
                .active()
                .singleResult();
        if (ObjectUtil.isNull(processInstance)) {
            throw new RuntimeException("流程已结束或已挂起，无法执行撤回操作");
        }
        // 获取待撤回任务实例
        HistoricTaskInstance currTaskIns = historyService.createHistoricTaskInstanceQuery()
                .taskId(taskId)
                .taskAssignee(TaskUtils.getUserId())
                .singleResult();
        if (ObjectUtil.isNull(currTaskIns)) {
            throw new RuntimeException("当前任务不存在，无法执行撤回操作");
        }
        // 获取 bpmn 模型
        BpmnModel bpmnModel = repositoryService.getBpmnModel(currTaskIns.getProcessDefinitionId());
        UserTask currUserTask = ModelUtils.getUserTaskByKey(bpmnModel, currTaskIns.getTaskDefinitionKey());
        // 查找下一级用户任务列表
        List<UserTask> nextUserTaskList = ModelUtils.findNextUserTasks(currUserTask);
        List<String> nextUserTaskKeys = nextUserTaskList.stream().map(UserTask::getId).collect(Collectors.toList());

        // 获取当前节点之后已完成的流程历史节点
        List<HistoricTaskInstance> finishedTaskInsList = historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(procInsId)
                .taskCreatedAfter(currTaskIns.getEndTime())
                .finished()
                .list();
        for (HistoricTaskInstance finishedTaskInstance : finishedTaskInsList) {
            // 检查已完成流程历史节点是否存在下一级中
            if (CollUtil.contains(nextUserTaskKeys, finishedTaskInstance.getTaskDefinitionKey())) {
                throw new RuntimeException("下一流程已处理，无法执行撤回操作");
            }
        }
        // 获取所有激活的任务节点，找到需要撤回的任务
        List<Task> activateTaskList = taskService.createTaskQuery().processInstanceId(procInsId).list();
        List<String> revokeExecutionIds = new ArrayList<>();
        for (Task task : activateTaskList) {
            // 检查激活的任务节点是否存在下一级中，如果存在，则加入到需要撤回的节点
            if (CollUtil.contains(nextUserTaskKeys, task.getTaskDefinitionKey())) {
                // 添加撤回审批信息
                taskService.setAssignee(task.getId(), TaskUtils.getUserId());
//                taskService.addComment(task.getId(), task.getProcessInstanceId(), FlowComment.REVOKE.getType(), LoginHelper.getNickName() + "撤回流程审批");
                revokeExecutionIds.add(task.getExecutionId());
            }
        }
        try {
            runtimeService.createChangeActivityStateBuilder()
                    .processInstanceId(procInsId)
                    .moveExecutionsToSingleActivityId(revokeExecutionIds, currTaskIns.getTaskDefinitionKey()).changeState();
        } catch (FlowableObjectNotFoundException e) {
            throw new RuntimeException("未找到流程实例，流程可能已发生变化");
        } catch (FlowableException e) {
            throw new RuntimeException("执行撤回操作失败");
        } catch (NoSuchElementException e) {
            throw new RuntimeException("流程可能已发生变化，不允许撤回");
        }
    }

    /**
     * 获取流程过程图
     *
     * @param processId
     * @return
     */
    @Override
    public InputStream diagram(String processId) {
        String processDefinitionId;
        // 获取当前的流程实例
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processId).singleResult();
        // 如果流程已经结束，则得到结束节点
        if (Objects.isNull(processInstance)) {
            HistoricProcessInstance pi = historyService.createHistoricProcessInstanceQuery().processInstanceId(processId).singleResult();

            processDefinitionId = pi.getProcessDefinitionId();
        } else {// 如果流程没有结束，则取当前活动节点
            // 根据流程实例ID获得当前处于活动状态的ActivityId合集
            ProcessInstance pi = runtimeService.createProcessInstanceQuery().processInstanceId(processId).singleResult();
            processDefinitionId = pi.getProcessDefinitionId();
        }

        // 获得活动的节点
        List<HistoricActivityInstance> highLightedFlowList = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(processId).orderByHistoricActivityInstanceStartTime().asc().list();

        List<String> highLightedFlows = new ArrayList<>();
        List<String> highLightedNodes = new ArrayList<>();
        //高亮线
        for (HistoricActivityInstance tempActivity : highLightedFlowList) {
            if ("sequenceFlow".equals(tempActivity.getActivityType())) {
                //高亮线
                highLightedFlows.add(tempActivity.getActivityId());
            } else {
                //高亮节点
                highLightedNodes.add(tempActivity.getActivityId());
            }
        }

        //获取流程图
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);
        ProcessEngineConfiguration configuration = processEngine.getProcessEngineConfiguration();
        //获取自定义图片生成器
        ProcessDiagramGenerator diagramGenerator = new CustomProcessDiagramGenerator();
        return diagramGenerator.generateDiagram(bpmnModel, "png", highLightedNodes, highLightedFlows, configuration.getActivityFontName(),
                configuration.getLabelFontName(), configuration.getAnnotationFontName(), configuration.getClassLoader(), 1.0, true);

    }

    /**
     * 获取流程变量
     *
     * @param taskId 任务ID
     * @return 流程变量
     */
    @Override
    public Map<String, Object> getProcessVariables(String taskId) {
        HistoricTaskInstance historicTaskInstance = historyService.createHistoricTaskInstanceQuery()
                .includeProcessVariables()
                .finished()
                .taskId(taskId)
                .singleResult();
        if (Objects.nonNull(historicTaskInstance)) {
            return historicTaskInstance.getProcessVariables();
        }
        return taskService.getVariables(taskId);
    }

    /**
     * 获取流程变量
     *
     * @param traceabilityCode
     * @return
     */
    @Override
    public Map<String, Object> getProcessVariablesByTraceabilityCode(String traceabilityCode) {
        // 1. 双重查询机制：先查运行中实例，再查历史实例
        ProcessInstance runtimeInstance = runtimeService.createProcessInstanceQuery()
                .variableValueEquals("traceabilityCode", traceabilityCode)
                .singleResult();
        HistoricProcessInstance historicInstance = historyService.createHistoricProcessInstanceQuery()
                .variableValueEquals("traceabilityCode", traceabilityCode)
                .singleResult();
        // 2. 确定流程实例ID和状态
        String processInstanceId;
        boolean isCompleted = false;

        if (runtimeInstance != null) {
            processInstanceId = runtimeInstance.getId();
        } else if (historicInstance != null) {
            processInstanceId = historicInstance.getId();
            isCompleted = true;
        } else {
            processInstanceId = null;
            throw new RuntimeException("未找到匹配的流程实例");
        }
        // 4. 获取表单数据（兼容运行中和已完成）
        Map<String, Object> formData;
        if (!isCompleted) {
            // 运行中流程获取实时变量
            formData = runtimeService.getVariables(processInstanceId);
        } else {
            formData = new HashMap<>();
            // 已完成流程获取历史变量
            List<HistoricVariableInstance> vars = historyService.createHistoricVariableInstanceQuery()
                    .processInstanceId(processInstanceId)
                    .list();
            vars.forEach(var -> formData.put(var.getVariableName(), var.getValue()));
        }
        // 5. 获取审批历史（含驳回信息）
        List<HistoricTaskInstance> allTasks = historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(processInstanceId)
                .orderByHistoricTaskInstanceStartTime().asc()
                .list();

        List<Map<String, Object>> approvalHistory = allTasks.stream().map(task -> {
            Map<String, Object> detail = new HashMap<>();
            detail.put("status", task.getDeleteReason() != null ? "REJECTED" : "APPROVED");

            // 获取审批意见
            List<Comment> processInstanceComments = taskService.getProcessInstanceComments(processInstanceId);
            List<String> comments = processInstanceComments.stream().filter(comment -> task.getId().equals(comment.getTaskId()))
                    .map(Comment::getFullMessage)
                    .collect(Collectors.toList());
            detail.put("comments", comments);
            return detail;
        }).collect(Collectors.toList());

        // 6. 构造返回结果
        Map<String, Object> result = new LinkedHashMap<>();
        boolean hasReject = approvalHistory.stream()
                .anyMatch(entry -> "REJECTED".equals(entry.get("status")));
        result.put("status", isCompleted ? (hasReject ? "REJECTED" : "COMPLETED") : "RUNNING");
        Set<String> keysToRemove = Set.of("oldData", "newData", "initiator", "ipAddress", "processStatus", "");
        formData.keySet().removeAll(keysToRemove);
        result.put("formData", formData);
        result.put("approvalHistory", approvalHistory);

        return result;
    }

    /**
     * 启动第一个任务
     *
     * @param processInstance 流程实例
     * @param variables       流程参数
     */
    @Override
    public void startFirstTask(ProcessInstance processInstance, Map<String, Object> variables) {
        // 若第一个用户任务为发起人，则自动完成任务
        List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstance.getProcessInstanceId()).list();
        if (CollUtil.isNotEmpty(tasks)) {
            String userIdStr = (String) variables.get(TaskConstants.PROCESS_INITIATOR);
            for (Task task : tasks) {
                if (StrUtil.equals(task.getAssignee(), userIdStr)) {
//                    taskService.addComment(task.getId(), processInstance.getProcessInstanceId(), FlowComment.NORMAL.getType(), LoginHelper.getNickName() + "发起流程申请");
                    taskService.complete(task.getId(), variables);
                }
            }
        }
    }

    /**
     * 指派下一任务审批人
     *
     * @param bpmnModel    bpmn模型
     * @param processInsId 流程实例id
     * @param userIds      用户ids
     */
    private void assignNextUsers(BpmnModel bpmnModel, String processInsId, String userIds) {
        // 获取所有节点信息
        List<Task> list = taskService.createTaskQuery()
                .processInstanceId(processInsId)
                .list();
        if (list.size() == 0) {
            return;
        }
        Queue<String> assignIds = CollUtil.newLinkedList(userIds.split(","));
        if (list.size() == assignIds.size()) {
            for (Task task : list) {
                taskService.setAssignee(task.getId(), assignIds.poll());
            }
            return;
        }
        // 优先处理非多实例任务
        Iterator<Task> iterator = list.iterator();
        while (iterator.hasNext()) {
            Task task = iterator.next();
            if (!ModelUtils.isMultiInstance(bpmnModel, task.getTaskDefinitionKey())) {
                if (!assignIds.isEmpty()) {
                    taskService.setAssignee(task.getId(), assignIds.poll());
                }
                iterator.remove();
            }
        }
        // 若存在多实例任务，则进行动态加减签
        if (CollUtil.isNotEmpty(list)) {
            if (assignIds.isEmpty()) {
                // 动态减签
                for (Task task : list) {
                    runtimeService.deleteMultiInstanceExecution(task.getExecutionId(), true);
                }
            } else {
                // 动态加签
                for (String assignId : assignIds) {
                    Map<String, Object> assignVariables = Collections.singletonMap(BpmnXMLConstants.ATTRIBUTE_TASK_USER_ASSIGNEE, assignId);
                    runtimeService.addMultiInstanceExecution(list.get(0).getTaskDefinitionKey(), list.get(0).getProcessInstanceId(), assignVariables);
                }
            }
        }
    }

    @Override
    public int designatedPerson(String processInstanceId, String taskDefKey, Set<Long> users) {
        if (CollectionUtils.isEmpty(users)) {
            return 0;
        }
        List<Task> tasks = taskService.createTaskQuery()
                .processInstanceId(processInstanceId)
                .taskDefinitionKey(taskDefKey)
                .taskUnassigned()
                .list();
        for (Task task : tasks) {
            String id = task.getId();
            for (Long user : users) {
                taskService.addCandidateUser(id, user + "");
            }
        }
        List<String> userPhoneList = new ArrayList<>();
        List<String> userNameList = new ArrayList<>();
        /*List<SysUser> sysUsers = userMapper.selectUserByIds(new ArrayList<>(users));
        String deptId = "";
        for (int i = 0; i < sysUsers.size(); i++) {
            SysUser sysUser = sysUsers.get(i);
            if (i == 0) {
                deptId = sysUser.getDeptId() + "";
            }
            userPhoneList.add(sysUser.getPhonenumber());
            userNameList.add(sysUser.getNickName());
        }
        String dingTalkUserIds = DingTalkUtil.getDingtalkUidByUserList(userPhoneList);*/
        /*if (StringUtils.isNotBlank(dingTalkUserIds)) {
            String taskId = "", errCode = "", errMsg = "";
            try {
                String resultMsg = DingTalkMessageUtil.sendOaMessage(dingTalkUserIds, "流程待办提醒", "您有待办流程需要进行处理", "5");
                com.alibaba.fastjson2.JSONObject jsonObject = com.alibaba.fastjson2.JSONObject.parseObject(resultMsg);
                taskId = jsonObject.getString("task_id");
                errCode = jsonObject.getString("errcode");
                errMsg = jsonObject.getString("errmsg");
            } catch (Exception e) {
                System.err.println("流程待办消息推送++失败+++++userDingCode:" + dingTalkUserIds);
                return 0;
            }
        }*/
        return 1;
    }
}
