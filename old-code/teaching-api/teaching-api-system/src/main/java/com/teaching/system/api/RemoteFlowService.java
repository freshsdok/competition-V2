package com.teaching.system.api;

import com.teaching.common.core.constant.SecurityConstants;
import com.teaching.common.core.constant.ServiceNameConstants;
import com.teaching.common.core.domain.R;
import com.teaching.system.api.domain.OperationFlow;
import com.teaching.system.api.factory.RemoteFlowFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 流程服务
 *
 * @author teaching
 */
@FeignClient(contextId = "remoteFlowService", value = ServiceNameConstants.Flow_SERVICE, fallbackFactory = RemoteFlowFallbackFactory.class)
public interface RemoteFlowService {

    /**
     * 启动流程 根据流程分类
     *
     * @param variables 流程变量
     * @param category  流程分类
     * @param teamCode  团队编码
     * @param source
     * @return
     */
    @PostMapping("/process/startByCategory/{category}/{teamCode}")
    public R<Void> startByCategory(@RequestBody Map<String, Object> variables, @PathVariable String category, @PathVariable String teamCode, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);


    /**
     * 根据teamCode和flowType获取流程信息
     * 返回此团队所有类型的流程的信息
     * @param teamCode
     * @param source
     * @return
     */
    @GetMapping("/flow/getFlowVariables/{teamCode}")
    public R<Map<String, Object>> getFlowVariables(@PathVariable String teamCode, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * 根据teamCode获取是否有进行中的流程
     * @param teamCode
     * @param source
     * @return true 有，false 没有
     */
    @GetMapping("/flow/getRunning/{teamCode}")
    public R<Boolean> getRunning(@PathVariable String teamCode, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    //
    @GetMapping("/flow/getTeamCodeOperatorType/{teamCode}")
    public R<OperationFlow> getInnerInfo(@PathVariable String teamCode, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);
}
