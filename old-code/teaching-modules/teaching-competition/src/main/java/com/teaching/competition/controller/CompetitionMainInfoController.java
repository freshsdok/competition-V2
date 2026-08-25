package com.teaching.competition.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.teaching.common.core.constant.Constants;
import com.teaching.common.security.annotation.InnerAuth;
import com.teaching.competition.domain.CompetitionChildren;
import com.teaching.competition.domain.CompetitionTreeInfo;
import com.teaching.competition.service.*;
import com.teaching.system.api.domain.*;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.web.page.TableDataInfo;

/**
 * 赛事主数据Controller
 *
 * @author teaching
 * @date 2025-10-10
 */
@RestController
@RequestMapping("/competitionManager")
public class CompetitionMainInfoController extends BaseController
{
    @Autowired
    private ICompetitionMainInfoService competitionMainInfoService;

    @Autowired
    private ICompetitionSeriesInfoService seriesInfoService;

    @Autowired
    private ICompetitionStageConfigService stageConfigService;

    @Autowired
    private ICompetitionAwardsConfigService awardsConfigService;

    @Autowired
    private ICompetitionEnterpriseRelaService enterpriseRelaService;

    @Autowired
    private ICompetitionCourseConfigService competitionCourseConfigService;

    /**
     * 查询赛事主数据列表
     */
    @RequiresPermissions("competition:competitionManager:list")
    @GetMapping("/list")
    public TableDataInfo list(CompetitionMainInfoReq req)
    {
        startPage();
        List<CompetitionMainInfo> list = competitionMainInfoService.selectCompetitionMainInfoList(req);
        return getDataTable(list);
    }


    @InnerAuth
    @GetMapping("/queryCompetitionInfo")
    public AjaxResult queryCompetitionInfo() {
        List<Map<String,Object>> list = competitionMainInfoService.selectCompetitionMainInfoListInner(new CompetitionMainInfoReq());
        return success(list);
    }


    @GetMapping("/pullDownList")
    public AjaxResult pullDownList(CompetitionMainInfoReq req) {
        List<CompetitionMainInfo> list = competitionMainInfoService.selectCompetitionMainInfoPullDownList(req);
        return success(list);
    }

    @GetMapping("/queryCompetitionInfoByCompetitionName")
    public AjaxResult selectCompetitionMainInfoByCompetitionName(@RequestParam(required = false) String competitionName) {
        List<CompetitionMainInfo> list = competitionMainInfoService.selectCompetitionSeriesInfoByCompetitionName(competitionName);
        return success(list);
    }

    /**
     * 获取赛事数据详细信息
     */
    @RequiresPermissions("competition:competitionManager:query")
    @GetMapping(value = "/getCompetitionDetailInfoById")
    public AjaxResult getInfo(CompetitionMainInfoReq req) {
        List<CompetitionDetailInfo> competitionDetailInfoList = competitionMainInfoService.selectCompetitionDetailInfoByCompetitionId(req);
        if (CollectionUtils.isEmpty(competitionDetailInfoList)) {
            return error("未查询到赛事信息");
        }
        return success(competitionDetailInfoList.get(0));
    }

    /**
     * 获取赛事处于阶段
     */
    @RequiresPermissions("competition:competitionManager:query")
    @GetMapping(value = "/queryNowCompetitionStageConfig")
    public AjaxResult queryNowCompetitionStageConfig(CompetitionMainInfoReq req) {
        return success(stageConfigService.selectNowCompetitionStageConfig(req.getCompetitionSeriesId()));
    }


    /**
     * 根据状态查询赛事信息
     */
    @InnerAuth
    @GetMapping(value = "/getNoStartCompetitionInfo")
    public AjaxResult getNoStartCompetitionInfo(CompetitionSeriesInfo competitionSeriesInfo) {
        return success(seriesInfoService.selectCompetitionSeriesInfoList(competitionSeriesInfo));
    }


    /**
     * 新增赛事主数据
     */
    @RequiresPermissions("competition:competitionManager:add")
    @Log(title = "新增赛事数据", businessType = BusinessType.INSERT)
    @PostMapping("/saveCompetitionInfo")
    public AjaxResult add(@RequestBody CompetitionDetailInfo competitionDetailInfo)
    {
        return success(competitionMainInfoService.insertCompetitionMainInfo(competitionDetailInfo));
    }

    /**
     * 修改赛事主数据
     */
    @RequiresPermissions("competition:competitionManager:edit")
    @Log(title = "修改赛事主数据", businessType = BusinessType.UPDATE)
    @PostMapping("/updateCompetitionInfo")
    public AjaxResult edit(@RequestBody CompetitionDetailInfo competitionDetailInfo)
    {
        return success(competitionMainInfoService.updateCompetitionMainInfo(competitionDetailInfo));
    }

    /**
     * 删除赛事主数据
     */
    @RequiresPermissions("competition:competitionManager:remove")
    @Log(title = "删除赛事主数据", businessType = BusinessType.DELETE)
	@PostMapping("/removeCompetitionMainInfo")
    public AjaxResult removeCompetitionMainInfo(@RequestBody CompetitionMainInfoReq req)
    {
        return toAjax(competitionMainInfoService.deleteCompetitionMainInfoByCompetitionIds(req));
    }

    /**
     * 修改赛事主数据
     */
    @RequiresPermissions("competition:competitionManager:editStatus")
    @Log(title = "修改赛事主数据状态", businessType = BusinessType.UPDATE)
    @PostMapping("/updateCompetitionInfoStatus")
    public AjaxResult updateCompetitionInfoStatus(@Validated @RequestBody CompetitionSeriesInfo competitionSeriesInfo)
    {
        return success(seriesInfoService.updateCompetitionSeriesInfo(competitionSeriesInfo));
    }

    @InnerAuth
    @PostMapping("/updateTaskCompetitionInfoStatus")
    public AjaxResult updateTaskCompetitionInfoStatus(@Validated @RequestBody CompetitionSeriesInfo competitionSeriesInfo)
    {
        return success(seriesInfoService.updateCompetitionSeriesInfo(competitionSeriesInfo));
    }

    @PostMapping("/insertCompetitionStageConfig")
    public AjaxResult save(@RequestBody List<CompetitionCourseConfig> competitionCourseConfigs)
    {
        return success(competitionCourseConfigService.insertCompetitionCourseConfig(competitionCourseConfigs));
    }

    @InnerAuth
    @Log(title = "内部调用修改赛事状态", businessType = BusinessType.UPDATE)
    @PostMapping("/updateInnerCompetitionInfoStatus")
    public AjaxResult updateInnerCompetitionInfoStatus(@Validated @RequestBody CompetitionSeriesInfo competitionSeriesInfo)
    {
        return success(seriesInfoService.updateCompetitionSeriesInfo(competitionSeriesInfo));
    }

    // 赛事审核查询赛事信息
    @InnerAuth
    @GetMapping(value = "/getInnerCompetitionDetailInfo")
    public AjaxResult getInnerCompetitionDetailInfo(CompetitionMainInfoReq req) {
        List<CompetitionDetailInfo> competitionDetailInfoList = competitionMainInfoService.selectCompetitionDetailInfoByCompetitionId(req);
        if (CollectionUtils.isEmpty(competitionDetailInfoList)) {
            return error("未查询到赛事信息");
        }
        return success(competitionDetailInfoList.get(0));
    }

    // 获取赛事下所有信息
//    @InnerAuth
    @GetMapping(value = "/selectAllCompetitionDetailInfo")
    public AjaxResult selectAllCompetitionDetailInfo(CompetitionMainInfoReq req) {
        req.setCheckStatus(Constants.COMPETITION_PUBLISH + "," + Constants.COMPETITION_RUNNING + "," + Constants.COMPETITION_END + "," + Constants.COMPETITION_REPEAL_PUBLISH);
        List<CompetitionDetailInfo> competitionDetailInfoList = competitionMainInfoService.selectAllCompetitionDetailInfo(req);
        if (CollectionUtils.isEmpty(competitionDetailInfoList)) {
            return error("未查询到赛事信息");
        }
        List<CompetitionTreeInfo> competitionTreeInfoList = new ArrayList<>();
        for (CompetitionDetailInfo competitionDetailInfo : competitionDetailInfoList) {
            CompetitionTreeInfo competitionTreeInfo = new CompetitionTreeInfo();
            competitionTreeInfo.setCompetitionId(competitionDetailInfo.getCompetitionId());
            competitionTreeInfo.setCompetitionName(competitionDetailInfo.getCompetitionName());
            competitionTreeInfo.setCompetitionSeriesId(competitionDetailInfo.getCompetitionSeriesId());
            competitionTreeInfo.setCompetitionSeriesName(competitionDetailInfo.getCompetitionSeriesName());
            competitionTreeInfo.setSort(1);
            competitionTreeInfo.setCompetitionStageConfigList(competitionDetailInfo.getCompetitionStageList());
            List<CompetitionChildren> competitionChildrenList = new ArrayList<>();
            if(CollectionUtils.isNotEmpty(competitionDetailInfo.getCompetitionTrackList())){
                competitionDetailInfo.getCompetitionTrackList().forEach(competitionTrackInfo -> {
                    CompetitionChildren competitionChildren = new CompetitionChildren();
                    competitionChildren.setId(competitionTrackInfo.getCompetitionTrackId());
                    competitionChildren.setLabel(competitionTrackInfo.getCompetitionTrackName());
                    competitionChildren.setSort(3);
                    competitionChildrenList.add(competitionChildren);
                    List<CompetitionTrackConfig> competitionTrackConfigList = competitionTrackInfo.getCompetitionTrackConfigList();
                    if(CollectionUtils.isNotEmpty(competitionTrackConfigList)){
                        List<CompetitionChildren> secondLevelChildrenList = new ArrayList<>();
                        competitionTrackConfigList.stream().forEach(competitionTrackConfig -> {
                            CompetitionChildren secondLevelChildren = new CompetitionChildren();
                            secondLevelChildren.setId(competitionTrackConfig.getSecondLevelCode());
                            secondLevelChildren.setLabel(competitionTrackConfig.getSecondLevelName());
                            secondLevelChildren.setSort(4);
                            secondLevelChildrenList.add(secondLevelChildren);
                        });
                        competitionChildren.setChildren(secondLevelChildrenList);
                    }
                });
                competitionTreeInfo.setCompetitionChildren(competitionChildrenList);
            }
            competitionTreeInfoList.add(competitionTreeInfo);
        }
        return success(competitionTreeInfoList);
    }

    /**
     * 用户组查询赛事专用
     * @param req
     * @return
     */
    @GetMapping(value = "/selectAllCompetitionDetailInfoForUserGroup")
    public AjaxResult selectAllCompetitionDetailInfoByUserGroup(CompetitionMainInfoReq req) {
        req.setCheckStatus(Constants.COMPETITION_PUBLISH + "," + Constants.COMPETITION_RUNNING + "," + Constants.COMPETITION_END + "," + Constants.COMPETITION_REPEAL_PUBLISH);
        List<CompetitionDetailInfo> competitionDetailInfoList = competitionMainInfoService.selectAllCompetitionDetailInfo(req);
        if (CollectionUtils.isEmpty(competitionDetailInfoList)) {
            return error("未查询到赛事信息");
        }
        List<CompetitionTreeInfo> competitionTreeInfoList = new ArrayList<>();
        for (CompetitionDetailInfo competitionDetailInfo : competitionDetailInfoList) {
            CompetitionTreeInfo competitionTreeInfo = new CompetitionTreeInfo();
            competitionTreeInfo.setCompetitionId(competitionDetailInfo.getCompetitionId());
            competitionTreeInfo.setCompetitionName(competitionDetailInfo.getCompetitionName());
            competitionTreeInfo.setCompetitionSeriesId(competitionDetailInfo.getCompetitionSeriesId());
            competitionTreeInfo.setCompetitionSeriesName(competitionDetailInfo.getCompetitionSeriesName());
            competitionTreeInfo.setSort(1);
            competitionTreeInfo.setCompetitionStageConfigList(competitionDetailInfo.getCompetitionStageList());
            List<CompetitionChildren> competitionChildrenList = new ArrayList<>();
            if(CollectionUtils.isNotEmpty(competitionDetailInfo.getCompetitionTrackList())){
                competitionDetailInfo.getCompetitionTrackList().forEach(competitionTrackInfo -> {
                    CompetitionChildren competitionChildren = new CompetitionChildren();
                    competitionChildren.setId(competitionTrackInfo.getCompetitionTrackId());
                    competitionChildren.setLabel(competitionTrackInfo.getCompetitionTrackName());
                    competitionChildren.setSort(3);
                    competitionChildrenList.add(competitionChildren);
                    List<CompetitionTrackConfig> competitionTrackConfigList = competitionTrackInfo.getCompetitionTrackConfigList();
                    if(CollectionUtils.isNotEmpty(competitionTrackConfigList)){
                        List<CompetitionChildren> secondLevelChildrenList = new ArrayList<>();
                        competitionTrackConfigList.stream().forEach(competitionTrackConfig -> {
                            CompetitionChildren secondLevelChildren = new CompetitionChildren();
                            secondLevelChildren.setId(competitionTrackConfig.getSecondLevelCode());
                            secondLevelChildren.setLabel(competitionTrackConfig.getSecondLevelName());
                            secondLevelChildren.setSort(4);
                            secondLevelChildrenList.add(secondLevelChildren);
                        });
                        competitionChildren.setChildren(secondLevelChildrenList);
                    }
                });
                competitionTreeInfo.setCompetitionChildren(competitionChildrenList);
            }
            competitionTreeInfoList.add(competitionTreeInfo);
        }
        return success(competitionTreeInfoList);
    }
}
