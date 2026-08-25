package com.teaching.competition.controller;

import com.teaching.common.core.utils.poi.ExcelUtil;
import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.web.page.TableDataInfo;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.competition.domain.CompetitionCheckDataPackage;
import com.teaching.competition.service.ICompetitionCheckDataPackageService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 校验包Controller
 *
 * @author teaching
 * @date 2025-12-18
 */
@RestController
@RequestMapping("/checkPackage")
public class CompetitionCheckDataPackageController extends BaseController {
    @Autowired
    private ICompetitionCheckDataPackageService competitionCheckDataPackageService;

    /**
     * 查询校验包列表
     */
    @RequiresPermissions("competition:checkPackage:list")
    @GetMapping("/list")
    public TableDataInfo list(CompetitionCheckDataPackage competitionCheckDataPackage) {
        startPage();
        List<CompetitionCheckDataPackage> list = competitionCheckDataPackageService.selectCompetitionCheckDataPackageList(competitionCheckDataPackage);
        return getDataTable(list);
    }

    /**
     * 获取校验包列表信息
     *
     * @param competitionCheckDataPackage
     * @return
     */
    @GetMapping(value = "/getList")
    public AjaxResult getInfoList(CompetitionCheckDataPackage competitionCheckDataPackage) {
        return success(competitionCheckDataPackageService.selectCompetitionCheckDataPackageList(competitionCheckDataPackage));
    }


    /**
     * 导出校验包列表
     */
    @RequiresPermissions("competition:checkPackage:export")
    @Log(title = "校验包", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CompetitionCheckDataPackage competitionCheckDataPackage) {
        List<CompetitionCheckDataPackage> list = competitionCheckDataPackageService.selectCompetitionCheckDataPackageList(competitionCheckDataPackage);
        ExcelUtil<CompetitionCheckDataPackage> util = new ExcelUtil<CompetitionCheckDataPackage>(CompetitionCheckDataPackage.class);
        util.exportExcel(response, list, "校验包数据");
    }

    /**
     * 获取校验包详细信息
     */
    @RequiresPermissions("competition:checkPackage:query")
    @GetMapping(value = "/{packageId}")
    public AjaxResult getInfo(@PathVariable("packageId") Long packageId) {
        return success(competitionCheckDataPackageService.selectCompetitionCheckDataPackageByPackageId(packageId));
    }

    /**
     * 新增校验包
     */
//    @RequiresPermissions("competition:checkPackage:add")
    @Log(title = "校验包", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CompetitionCheckDataPackage competitionCheckDataPackage) {
        return toAjax(competitionCheckDataPackageService.insertCompetitionCheckDataPackage(competitionCheckDataPackage));
    }

    /**
     * 修改校验包
     */
    @RequiresPermissions("competition:checkPackage:edit")
    @Log(title = "校验包", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CompetitionCheckDataPackage competitionCheckDataPackage) {
        return toAjax(competitionCheckDataPackageService.updateCompetitionCheckDataPackage(competitionCheckDataPackage));
    }

    /**
     * 删除校验包
     */
    @RequiresPermissions("competition:checkPackage:remove")
    @Log(title = "校验包", businessType = BusinessType.DELETE)
    @DeleteMapping("/{packageIds}")
    public AjaxResult remove(@PathVariable Long[] packageIds) {
        return toAjax(competitionCheckDataPackageService.deleteCompetitionCheckDataPackageByPackageIds(packageIds));
    }
}
