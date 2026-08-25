package com.teaching.system.controller;

import java.util.Arrays;
import java.util.List;

import com.teaching.common.core.domain.R;
import com.teaching.common.core.exception.GlobalException;
import com.teaching.common.core.exception.ServiceException;
import com.teaching.common.security.annotation.Logical;
import com.teaching.system.api.domain.SysOrg;
import com.teaching.system.service.ISysOrgService;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.teaching.common.core.utils.poi.ExcelUtil;
import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.web.page.TableDataInfo;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.system.api.domain.SysRole;
import com.teaching.system.api.domain.SysUser;
import com.teaching.system.domain.SysUserRole;
import com.teaching.system.service.ISysRoleService;
import com.teaching.system.service.ISysUserService;

/**
 * 角色信息
 *
 * @author teaching
 */
@RestController
@RequestMapping("/role")
public class SysRoleController extends BaseController
{
    @Autowired
    private ISysRoleService roleService;

    @Autowired
    private ISysUserService userService;

    @Autowired
    private ISysOrgService sysOrgService;

    @RequiresPermissions(value = {"system:role:list", "workflow:model:list"}, logical = Logical.OR)
    @GetMapping("/list")
    public TableDataInfo list(SysRole role)
    {
        startPage();
        List<SysRole> list = roleService.selectRoleList(role);
        return getDataTable(list);
    }

    @Log(title = "角色管理", businessType = BusinessType.EXPORT)
    @RequiresPermissions("system:role:export")
    @PostMapping("/export")
    public void export(HttpServletResponse response, SysRole role)
    {
        List<SysRole> list = roleService.selectRoleList(role);
        ExcelUtil<SysRole> util = new ExcelUtil<SysRole>(SysRole.class);
        util.exportExcel(response, list, "角色数据");
    }

    /**
     * 根据角色编号获取详细信息
     */
    @RequiresPermissions("system:role:query")
    @GetMapping(value = "/{roleId}")
    public AjaxResult getInfo(@PathVariable Long roleId)
    {
        roleService.checkRoleDataScope(roleId);
        return success(roleService.selectRoleById(roleId));
    }

    @GetMapping("/byId/{roleId}")
    public R< SysRole> info(@PathVariable("roleId") Long roleId) {
        SysRole sysRole = roleService.selectRoleById(roleId);
        return R.ok(sysRole);
    }

    /**
     * 新增角色
     */
    @RequiresPermissions("system:role:add")
    @Log(title = "角色管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SysRole role)
    {
        if (!roleService.checkRoleNameUnique(role))
        {
            return error("新增角色'" + role.getRoleName() + "'失败，角色名称已存在");
        }
        else if (!roleService.checkRoleKeyUnique(role))
        {
            return error("新增角色'" + role.getRoleName() + "'失败，角色权限已存在");
        }
        // 独立角色同级别只能存在一个
        if(roleService.checkIndependentRoleUnique(role)){
            return error("新增角色'" + role.getRoleName() + "'失败，该角色为独立角色同级别只能存在一个");
        }
        role.setCreateBy(SecurityUtils.getUsername());
        return toAjax(roleService.insertRole(role));

    }

    /**
     * 修改保存角色
     */
    @RequiresPermissions("system:role:edit")
    @Log(title = "角色管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SysRole role)
    {
        roleService.checkRoleAllowed(role);
        roleService.checkRoleDataScope(role.getRoleId());
        if (!roleService.checkRoleNameUnique(role))
        {
            return error("修改角色'" + role.getRoleName() + "'失败，角色名称已存在");
        }
        else if (!roleService.checkRoleKeyUnique(role))
        {
            return error("修改角色'" + role.getRoleName() + "'失败，角色权限已存在");
        }
        // 独立角色同级别只能存在一个
        if(roleService.checkIndependentRoleUnique(role)){
            return error("修改角色'" + role.getRoleName() + "'失败，该角色为独立角色同级别只能存在一个");
        }
        SysRole sysRole = roleService.selectRoleById(role.getRoleId());
        if (sysRole.isLockFlag() && !SysUser.isAdmin(SecurityUtils.getUserId())) {
            throw new ServiceException("该角色是锁定的角色，不能修改，只能超级管理员可以修改");
        }
        role.setUpdateBy(SecurityUtils.getUsername());
        return toAjax(roleService.updateRole(role));
    }

    /**
     * 修改保存数据权限
     */
    @RequiresPermissions("system:role:edit")
    @Log(title = "角色管理", businessType = BusinessType.UPDATE)
    @PutMapping("/dataScope")
    public AjaxResult dataScope(@RequestBody SysRole role)
    {
        roleService.checkRoleAllowed(role);
        roleService.checkRoleDataScope(role.getRoleId());
        return toAjax(roleService.authDataScope(role));
    }

    /**
     * 状态修改
     */
    @RequiresPermissions("system:role:edit")
    @Log(title = "角色管理", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public AjaxResult changeStatus(@RequestBody SysRole role)
    {
        roleService.checkRoleAllowed(role);
        roleService.checkRoleDataScope(role.getRoleId());
        role.setUpdateBy(SecurityUtils.getUsername());
        return toAjax(roleService.updateRoleStatus(role));
    }

    /**
     * 删除角色
     */
    @RequiresPermissions("system:role:remove")
    @Log(title = "角色管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{roleIds}")
    public AjaxResult remove(@PathVariable Long[] roleIds)
    {
        return toAjax(roleService.deleteRoleByIds(roleIds));
    }

    /**
     * 获取角色选择框列表
     */
    @RequiresPermissions("system:role:query")
    @GetMapping("/optionselect")
    public AjaxResult optionselect()
    {
        return success(roleService.selectRoleAll());
    }
    /**
     * 查询已分配用户角色列表
     */
    @RequiresPermissions("system:role:list")
    @GetMapping("/authUser/allocatedList")
    public TableDataInfo allocatedList(SysUser user)
    {
        startPage();
        List<SysUser> list = userService.selectAllocatedList(user);
        return getDataTable(list);
    }

    /**
     * 查询未分配用户角色列表
     */
    @RequiresPermissions("system:role:list")
    @GetMapping("/authUser/unallocatedList")
    public TableDataInfo unallocatedList(SysUser user)
    {
        startPage();
        List<SysUser> list = userService.selectUnallocatedList(user);
        return getDataTable(list);
    }

    /**
     * 取消授权用户
     */
    @RequiresPermissions("system:role:edit")
    @Log(title = "角色管理", businessType = BusinessType.GRANT)
    @PutMapping("/authUser/cancel")
    public AjaxResult cancelAuthUser(@RequestBody SysUserRole userRole)
    {
        // 锁定角色，用户锁定后角色不能修改，只能超级管理员可以取消
        SysRole sysRole = roleService.selectRoleById(userRole.getRoleId());
        if (sysRole.isLockFlag() && !SysUser.isAdmin(SecurityUtils.getUserId())) {
            return error("该角色已锁定，请勿操作,只能超级管理员可取消");
        }
        return toAjax(roleService.deleteAuthUser(userRole));
    }

    /**
     * 批量取消授权用户
     */
    @RequiresPermissions("system:role:edit")
    @Log(title = "角色管理", businessType = BusinessType.GRANT)
    @PutMapping("/authUser/cancelAll")
    public AjaxResult cancelAuthUserAll(Long roleId, Long[] userIds)
    {
        // 锁定角色，用户锁定后角色不能修改，只能超级管理员可以取消
        List<Long> list = Arrays.stream(userIds).toList();
        if(CollectionUtils.isNotEmpty(list)){
            for (Long userId : list){
                SysRole sysRole = roleService.selectRoleById(userId);
                if (sysRole != null && sysRole.isLockFlag() && !SysUser.isAdmin(SecurityUtils.getUserId())) {
                    return error("存在已锁定角色，请排除锁定角色,锁定角色只能超级管理员可取消");
                }
            }
        }
        return toAjax(roleService.deleteAuthUsers(roleId, userIds));
    }

    /**
     * 批量选择用户授权
     */
    @RequiresPermissions("system:role:edit")
    @Log(title = "角色管理", businessType = BusinessType.GRANT)
    @PutMapping("/authUser/selectAll")
    public AjaxResult selectAuthUserAll(Long roleId, Long[] userIds)
    {
        roleService.checkRoleDataScope(roleId);
        // 如果该角色是独立角色，则其他非独立角色将失效
        SysRole sysRole = roleService.selectRoleById(roleId);
        if (sysRole.isExclusionFlag()) {
            Arrays.stream(userIds).toList().forEach(userId -> {
                List<SysRole> roles = roleService.selectRolesByUserId(userId);
                roles.forEach(role -> {
                    if (!role.isExclusionFlag()) {
                        SysUserRole userRole = new SysUserRole();
                        userRole.setRoleId(role.getRoleId());
                        userRole.setUserId(userId);
                        roleService.deleteAuthUser(userRole);
                    }
                });
            });
        }
        // 如果用户存在独立角色则不能添加
        Arrays.stream(userIds).toList().forEach(userId -> {
            List<SysRole> roles = roleService.selectRolesByUserId(userId);
            if(CollectionUtils.isNotEmpty(roles)){
                roles.stream().forEach(role -> {
                    if (role.isFlag() && role.isExclusionFlag()) {
                        throw new GlobalException("该用户已存在独立角色，不能添加其他角色");
                    }
                });
            }
        });
        return toAjax(roleService.insertAuthUsers(roleId, userIds));
    }

    /**
     * 获取对应角色机构列表
     */
    @RequiresPermissions("system:role:query")
    @GetMapping("/orgTree/{roleId}")
    public AjaxResult orgTree(@PathVariable("roleId") Long roleId)
    {
        AjaxResult ajax = AjaxResult.success();
        ajax.put("checkedKeys", sysOrgService.selectSysOrgListByRoleId(roleId));
        ajax.put("orgs", sysOrgService.selectOrgTreeList(new SysOrg()));
        return success();
    }
}
