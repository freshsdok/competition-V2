package com.teaching.competition.controller;

import java.util.List;

import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.competition.domain.CompetitionWorks;
import com.teaching.competition.domain.UserGradeInfo;
import com.teaching.competition.service.ICompetitionMainInfoService;
import com.teaching.competition.service.IUserGradeInfoService;
import com.teaching.system.api.domain.CompetitionAwardsConfig;
import com.teaching.system.api.domain.CompetitionStageConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.web.page.TableDataInfo;

/**
 * 用户成绩信息Controller
 * 
 * @author teaching
 * @date 2025-10-22
 */
@RestController
@RequestMapping("/userGradeInfo")
public class UserGradeInfoController extends BaseController
{
    @Autowired
    private IUserGradeInfoService userGradeInfoService;

    /**
     * 查询用户成绩信息列表
     */
    @GetMapping("/list")
    public TableDataInfo list(UserGradeInfo userGradeInfo)
    {
        startPage();
        userGradeInfo.setUserId(SecurityUtils.getLoginUser().getSysUser().getUserId());
        List<UserGradeInfo> list = userGradeInfoService.selectUserGradeInfoList(userGradeInfo);
        return getDataTable(list);
    }

    // 生成用户晋级情况信息
    @RequiresPermissions("competition:userGradeInfo:save")
    @GetMapping("/createAdvanceUserGradeInfo")
    public TableDataInfo createAdvanceUserGradeInfo(UserGradeInfo userGradeInfo) {
        startPage();
        return getDataTable(userGradeInfoService.createAdvanceUserGradeInfo(userGradeInfo));
    }

    // 保存最终用户晋级情况信息
    @RequiresPermissions("competition:userGradeInfo:save")
    @PostMapping("/saveAdvanceUserGradeInfo")
    public AjaxResult saveAdvanceUserGradeInfo(@RequestBody List<CompetitionWorks> competitionWorksList) {
        return success(userGradeInfoService.saveAdvanceUserGradeInfo(competitionWorksList));
    }

    // 生成用户成绩(系统判定)
    @RequiresPermissions("competition:userGradeInfo:save")
    @GetMapping("/createUserGradeInfo")
    public TableDataInfo createUserGradeInfo(UserGradeInfo userGradeInfo) {
        startPage();
        return getDataTable(userGradeInfoService.createUserGradeInfo(userGradeInfo));
    }

    // 用户成绩(人为参与成绩及获奖名单)
    @RequiresPermissions("competition:userGradeInfo:save")
    @PostMapping("/saveFinalUserGradeInfo")
    public AjaxResult saveFinalUserGradeInfo(@RequestBody List<UserGradeInfo> userGradeInfoList) {
        return success(userGradeInfoService.insertUserGradeInfo(userGradeInfoList));
    }

    //更新用户奖项
    @RequiresPermissions("competition:userGradeInfo:edit")
    @PostMapping("/updateUserGradeInfo")
    public AjaxResult updateUserGradeInfo(@RequestBody List<UserGradeInfo> userGradeInfoList) {
        return success(userGradeInfoService.updateUserGradeInfo(userGradeInfoList));
    }

    // 生成晋级调整赛事晋级分数，晋级人数
    @RequiresPermissions("competition:userGradeInfo:edit")
    @PostMapping("/saveUserGradeCompetitionStageConfig")
    public AjaxResult updateUserGradeCompetitionStageConfig(@RequestBody CompetitionStageConfig stageConfig) {
        return success(userGradeInfoService.updateUserGradeCompetitionStageConfig(stageConfig));
    }

    // 生成成绩按照奖项调整人数
    @RequiresPermissions("competition:userGradeInfo:edit")
    @PostMapping("/saveCompetitionAwardsConfig")
    public AjaxResult updateCompetitionAwardsConfig(@RequestBody CompetitionAwardsConfig awardsConfig) {
        return success(userGradeInfoService.updateCompetitionAwardsConfig(awardsConfig));
    }
}
