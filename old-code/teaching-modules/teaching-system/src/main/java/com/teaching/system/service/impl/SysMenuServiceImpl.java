package com.teaching.system.service.impl;

import com.alibaba.fastjson2.JSON;
import com.teaching.common.core.constant.Constants;
import com.teaching.common.core.constant.SecurityConstants;
import com.teaching.common.core.constant.UserConstants;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.common.core.utils.ip.IpUtils;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.system.api.RemoteAuditLogService;
import com.teaching.system.api.domain.SysAuditLog;
import com.teaching.system.api.domain.SysRole;
import com.teaching.system.api.domain.SysRoleMenu;
import com.teaching.system.domain.SysMenu;
import com.teaching.system.domain.vo.MetaVo;
import com.teaching.system.domain.vo.RouterVo;
import com.teaching.system.domain.vo.TreeSelect;
import com.teaching.system.domain.vo.TreeSelectPc;
import com.teaching.system.mapper.SysMenuMapper;
import com.teaching.system.mapper.SysRoleMapper;
import com.teaching.system.mapper.SysRoleMenuMapper;
import com.teaching.system.service.ISysMenuService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 菜单 业务层处理
 *
 * @author teaching
 */
@Slf4j
@Service
public class SysMenuServiceImpl implements ISysMenuService {
    public static final String PREMISSION_STRING = "perms[\"{0}\"]";

    @Autowired
    private SysMenuMapper menuMapper;

    @Autowired
    private SysRoleMapper roleMapper;

    @Autowired
    private SysRoleMenuMapper roleMenuMapper;

    @Autowired(required = false)
    private RemoteAuditLogService remoteAuditLogService;

    /**
     * 根据用户查询系统菜单列表
     *
     * @param userId 用户ID
     * @return 菜单列表
     */
    @Override
    public List<SysMenu> selectMenuList(Long userId) {
        return selectMenuList(new SysMenu(), userId);
    }

    /**
     * 查询系统菜单列表
     *
     * @param menu 菜单信息
     * @return 菜单列表
     */
    @Override
    public List<SysMenu> selectMenuList(SysMenu menu, Long userId) {
        return menuMapper.selectMenuList(menu);
    }

    @Override
    public List<SysMenu> selectMenuListNoPc(SysMenu menu) {
        List<SysMenu> sysMenus = menuMapper.selectMenuList(menu);
        //只要platformType是admin的菜单
        return sysMenus.stream().filter(sysMenu -> "admin".equals(sysMenu.getPlatformType())).collect(Collectors.toList());
    }

    /**
     * 查询系统菜单列表 pc
     *
     * @param menu
     * @param userId
     * @return
     */
    @Override
    public List<SysMenu> selectPcMenuList(SysMenu menu, Long userId) {
        return menuMapper.selectPcMenuList(menu);
    }

    @Override
    public Map<String, List<TreeSelectPc>> selectMenuPcList(SysMenu menu) {
        Map<String, List<TreeSelectPc>> result = new HashMap<>();
        menu.setStatus("0");
        menu.setVisible("0");
        menu.setPlatformType("pc");
        List<SysMenu> sysMenus = menuMapper.selectMenuPcList(menu);
        //根据platformType分组
        Map<String, List<SysMenu>> collect = sysMenus.stream().collect(Collectors.groupingBy(SysMenu::getPlatformType));
        if (MapUtils.isNotEmpty(collect)) {
            for (String key : collect.keySet()) {
                List<SysMenu> menus = collect.get(key);
                result.put(key, buildMenuTreeSelect2(menus));
            }
        }
        return result;
    }

    /**
     * 根据用户ID查询权限
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    @Override
    public Set<String> selectMenuPermsByUserId(Long userId) {
        List<String> perms = menuMapper.selectMenuPermsByUserId(userId);
        Set<String> permsSet = new HashSet<>();
        for (String perm : perms) {
            if (StringUtils.isNotEmpty(perm)) {
                permsSet.addAll(Arrays.asList(perm.trim().split(",")));
            }
        }
        return permsSet;
    }

    /**
     * 根据角色ID查询权限
     *
     * @param roleId 角色ID
     * @return 权限列表
     */
    @Override
    public Set<String> selectMenuPermsByRoleId(Long roleId) {
        List<String> perms = menuMapper.selectMenuPermsByRoleId(roleId);
        Set<String> permsSet = new HashSet<>();
        for (String perm : perms) {
            if (StringUtils.isNotEmpty(perm)) {
                permsSet.addAll(Arrays.asList(perm.trim().split(",")));
            }
        }
        return permsSet;
    }

    /**
     * 根据用户ID查询菜单
     *
     * @param userId 用户名称
     * @return 菜单列表
     */
    @Override
    public List<SysMenu> selectMenuTreeByUserId(Long userId) {
        List<SysMenu> menus = null;
        if (SecurityUtils.isAdmin(userId)) {
            menus = menuMapper.selectMenuTreeAll();
        } else {
            menus = menuMapper.selectMenuTreeByUserId(userId);
        }
        return getChildPerms(menus, 0);
    }

    /**
     * 根据角色ID查询菜单树信息
     *
     * @param roleId 角色ID
     * @return 选中菜单列表
     */
    @Override
    public List<SysRoleMenu> selectMenuListByRoleId(Long roleId) {
        SysRole role = roleMapper.selectRoleById(roleId);
        role.setMenuCheckStrictly(false);
        return menuMapper.selectMenuListByRoleId(roleId, role.isMenuCheckStrictly());
    }

    /**
     * 构建前端路由所需要的菜单
     *
     * @param menus 菜单列表
     * @return 路由列表
     */
    @Override
    public List<RouterVo> buildMenus(List<SysMenu> menus) {
        List<RouterVo> routers = new LinkedList<RouterVo>();
        for (SysMenu menu : menus) {
            RouterVo router = new RouterVo();
            router.setHidden("1".equals(menu.getVisible()));
            router.setName(getRouteName(menu));
            router.setPath(getRouterPath(menu));
            router.setComponent(getComponent(menu));
            router.setQuery(menu.getQuery());
            router.setMeta(new MetaVo(menu.getMenuName(), menu.getIcon(), StringUtils.equals("1", menu.getIsCache()), menu.getPath()));
            List<SysMenu> cMenus = menu.getChildren();
            if (StringUtils.isNotEmpty(cMenus) && UserConstants.TYPE_DIR.equals(menu.getMenuType())) {
                router.setAlwaysShow(true);
                router.setRedirect("noRedirect");
                router.setChildren(buildMenus(cMenus));
            } else if (isMenuFrame(menu)) {
                router.setMeta(null);
                List<RouterVo> childrenList = new ArrayList<RouterVo>();
                RouterVo children = new RouterVo();
                children.setPath(menu.getPath());
                children.setComponent(menu.getComponent());
                children.setName(getRouteName(menu.getRouteName(), menu.getPath()));
                children.setMeta(new MetaVo(menu.getMenuName(), menu.getIcon(), StringUtils.equals("1", menu.getIsCache()), menu.getPath()));
                children.setQuery(menu.getQuery());
                childrenList.add(children);
                router.setChildren(childrenList);
            } else if (menu.getParentId().intValue() == 0 && isInnerLink(menu)) {
                router.setMeta(new MetaVo(menu.getMenuName(), menu.getIcon()));
                router.setPath("/");
                List<RouterVo> childrenList = new ArrayList<RouterVo>();
                RouterVo children = new RouterVo();
                String routerPath = innerLinkReplaceEach(menu.getPath());
                children.setPath(routerPath);
                children.setComponent(UserConstants.INNER_LINK);
                children.setName(getRouteName(menu.getRouteName(), routerPath));
                children.setMeta(new MetaVo(menu.getMenuName(), menu.getIcon(), menu.getPath()));
                childrenList.add(children);
                router.setChildren(childrenList);
            }
            routers.add(router);
        }
        return routers;
    }

    /**
     * 构建前端所需要树结构
     *
     * @param menus 菜单列表
     * @return 树结构列表
     */
    @Override
    public List<SysMenu> buildMenuTree(List<SysMenu> menus) {
        List<SysMenu> returnList = new ArrayList<SysMenu>();
        List<Long> tempList = menus.stream().map(SysMenu::getMenuId).collect(Collectors.toList());
        for (Iterator<SysMenu> iterator = menus.iterator(); iterator.hasNext(); ) {
            SysMenu menu = (SysMenu) iterator.next();
            // 如果是顶级节点, 遍历该父节点的所有子节点
            if (!tempList.contains(menu.getParentId())) {
                recursionFn(menus, menu);
                returnList.add(menu);
            }
        }
        if (returnList.isEmpty()) {
            returnList = menus;
        }
        return returnList;
    }

    /**
     * 构建前端所需要下拉树结构
     *
     * @param menus 菜单列表
     * @return 下拉树结构列表
     */
    @Override
    public List<TreeSelect> buildMenuTreeSelect(List<SysMenu> menus) {
        List<SysMenu> menuTrees = buildMenuTree(menus);
        return menuTrees.stream().map(TreeSelect::new).collect(Collectors.toList());
    }

    /**
     * 构建前端所需要下拉树结构
     *
     * @param menus 菜单列表
     * @return 下拉树结构列表
     */
    @Override
    public List<TreeSelectPc> buildMenuTreeSelect2(List<SysMenu> menus) {
        List<SysMenu> menuTrees = buildMenuTree(menus);
        return menuTrees.stream().map(TreeSelectPc::new).collect(Collectors.toList());
    }

    /**
     * 根据菜单ID查询信息
     *
     * @param menuId 菜单ID
     * @return 菜单信息
     */
    @Override
    public SysMenu selectMenuById(Long menuId) {
        return menuMapper.selectMenuById(menuId);
    }

    /**
     * 根据菜单ID查询 栏目的详情信息
     *
     * @param menuId
     * @return
     */
    @Override
    public Map<String, Object> selectMenuInfoById(Long menuId) {
        Map<String, Object> column = menuMapper.selectColumnByMenuId(menuId);
        if (MapUtils.isEmpty(column)) {
            return Collections.emptyMap();
        }

        Map<String, Object> result = new LinkedHashMap<>();
        String columnType = MapUtils.getString(column, "columnType");
        Long columnId = MapUtils.getLong(column, "columnId");
        result.put("menuName", MapUtils.getString(column, "menuName"));
        result.put("columnType", columnType);
        if ("1".equals(columnType) || "3".equals(columnType)) {
            //content_detail 1列表
            List<Map<String, Object>> details = menuMapper.selectDetailByColumnId(columnId);
            result.put("detailList", details.stream()
                    .map(detail -> publicDetail(detail, "3".equals(columnType)))
                    .collect(Collectors.toList()));
        }
        if ("2".equals(columnType) || "4".equals(columnType)) {
            //content_file 2列表
            List<Map<String, Object>> files = menuMapper.selectFileByColumnId(columnId);
            result.put("detailList", files.stream()
                    .map(SysMenuServiceImpl::publicFile)
                    .collect(Collectors.toList()));
        }
        if (!result.containsKey("detailList")) {
            result.put("detailList", Collections.emptyList());
        }
        return result;
    }

    @Override
    public Map<String, Object> selectDetailById(Long detailId) {
        Map<String, Object> detail = menuMapper.selectDetailById(detailId);
        return MapUtils.isEmpty(detail) ? Collections.emptyMap() : publicDetailContent(detail);
    }

    private static Map<String, Object> publicDetail(Map<String, Object> source, boolean includeContent) {
        Map<String, Object> result = new LinkedHashMap<>();
        copyPresent(source, result, "detailId", "detailTitle", "detailImage", "orderNum");
        if (includeContent) {
            copyPresent(source, result, "detailContent");
        }
        return result;
    }

    private static Map<String, Object> publicDetailContent(Map<String, Object> source) {
        Map<String, Object> result = new LinkedHashMap<>();
        copyPresent(source, result, "detailContent", "detailImage");
        return result;
    }

    private static Map<String, Object> publicFile(Map<String, Object> source) {
        Map<String, Object> result = new LinkedHashMap<>();
        copyPresent(source, result, "fileId", "fileName", "fileUrl", "fileType", "orderNum");
        return result;
    }

    private static void copyPresent(Map<String, Object> source, Map<String, Object> target,
                                    String... fields) {
        for (String field : fields) {
            if (source.containsKey(field)) {
                target.put(field, source.get(field));
            }
        }
    }

    /**
     * 是否存在菜单子节点
     *
     * @param menuId 菜单ID
     * @return 结果
     */
    @Override
    public boolean hasChildByMenuId(Long menuId) {
        int result = menuMapper.hasChildByMenuId(menuId);
        return result > 0;
    }

    /**
     * 查询菜单使用数量
     *
     * @param menuId 菜单ID
     * @return 结果
     */
    @Override
    public boolean checkMenuExistRole(Long menuId) {
        int result = roleMenuMapper.checkMenuExistRole(menuId);
        return result > 0;
    }

    /**
     * 新增保存菜单信息
     *
     * @param menu 菜单信息
     * @return 结果
     */
    @Override
    public int insertMenu(SysMenu menu) {
        int result = menuMapper.insertMenu(menu);
        if (result > 0) {
            // 记录菜单新增审计日志
            recordMenuAudit("新增菜单权限", "新增系统菜单", null, menu, "新增");
        }
        return result;
    }

    /**
     * 修改保存菜单信息
     *
     * @param menu 菜单信息
     * @return 结果
     */
    @Override
    public int updateMenu(SysMenu menu) {
        // 获取修改前的菜单信息
        SysMenu oldMenu = menuMapper.selectMenuById(menu.getMenuId());
        int result = menuMapper.updateMenu(menu);
        if (result > 0) {
            // 记录菜单修改审计日志
            recordMenuAudit("修改菜单权限", "修改系统菜单", oldMenu, menu, "修改");
        }
        return result;
    }

    /**
     * 删除菜单管理信息
     *
     * @param menuId 菜单ID
     * @return 结果
     */
    @Override
    public int deleteMenuById(Long menuId) {
        // 获取删除前的菜单信息
        SysMenu menu = menuMapper.selectMenuById(menuId);
        int result = menuMapper.deleteMenuById(menuId);
        if (result > 0) {
            // 记录菜单删除审计日志
            recordMenuAudit("删除菜单权限", "删除系统菜单", menu, null, "删除");
        }
        return result;
    }

    /**
     * 校验菜单名称是否唯一
     *
     * @param menu 菜单信息
     * @return 结果
     */
    @Override
    public boolean checkMenuNameUnique(SysMenu menu) {
        Long menuId = StringUtils.isNull(menu.getMenuId()) ? -1L : menu.getMenuId();
        SysMenu info = menuMapper.checkMenuNameUnique(menu.getPlatformType(), menu.getMenuName(), menu.getParentId());
        if (StringUtils.isNotNull(info) && info.getMenuId().longValue() != menuId.longValue()) {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 获取路由名称
     *
     * @param menu 菜单信息
     * @return 路由名称
     */
    public String getRouteName(SysMenu menu) {
        // 非外链并且是一级目录（类型为目录）
        if (isMenuFrame(menu)) {
            return StringUtils.EMPTY;
        }
        return getRouteName(menu.getRouteName(), menu.getPath());
    }

    /**
     * 获取路由名称，如没有配置路由名称则取路由地址
     *
     * @param name 路由名称
     * @param path 路由地址
     * @return 路由名称（驼峰格式）
     */
    public String getRouteName(String name, String path) {
        String routerName = StringUtils.isNotEmpty(name) ? name : path;
        return StringUtils.capitalize(routerName);
    }

    /**
     * 获取路由地址
     *
     * @param menu 菜单信息
     * @return 路由地址
     */
    public String getRouterPath(SysMenu menu) {
        String routerPath = menu.getPath();
        // 内链打开外网方式
        if (menu.getParentId().intValue() != 0 && isInnerLink(menu)) {
            routerPath = innerLinkReplaceEach(routerPath);
        }
        // 非外链并且是一级目录（类型为目录）
        if (0 == menu.getParentId().intValue() && UserConstants.TYPE_DIR.equals(menu.getMenuType())
                && UserConstants.NO_FRAME.equals(menu.getIsFrame())) {
            routerPath = "/" + menu.getPath();
        }
        // 非外链并且是一级目录（类型为菜单）
        else if (isMenuFrame(menu)) {
            routerPath = "/";
        }
        return routerPath;
    }

    /**
     * 获取组件信息
     *
     * @param menu 菜单信息
     * @return 组件信息
     */
    public String getComponent(SysMenu menu) {
        String component = UserConstants.LAYOUT;
        if (StringUtils.isNotEmpty(menu.getComponent()) && !isMenuFrame(menu)) {
            component = menu.getComponent();
        } else if (StringUtils.isEmpty(menu.getComponent()) && menu.getParentId().intValue() != 0 && isInnerLink(menu)) {
            component = UserConstants.INNER_LINK;
        } else if (StringUtils.isEmpty(menu.getComponent()) && isParentView(menu)) {
            component = UserConstants.PARENT_VIEW;
        }
        return component;
    }

    /**
     * 是否为菜单内部跳转
     *
     * @param menu 菜单信息
     * @return 结果
     */
    public boolean isMenuFrame(SysMenu menu) {
        return menu.getParentId().intValue() == 0 && UserConstants.TYPE_MENU.equals(menu.getMenuType())
                && menu.getIsFrame().equals(UserConstants.NO_FRAME);
    }

    /**
     * 是否为内链组件
     *
     * @param menu 菜单信息
     * @return 结果
     */
    public boolean isInnerLink(SysMenu menu) {
        return menu.getIsFrame().equals(UserConstants.NO_FRAME) && StringUtils.ishttp(menu.getPath());
    }

    /**
     * 是否为parent_view组件
     *
     * @param menu 菜单信息
     * @return 结果
     */
    public boolean isParentView(SysMenu menu) {
        return menu.getParentId().intValue() != 0 && UserConstants.TYPE_DIR.equals(menu.getMenuType());
    }

    /**
     * 根据父节点的ID获取所有子节点
     *
     * @param list     分类表
     * @param parentId 传入的父节点ID
     * @return String
     */
    public List<SysMenu> getChildPerms(List<SysMenu> list, int parentId) {
        List<SysMenu> returnList = new ArrayList<SysMenu>();
        List<SysMenu> list1 = list.stream().filter(sysMenu -> "admin".equals(sysMenu.getPlatformType())).toList();
        for (Iterator<SysMenu> iterator = list1.iterator(); iterator.hasNext(); ) {
            SysMenu t = (SysMenu) iterator.next();
            // 一、根据传入的某个父节点ID,遍历该父节点的所有子节点
            if (t.getParentId() == parentId) {
                recursionFn(list1, t);
                returnList.add(t);
            }
        }
        return returnList;
    }

    /**
     * 递归列表
     *
     * @param list 分类表
     * @param t    子节点
     */
    private void recursionFn(List<SysMenu> list, SysMenu t) {
        // 得到子节点列表
        List<SysMenu> childList = getChildList(list, t);
        t.setChildren(childList);
        for (SysMenu tChild : childList) {
            if (hasChild(list, tChild)) {
                recursionFn(list, tChild);
            }
        }
    }

    /**
     * 得到子节点列表
     */
    private List<SysMenu> getChildList(List<SysMenu> list, SysMenu t) {
        List<SysMenu> tlist = new ArrayList<SysMenu>();
        Iterator<SysMenu> it = list.iterator();
        while (it.hasNext()) {
            SysMenu n = (SysMenu) it.next();
            if (n.getParentId().longValue() == t.getMenuId().longValue()) {
                tlist.add(n);
            }
        }
        return tlist;
    }

    /**
     * 判断是否有子节点
     */
    private boolean hasChild(List<SysMenu> list, SysMenu t) {
        return getChildList(list, t).size() > 0;
    }

    /**
     * 内链域名特殊字符替换
     *
     * @return 替换后的内链域名
     */
    public String innerLinkReplaceEach(String path) {
        return StringUtils.replaceEach(path, new String[]{Constants.HTTP, Constants.HTTPS, Constants.WWW, ".", ":"},
                new String[]{"", "", "", "/", "/"});
    }

    /**
     * 记录菜单权限审计日志
     *
     * @param eventName     事件名称
     * @param eventDesc     事件描述
     * @param oldMenu       修改前的菜单
     * @param newMenu       修改后的菜单
     * @param operationType 操作类型
     */
    private void recordMenuAudit(String eventName, String eventDesc, SysMenu oldMenu, SysMenu newMenu, String operationType) {
        try {
            if (remoteAuditLogService == null) {
                return;
            }

            SysAuditLog auditLog = new SysAuditLog();
            auditLog.setAuditType("权限审计");
            auditLog.setAuditCategory("权限变更");
            auditLog.setEventName(eventName);
            auditLog.setEventDesc(eventDesc);

            // 用户信息
            try {
                Long userId = SecurityUtils.getUserId();
                String username = SecurityUtils.getUsername();
                auditLog.setUserId(userId);
                auditLog.setUserName(username);
            } catch (Exception e) {
                // 获取用户信息失败时继续执行
            }

            // 操作信息
            auditLog.setOperationType(operationType);
            auditLog.setOperationModule("菜单管理");

            // 获取请求信息
            HttpServletRequest request = getRequest();
            if (request != null) {
                auditLog.setRequestUrl(request.getRequestURI());
                auditLog.setRequestMethod(request.getMethod());
            }

            // 安全信息
            auditLog.setIpAddress(IpUtils.getIpAddr());

            // 数据变更信息
            SysMenu menu = newMenu != null ? newMenu : oldMenu;
            if (menu != null) {
                auditLog.setDataId(String.valueOf(menu.getMenuId()));
                auditLog.setDataType("系统菜单");

                // 记录变更前后的菜单信息
                if (oldMenu != null) {
                    auditLog.setOldValue(buildMenuDesc(oldMenu));
                }
                if (newMenu != null) {
                    auditLog.setNewValue(buildMenuDesc(newMenu));
                }
            }

            // 风险级别 - 菜单权限修改属于中等风险
            auditLog.setRiskLevel("MEDIUM");

            // 审计状态
            auditLog.setAuditStatus("0"); // 待审计
            auditLog.setIsAbnormal("0");
            auditLog.setOperationTime(new Date());

            // 异步保存审计日志
            remoteAuditLogService.saveAuditLog(auditLog, SecurityConstants.INNER);
        } catch (Exception e) {
            // 记录审计日志失败不影响主业务
        }
    }

    /**
     * 构建菜单描述
     *
     * @param menu 菜单信息
     * @return 菜单描述
     */
    private String buildMenuDesc(SysMenu menu) {
        if (menu == null) {
            return "";
        }

        return JSON.toJSONString(new Object() {
            public final Long menuId = menu.getMenuId();
            public final String menuName = menu.getMenuName();
            public final String menuType = menu.getMenuType();
            public final String perms = menu.getPerms();
            public final String path = menu.getPath();
            public final String component = menu.getComponent();
            public final String visible = menu.getVisible();
            public final String status = menu.getStatus();
        });
    }

    /**
     * 获取当前HTTP请求
     *
     * @return HttpServletRequest
     */
    private HttpServletRequest getRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            return null;
        }
    }
}
