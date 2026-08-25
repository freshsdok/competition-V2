package com.teaching.competition.controller;

import com.teaching.common.core.constant.DictConstant;
import com.teaching.common.core.constant.SecurityConstants;
import com.teaching.common.core.domain.R;
import com.teaching.common.core.utils.poi.ExcelUtil;
import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.web.page.TableDataInfo;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.competition.contant.ApplyConstants;
import com.teaching.competition.domain.AwardDetails;
import com.teaching.competition.service.IAwardDetailsUserService;
import com.teaching.competition.service.ICompetitionApplyInfoService;
import com.teaching.competition.service.ITeamManagerInfoService;
import com.teaching.system.api.RemoteFileService;
import com.teaching.system.api.RemoteUserService;
import com.teaching.system.api.domain.CompetitionApplyInfo;
import com.teaching.system.api.domain.TeamManagerInfoAwardsInfo;
import com.teaching.system.api.domain.TeamManagerInfoAwardsUserInfo;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 获奖公示明细pc端接口
 *
 * @author teaching
 * @date 2026-05-12
 */
@RestController
@RequestMapping("/awardDetailsUser")
public class AwardDetailsUserController extends BaseController {

    @Autowired
    private IAwardDetailsUserService awardDetailsUserService;
    @Autowired
    private RemoteUserService remoteUserService;

    @Autowired
    private RemoteFileService remoteFileService;

    @Autowired
    private ICompetitionApplyInfoService competitionApplyInfoService;

    @Autowired
    private ITeamManagerInfoService teamManagerInfoService;

    private static ThreadPoolExecutor threadPoolPCExecutor = new ThreadPoolExecutor(5, 10, 10,
            TimeUnit.MINUTES, new LinkedBlockingQueue<>(), new ThreadPoolExecutor.DiscardOldestPolicy());


    /**
     * 用户端获奖公示明细列表
     */
    @RequestMapping("/awardDetailsList")
    public TableDataInfo selectAwardDetailsCompetitionApplyInfoList(AwardDetails awardDetails) {
        startPage();
        List<AwardDetails> awardDetailsList = awardDetailsUserService.selectAwardDetailsList(awardDetails);
        return getDataTable(awardDetailsList);
    }

    /**
     *
     * 用户端获奖公示列表
     * */
    @GetMapping("/awardPublicityList")
    public AjaxResult selectAwardDetailsCompetitionApplyInfoList() {
        Long userId = SecurityUtils.getLoginUser().getSysUser().getUserId();
        return success(awardDetailsUserService.selectAwardPublicityList(userId));
    }

    /**
     * 用户端更新获奖公示明细
     */
    @PostMapping("/updateAwardDetailsList")
    public AjaxResult updateAwardDetails(@RequestBody List<AwardDetails> awardDetails) {
        return success(awardDetailsUserService.updateAwardDetails(awardDetails));
    }

    /**
     * 用户端导出获奖公示明细列表
     */
    @Log(title = "获奖公示明细", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response,AwardDetails awardDetails) {

        //将文件上传到服务器，并保存到导出记录中
        Long userId = SecurityUtils.getLoginUser().getSysUser().getUserId();
        CompetitionApplyInfo competitionApplyInfoReq = new CompetitionApplyInfo();
        if (!"all".equals(awardDetails.getExportType())) {
            BeanUtils.copyProperties(awardDetails, competitionApplyInfoReq);
        }
        competitionApplyInfoReq.setPayStatus(DictConstant.PAID);
//        competitionApplyInfoReq.setCompetitionRoleName(ApplyConstants.TEAM_GUIDE_TEACHER);
        competitionApplyInfoReq.setLeaderTeacherId(userId);
        competitionApplyInfoReq.setAwardPublicityId(awardDetails.getAwardPublicityId());
//        List<CompetitionApplyInfo> applyInfoList = competitionApplyInfoService.exportCompetitionApplyInfoAwardsList(competitionApplyInfoReq);
        competitionApplyInfoReq.setCompetitionRoleName(null);
        List<TeamManagerInfoAwardsUserInfo> teamManagerInfoList = teamManagerInfoService.selectTeamManagerInfoAwardsExportPCList(competitionApplyInfoReq);
        Map<String, List<?>> sheetDataMap = new java.util.LinkedHashMap<>();
//        sheetDataMap.put("获奖名单（按人）", applyInfoList);
        sheetDataMap.put("获奖名单（按团队）", teamManagerInfoList);
        ExcelUtil excelUtil = new ExcelUtil<>();
        excelUtil.exportExcelByteData(response, sheetDataMap);
    }
}
