package com.teaching.system.controller;

import com.teaching.common.core.utils.poi.ExcelUtil;
import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.web.page.TableDataInfo;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.system.domain.SchoolSpecialtyInfo;
import com.teaching.system.service.ISchoolSpecialtyInfoService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 专业信息Controller
 *
 * @author teaching
 * @date 2025-12-03
 */
@RestController
@RequestMapping("/discipline")
public class SchoolSpecialtyInfoController extends BaseController {
    @Autowired
    private ISchoolSpecialtyInfoService schoolSpecialtyInfoService;

    /**
     * 查询专业信息列表
     */
    @RequiresPermissions("system:major:list")
    @GetMapping("/list")
    public TableDataInfo list(SchoolSpecialtyInfo schoolSpecialtyInfo) {
        startPage();
        List<SchoolSpecialtyInfo> list = schoolSpecialtyInfoService.selectSchoolSpecialtyInfoList(schoolSpecialtyInfo);
        return getDataTable(list);
    }

    /**
     * 导出专业信息列表
     */
    @RequiresPermissions("system:major:export")
    @Log(title = "专业信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, SchoolSpecialtyInfo schoolSpecialtyInfo) {
        List<SchoolSpecialtyInfo> list = schoolSpecialtyInfoService.selectSchoolSpecialtyInfoList(schoolSpecialtyInfo);
        ExcelUtil<SchoolSpecialtyInfo> util = new ExcelUtil<SchoolSpecialtyInfo>(SchoolSpecialtyInfo.class);
        util.exportExcel(response, list, "专业信息数据");
    }

    /**
     * 获取专业信息详细信息
     */
    @RequiresPermissions("system:major:query")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(schoolSpecialtyInfoService.selectSchoolSpecialtyInfoById(id));
    }

    /**
     * 获取专业信息详细信息  不分页只返回前十个
     * @param schoolSpecialtyInfo
     * @return
     */
    @GetMapping(value = "/pc/list")
    public AjaxResult getListInfo10(SchoolSpecialtyInfo schoolSpecialtyInfo) {
        return success(schoolSpecialtyInfoService.getSchoolSpecialtyInfoByMajorClassList10(schoolSpecialtyInfo));
    }

    /**
     * 新增专业信息
     */
    @RequiresPermissions("system:major:add")
    @Log(title = "专业信息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody SchoolSpecialtyInfo schoolSpecialtyInfo) {
        return toAjax(schoolSpecialtyInfoService.insertSchoolSpecialtyInfo(schoolSpecialtyInfo));
    }

    /**
     * 修改专业信息
     */
    @RequiresPermissions("system:major:edit")
    @Log(title = "专业信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody SchoolSpecialtyInfo schoolSpecialtyInfo) {
        return toAjax(schoolSpecialtyInfoService.updateSchoolSpecialtyInfo(schoolSpecialtyInfo));
    }

    /**
     * 删除专业信息
     */
    @RequiresPermissions("system:major:remove")
    @Log(title = "专业信息", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(schoolSpecialtyInfoService.deleteSchoolSpecialtyInfoByIds(ids));
    }
}
