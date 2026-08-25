package com.teaching.competition.controller;

import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.competition.domain.CompetitionSceneResourceScheduleScope;
import com.teaching.competition.domain.CompetitionSceneResourceScheduleScopeBatchReq;
import com.teaching.competition.service.ICompetitionSceneResourceScheduleScopeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 大赛现场资源允许预约赛场范围Controller。
 */
@RestController
@RequestMapping({"/sceneResourceScheduleScope", "/competition/sceneResourceScheduleScope"})
public class CompetitionSceneResourceScheduleScopeController extends BaseController {

    @Autowired
    private ICompetitionSceneResourceScheduleScopeService scheduleScopeService;

    @RequiresPermissions("competition:sceneScheduleResource:list")
    @GetMapping("/list")
    public AjaxResult list(Long scheduleResourceId) {
        return success(scheduleScopeService.listByScheduleResourceId(scheduleResourceId));
    }

    @RequiresPermissions("competition:sceneScheduleResource:edit")
    @Log(title = "赛事现场资源允许预约赛场范围", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    public AjaxResult add(@RequestBody CompetitionSceneResourceScheduleScope scope) {
        return success(scheduleScopeService.addManualBindSchedule(scope.getScheduleResourceId(),
                scope.getResourceId(), scope.getAllowedScheduleId()));
    }

    @RequiresPermissions("competition:sceneScheduleResource:edit")
    @Log(title = "赛事现场资源允许预约赛场范围", businessType = BusinessType.UPDATE)
    @PostMapping("/ensure")
    public AjaxResult ensure(@RequestBody CompetitionSceneResourceScheduleScope scope) {
        return success(scheduleScopeService.ensureManualBindSchedule(scope.getScheduleResourceId(),
                scope.getResourceId(), scope.getAllowedScheduleId()));
    }

    @RequiresPermissions("competition:sceneScheduleResource:edit")
    @Log(title = "赛事现场资源允许预约赛场范围", businessType = BusinessType.INSERT)
    @PostMapping("/batchEnsure")
    public AjaxResult batchEnsure(@RequestBody CompetitionSceneResourceScheduleScopeBatchReq req) {
        return success(scheduleScopeService.batchEnsureManualBindSchedules(req.getScheduleResourceId(),
                req.getResourceId(), req.getAllowedScheduleIds()));
    }

    @RequiresPermissions("competition:sceneScheduleResource:edit")
    @Log(title = "赛事现场资源允许预约赛场范围", businessType = BusinessType.DELETE)
    @PostMapping("/remove")
    public AjaxResult remove(@RequestBody CompetitionSceneResourceScheduleScope scope) {
        return toAjax(scheduleScopeService.removeManualBindSchedule(scope.getScheduleResourceId(),
                scope.getAllowedScheduleId()));
    }
}
