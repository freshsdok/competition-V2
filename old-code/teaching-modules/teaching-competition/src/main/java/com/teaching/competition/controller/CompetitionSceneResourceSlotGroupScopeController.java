package com.teaching.competition.controller;

import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.competition.domain.CompetitionSceneResourceSlotGroupScope;
import com.teaching.competition.domain.CompetitionSceneResourceSlotGroupScopeReplaceReq;
import com.teaching.competition.mapper.CompetitionSceneScheduleTargetMapper;
import com.teaching.competition.service.ICompetitionSceneResourceSlotGroupScopeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 大赛现场资源预约时段允许组别Controller。
 */
@RestController
@RequestMapping({"/sceneResourceSlotGroupScope", "/competition/sceneResourceSlotGroupScope"})
public class CompetitionSceneResourceSlotGroupScopeController extends BaseController {

    @Autowired
    private ICompetitionSceneResourceSlotGroupScopeService slotGroupScopeService;

    @Autowired
    private CompetitionSceneScheduleTargetMapper targetMapper;

    @RequiresPermissions("competition:sceneResourceSlot:list")
    @GetMapping("/listBySlot")
    public AjaxResult listBySlot(Long slotId) {
        return success(slotGroupScopeService.listBySlotId(slotId));
    }

    @RequiresPermissions("competition:sceneResourceSlot:list")
    @GetMapping("/groupOptions")
    public AjaxResult groupOptions(Long scheduleId, Long competitionSeriesId) {
        return success(targetMapper.selectDistinctGroupOptions(scheduleId, competitionSeriesId));
    }

    @RequiresPermissions("competition:sceneResourceSlot:edit")
    @Log(title = "赛事现场资源预约时段允许组别", businessType = BusinessType.UPDATE)
    @PostMapping("/replace")
    public AjaxResult replace(@RequestBody CompetitionSceneResourceSlotGroupScopeReplaceReq req) {
        return success(slotGroupScopeService.replaceSlotGroups(req.getSlotId(),
                req.getScheduleResourceId(), req.getGroups()));
    }

    @RequiresPermissions("competition:sceneResourceSlot:edit")
    @Log(title = "赛事现场资源预约时段允许组别批量替换", businessType = BusinessType.UPDATE)
    @PostMapping("/batchReplace")
    public AjaxResult batchReplace(@RequestBody List<CompetitionSceneResourceSlotGroupScope> groups) {
        return success(slotGroupScopeService.batchReplaceSlotGroups(groups));
    }
}
