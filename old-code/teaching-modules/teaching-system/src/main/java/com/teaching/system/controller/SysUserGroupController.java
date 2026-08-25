package com.teaching.system.controller;

import com.teaching.common.core.utils.poi.ExcelUtil;
import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.web.page.TableDataInfo;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.InnerAuth;
import com.teaching.common.security.annotation.Logical;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.system.domain.SysUserGroup;
import com.teaching.system.service.ISysUserGroupService;
import com.teaching.system.service.SysAsyncService;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * 用户组管理Controller
 *
 * @author teaching
 * @date 2026-01-07
 */
@RestController
@RequestMapping("/userGroup")
public class SysUserGroupController extends BaseController {
    @Autowired
    private ISysUserGroupService sysUserGroupService;
    @Autowired
    private SysAsyncService sysAsyncService;

    /**
     * 查询用户组管理列表
     * 用户组管理权限 或 文件任务管理新增权限
     */
    @RequiresPermissions(value = {"system:userGroup:list", "system:fileDistributeTask:add"}, logical = Logical.OR)
    @GetMapping("/list")
    public TableDataInfo list(SysUserGroup sysUserGroup) {
        //进入列表页，异步更新用户组下的人员
        sysAsyncService.getUserInfosByUserGroups(null, null);
        startPage();
        List<SysUserGroup> list = sysUserGroupService.selectSysUserGroupList(sysUserGroup);
        return getDataTable(list);
    }

    /**
     * 导出用户组管理列表
     */
    @RequiresPermissions("system:userGroup:export")
    @Log(title = "用户组管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, SysUserGroup sysUserGroup) {
        List<SysUserGroup> list = sysUserGroupService.selectSysUserGroupList(sysUserGroup);
        ExcelUtil<SysUserGroup> util = new ExcelUtil<SysUserGroup>(SysUserGroup.class);
        util.exportExcel(response, list, "用户组管理数据");
    }

    /**
     * 获取用户组管理详细信息
     */
    @RequiresPermissions("system:userGroup:query")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(sysUserGroupService.selectSysUserGroupById(id));
    }

    /**
     * 新增用户组管理
     */
    @RequiresPermissions("system:userGroup:add")
    @Log(title = "用户组管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody SysUserGroup sysUserGroup) {
        sysUserGroup.setCreateBy(SecurityUtils.getUserId() + "");
        return toAjax(sysUserGroupService.insertSysUserGroup(sysUserGroup));
    }

    /**
     * 修改用户组管理
     */
    @RequiresPermissions("system:userGroup:edit")
    @Log(title = "用户组管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody SysUserGroup sysUserGroup) {
        sysUserGroup.setUpdateBy(SecurityUtils.getUserId() + "");
        return toAjax(sysUserGroupService.updateSysUserGroup(sysUserGroup));
    }

    /**
     * 删除用户组管理
     */
    @RequiresPermissions("system:userGroup:remove")
    @Log(title = "用户组管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(sysUserGroupService.deleteSysUserGroupByIds(ids));
    }

    @InnerAuth
    @GetMapping("/updateUserIdsByUserGroup")
    public AjaxResult updateUserInfosByUserGroups(@RequestParam(required = false) Long groupId) {
        sysAsyncService.getUserInfosByUserGroups(null, groupId);
        return success();
    }

    /**
     * 根据用户组获取用户信息
     *
     * @param groupId
     * @return
     */
    @GetMapping("/pc/getUsers/{groupId}")
    public AjaxResult getUsers(@PathVariable Long groupId) {
        List<Map<String, Object>> userByUserGroup = sysUserGroupService.getUserByUserGroup(groupId);
        return success(userByUserGroup).put("total", CollectionUtils.isNotEmpty(userByUserGroup) ? userByUserGroup.size() : 0);
    }

    /**
     * 根据用户组ids获取用户组
     * @param groupIds
     * @return
     */
    @InnerAuth
    @GetMapping("/getGroupNames/ids")
    public AjaxResult getSysUserGroupByIds(@RequestParam List<Long> groupIds) {
        List<SysUserGroup> sysUserGroupByIds = sysUserGroupService.getSysUserGroupByIds(groupIds);
        String collect = sysUserGroupByIds.stream().map(SysUserGroup::getName).collect(Collectors.joining(","));
        return AjaxResult.success("查询成功",collect);
    }
}
