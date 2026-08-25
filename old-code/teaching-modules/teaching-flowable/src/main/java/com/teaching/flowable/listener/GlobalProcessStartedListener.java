package com.teaching.flowable.listener;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.RandomUtil;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.core.utils.SpringUtils;
import com.teaching.common.core.utils.ip.IpUtils;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.flowable.service.IOperationFlowService;
import com.teaching.system.api.domain.OperationFlow;
import org.flowable.engine.delegate.event.AbstractFlowableEngineEventListener;
import org.flowable.engine.delegate.event.FlowableProcessStartedEvent;
import org.flowable.engine.delegate.event.impl.FlowableEntityEventImpl;
import org.flowable.engine.impl.persistence.entity.ExecutionEntityImpl;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 流程启动监听
 *
 * @author Administrator
 */
@Component
public class GlobalProcessStartedListener extends AbstractFlowableEngineEventListener {


    /**
     * 流程启动时调用，只调用一次
     *
     * @param event
     */
    @Override
    protected void processStarted(FlowableProcessStartedEvent event) {
        System.out.println("流程启动监听器执行,流程启动后给流程中的字段赋值");
        FlowableEntityEventImpl flowableEntityEvent = (FlowableEntityEventImpl) event;
        ExecutionEntityImpl processInstance = (ExecutionEntityImpl) flowableEntityEvent.getEntity();
        Map<String, Object> variables = processInstance.getVariables();
//        String nickName = SecurityUtils.getLoginUser().getSysUser().getNickName();
        //追溯码
        String code = DateUtils.dateTimeNow() + RandomUtil.randomNumbers(5);
        variables.put("traceabilityCode", code);
        variables.put("ipAddress", IpUtils.getIpAddr());
        /*variables.put("nickName", nickName);
        variables.put("userId", SecurityUtils.getUserId());*/
        processInstance.setVariables(variables);
        IOperationFlowService operationFlowService = SpringUtils.getBean(IOperationFlowService.class);
        operationFlowService.insertOperationFlow(new OperationFlow(MapUtil.getStr(variables, "teamCode"), code, MapUtil.getStr(variables, "operationType"), "running"));
    }
}
