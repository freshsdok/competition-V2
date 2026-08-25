package com.teaching.system.controller;

import com.teaching.common.core.utils.poi.ExcelUtil;
import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.web.page.TableDataInfo;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.system.domain.SysAuditMainConfig;
import com.teaching.system.service.ISysAuditMainConfigService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统审核流程配置Controller
 *
 * @author teaching
 * @date 2025-10-15
 */
@RestController
@RequestMapping("/audit")
public class SysAuditMainConfigController extends BaseController {
    @Autowired
    private ISysAuditMainConfigService sysAuditMainConfigService;

    /**
     * 查询系统审核配置列表
     */
    @RequiresPermissions("system:audit:list")
    @GetMapping("/list")
    public TableDataInfo list(SysAuditMainConfig sysAuditMainConfig) {
        startPage();
        List<SysAuditMainConfig> list = sysAuditMainConfigService.selectSysAuditMainCofigList(sysAuditMainConfig);
        return getDataTable(list);
    }

    /**
     * 导出系统审核配置列表
     */
    @RequiresPermissions("system:audit:export")
    @Log(title = "系统审核配置", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, SysAuditMainConfig sysAuditMainConfig) {
        List<SysAuditMainConfig> list = sysAuditMainConfigService.selectSysAuditMainCofigList(sysAuditMainConfig);
        ExcelUtil<SysAuditMainConfig> util = new ExcelUtil<SysAuditMainConfig>(SysAuditMainConfig.class);
        util.exportExcel(response, list, "系统审核配置数据");
    }

    /**
     * 获取系统审核配置详细信息
     */
    @RequiresPermissions("system:audit:query")
    @GetMapping(value = "/{auditId}")
    public AjaxResult getInfo(@PathVariable("auditId") Long auditId) {
        return success(sysAuditMainConfigService.selectSysAuditMainConfigByAuditId(auditId));
    }

    /**
     * 新增系统审核配置
     */
    @RequiresPermissions("system:audit:add")
    @Log(title = "系统审核配置", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SysAuditMainConfig sysAuditMainConfig) {
        return toAjax(sysAuditMainConfigService.insertSysAuditMainConfig(sysAuditMainConfig));
    }

    /**
     * 复制系统审核配置
     */
    @RequiresPermissions("system:audit:add")
    @Log(title = "系统审核配置", businessType = BusinessType.INSERT)
    @GetMapping("/copy/{auditId}")
    public AjaxResult copy(@PathVariable Long auditId) {
        return toAjax(sysAuditMainConfigService.copySysAuditMainConfig(auditId));
    }

    /**
     * 修改系统审核配置
     */
    @RequiresPermissions("system:audit:edit")
    @Log(title = "系统审核配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody SysAuditMainConfig sysAuditMainConfig) {
        sysAuditMainConfig.setUpdateBy(SecurityUtils.getLoginUser().getSysUser().getNickName());
        return toAjax(sysAuditMainConfigService.updateSysAuditMainConfig(sysAuditMainConfig));
    }

    /**
     * 启用/停用系统审核流程配置
     */
    @RequiresPermissions("system:audit:edit")
    @Log(title = "系统审核配置", businessType = BusinessType.UPDATE)
    @PutMapping("/enableOrDeactivate")
    public AjaxResult enableOrDeactivate(@RequestBody SysAuditMainConfig sysAuditMainConfig) {
        sysAuditMainConfig.setUpdateBy(SecurityUtils.getLoginUser().getSysUser().getNickName());
        return toAjax(sysAuditMainConfigService.enableOrDeactivate(sysAuditMainConfig));
    }

    /**
     * 删除系统审核配置
     */
    @RequiresPermissions("system:audit:remove")
    @Log(title = "系统审核配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/{auditIds}")
    public AjaxResult remove(@PathVariable Long[] auditIds) {
        return toAjax(sysAuditMainConfigService.deleteSysAuditMainConfigByAuditIds(auditIds));
    }
}
