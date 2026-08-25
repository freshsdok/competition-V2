package com.teaching.competition.controller;

import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.web.page.TableDataInfo;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.competition.domain.CompetitionSceneResourceSlot;
import com.teaching.competition.domain.CompetitionSceneResourceSlotBatchReq;
import com.teaching.competition.domain.CompetitionSceneResourceSlotQuery;
import com.teaching.competition.domain.CompetitionSceneResourceSlotStatusReq;
import com.teaching.competition.domain.CompetitionSceneResourceSlotVO;
import com.teaching.competition.service.ICompetitionSceneResourceSlotService;
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
 * 大赛现场设备资源预约时段Controller。
 */
@RestController
@RequestMapping({"/sceneResourceSlot", "/competition/sceneResourceSlot"})
public class CompetitionSceneResourceSlotController extends BaseController {

    @Autowired
    private ICompetitionSceneResourceSlotService competitionSceneResourceSlotService;

    @RequiresPermissions("competition:sceneResourceSlot:list")
    @GetMapping("/list")
    public TableDataInfo list(CompetitionSceneResourceSlotQuery query) {
        startPage();
        List<CompetitionSceneResourceSlotVO> list =
                competitionSceneResourceSlotService.selectCompetitionSceneResourceSlotList(query);
        return getDataTable(list);
    }

    @RequiresPermissions("competition:sceneResourceSlot:query")
    @GetMapping("/{slotId}")
    public AjaxResult getInfo(@PathVariable("slotId") Long slotId) {
        return success(competitionSceneResourceSlotService.selectCompetitionSceneResourceSlotById(slotId));
    }

    @RequiresPermissions("competition:sceneResourceSlot:add")
    @Log(title = "赛事现场资源预约时段", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CompetitionSceneResourceSlot slot) {
        return toAjax(competitionSceneResourceSlotService.insertCompetitionSceneResourceSlot(slot));
    }

    @RequiresPermissions("competition:sceneResourceSlot:add")
    @Log(title = "赛事现场资源预约时段批量生成", businessType = BusinessType.INSERT)
    @PostMapping("/batch")
    public AjaxResult batch(@RequestBody CompetitionSceneResourceSlotBatchReq req) {
        return success(competitionSceneResourceSlotService.batchGenerateCompetitionSceneResourceSlot(req));
    }

    @RequiresPermissions("competition:sceneResourceSlot:edit")
    @Log(title = "赛事现场资源预约时段", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CompetitionSceneResourceSlot slot) {
        return toAjax(competitionSceneResourceSlotService.updateCompetitionSceneResourceSlot(slot));
    }

    @RequiresPermissions("competition:sceneResourceSlot:remove")
    @Log(title = "赛事现场资源预约时段", businessType = BusinessType.DELETE)
    @DeleteMapping("/{slotIds}")
    public AjaxResult remove(@PathVariable Long[] slotIds) {
        return toAjax(competitionSceneResourceSlotService.deleteCompetitionSceneResourceSlotByIds(slotIds));
    }

    @RequiresPermissions("competition:sceneResourceSlot:changeStatus")
    @Log(title = "赛事现场资源预约时段状态", businessType = BusinessType.UPDATE)
    @PostMapping("/changeStatus")
    public AjaxResult changeStatus(@RequestBody CompetitionSceneResourceSlotStatusReq req) {
        return toAjax(competitionSceneResourceSlotService.changeCompetitionSceneResourceSlotStatus(req));
    }
}
