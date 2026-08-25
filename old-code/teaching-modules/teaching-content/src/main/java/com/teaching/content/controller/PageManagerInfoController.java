package com.teaching.content.controller;

import com.teaching.common.core.utils.poi.ExcelUtil;
import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.web.page.TableDataInfo;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.content.service.IPageManagerInfoService;
import com.teaching.system.api.domain.PageInfo;
import com.teaching.system.api.domain.PageManagerInfo;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 页面管理信息Controller
 *
 * @author teaching
 * @date 2025-10-14
 */
@RestController
@RequestMapping("/page")
public class PageManagerInfoController extends BaseController {
    @Autowired
    private IPageManagerInfoService pageManagerInfoService;

    /**
     * 查询页面管理信息列表
     */
    @RequiresPermissions("content:page:list")
    @GetMapping("/list")
    public TableDataInfo list(PageManagerInfo pageManagerInfo) {
        startPage();
        List<PageManagerInfo> list = pageManagerInfoService.selectPageManagerInfoList(pageManagerInfo);
        return getDataTable(list);
    }

    /**
     * 导出页面管理信息列表
     */
    @RequiresPermissions("content:page:export")
    @Log(title = "页面管理信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, PageManagerInfo pageManagerInfo) {
        List<PageManagerInfo> list = pageManagerInfoService.selectPageManagerInfoList(pageManagerInfo);
        ExcelUtil<PageManagerInfo> util = new ExcelUtil<PageManagerInfo>(PageManagerInfo.class);
        util.exportExcel(response, list, "页面管理信息数据");
    }

    /**
     * 获取页面管理信息详细信息
     */
    @RequiresPermissions("content:page:query")
    @GetMapping(value = "/{pageId}")
    public AjaxResult getInfo(@PathVariable("pageId") Long pageId) {
        return success(pageManagerInfoService.selectPageManagerInfoByPageId(pageId));
    }

    /**
     * 新增页面管理信息
     */
    @RequiresPermissions("content:page:add")
    @Log(title = "页面管理信息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody PageManagerInfo pageManagerInfo) {
        return toAjax(pageManagerInfoService.insertPageManagerInfo(pageManagerInfo));
    }

    /**
     * copy页面管理信息
     */
    @RequiresPermissions("content:page:copy")
    @Log(title = "页面管理信息", businessType = BusinessType.INSERT)
    @GetMapping("/copy/{pageId}")
    public AjaxResult copy(@PathVariable Long pageId) {
        return toAjax(pageManagerInfoService.copyPageManagerInfo(pageId));
    }

    /**
     * 修改页面基本信息
     */
    @RequiresPermissions("content:page:editBasic")
    @Log(title = "页面管理信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody PageManagerInfo pageManagerInfo) {
        pageManagerInfo.setUpdateBy(SecurityUtils.getLoginUser().getSysUser().getNickName());
        return toAjax(pageManagerInfoService.updatePageManagerBaseInfo(pageManagerInfo));
    }

    /**
     * 修改页面内容信息
     */
    @RequiresPermissions("content:page:editContent")
    @Log(title = "页面管理信息", businessType = BusinessType.UPDATE)
    @PutMapping("/editContent")
    public AjaxResult editPageContent(@Validated @RequestBody PageManagerInfo pageManagerInfo) {
        pageManagerInfo.setUpdateBy(SecurityUtils.getLoginUser().getSysUser().getNickName());
        return toAjax(pageManagerInfoService.updatePageManagerContentInfo(pageManagerInfo));
    }

    /**
     * 修改页面管理审核状态
     */
    @Log(title = "页面管理信息", businessType = BusinessType.UPDATE)
    @PostMapping("/updateStatus")
    public AjaxResult updateStatus(@RequestBody PageInfo pageManagerInfo) {
        return success(pageManagerInfoService.updatePageManagerStatus(pageManagerInfo));
    }

    /**
     * 删除页面管理信息
     */
    @RequiresPermissions("content:page:remove")
    @Log(title = "页面管理信息", businessType = BusinessType.DELETE)
    @DeleteMapping
    public AjaxResult remove(@RequestBody PageManagerInfo pageInfo) {
        return toAjax(pageManagerInfoService.deletePageManagerInfoByPageId(pageInfo));
    }


    /**
     * 根据类型和url查询页面管理信息
     *
     * @param pt  pc/mini
     * @param url url
     * @return 详情
     */
    @GetMapping(value = "/pc/{pt}/{url}")
    public AjaxResult getInfoByTypeAndUrl(@PathVariable("pt") String pt, @PathVariable("url") String url) {
        return success(pageManagerInfoService.getInfoByTypeAndUrl(pt, url));
    }

    /**
     * 根据类型和url查询页面管理信息，处理url中有/的情况
     *
     * @param pt  pc/mini
     * @param url url
     * @return 详情
     */
    @GetMapping(value = "/pc")
    public AjaxResult getInfoByTypeAndUrl2(@RequestParam("pt") String pt, @RequestParam("url") String url) {
        return success(pageManagerInfoService.getInfoByTypeAndUrl(pt, url));
    }

    /**
     * C端页面预览
     *
     * @param id pageId
     * @return 详情
     */
    @GetMapping(value = "/pc/{id}")
    public AjaxResult getInfoById(@PathVariable("id") Long id) {
        return success(pageManagerInfoService.selectPageManagerInfoByPageId(id));
    }
}
