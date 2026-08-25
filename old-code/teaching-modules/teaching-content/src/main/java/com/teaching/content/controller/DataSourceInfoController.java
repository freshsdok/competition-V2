package com.teaching.content.controller;

import com.teaching.common.core.utils.poi.ExcelUtil;
import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.web.page.TableDataInfo;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.content.domain.DataSourceInfo;
import com.teaching.content.service.IDataSourceInfoService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 数据源信息Controller
 *
 * @author teaching
 * @date 2025-10-13
 */
@RestController
@RequestMapping("/source")
public class DataSourceInfoController extends BaseController {
    @Autowired
    private IDataSourceInfoService dataSourceInfoService;

    /**
     * 查询数据源列表不分页
     *
     * @param dataSourceInfo
     * @return
     */
    @GetMapping("/getList")
    public AjaxResult getList(DataSourceInfo dataSourceInfo) {
        List<DataSourceInfo> list = dataSourceInfoService.selectDataSourceInfoList(dataSourceInfo);
        return success(list);
    }

    /**
     * 查询数据源信息列表
     */
    @RequiresPermissions("content:source:list")
    @GetMapping("/list")
    public TableDataInfo list(DataSourceInfo dataSourceInfo) {
        startPage();
        List<DataSourceInfo> list = dataSourceInfoService.selectDataSourceInfoList(dataSourceInfo);
        return getDataTable(list);
    }

    /**
     * 导出数据源信息列表
     */
    @RequiresPermissions("content:source:export")
    @Log(title = "数据源信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, DataSourceInfo dataSourceInfo) {
        List<DataSourceInfo> list = dataSourceInfoService.selectDataSourceInfoList(dataSourceInfo);
        ExcelUtil<DataSourceInfo> util = new ExcelUtil<DataSourceInfo>(DataSourceInfo.class);
        util.exportExcel(response, list, "数据源信息数据");
    }

    /**
     * 获取数据源信息详细信息
     */
    @RequiresPermissions("content:source:query")
    @GetMapping(value = "/{dataId}")
    public AjaxResult getInfo(@PathVariable("dataId") Long dataId) {
        return success(dataSourceInfoService.selectDataSourceInfoByDataId(dataId));
    }

    /**
     * 新增数据源信息
     */
    @RequiresPermissions("content:source:add")
    @Log(title = "数据源信息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody DataSourceInfo dataSourceInfo) {
        return toAjax(dataSourceInfoService.insertDataSourceInfo(dataSourceInfo));
    }

    /**
     * 修改数据源信息
     */
    @RequiresPermissions("content:source:edit")
    @Log(title = "数据源信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody DataSourceInfo dataSourceInfo) {
        dataSourceInfo.setUpdateBy(SecurityUtils.getLoginUser().getSysUser().getNickName());
        return toAjax(dataSourceInfoService.updateDataSourceInfo(dataSourceInfo));
    }

    /**
     * 删除数据源信息
     */
    @RequiresPermissions("content:source:remove")
    @Log(title = "数据源信息", businessType = BusinessType.DELETE)
    @DeleteMapping("/{dataIds}")
    public AjaxResult remove(@PathVariable Long[] dataIds) {
        return toAjax(dataSourceInfoService.deleteDataSourceInfoByDataIds(dataIds));
    }
}
