package com.teaching.wxApp.controller;

import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.web.page.TableDataInfo;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.wxApp.domain.WxQcCodeConfig;
import com.teaching.wxApp.service.IWxQcCodeConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 二维码配置Controller
 *
 * @author teaching
 * @date 2026-04-08
 */
@RestController
@RequestMapping("/wxQcCodeConfig")
public class WxQcCodeConfigController extends BaseController {

    @Autowired
    private IWxQcCodeConfigService wxQcCodeConfigService;

    /**
     * 查询二维码配置列表
     */
    @RequiresPermissions("wxApp:wxQcCodeConfig:list")
    @GetMapping("/list")
    public TableDataInfo list(WxQcCodeConfig wxQcCodeConfig) {
        startPage();
        List<WxQcCodeConfig> list = wxQcCodeConfigService.selectWxQcCodeConfigList(wxQcCodeConfig);
        return getDataTable(list);
    }

    /**
     * 导出二维码配置列表
     */
    @RequiresPermissions("wxApp:wxQcCodeConfig:export")
    @Log(title = "二维码配置", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public AjaxResult export(WxQcCodeConfig wxQcCodeConfig) {
        List<WxQcCodeConfig> list = wxQcCodeConfigService.selectWxQcCodeConfigList(wxQcCodeConfig);
        return success(list);
    }

    /**
     * 获取二维码配置详细信息
     */
    @RequiresPermissions("wxApp:wxQcCodeConfig:query")
    @GetMapping(value = "/{codeConfigId}")
    public AjaxResult getInfo(@PathVariable("codeConfigId") Long codeConfigId) {
        return success(wxQcCodeConfigService.selectWxQcCodeConfigByCodeConfigId(codeConfigId));
    }

    /**
     * 新增二维码配置
     */
    @RequiresPermissions("wxApp:wxQcCodeConfig:add")
    @Log(title = "二维码配置", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody WxQcCodeConfig wxQcCodeConfig) {
        return toAjax(wxQcCodeConfigService.insertWxQcCodeConfig(wxQcCodeConfig));
    }

    /**
     * 修改二维码配置
     */
    @RequiresPermissions("wxApp:wxQcCodeConfig:edit")
    @Log(title = "二维码配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody WxQcCodeConfig wxQcCodeConfig) {
        return toAjax(wxQcCodeConfigService.updateWxQcCodeConfig(wxQcCodeConfig));
    }

    /**
     * 删除二维码配置
     */
    @RequiresPermissions("wxApp:wxQcCodeConfig:remove")
    @Log(title = "二维码配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/{codeConfigIds}")
    public AjaxResult remove(@PathVariable Long[] codeConfigIds) {
        return toAjax(wxQcCodeConfigService.deleteWxQcCodeConfigByCodeConfigIds(codeConfigIds));
    }
}
