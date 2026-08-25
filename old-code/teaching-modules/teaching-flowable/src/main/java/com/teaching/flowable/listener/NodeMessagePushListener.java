package com.teaching.flowable.listener;

import cn.hutool.core.map.MapUtil;
import com.alibaba.fastjson2.JSONObject;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.core.utils.SpringUtils;
import com.teaching.flowable.common.constant.ProcessConstants;
import com.teaching.flowable.common.constant.TaskConstants;
import com.teaching.flowable.common.enums.ProcessStatus;
import com.teaching.system.api.domain.SysUser;
import org.apache.commons.collections4.CollectionUtils;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEntityEvent;
import org.flowable.engine.ProcessEngines;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.delegate.event.AbstractFlowableEngineEventListener;
import org.flowable.engine.impl.persistence.entity.ExecutionEntityImpl;
import org.flowable.identitylink.api.IdentityLink;
import org.flowable.task.service.impl.persistence.entity.TaskEntity;
import org.springframework.context.annotation.Configuration;

import java.util.*;

/**
 * 消息提醒
 *
 * @author Administrator
 */
@Configuration
public class NodeMessagePushListener extends AbstractFlowableEngineEventListener {


}
