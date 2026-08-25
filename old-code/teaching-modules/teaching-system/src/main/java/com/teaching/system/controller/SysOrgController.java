package com.teaching.system.controller;

import com.teaching.common.core.constant.UserConstants;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.web.page.TableDataInfo;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.system.api.domain.SysOrg;
import com.teaching.system.service.ISysOrgService;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统机构信息Controller
 *
 * @author teaching
 * @date 2025-10-23
 */
@RestController
@RequestMapping("/org")
public class SysOrgController extends BaseController {
    @Autowired
    private ISysOrgService sysOrgService;

    /**
     * 查询系统机构信息列表
     */
    @RequiresPermissions("system:org:list")
    @GetMapping("/list")
    public TableDataInfo list(SysOrg sysOrg) {
        startPage();
        List<SysOrg> list = sysOrgService.selectSysOrgList(sysOrg);
        return getDataTable(list);
    }

    @GetMapping("/lists")
    public TableDataInfo lists(SysOrg sysOrg) {
        startPage();
        List<SysOrg> list = sysOrgService.selectSysOrgList(sysOrg);
        return getDataTable(list);
    }

    /**
     * 查询机构列表（正常状态的不分页）
     * @param sysOrg
     * @return
     */
    @RequiresPermissions("system:org:list")
    @GetMapping("/getList")
    public AjaxResult getList(SysOrg sysOrg) {
        sysOrg.setStatus("0");
        List<SysOrg> sysOrgs = sysOrgService.selectSysOrgList(sysOrg);
        return success(sysOrgs);
    }


    /**
     * 查询机构列表（排除节点）
     */
    @RequiresPermissions("system:org:list")
    @GetMapping("/list/exclude/{orgId}")
    public AjaxResult excludeChild(@PathVariable(value = "orgId", required = false) Long orgId) {
        List<SysOrg> sysOrgs = sysOrgService.selectSysOrgList(new SysOrg());
        sysOrgs.removeIf(d -> d.getOrgId().intValue() == orgId || ArrayUtils.contains(StringUtils.split(d.getAncestors(), ","), orgId + ""));
        return success(sysOrgs);
    }

    /**
     * 根据机构编号获取详细信息
     */
    @RequiresPermissions("system:org:query")
    @GetMapping(value = "/getOrgDetail/{orgId}")
    public AjaxResult getInfo(@PathVariable Long orgId) {
        sysOrgService.checkOrgDataScope(orgId);
        return success(sysOrgService.selectSysOrgByOrgId(orgId));
    }

    /**
     * 新增机构
     */
    @RequiresPermissions("system:org:add")
    @Log(title = "机构管理", businessType = BusinessType.INSERT)
    @PostMapping("/saveOrgInfo")
    public AjaxResult add(@Validated @RequestBody SysOrg org) {
        if (!sysOrgService.checkOrgNameUnique(org)) {
            return error("新增机构'" + org.getOrgName() + "'失败，机构名称已存在");
        }
        org.setCreateBy(SecurityUtils.getUsername());
        return toAjax(sysOrgService.insertSysOrg(org));
    }

    /**
     * 修改机构
     */
    @RequiresPermissions("system:org:edit")
    @Log(title = "机构管理", businessType = BusinessType.UPDATE)
    @PostMapping("/updateOrgInfo")
    public AjaxResult edit(@Validated @RequestBody SysOrg org) {
        Long orgId = org.getOrgId();
        sysOrgService.checkOrgDataScope(orgId);
        if (!sysOrgService.checkOrgNameUnique(org)) {
            return error("修改机构'" + org.getOrgName() + "'失败，机构名称已存在");
        } else if (org.getParentId() != null && org.getParentId().equals(orgId)) {
            return error("修改机构'" + org.getOrgName() + "'失败，上级机构不能是自己");
        } else if (StringUtils.equals(UserConstants.DEPT_DISABLE, org.getStatus()) && sysOrgService.selectNormalChildrenOrgById(orgId) > 0) {
            return error("该机构包含未停用的子机构！");
        }
        org.setUpdateBy(SecurityUtils.getUsername());
        return toAjax(sysOrgService.updateSysOrg(org));
    }

    /**
     * 删除机构
     */
    @RequiresPermissions("system:org:remove")
    @Log(title = "机构管理", businessType = BusinessType.DELETE)
    @GetMapping("/remove/{orgId}")
    public AjaxResult remove(@PathVariable Long orgId) {
        if (sysOrgService.hasChildByOrgId(orgId)) {
            return warn("存在下级机构,不允许删除");
        }
        if (sysOrgService.checkOrgExistUser(orgId)) {
            return warn("机构存在用户,不允许删除");
        }
        sysOrgService.checkOrgDataScope(orgId);
        return toAjax(sysOrgService.deleteSysOrgByOrgId(orgId));
    }
}
