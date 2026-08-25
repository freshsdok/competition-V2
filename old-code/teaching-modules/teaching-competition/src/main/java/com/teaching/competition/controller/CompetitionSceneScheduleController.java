package com.teaching.competition.controller;

import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.web.page.TableDataInfo;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.competition.domain.CompetitionSceneSchedule;
import com.teaching.competition.domain.CompetitionSceneScheduleAutoSequenceDTO;
import com.teaching.competition.domain.CompetitionSceneScheduleManualTargetDTO;
import com.teaching.competition.domain.CompetitionSceneScheduleNameSequenceDTO;
import com.teaching.competition.domain.CompetitionSceneSchedulePersonBindDTO;
import com.teaching.competition.domain.CompetitionSceneScheduleReviewObjectBindDTO;
import com.teaching.competition.domain.CompetitionSceneScheduleTarget;
import com.teaching.competition.domain.CompetitionSceneScheduleSyncReviewSessionDTO;
import com.teaching.competition.domain.CompetitionSceneScheduleTargetSequenceDTO;
import com.teaching.competition.domain.CompetitionSceneScheduleTeamBindDTO;
import com.teaching.competition.service.ICompetitionSceneScheduleService;
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
 * 赛事现场赛场安排Controller。
 */
@RestController
@RequestMapping({"/sceneSchedule", "/competition/sceneSchedule", "/scene/schedule", "/competition/scene/schedule"})
public class CompetitionSceneScheduleController extends BaseController {

    @Autowired
    private ICompetitionSceneScheduleService competitionSceneScheduleService;

    @RequiresPermissions("competition:sceneSchedule:list")
    @GetMapping("/list")
    public TableDataInfo list(CompetitionSceneSchedule schedule) {
        startPage();
        List<CompetitionSceneSchedule> list = competitionSceneScheduleService.selectCompetitionSceneScheduleList(schedule);
        return getDataTable(list);
    }

    @RequiresPermissions("competition:sceneSchedule:query")
    @GetMapping("/{scheduleId}")
    public AjaxResult getInfo(@PathVariable("scheduleId") Long scheduleId) {
        return success(competitionSceneScheduleService.selectCompetitionSceneScheduleById(scheduleId));
    }

    @RequiresPermissions("competition:sceneSchedule:add")
    @Log(title = "赛事现场赛场安排", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CompetitionSceneSchedule schedule) {
        return toAjax(competitionSceneScheduleService.insertCompetitionSceneSchedule(schedule));
    }

    @RequiresPermissions("competition:sceneSchedule:edit")
    @Log(title = "赛事现场赛场安排", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CompetitionSceneSchedule schedule) {
        return toAjax(competitionSceneScheduleService.updateCompetitionSceneSchedule(schedule));
    }

    @RequiresPermissions("competition:sceneSchedule:remove")
    @Log(title = "赛事现场赛场安排", businessType = BusinessType.DELETE)
    @DeleteMapping("/{scheduleIds}")
    public AjaxResult remove(@PathVariable Long[] scheduleIds) {
        return toAjax(competitionSceneScheduleService.deleteCompetitionSceneScheduleByIds(scheduleIds));
    }

    @RequiresPermissions("competition:sceneSchedule:edit")
    @Log(title = "赛事现场安排对象匹配", businessType = BusinessType.INSERT)
    @PostMapping("/match/{scheduleId}")
    public AjaxResult matchTargets(@PathVariable("scheduleId") Long scheduleId) {
        return success(competitionSceneScheduleService.matchScheduleTargets(scheduleId));
    }

    @RequiresPermissions("competition:sceneSchedule:list")
    @GetMapping("/target/list")
    public TableDataInfo targetList(CompetitionSceneScheduleTarget target) {
        startPage();
        List<CompetitionSceneScheduleTarget> list = competitionSceneScheduleService.selectScheduleTargetList(target);
        return getDataTable(list);
    }

    @RequiresPermissions("competition:sceneSchedule:add")
    @Log(title = "赛事现场安排对象", businessType = BusinessType.INSERT)
    @PostMapping("/target")
    public AjaxResult addTarget(@RequestBody CompetitionSceneScheduleTarget target) {
        return success(competitionSceneScheduleService.insertScheduleTarget(target));
    }

    @RequiresPermissions("competition:sceneSchedule:add")
    @Log(title = "赛事现场安排对象批量新增", businessType = BusinessType.INSERT)
    @PostMapping("/target/batch")
    public AjaxResult addTargets(@RequestBody List<CompetitionSceneScheduleTarget> targets) {
        return success(competitionSceneScheduleService.insertScheduleTargets(targets));
    }

    @RequiresPermissions("competition:sceneSchedule:edit")
    @Log(title = "赛事现场安排对象", businessType = BusinessType.UPDATE)
    @PutMapping("/target")
    public AjaxResult editTarget(@RequestBody CompetitionSceneScheduleTarget target) {
        return toAjax(competitionSceneScheduleService.updateScheduleTarget(target));
    }

    @RequiresPermissions("competition:sceneSchedule:remove")
    @Log(title = "赛事现场安排对象", businessType = BusinessType.DELETE)
    @DeleteMapping("/target/{targetIds}")
    public AjaxResult removeTarget(@PathVariable Long[] targetIds) {
        return toAjax(competitionSceneScheduleService.deleteScheduleTargetByIds(targetIds));
    }

    @RequiresPermissions("competition:sceneSchedule:add")
    @Log(title = "赛场安排绑定评审对象", businessType = BusinessType.INSERT)
    @PostMapping("/{scheduleId}/targets/review-objects")
    public AjaxResult bindReviewObjects(@PathVariable("scheduleId") Long scheduleId,
                                        @RequestBody CompetitionSceneScheduleReviewObjectBindDTO dto) {
        return success(competitionSceneScheduleService.bindReviewObjects(scheduleId, dto));
    }

    @RequiresPermissions("competition:sceneSchedule:add")
    @Log(title = "赛场安排绑定团队", businessType = BusinessType.INSERT)
    @PostMapping("/{scheduleId}/targets/teams")
    public AjaxResult bindTeams(@PathVariable("scheduleId") Long scheduleId,
                                @RequestBody CompetitionSceneScheduleTeamBindDTO dto) {
        return success(competitionSceneScheduleService.bindTeams(scheduleId, dto));
    }

    @RequiresPermissions("competition:sceneSchedule:add")
    @Log(title = "赛场安排绑定人员", businessType = BusinessType.INSERT)
    @PostMapping("/{scheduleId}/targets/persons")
    public AjaxResult bindPersons(@PathVariable("scheduleId") Long scheduleId,
                                  @RequestBody CompetitionSceneSchedulePersonBindDTO dto) {
        return success(competitionSceneScheduleService.bindPersons(scheduleId, dto));
    }

    @RequiresPermissions("competition:sceneSchedule:add")
    @Log(title = "赛场安排手工对象", businessType = BusinessType.INSERT)
    @PostMapping("/{scheduleId}/targets/manual")
    public AjaxResult addManualTarget(@PathVariable("scheduleId") Long scheduleId,
                                      @RequestBody CompetitionSceneScheduleManualTargetDTO dto) {
        return success(competitionSceneScheduleService.insertManualTarget(scheduleId, dto));
    }

    @RequiresPermissions("competition:sceneSchedule:edit")
    @Log(title = "赛场安排对象顺序", businessType = BusinessType.UPDATE)
    @PostMapping("/{scheduleId}/targets/sequence")
    public AjaxResult updateTargetSequence(@PathVariable("scheduleId") Long scheduleId,
                                           @RequestBody List<CompetitionSceneScheduleTargetSequenceDTO> items) {
        return success(competitionSceneScheduleService.updateTargetSequences(scheduleId, items));
    }

    @RequiresPermissions("competition:sceneSchedule:edit")
    @Log(title = "赛场安排自动生成顺序", businessType = BusinessType.UPDATE)
    @PostMapping("/{scheduleId}/targets/sequence/auto-generate")
    public AjaxResult autoGenerateTargetSequence(@PathVariable("scheduleId") Long scheduleId,
                                                 @RequestBody CompetitionSceneScheduleAutoSequenceDTO dto) {
        return success(competitionSceneScheduleService.autoGenerateTargetSequence(scheduleId, dto));
    }

    @RequiresPermissions("competition:sceneSchedule:edit")
    @Log(title = "赛场安排按姓名排序", businessType = BusinessType.UPDATE)
    @PostMapping("/{scheduleId}/targets/sequence/by-name")
    public AjaxResult generateTargetSequenceByNames(@PathVariable("scheduleId") Long scheduleId,
                                                    @RequestBody CompetitionSceneScheduleNameSequenceDTO dto) {
        return success(competitionSceneScheduleService.generateTargetSequenceByNames(scheduleId, dto));
    }

    @RequiresPermissions("competition:sceneSchedule:edit")
    @Log(title = "赛场安排同步评审场次", businessType = BusinessType.INSERT)
    @PostMapping("/{scheduleId}/targets/sync-review-session")
    public AjaxResult syncReviewSession(@PathVariable("scheduleId") Long scheduleId,
                                        @RequestBody CompetitionSceneScheduleSyncReviewSessionDTO dto) {
        return success(competitionSceneScheduleService.syncTargetsToReviewSession(scheduleId, dto));
    }
}
