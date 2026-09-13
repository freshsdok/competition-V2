package com.teaching.system.controller;

import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.exception.ServiceException;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.system.integration.payout.V2PayoutTransitionClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 临时入口只接受V1现有登录态，并把当前用户ID作为来源谱系交给V2解析。
 * 是否存在可领取款项由V2已发布PayoutItem决定，本Controller无资格推导或返回金额。
 */
@RestController
@RequestMapping("/v2PayoutTransition")
public class V2PayoutTransitionController {
    private final ObjectProvider<V2PayoutTransitionClient> clientProvider;

    public V2PayoutTransitionController(ObjectProvider<V2PayoutTransitionClient> clientProvider) {
        this.clientProvider = clientProvider;
    }

    @PostMapping("/entry")
    public AjaxResult entry() {
        Long userId = SecurityUtils.getUserId();
        // V1的SecurityContextHolder在缺少网关上下文时返回0而不是抛错；过渡入口必须在这里
        // 再次fail closed，避免把“0”伪装成可解析的V1来源人员并进入V2 Subject解析链。
        if (userId == null || userId <= 0) {
            throw new ServiceException("登录状态无效", 401);
        }
        // 桥接未启用时仍保留路由，返回业务提示，避免落入静态资源处理。
        V2PayoutTransitionClient client = clientProvider.getIfAvailable();
        if (client == null) {
            throw new ServiceException("付款资料办理暂未开放，请稍后重试", 503);
        }
        String sourceUserKey = String.valueOf(userId);
        return AjaxResult.success(client.issue(sourceUserKey));
    }
}
