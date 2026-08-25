package com.teaching.content.controller;

import com.teaching.common.core.utils.poi.ExcelUtil;
import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.web.page.TableDataInfo;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.content.domain.ContentDetail;
import com.teaching.content.service.IContentDetailService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 内容详情Controller
 *
 * @author teaching
 * @date 2025-12-10
 */
@RestController
@RequestMapping("/contentDetail")
public class ContentDetailController extends BaseController {
    @Autowired
    private IContentDetailService contentDetailService;

    /**
     * 查询内容详情列表
     */
    @RequiresPermissions("content:detail:list")
    @GetMapping("/list")
    public TableDataInfo list(ContentDetail contentDetail) {
        startPage();
        List<ContentDetail> list = contentDetailService.selectContentDetailList(contentDetail);
        return getDataTable(list);
    }

    /**
     * 导出内容详情列表
     */
    @RequiresPermissions("content:detail:export")
    @Log(title = "内容详情", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ContentDetail contentDetail) {
        List<ContentDetail> list = contentDetailService.selectContentDetailList(contentDetail);
        ExcelUtil<ContentDetail> util = new ExcelUtil<ContentDetail>(ContentDetail.class);
        util.exportExcel(response, list, "内容详情数据");
    }

    /**
     * 获取内容详情详细信息
     */
    @RequiresPermissions("content:detail:query")
    @GetMapping(value = "/{detailId}")
    public AjaxResult getInfo(@PathVariable("detailId") Long detailId) {
        return success(contentDetailService.selectContentDetailByDetailId(detailId));
    }

    /**
     * 根据栏目ID获取详情（前端页面使用，无需权限验证）
     */
    @GetMapping("/getByColumnId/{columnId}")
    public AjaxResult getByColumnId(@PathVariable("columnId") Long columnId) {
        ContentDetail detail = contentDetailService.selectContentDetailByColumnId(columnId);
        return success(detail);
    }

    /**
     * 新增内容详情
     */
    @RequiresPermissions("content:detail:add")
    @Log(title = "内容详情", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody ContentDetail contentDetail) {
        return toAjax(contentDetailService.insertContentDetail(contentDetail));
    }

    /**
     * 修改内容详情
     */
    @RequiresPermissions("content:detail:add")
    @Log(title = "内容详情", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody ContentDetail contentDetail) {
        return toAjax(contentDetailService.updateContentDetail(contentDetail));
    }

    /**
     * 删除内容详情
     */
    @RequiresPermissions("content:detail:remove")
    @Log(title = "内容详情", businessType = BusinessType.DELETE)
    @DeleteMapping("/{detailIds}")
    public AjaxResult remove(@PathVariable Long[] detailIds) {
        return toAjax(contentDetailService.deleteContentDetailByDetailIds(detailIds));
    }

    /**
     * 获取"维护公告"信息
     *
     * @return
     */
    @GetMapping("/pc/getNotices")
    public AjaxResult getNotices() {
        return success(contentDetailService.getNotices());
    }
}
