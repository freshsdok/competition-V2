package com.teaching.system.api.factory;

import com.teaching.common.core.domain.R;
import com.teaching.system.api.RemoteFlowService;
import com.teaching.system.api.domain.OperationFlow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 用户服务降级处理
 *
 * @author teaching
 */
@Component
public class RemoteFlowFallbackFactory implements FallbackFactory<RemoteFlowService> {
    private static final Logger log = LoggerFactory.getLogger(RemoteFlowFallbackFactory.class);

    @Override
    public RemoteFlowService create(Throwable throwable) {
        log.error("流程服务调用失败:{}", throwable.getMessage());
        return new RemoteFlowService() {

            @Override
            public R<Void> startByCategory(Map<String, Object> variables, String category, String teamCode, String source) {
                return R.fail("流程启动失败:" + throwable.getMessage());
            }

            @Override
            public R<Map<String, Object>> getFlowVariables(String teamCode, String source) {
                return R.fail("获取团队的流程信息失败:" + throwable.getMessage());
            }

            @Override
            public R<Boolean> getRunning(String teamCode, String source) {
                return R.fail("获取团队进行中的流程信息失败:" + throwable.getMessage());
            }

            @Override
            public R<OperationFlow> getInnerInfo(String teamCode, String source) {
                return R.fail("获取团队操作最新得类型:" + throwable.getMessage());
            }
        };
    }
}
