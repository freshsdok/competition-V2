package com.teaching.competition.controller;

import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.web.page.TableDataInfo;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.competition.domain.CompetitionSceneCheckinOverviewQuery;
import com.teaching.competition.domain.CompetitionSceneCheckinPersonVO;
import com.teaching.competition.domain.CompetitionSceneCheckinScheduleCardVO;
import com.teaching.competition.service.ICompetitionSceneCheckinOverviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 赛事现场签到概览Controller。
 */
@RestController
@RequestMapping({"/sceneCheckinOverview", "/scene/checkin-overview", "/competition/scene/checkin-overview"})
public class CompetitionSceneCheckinOverviewController extends BaseController {

    @Autowired
    private ICompetitionSceneCheckinOverviewService checkinOverviewService;

    @RequiresPermissions("competition:checkinOverview:list")
    @GetMapping("/statistics")
    public AjaxResult statistics(CompetitionSceneCheckinOverviewQuery query) {
        return success(checkinOverviewService.selectStatistics(query));
    }

    @RequiresPermissions("competition:checkinOverview:list")
    @GetMapping("/schedules")
    public TableDataInfo schedules(CompetitionSceneCheckinOverviewQuery query) {
        startPage();
        List<CompetitionSceneCheckinScheduleCardVO> list = checkinOverviewService.selectScheduleCards(query);
        return getDataTable(list);
    }

    @RequiresPermissions("competition:checkinOverview:list")
    @GetMapping("/schedules/{scheduleId}")
    public AjaxResult scheduleDetail(@PathVariable("scheduleId") Long scheduleId) {
        return success(checkinOverviewService.selectScheduleDetail(scheduleId));
    }

    @RequiresPermissions("competition:checkinOverview:list")
    @GetMapping("/schedules/{scheduleId}/persons")
    public TableDataInfo persons(@PathVariable("scheduleId") Long scheduleId, CompetitionSceneCheckinOverviewQuery query) {
        query.setScheduleId(scheduleId);
        startPage();
        List<CompetitionSceneCheckinPersonVO> list = checkinOverviewService.selectPersons(query);
        return getDataTable(list);
    }
}
