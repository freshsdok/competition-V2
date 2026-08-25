package com.teaching.system.controller;

import java.util.List;
import java.io.IOException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.system.domain.TeacherTmpInfo;
import com.teaching.system.service.ITeacherTmpInfoService;
import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.utils.poi.ExcelUtil;
import com.teaching.common.core.web.page.TableDataInfo;

/**
 * 教师导入临时Controller
 * 
 * @author teaching
 * @date 2025-12-19
 */
@RestController
@RequestMapping("/teacherTmpInfo")
public class TeacherTmpInfoController extends BaseController
{
    @Autowired
    private ITeacherTmpInfoService teacherTmpInfoService;

    /**
     * 查询教师导入临时列表
     */
    @RequiresPermissions("system:info:list")
    @GetMapping("/list")
    public TableDataInfo list(TeacherTmpInfo teacherTmpInfo)
    {
        startPage();
        List<TeacherTmpInfo> list = teacherTmpInfoService.selectTeacherTmpInfoList(teacherTmpInfo);
        return getDataTable(list);
    }

    /**
     * 导出教师导入临时列表
     */
    @RequiresPermissions("system:info:export")
    @Log(title = "教师导入临时", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TeacherTmpInfo teacherTmpInfo)
    {
        List<TeacherTmpInfo> list = teacherTmpInfoService.selectTeacherTmpInfoList(teacherTmpInfo);
        ExcelUtil<TeacherTmpInfo> util = new ExcelUtil<TeacherTmpInfo>(TeacherTmpInfo.class);
        util.exportExcel(response, list, "教师导入临时数据");
    }

    /**
     * 获取教师导入临时详细信息
     */
    @RequiresPermissions("system:info:query")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id)
    {
        return success(teacherTmpInfoService.selectTeacherTmpInfoById(id));
    }

    /**
     * 新增教师导入临时
     */
    @RequiresPermissions("system:info:add")
    @Log(title = "教师导入临时", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TeacherTmpInfo teacherTmpInfo)
    {
        return toAjax(teacherTmpInfoService.insertTeacherTmpInfo(teacherTmpInfo));
    }

    @PostMapping("/insertTeacherTmpInfo")
    public AjaxResult add() throws Exception {
        teacherTmpInfoService.saveTeacherTmpInfo();
        return success();
    }

    /**
     * 修改教师导入临时
     */
    @RequiresPermissions("system:info:edit")
    @Log(title = "教师导入临时", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TeacherTmpInfo teacherTmpInfo)
    {
        return toAjax(teacherTmpInfoService.updateTeacherTmpInfo(teacherTmpInfo));
    }

    /**
     * 删除教师导入临时
     */
    @RequiresPermissions("system:info:remove")
    @Log(title = "教师导入临时", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids)
    {
        return toAjax(teacherTmpInfoService.deleteTeacherTmpInfoByIds(ids));
    }
}
