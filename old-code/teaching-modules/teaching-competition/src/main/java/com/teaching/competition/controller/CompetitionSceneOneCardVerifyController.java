package com.teaching.competition.controller;

import com.teaching.common.core.utils.StringUtils;
import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.competition.domain.CompetitionSceneOneCardVerifyReq;
import com.teaching.competition.service.ICompetitionSceneOneCardVerifyService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 现场一证多权旁路扫码Controller。
 */
@RestController
@RequestMapping({"/sceneOneCardVerify", "/competition/sceneOneCardVerify"})
public class CompetitionSceneOneCardVerifyController extends BaseController {

    @Autowired
    private ICompetitionSceneOneCardVerifyService oneCardVerifyService;

    @PostMapping("/pilot/scan")
    public AjaxResult scan(@RequestBody CompetitionSceneOneCardVerifyReq req, HttpServletRequest request) {
        fillScanIp(req, request);
        return success(oneCardVerifyService.scan(req));
    }

    @PostMapping("/pilot/confirm")
    public AjaxResult confirm(@RequestBody CompetitionSceneOneCardVerifyReq req, HttpServletRequest request) {
        fillScanIp(req, request);
        return success(oneCardVerifyService.confirm(req));
    }

    private void fillScanIp(CompetitionSceneOneCardVerifyReq req, HttpServletRequest request) {
        if (req != null && StringUtils.isEmpty(req.getScanIp()) && request != null) {
            req.setScanIp(request.getRemoteAddr());
        }
    }
}
