package com.teaching.system.controller;

import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.exception.ServiceException;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.system.integration.credential.V2CredentialBridgeClient;
import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.bind.annotation.*;

/** 后端从V1受信上下文取userId；浏览器不能提交或替换来源账号。 */
@RestController
@RequestMapping("/v2CredentialBridge")
public class V2CredentialBridgeController {
    private final ObjectProvider<V2CredentialBridgeClient> clients;
    public V2CredentialBridgeController(ObjectProvider<V2CredentialBridgeClient> clients){this.clients=clients;}
    @GetMapping("/discovery") public AjaxResult discovery(){
        var client=clients.getIfAvailable();return AjaxResult.success(client==null?List.of():client.discover());
    }
    @PostMapping("/entry") public AjaxResult entry(){
        Long id=SecurityUtils.getUserId();
        if(id==null || id<=0) throw new ServiceException("请先登录原平台",401);
        var client=clients.getIfAvailable();
        if(client==null) throw new ServiceException("赛证互通暂未开放",503);
        try {return AjaxResult.success(client.issue(id));}
        catch(org.springframework.web.client.RestClientException | IllegalStateException failure){throw new ServiceException("办理入口暂时不可用，请稍后重试",503);}
    }
}
