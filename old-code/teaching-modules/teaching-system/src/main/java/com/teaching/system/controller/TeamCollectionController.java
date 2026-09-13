package com.teaching.system.controller;

import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.security.annotation.RequiresLogin;
import com.teaching.system.service.TeamCollectionActor;
import com.teaching.system.service.TeamCollectionService;
import org.springframework.web.bind.annotation.*;

/** 浏览器只提交办理记录ID及版本，身份始终来自已验证的登录对象。 */
@RestController
@RequiresLogin
@RequestMapping("/teamCollection")
public class TeamCollectionController {
    private final TeamCollectionService service;
    public TeamCollectionController(TeamCollectionService service) { this.service = service; }

    @GetMapping("/mine")
    public AjaxResult mine() { return AjaxResult.success(service.mine(TeamCollectionActor.currentUserId())); }

    @PostMapping("/{id}/start")
    public AjaxResult start(@PathVariable long id, @RequestBody Version request) {
        return AjaxResult.success(service.start(id, TeamCollectionActor.currentUserId(), request.version()));
    }

    @PostMapping("/{id}/confirm")
    public AjaxResult confirm(@PathVariable long id, @RequestBody Version request) {
        return AjaxResult.success(service.confirm(id, TeamCollectionActor.currentUserId(), request.version()));
    }

    @PostMapping("/{id}/release")
    public AjaxResult release(@PathVariable long id, @RequestBody Version request) {
        return AjaxResult.success(service.release(id, TeamCollectionActor.currentUserId(), request.version()));
    }

    public record Version(long version) {}
}
