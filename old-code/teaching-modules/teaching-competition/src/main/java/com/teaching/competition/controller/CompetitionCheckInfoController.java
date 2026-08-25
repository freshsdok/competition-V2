package com.teaching.competition.controller;

import com.teaching.common.core.utils.poi.ExcelUtil;
import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.web.page.TableDataInfo;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.competition.domain.CompetitionCheckInfo;
import com.teaching.competition.service.ICompetitionCheckInfoService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 校验项Controller
 *
 * @author teaching
 * @date 2025-12-18
 */
@RestController
@RequestMapping("/checkInfo")
public class CompetitionCheckInfoController extends BaseController {
    @Autowired
    private ICompetitionCheckInfoService competitionCheckInfoService;

    /**
     * 查询校验项列表
     */
    @RequiresPermissions("competition:checkInfo:list")
    @GetMapping("/list")
    public TableDataInfo list(CompetitionCheckInfo competitionCheckInfo) {
        startPage();
        List<CompetitionCheckInfo> list = competitionCheckInfoService.selectCompetitionCheckInfoList(competitionCheckInfo);
        return getDataTable(list);
    }

    /**
     * 导出校验项列表
     */
    @RequiresPermissions("competition:checkInfo:export")
    @Log(title = "校验项", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CompetitionCheckInfo competitionCheckInfo) {
        List<CompetitionCheckInfo> list = competitionCheckInfoService.selectCompetitionCheckInfoList(competitionCheckInfo);
        ExcelUtil<CompetitionCheckInfo> util = new ExcelUtil<CompetitionCheckInfo>(CompetitionCheckInfo.class);
        util.exportExcel(response, list, "校验项数据");
    }

    /**
     * 获取校验项详细信息
     */
    @RequiresPermissions("competition:checkInfo:query")
    @GetMapping(value = "/{checkItemId}")
    public AjaxResult getInfo(@PathVariable("checkItemId") Long checkItemId) {
        return success(competitionCheckInfoService.selectCompetitionCheckInfoByCheckItemId(checkItemId));
    }

    /**
     * 新增校验项
     */
    @RequiresPermissions("competition:checkInfo:add")
    @Log(title = "校验项", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CompetitionCheckInfo competitionCheckInfo) {
        return toAjax(competitionCheckInfoService.insertCompetitionCheckInfo(competitionCheckInfo));
    }

    /**
     * 修改校验项
     */
    @RequiresPermissions("competition:checkInfo:edit")
    @Log(title = "校验项", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CompetitionCheckInfo competitionCheckInfo) {
        return toAjax(competitionCheckInfoService.updateCompetitionCheckInfo(competitionCheckInfo));
    }

    /**
     * 删除校验项
     */
    @RequiresPermissions("competition:checkInfo:remove")
    @Log(title = "校验项", businessType = BusinessType.DELETE)
    @DeleteMapping("/{checkItemIds}")
    public AjaxResult remove(@PathVariable Long[] checkItemIds) {
        return toAjax(competitionCheckInfoService.deleteCompetitionCheckInfoByCheckItemIds(checkItemIds));
    }
}
