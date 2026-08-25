package com.teaching.content.controller;

import com.teaching.common.core.utils.poi.ExcelUtil;
import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.web.page.TableDataInfo;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.content.domain.ContentBannerInfo;
import com.teaching.content.service.IContentBannerInfoService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * banner图管理Controller
 *
 * @author teaching
 * @date 2025-10-22
 */
@RestController
@RequestMapping("/bannerInfo")
public class ContentBannerInfoController extends BaseController {
    @Autowired
    private IContentBannerInfoService contentBannerInfoService;

    /**
     * 查询banner图管理列表
     */
    @RequiresPermissions("content:bannerInfo:list")
    @GetMapping("/list")
    public TableDataInfo list(ContentBannerInfo contentBannerInfo) {
        startPage();
        List<ContentBannerInfo> list = contentBannerInfoService.selectContentBannerInfoList(contentBannerInfo);
        return getDataTable(list);
    }

    /**
     * 导出banner图管理列表
     */
    @RequiresPermissions("content:bannerInfo:export")
    @Log(title = "banner图管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ContentBannerInfo contentBannerInfo) {
        List<ContentBannerInfo> list = contentBannerInfoService.selectContentBannerInfoList(contentBannerInfo);
        ExcelUtil<ContentBannerInfo> util = new ExcelUtil<ContentBannerInfo>(ContentBannerInfo.class);
        util.exportExcel(response, list, "banner图管理数据");
    }

    /**
     * 获取banner图管理详细信息
     */
    @RequiresPermissions("content:bannerInfo:query")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(contentBannerInfoService.selectContentBannerInfoById(id));
    }

    /**
     * 新增banner图管理
     */
    @RequiresPermissions("content:bannerInfo:add")
    @Log(title = "banner图管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody ContentBannerInfo contentBannerInfo) {
        return toAjax(contentBannerInfoService.insertContentBannerInfo(contentBannerInfo));
    }

    /**
     * 修改banner图管理
     */
    @RequiresPermissions("content:bannerInfo:edit")
    @Log(title = "banner图管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody ContentBannerInfo contentBannerInfo) {
        return toAjax(contentBannerInfoService.updateContentBannerInfo(contentBannerInfo));
    }

    /**
     * 删除banner图管理
     */
    @RequiresPermissions("content:bannerInfo:remove")
    @Log(title = "banner图管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(contentBannerInfoService.deleteContentBannerInfoByIds(ids));
    }


    /**
     * 获取banner图管理列表  PC页面使用
     *
     * @param contentBannerInfo
     * @return
     */
    @GetMapping("/pc/list")
    public AjaxResult getList(ContentBannerInfo contentBannerInfo) {
        List<Map<String, Object>> list = contentBannerInfoService.getContentBannerInfoListByPc(contentBannerInfo);
        return success(list);
    }
}
