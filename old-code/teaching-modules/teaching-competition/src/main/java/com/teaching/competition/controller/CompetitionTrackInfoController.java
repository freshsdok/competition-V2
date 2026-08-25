package com.teaching.competition.controller;

import java.util.List;

import com.teaching.common.security.annotation.InnerAuth;
import com.teaching.competition.service.ICompetitionTrackInfoService;
import com.teaching.system.api.domain.CompetitionTrackInfo;
import com.teaching.system.api.domain.CompetitionTrackInfoEntity;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.utils.poi.ExcelUtil;
import com.teaching.common.core.web.page.TableDataInfo;

/**
 * 赛事赛道配置Controller
 *
 * @author teaching
 * @date 2025-11-17
 */
@RestController
@RequestMapping("/competitionTrackInfo")
public class CompetitionTrackInfoController extends BaseController
{
    @Autowired
    private ICompetitionTrackInfoService competitionTrackInfoService;

    /**
     * 查询赛事赛道配置列表
     */
    @RequiresPermissions("competition:competitionTrackInfo:list")
    @GetMapping("/list")
    public TableDataInfo list(CompetitionTrackInfo competitionTrackInfo) {
        startPage();
        List<CompetitionTrackInfo> list = competitionTrackInfoService.selectCompetitionTrackInfoList(competitionTrackInfo);
        return getDataTable(list);
    }

    /**
     * 导出赛事赛道配置列表
     */
    @RequiresPermissions("competition:competitionTrackInfo:export")
    @Log(title = "赛事赛道配置", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CompetitionTrackInfo competitionTrackInfo) {
        List<CompetitionTrackInfo> list = competitionTrackInfoService.selectCompetitionTrackInfoList(competitionTrackInfo);
        ExcelUtil<CompetitionTrackInfo> util = new ExcelUtil<CompetitionTrackInfo>(CompetitionTrackInfo.class);
        util.exportExcel(response, list, "赛事赛道配置数据");
    }

    /**
     * 获取赛事赛道配置详细信息
     */
    @RequiresPermissions("competition:competitionTrackInfo:query")
    @GetMapping(value = "/getCompetitionDetail/{competitionTrackId}")
    public AjaxResult getCompetitionTrackDetail(@PathVariable("competitionTrackId") String competitionTrackId) {
        return success(competitionTrackInfoService.selectCompetitionTrackInfoByCompetitionTrackId(competitionTrackId));
    }

    /**
     * 获取赛事赛道配置详细信息,内部调用
     */
    @InnerAuth
    @GetMapping(value = "/getInnerCompetitionTrackDetail/{trackId}")
    public AjaxResult getInnerCompetitionTrackDetail(@PathVariable("trackId") Long trackId) {
        return success(competitionTrackInfoService.selectCompetitionTrackInfoByTrackId(trackId));
    }

    /**
     * 新增赛事赛道配置
     */
    @RequiresPermissions("competition:competitionTrackInfo:add")
    @Log(title = "保存赛事赛道配置", businessType = BusinessType.INSERT)
    @PostMapping("/saveCompetitionTrackInfo")
    public AjaxResult saveCompetitionTrackInfo(@RequestBody CompetitionTrackInfoEntity competitionTrackInfoEntity) {
        return success(competitionTrackInfoService.insertCompetitionTrackInfo(competitionTrackInfoEntity));
    }

    /**
     * 修改赛事赛道配置状态
     */
    @InnerAuth
    @PostMapping("/updateCompetitionTrackStatus")
    public AjaxResult updateCompetitionTrackStatus(@RequestBody CompetitionTrackInfo competitionTrackInfo) {
        return success(competitionTrackInfoService.updateCompetitionTrackStatus(competitionTrackInfo));
    }

    /**
     * 删除赛事赛道配置
     */
    @RequiresPermissions("competition:competitionTrackInfo:remove")
    @Log(title = "赛事赛道配置", businessType = BusinessType.DELETE)
	@GetMapping("/removeCompetitionTrack/{competitionTrackId}")
    public AjaxResult remove(@PathVariable String competitionTrackId)
    {
        return toAjax(competitionTrackInfoService.deleteCompetitionTrackInfoByCompetitionTrackId(competitionTrackId));
    }
}
