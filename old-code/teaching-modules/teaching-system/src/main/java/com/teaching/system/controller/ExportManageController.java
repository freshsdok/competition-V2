package com.teaching.system.controller;

import java.util.List;
import java.util.Map;

import com.teaching.common.core.domain.R;
import com.teaching.common.security.annotation.InnerAuth;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.system.domain.ExportManage;
import com.teaching.system.service.IExportManageService;
import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.utils.poi.ExcelUtil;
import com.teaching.common.core.web.page.TableDataInfo;
import org.springframework.web.multipart.MultipartFile;

/**
 * 导出管理Controller
 * 
 * @author teaching
 * @date 2026-01-09
 */
@RestController
@RequestMapping("/exportManage")
public class ExportManageController extends BaseController
{
    @Autowired
    private IExportManageService exportManageService;

    /**
     * 查询导出管理列表
     */
    @RequiresPermissions("system:manage:list")
    @GetMapping("/list")
    public TableDataInfo list(ExportManage exportManage)
    {
        startPage();
        List<ExportManage> list = exportManageService.selectExportManageList(exportManage);
        return getDataTable(list);
    }

    /**
     * 导出导出管理列表
     */
    @RequiresPermissions("system:manage:export")
    @Log(title = "导出管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ExportManage exportManage)
    {
        List<ExportManage> list = exportManageService.selectExportManageList(exportManage);
        ExcelUtil<ExportManage> util = new ExcelUtil<ExportManage>(ExportManage.class);
        util.exportExcel(response, list, "导出管理数据");
    }

    /**
     * 获取导出管理详细信息
     */
    @RequiresPermissions("system:manage:query")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(exportManageService.selectExportManageById(id));
    }

    /**
     * 新增导出管理
     */
    @RequiresPermissions("system:manage:add")
    @Log(title = "导出管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ExportManage exportManage)
    {
        return toAjax(exportManageService.insertExportManage(exportManage));
    }

    /**
     * 修改导出管理
     */
    @RequiresPermissions("system:manage:edit")
    @Log(title = "导出管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ExportManage exportManage)
    {
        return toAjax(exportManageService.updateExportManage(exportManage));
    }

    @InnerAuth
    @PostMapping("/saveOssExportFile")
    public AjaxResult saveOssExportFile(@RequestBody Map<String,Object> fileParam) {
        return success(exportManageService.saveExportManageInner(fileParam));
    }
    //
    @InnerAuth
    @PostMapping("/updateExportManageInner")
    public AjaxResult updateExportManageInner(@RequestBody Map<String,Object> fileParam) {
        return success(exportManageService.updateExportManageInner(fileParam));
    }

    /**
     * 删除导出管理
     */
    @RequiresPermissions("system:manage:remove")
    @Log(title = "导出管理", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(exportManageService.deleteExportManageByIds(ids));
    }

    /**
     * 上传文件管理-文件导出
     * @param urls 下载链接
     * @return
     */
//    @PostMapping("/exportFiles")
//    public AjaxResult exportFiles(@RequestBody List<String> urls){
//        return AjaxResult.success(exportManageService.exportFiles(urls));
//    }

}
