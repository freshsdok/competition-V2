package com.teaching.flowable.config;

import com.teaching.flowable.listener.GlobalEventListener;
import com.teaching.flowable.listener.GlobalProcessStartedListener;
import lombok.AllArgsConstructor;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEventType;
import org.flowable.engine.RuntimeService;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * flowable全局监听配置
 *
 * @author ssc
 */
@Configuration
@AllArgsConstructor
public class GlobalEventListenerConfig implements ApplicationListener<ContextRefreshedEvent> {

    private final GlobalEventListener globalEventListener;
    private final GlobalProcessStartedListener globalProcessStartedListener;
//    private final NodeMessagePushListener nodeMessagePushListener;
    private final RuntimeService runtimeService;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // 流程正常结束
        runtimeService.addEventListener(globalEventListener, FlowableEngineEventType.PROCESS_COMPLETED);
        //流程开始
        runtimeService.addEventListener(globalProcessStartedListener, FlowableEngineEventType.PROCESS_STARTED);
        //消息推送监听
//        runtimeService.addEventListener(nodeMessagePushListener, FlowableEngineEventType.TASK_CREATED, FlowableEngineEventType.TASK_ASSIGNED, FlowableEngineEventType.PROCESS_COMPLETED);
    }
}
