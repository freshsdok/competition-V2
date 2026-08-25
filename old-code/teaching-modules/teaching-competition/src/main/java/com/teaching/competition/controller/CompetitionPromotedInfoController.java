package com.teaching.competition.controller;

import com.teaching.common.core.utils.poi.ExcelUtil;
import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.web.page.TableDataInfo;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.competition.domain.CompetitionPromotedInfo;
import com.teaching.competition.service.ICompetitionPromotedInfoService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 赛事晋级Controller
 *
 * @author teaching
 * @date 2026-05-19
 */
@RestController
@RequestMapping("/promotedInfo")
public class CompetitionPromotedInfoController extends BaseController {
    @Autowired
    private ICompetitionPromotedInfoService competitionPromotedInfoService;

    /**
     * 查询赛事晋级列表
     */
//    @RequiresPermissions("competition:promotedInfo:list")
    @GetMapping("/list")
    public TableDataInfo list(CompetitionPromotedInfo competitionPromotedInfo) {
        startPage();
        List<CompetitionPromotedInfo> list = competitionPromotedInfoService.selectCompetitionPromotedInfoList(competitionPromotedInfo);
        return getDataTable(list);
    }

    /**
     * 获取pc端赛事晋级信息
     * @param competitionPromotedInfo
     * @return
     */
    @GetMapping(value = "/pcList")
    public AjaxResult getPcList(CompetitionPromotedInfo competitionPromotedInfo) {
        competitionPromotedInfo.setLeaderTeacherId(SecurityUtils.getLoginUser().getSysUser().getUserId());
        return success(competitionPromotedInfoService.selectCompetitionPromotedInfoList(competitionPromotedInfo));
    }

    /**
     * 导出赛事晋级列表
     */
//    @RequiresPermissions("competition:promotedInfo:export")
//    @Log(title = "赛事晋级", businessType = BusinessType.EXPORT)
//    @PostMapping("/export")
//    public void export(HttpServletResponse response, CompetitionPromotedInfo competitionPromotedInfo) {
//        List<CompetitionPromotedInfo> list = competitionPromotedInfoService.selectCompetitionPromotedInfoList(competitionPromotedInfo);
//        ExcelUtil<CompetitionPromotedInfo> util = new ExcelUtil<CompetitionPromotedInfo>(CompetitionPromotedInfo.class);
//        util.exportExcel(response, list, "赛事晋级数据");
//    }

    /**
     * 获取赛事晋级详细信息
     */
//    @RequiresPermissions("competition:promotedInfo:query")
    @GetMapping(value = "/getDetailInfo/{promotedId}")
    public AjaxResult getInfo(@PathVariable("promotedId") Long promotedId) {
        return success(competitionPromotedInfoService.selectCompetitionPromotedInfoByPromotedId(promotedId));
    }

    /**
     * 新增赛事晋级
     */
//    @RequiresPermissions("competition:promotedInfo:add")
//    @Log(title = "赛事晋级", businessType = BusinessType.INSERT)
//    @PostMapping
//    public AjaxResult add(@RequestBody CompetitionPromotedInfo competitionPromotedInfo) {
//        return toAjax(competitionPromotedInfoService.insertCompetitionPromotedInfo(competitionPromotedInfo));
//    }

    /**
     * 修改赛事晋级
     */
//    @RequiresPermissions("competition:promotedInfo:edit")
    @Log(title = "赛事晋级编辑", businessType = BusinessType.UPDATE)
    @PostMapping("/editCompetitionPromotedInfo")
    public AjaxResult edit(@RequestBody CompetitionPromotedInfo competitionPromotedInfo) {
        return toAjax(competitionPromotedInfoService.updateCompetitionPromotedInfo(competitionPromotedInfo));
    }

    /**
     * 删除赛事晋级
     */
//    @RequiresPermissions("competition:promotedInfo:remove")
    @Log(title = "赛事晋级删除", businessType = BusinessType.DELETE)
    @GetMapping("/remove/{promotedId}")
    public AjaxResult remove(@PathVariable Long promotedId) {
        return toAjax(competitionPromotedInfoService.deleteCompetitionPromotedInfoByPromotedId(promotedId));
    }
}
