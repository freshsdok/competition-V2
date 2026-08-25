package com.teaching.competition.controller;

import com.teaching.common.core.utils.StringUtils;
import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.web.page.TableDataInfo;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.competition.domain.CompetitionSceneOperationLog;
import com.teaching.competition.domain.CompetitionSceneVerifyReq;
import com.teaching.competition.service.ICompetitionSceneVerifyService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 赛事现场扫码核验Controller。
 */
@RestController
@RequestMapping({"/sceneVerify", "/competition/sceneVerify"})
public class CompetitionSceneVerifyController extends BaseController {

    @Autowired
    private ICompetitionSceneVerifyService competitionSceneVerifyService;

    @PostMapping("/scan")
    public AjaxResult scan(@RequestBody CompetitionSceneVerifyReq req, HttpServletRequest request) {
        fillScanIp(req, request);
        return success(competitionSceneVerifyService.scan(req));
    }

    @PostMapping("/confirm")
    public AjaxResult confirm(@RequestBody CompetitionSceneVerifyReq req, HttpServletRequest request) {
        fillScanIp(req, request);
        return success(competitionSceneVerifyService.confirm(req));
    }

    @RequiresPermissions("competition:sceneVerify:list")
    @GetMapping("/log/list")
    public TableDataInfo logList(CompetitionSceneOperationLog log) {
        startPage();
        List<CompetitionSceneOperationLog> list = competitionSceneVerifyService.selectOperationLogList(log);
        return getDataTable(list);
    }

    private void fillScanIp(CompetitionSceneVerifyReq req, HttpServletRequest request) {
        if (req != null && StringUtils.isEmpty(req.getScanIp()) && request != null) {
            req.setScanIp(request.getRemoteAddr());
        }
    }
}
