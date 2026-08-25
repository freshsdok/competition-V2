package com.teaching.competition.controller;

import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.web.page.TableDataInfo;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.competition.domain.CompetitionSceneResource;
import com.teaching.competition.domain.CompetitionSceneResourceQuery;
import com.teaching.competition.domain.CompetitionSceneResourceStatusReq;
import com.teaching.competition.domain.CompetitionSceneResourceVO;
import com.teaching.competition.service.ICompetitionSceneResourceService;
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
 * 大赛现场设备资源台账Controller。
 */
@RestController
@RequestMapping({"/sceneResource", "/competition/sceneResource"})
public class CompetitionSceneResourceController extends BaseController {

    @Autowired
    private ICompetitionSceneResourceService competitionSceneResourceService;

    @RequiresPermissions("competition:sceneResource:list")
    @GetMapping("/list")
    public TableDataInfo list(CompetitionSceneResourceQuery query) {
        startPage();
        List<CompetitionSceneResourceVO> list = competitionSceneResourceService.selectCompetitionSceneResourceList(query);
        return getDataTable(list);
    }

    @RequiresPermissions("competition:sceneResource:query")
    @GetMapping("/{resourceId}")
    public AjaxResult getInfo(@PathVariable("resourceId") Long resourceId) {
        return success(competitionSceneResourceService.selectCompetitionSceneResourceById(resourceId));
    }

    @RequiresPermissions("competition:sceneResource:add")
    @Log(title = "赛事现场设备资源", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CompetitionSceneResource resource) {
        return toAjax(competitionSceneResourceService.insertCompetitionSceneResource(resource));
    }

    @RequiresPermissions("competition:sceneResource:edit")
    @Log(title = "赛事现场设备资源", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CompetitionSceneResource resource) {
        return toAjax(competitionSceneResourceService.updateCompetitionSceneResource(resource));
    }

    @RequiresPermissions("competition:sceneResource:remove")
    @Log(title = "赛事现场设备资源", businessType = BusinessType.DELETE)
    @DeleteMapping("/{resourceIds}")
    public AjaxResult remove(@PathVariable Long[] resourceIds) {
        return toAjax(competitionSceneResourceService.deleteCompetitionSceneResourceByIds(resourceIds));
    }

    @RequiresPermissions("competition:sceneResource:changeStatus")
    @Log(title = "赛事现场设备资源状态", businessType = BusinessType.UPDATE)
    @PostMapping("/changeStatus")
    public AjaxResult changeStatus(@RequestBody CompetitionSceneResourceStatusReq req) {
        return toAjax(competitionSceneResourceService.changeCompetitionSceneResourceStatus(req));
    }
}
