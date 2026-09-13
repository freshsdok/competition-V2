package com.teaching.system.controller;

import com.teaching.common.core.exception.ServiceException;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.system.integration.embedded.V2InternalEmbeddedBridgeClient;
import com.teaching.system.service.TeamCollectionActor;
import com.teaching.system.service.TeamCollectionService;
import com.teaching.common.security.annotation.RequiresLogin;
import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

/** 浏览器不能提交V1 userId或任意V2 URL；两种入口都从V1 SecurityContext和固定目标生成。 */
@RestController
@RequestMapping("/v2EmbeddedBridge")
public class V2InternalEmbeddedBridgeController {
    private final ObjectProvider<V2InternalEmbeddedBridgeClient> clients;
    private final TeamCollectionService collections;
    public V2InternalEmbeddedBridgeController(ObjectProvider<V2InternalEmbeddedBridgeClient> clients, TeamCollectionService collections) {
        this.clients = clients;
        this.collections = collections;
    }

    @GetMapping("/discovery")
    public AjaxResult discovery() {
        var client = clients.getIfAvailable();
        return AjaxResult.success(client == null ? List.of() : client.discover());
    }

    @PostMapping("/settlement")
    @RequiresLogin
    public AjaxResult settlement(@RequestBody SettlementEntry request) {
        return AjaxResult.success(collections.entry(request.teamId(), TeamCollectionActor.currentUserId(), request.version()));
    }

    public record SettlementEntry(long teamId, long version) {}

    @PostMapping("/credential/{offeringId}")
    public AjaxResult credential(@PathVariable long offeringId) {
        return AjaxResult.success(client().credential(currentUser(), offeringId));
    }

    private long currentUser() {
        Long id = SecurityUtils.getUserId();
        if (id == null || id <= 0) throw new ServiceException("请先登录原平台", 401);
        return id;
    }

    private V2InternalEmbeddedBridgeClient client() {
        var client = clients.getIfAvailable();
        if (client == null) throw new ServiceException("新版安全办理入口暂未开放", 503);
        return client;
    }
}
