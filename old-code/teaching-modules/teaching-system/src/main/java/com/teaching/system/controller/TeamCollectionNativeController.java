package com.teaching.system.controller;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.security.annotation.RequiresLogin;
import com.teaching.system.service.TeamCollectionActor;
import com.teaching.system.service.TeamCollectionNativeService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiresLogin
@RequestMapping("/teamCollectionNative")
public class TeamCollectionNativeController {
    private final TeamCollectionNativeService service;
    public TeamCollectionNativeController(TeamCollectionNativeService service) { this.service=service; }
    @GetMapping("/mine") public AjaxResult mine() { return AjaxResult.success(service.mine(TeamCollectionActor.currentUserId())); }
    @PostMapping("/{id}/entry") public AjaxResult entry(@PathVariable long id,@RequestBody Version request) {
        return AjaxResult.success(service.entry(id,TeamCollectionActor.currentUserId(),request.version()));
    }
    public record Version(long version) {}
}
