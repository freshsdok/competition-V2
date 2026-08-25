package com.teaching.competition.controller;

import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.competition.domain.CompetitionSceneOneCardIssueReq;
import com.teaching.competition.service.ICompetitionSceneOneCardIssueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 现场一证多权旁路发证Controller。
 */
@RestController
@RequestMapping({"/sceneOneCardIssue", "/competition/sceneOneCardIssue"})
public class CompetitionSceneOneCardIssueController extends BaseController {

    @Autowired
    private ICompetitionSceneOneCardIssueService oneCardIssueService;

    @RequiresPermissions("competition:sceneCredential:add")
    @Log(title = "现场证件一证多权旁路发证", businessType = BusinessType.INSERT)
    @PostMapping("/issueByTarget")
    public AjaxResult issueByTarget(@RequestBody CompetitionSceneOneCardIssueReq req) {
        if (req == null) {
            return error("发证参数不能为空");
        }
        if (req.getScheduleId() == null) {
            return error("赛场安排ID不能为空");
        }
        if (req.getTargetId() == null) {
            return error("赛场对象ID不能为空");
        }
        return success(oneCardIssueService.issueOneCardByScheduleTarget(req.getScheduleId(), req.getTargetId()));
    }
}
