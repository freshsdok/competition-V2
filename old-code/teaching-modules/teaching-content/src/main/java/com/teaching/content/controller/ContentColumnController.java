package com.teaching.content.controller;

import com.teaching.common.core.utils.poi.ExcelUtil;
import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.web.page.TableDataInfo;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.content.domain.ContentColumn;
import com.teaching.content.service.IContentColumnService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 内容栏目Controller
 *
 * @author teaching
 * @date 2025-12-10
 */
@RestController
@RequestMapping("/contentColumn")
public class ContentColumnController extends BaseController {
    @Autowired
    private IContentColumnService contentColumnService;

    /**
     * 查询内容栏目列表
     */
    @RequiresPermissions("content:column:list")
    @GetMapping("/list")
    public TableDataInfo list(ContentColumn contentColumn) {
        startPage();
        List<ContentColumn> list = contentColumnService.selectContentColumnList(contentColumn);
        return getDataTable(list);
    }

    /**
     * 导出内容栏目列表
     */
    @RequiresPermissions("content:column:export")
    @Log(title = "内容栏目", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ContentColumn contentColumn) {
        List<ContentColumn> list = contentColumnService.selectContentColumnList(contentColumn);
        ExcelUtil<ContentColumn> util = new ExcelUtil<ContentColumn>(ContentColumn.class);
        util.exportExcel(response, list, "内容栏目数据");
    }

    /**
     * 获取内容栏目详细信息
     */
    @RequiresPermissions("content:column:query")
    @GetMapping(value = "/{columnId}")
    public AjaxResult getInfo(@PathVariable("columnId") Long columnId) {
        return success(contentColumnService.selectContentColumnByColumnId(columnId));
    }

    /**
     * 根据菜单ID获取栏目信息（前端页面使用，无需权限验证）
     */
    @GetMapping("/getByMenuId/{menuId}")
    public AjaxResult getByMenuId(@PathVariable("menuId") Long menuId) {
        ContentColumn column = contentColumnService.selectContentColumnByMenuId(menuId);
        return success(column);
    }

    /**
     * 获取栏目树形结构（前端页面使用，无需权限验证）
     */
    @GetMapping("/tree")
    public AjaxResult tree(ContentColumn contentColumn) {
        List<ContentColumn> list = contentColumnService.selectContentColumnTree(contentColumn);
        return success(list);
    }

    /**
     * 新增内容栏目
     */
    @RequiresPermissions("content:column:add")
    @Log(title = "内容栏目", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody ContentColumn contentColumn) {
        return toAjax(contentColumnService.insertContentColumn(contentColumn));
    }

    /**
     * 修改内容栏目
     */
    @RequiresPermissions("content:column:edit")
    @Log(title = "内容栏目", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody ContentColumn contentColumn) {
        return toAjax(contentColumnService.updateContentColumn(contentColumn));
    }

    /**
     * 删除内容栏目
     */
    @RequiresPermissions("content:column:remove")
    @Log(title = "内容栏目", businessType = BusinessType.DELETE)
    @DeleteMapping("/{columnIds}")
    public AjaxResult remove(@PathVariable Long[] columnIds) {
        // 检查是否存在子栏目
        for (Long columnId : columnIds) {
            if (contentColumnService.hasChildByColumnId(columnId) > 0) {
                ContentColumn column = contentColumnService.selectContentColumnByColumnId(columnId);
                return error("栏目【" + (column != null ? column.getColumnName() : columnId) + "】存在子栏目，不允许删除");
            }
        }
        return toAjax(contentColumnService.deleteContentColumnByColumnIds(columnIds));
    }
}
