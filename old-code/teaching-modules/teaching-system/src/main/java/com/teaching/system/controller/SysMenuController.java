package com.teaching.system.controller;

import com.teaching.common.core.constant.UserConstants;
import com.teaching.common.core.constant.HttpStatus;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.system.api.domain.SysRoleMenu;
import com.teaching.system.domain.SysMenu;
import com.teaching.system.domain.vo.TreeSelectPc;
import com.teaching.system.service.ISysMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 菜单信息
 *
 * @author teaching
 */
@RestController
@RequestMapping("/menu")
public class SysMenuController extends BaseController {
    @Autowired
    private ISysMenuService menuService;

    /**
     * 获取菜单列表
     */
    @RequiresPermissions("system:menu:list")
    @GetMapping("/list")
    public AjaxResult list(SysMenu menu) {
        Long userId = SecurityUtils.getUserId();
        List<SysMenu> menus = menuService.selectMenuList(menu, userId);
        return success(menus);
    }

    /**
     * pc 获取菜单列表
     *
     * @param menu
     * @return
     */
    @GetMapping("/pc/list")
    public AjaxResult pcList(SysMenu menu) {
        return success(menuService.selectMenuPcList(menu));
    }

    /**
     * 根据菜单编号获取详细信息
     */
    @RequiresPermissions("system:menu:query")
    @GetMapping(value = "/{menuId}")
    public AjaxResult getInfo(@PathVariable Long menuId) {
        return success(menuService.selectMenuById(menuId));
    }

    /**
     * 根据菜单编号获取详细信息  pc
     *
     * @param menuId
     * @return
     */
    @GetMapping(value = "/pc/{menuId}")
    public AjaxResult getPcInfo(@PathVariable Long menuId) {
        Map<String, Object> menuInfo = menuService.selectMenuInfoById(menuId);
        if (menuInfo == null || menuInfo.isEmpty()) {
            return AjaxResult.error(HttpStatus.NOT_FOUND, "资源不存在");
        }
        return success(menuInfo);
    }

    /**
     * 根据栏目详情编号获取详细信息
     *
     * @param detailId
     * @return
     */
    @GetMapping(value = "/pc/detail/{detailId}")
    public AjaxResult getDetailById(@PathVariable Long detailId) {
        Map<String, Object> detail = menuService.selectDetailById(detailId);
        if (detail == null || detail.isEmpty()) {
            return AjaxResult.error(HttpStatus.NOT_FOUND, "资源不存在");
        }
        return success(detail);
    }

    /**
     * 获取菜单下拉树列表
     */
    @GetMapping("/treeselect")
    public AjaxResult treeselect(SysMenu menu) {
        Long userId = SecurityUtils.getUserId();
        List<SysMenu> menus = menuService.selectMenuList(menu, userId);
        return success(menuService.buildMenuTreeSelect(menus));
    }

    /**
     * 获取菜单下拉树列表 不包含pc端菜单
     *
     * @param menu
     * @return
     */
    @GetMapping("/treeselectNoPc")
    public AjaxResult selectMenuListNoPc(SysMenu menu) {
        List<SysMenu> menus = menuService.selectMenuListNoPc(menu);
        return success(menuService.buildMenuTreeSelect(menus));
    }

    /**
     * 获取菜单下拉树列表
     */
    @GetMapping("/pcTreeselect")
    public AjaxResult pcTreeselect(SysMenu menu) {
        Long userId = SecurityUtils.getUserId();
        List<SysMenu> menus = menuService.selectPcMenuList(menu, userId);
        return success(menuService.buildMenuTreeSelect(menus));
    }

    /**
     * 加载对应角色菜单列表树
     */
    @GetMapping(value = "/roleMenuTreeselect/{roleId}")
    public AjaxResult roleMenuTreeselect(@PathVariable("roleId") Long roleId) {
        Long userId = SecurityUtils.getUserId();
        List<SysMenu> menus = menuService.selectMenuList(userId);
        AjaxResult ajax = AjaxResult.success();
        ajax.put("checkedKeys", menuService.selectMenuListByRoleId(roleId));
        List<SysRoleMenu> sysRoleMenus = menuService.selectMenuListByRoleId(roleId);
        if (StringUtils.isNotEmpty(menus) && StringUtils.isNotEmpty(sysRoleMenus)) {
            menus.stream().forEach(SysMenu -> {
                sysRoleMenus.stream().forEach(SysRoleMenu -> {
                    if (SysMenu.getMenuId().equals(SysRoleMenu.getMenuId())) {
                        SysMenu.setDataScope(SysRoleMenu.getDataScope());
                    }
                });
            });
        }
        ajax.put("menus", menuService.buildMenuTreeSelect(menus));
        return ajax;
    }
    /**
     * 加载对应角色菜单列表树
     */
    @GetMapping(value = "/roleMenuATreeSelect/{roleId}")
    public AjaxResult roleMenuATreeSelect(@PathVariable("roleId") Long roleId) {
        Long userId = SecurityUtils.getUserId();
        List<SysMenu> menus = menuService.selectMenuList(userId);
        List<SysMenu> list = menus.stream().filter(e -> "admin".equals(e.getPlatformType())).toList();
        AjaxResult ajax = AjaxResult.success();
        ajax.put("checkedKeys", menuService.selectMenuListByRoleId(roleId));
        List<SysRoleMenu> sysRoleMenus = menuService.selectMenuListByRoleId(roleId);
        if (StringUtils.isNotEmpty(list) && StringUtils.isNotEmpty(sysRoleMenus)) {
            list.forEach(SysMenu -> {
                sysRoleMenus.stream().forEach(SysRoleMenu -> {
                    if (SysMenu.getMenuId().equals(SysRoleMenu.getMenuId())) {
                        SysMenu.setDataScope(SysRoleMenu.getDataScope());
                    }
                });
            });
        }
        ajax.put("menus", menuService.buildMenuTreeSelect(list));
        return ajax;
    }

    /**
     * 新增菜单
     */
    @RequiresPermissions("system:menu:add")
    @Log(title = "菜单管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SysMenu menu) {
        if (!menuService.checkMenuNameUnique(menu)) {
            return error("新增菜单'" + menu.getMenuName() + "'失败，菜单名称已存在");
        } else if (UserConstants.YES_FRAME.equals(menu.getIsFrame()) && !StringUtils.ishttp(menu.getPath())) {
            return error("新增菜单'" + menu.getMenuName() + "'失败，地址必须以http(s)://开头");
        }
        menu.setCreateBy(SecurityUtils.getUsername());
        return toAjax(menuService.insertMenu(menu));
    }

    /**
     * 修改菜单
     */
    @RequiresPermissions("system:menu:edit")
    @Log(title = "菜单管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SysMenu menu) {
        if (!menuService.checkMenuNameUnique(menu)) {
            return error("修改菜单'" + menu.getMenuName() + "'失败，菜单名称已存在");
        } else if (UserConstants.YES_FRAME.equals(menu.getIsFrame()) && !StringUtils.ishttp(menu.getPath())) {
            return error("修改菜单'" + menu.getMenuName() + "'失败，地址必须以http(s)://开头");
        } else if (menu.getMenuId().equals(menu.getParentId())) {
            return error("修改菜单'" + menu.getMenuName() + "'失败，上级菜单不能选择自己");
        }
        menu.setUpdateBy(SecurityUtils.getUsername());
        return toAjax(menuService.updateMenu(menu));
    }

    /**
     * 删除菜单
     */
    @RequiresPermissions("system:menu:remove")
    @Log(title = "菜单管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{menuId}")
    public AjaxResult remove(@PathVariable("menuId") Long menuId) {
        if (menuService.hasChildByMenuId(menuId)) {
            return warn("存在子菜单,不允许删除");
        }
        if (menuService.checkMenuExistRole(menuId)) {
            return warn("菜单已分配,不允许删除");
        }
        return toAjax(menuService.deleteMenuById(menuId));
    }

    /**
     * 获取路由信息
     *
     * @return 路由信息
     */
    @GetMapping("getRouters")
    public AjaxResult getRouters() {
//        Long userId = SecurityUtils.getUserId();
        Long userId = SecurityUtils.getLoginUser().getUserid();
        List<SysMenu> menus = menuService.selectMenuTreeByUserId(userId);
        return success(menuService.buildMenus(menus));
    }
}
