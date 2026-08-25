package com.teaching.content.controller;

import com.teaching.common.core.utils.poi.ExcelUtil;
import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.web.page.TableDataInfo;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.content.domain.ComponentLibraryInfo;
import com.teaching.content.service.IComponentLibraryInfoService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 组件库信息Controller
 *
 * @author teaching
 * @date 2025-10-13
 */
@RestController
@RequestMapping("/subassembly")
public class ComponentLibraryInfoController extends BaseController {
    @Autowired
    private IComponentLibraryInfoService componentLibraryInfoService;

    /**
     * 查询组件库信息列表
     */
    @RequiresPermissions("content:subassembly:list")
    @GetMapping("/list")
    public TableDataInfo list(ComponentLibraryInfo componentLibraryInfo) {
        startPage();
        List<ComponentLibraryInfo> list = componentLibraryInfoService.selectComponentLibraryInfoList(componentLibraryInfo);
        return getDataTable(list);
    }

    /**
     * 查询组件库信息列表 分类不分页查开启的
     */
    @GetMapping("/getList")
    public AjaxResult getList(ComponentLibraryInfo componentLibraryInfo) {
        componentLibraryInfo.setStatus("1");
        Map<String, List<ComponentLibraryInfo>> stringListMap = componentLibraryInfoService.selectComponentLibraryInfoListGroupClass(componentLibraryInfo);
        return success(stringListMap);
    }

    /**
     * 导出组件库信息列表
     */
    @RequiresPermissions("content:subassembly:export")
    @Log(title = "组件库信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ComponentLibraryInfo componentLibraryInfo) {
        List<ComponentLibraryInfo> list = componentLibraryInfoService.selectComponentLibraryInfoList(componentLibraryInfo);
        ExcelUtil<ComponentLibraryInfo> util = new ExcelUtil<ComponentLibraryInfo>(ComponentLibraryInfo.class);
        util.exportExcel(response, list, "组件库信息数据");
    }

    /**
     * 获取组件库信息详细信息
     */
    @RequiresPermissions("content:subassembly:query")
    @GetMapping(value = "/{componentId}")
    public AjaxResult getInfo(@PathVariable("componentId") String componentId) {
        return success(componentLibraryInfoService.selectComponentLibraryInfoByComponentId(componentId));
    }

    /**
     * 新增组件库信息
     */
    @RequiresPermissions("content:subassembly:add")
    @Log(title = "组件库信息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody ComponentLibraryInfo componentLibraryInfo) {
        return toAjax(componentLibraryInfoService.insertComponentLibraryInfo(componentLibraryInfo));
    }

    /**
     * 修改组件库信息
     */
    @RequiresPermissions("content:subassembly:edit")
    @Log(title = "组件库信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody ComponentLibraryInfo componentLibraryInfo) {
        componentLibraryInfo.setUpdateBy(SecurityUtils.getLoginUser().getSysUser().getNickName());
        return toAjax(componentLibraryInfoService.updateComponentLibraryInfo(componentLibraryInfo));
    }

    /**
     * 删除组件库信息
     */
    @RequiresPermissions("content:subassembly:remove")
    @Log(title = "组件库信息", businessType = BusinessType.DELETE)
    @DeleteMapping("/{componentIds}")
    public AjaxResult remove(@PathVariable String[] componentIds) {
        return toAjax(componentLibraryInfoService.deleteComponentLibraryInfoByComponentIds(componentIds));
    }
}
