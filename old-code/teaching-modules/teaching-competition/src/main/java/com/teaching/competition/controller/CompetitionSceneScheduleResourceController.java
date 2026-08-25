package com.teaching.competition.controller;

import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.web.page.TableDataInfo;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.competition.domain.CompetitionSceneScheduleResource;
import com.teaching.competition.domain.CompetitionSceneScheduleResourceQuery;
import com.teaching.competition.domain.CompetitionSceneScheduleResourceStatusReq;
import com.teaching.competition.domain.CompetitionSceneScheduleResourceVO;
import com.teaching.competition.service.ICompetitionSceneScheduleResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 大赛现场赛场资源布置Controller。
 */
@RestController
@RequestMapping({"/sceneScheduleResource", "/competition/sceneScheduleResource"})
public class CompetitionSceneScheduleResourceController extends BaseController {

    @Autowired
    private ICompetitionSceneScheduleResourceService competitionSceneScheduleResourceService;

    @RequiresPermissions("competition:sceneScheduleResource:list")
    @GetMapping("/list")
    public TableDataInfo list(CompetitionSceneScheduleResourceQuery query) {
        startPage();
        List<CompetitionSceneScheduleResourceVO> list =
                competitionSceneScheduleResourceService.selectCompetitionSceneScheduleResourceList(query);
        return getDataTable(list);
    }

    @RequiresPermissions("competition:sceneScheduleResource:query")
    @GetMapping("/{scheduleResourceId}")
    public AjaxResult getInfo(@PathVariable("scheduleResourceId") Long scheduleResourceId) {
        return success(competitionSceneScheduleResourceService.selectCompetitionSceneScheduleResourceById(scheduleResourceId));
    }

    @RequiresPermissions("competition:sceneScheduleResource:add")
    @Log(title = "赛事现场赛场资源布置", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CompetitionSceneScheduleResource scheduleResource) {
        return toAjax(competitionSceneScheduleResourceService.insertCompetitionSceneScheduleResource(scheduleResource));
    }

    @RequiresPermissions("competition:sceneScheduleResource:edit")
    @Log(title = "赛事现场赛场资源布置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CompetitionSceneScheduleResource scheduleResource) {
        return toAjax(competitionSceneScheduleResourceService.updateCompetitionSceneScheduleResource(scheduleResource));
    }

    @RequiresPermissions("competition:sceneScheduleResource:remove")
    @Log(title = "赛事现场赛场资源布置", businessType = BusinessType.DELETE)
    @DeleteMapping("/{scheduleResourceIds}")
    public AjaxResult remove(@PathVariable Long[] scheduleResourceIds) {
        return toAjax(competitionSceneScheduleResourceService.deleteCompetitionSceneScheduleResourceByIds(scheduleResourceIds));
    }

    @RequiresPermissions("competition:sceneScheduleResource:changeBookingStatus")
    @Log(title = "赛事现场赛场资源布置预约状态", businessType = BusinessType.UPDATE)
    @PostMapping("/changeBookingStatus")
    public AjaxResult changeBookingStatus(@RequestBody CompetitionSceneScheduleResourceStatusReq req) {
        return toAjax(competitionSceneScheduleResourceService.changeCompetitionSceneScheduleResourceBookingStatus(req));
    }
}
