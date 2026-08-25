package com.teaching.flowable.listener;

import cn.hutool.core.map.MapUtil;
import com.teaching.common.core.constant.SecurityConstants;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.core.utils.SpringUtils;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.system.api.CompetitionService;
import com.teaching.system.api.OrderService;
import com.teaching.system.api.domain.ChangeLog;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

/**
 * 需要在流程中配置任务监听才能被调用
 * 退费订单修改状态监听器
 *
 * @author Administrator
 */
@Slf4j
@Component(value = "ChangeRefundOrderStatusListener")
public class ChangeRefundOrderStatusListener implements TaskListener {


    @Override
    public void notify(DelegateTask delegateTask) {
        System.out.println("进入状态修改任务监听器...");
        // 获取任务的变量
        OrderService orderService = SpringUtils.getBean(OrderService.class);
        Map<String, Object> variables = delegateTask.getVariables();
        //退费id
        String refundOrderId = MapUtil.getStr(variables, "refundOrderId");
        //操作类型（change人员变更，repayment退费重缴费,retired退赛）
        String changeType = MapUtil.getStr(variables, "operationType");
        if (StringUtils.isEmpty(refundOrderId)) {
            log.error("ChangeRefundOrderStatusListener refundOrderId为空, taskId={}, taskName={}, processInstanceId={}, executionId={}, variables={}",
                delegateTask.getId(), delegateTask.getName(), delegateTask.getProcessInstanceId(), delegateTask.getExecutionId(), variables);
            return;
        }
        try {
            orderService.updateRefundStatus(Long.parseLong(refundOrderId), null, changeType, SecurityConstants.INNER);
        }
        catch (NumberFormatException e) {
            log.error("ChangeRefundOrderStatusListener refundOrderId非数字, refundOrderId={}, taskId={}, taskName={}, processInstanceId={}, executionId={}, variables={} ",
                refundOrderId, delegateTask.getId(), delegateTask.getName(), delegateTask.getProcessInstanceId(), delegateTask.getExecutionId(), variables, e);
        }
    }
}
