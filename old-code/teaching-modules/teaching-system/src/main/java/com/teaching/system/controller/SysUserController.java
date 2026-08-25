package com.teaching.system.controller;

import com.teaching.common.core.constant.CacheConstants;
import com.teaching.common.core.constant.TdConstants;
import com.teaching.common.core.domain.R;
import com.teaching.common.core.text.Convert;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.common.core.utils.poi.ExcelUtil;
import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.web.page.TableDataInfo;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.redis.service.RedisService;
import com.teaching.common.security.annotation.InnerAuth;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.common.security.service.TokenService;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.system.api.domain.SysOrg;
import com.teaching.system.api.domain.SysRole;
import com.teaching.system.api.domain.SysUser;
import com.teaching.system.api.model.LoginUser;
import com.teaching.system.domain.SysUserOnline;
import com.teaching.system.service.*;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户信息
 *
 * @author teaching
 */
@RestController
@RequestMapping("/user")
public class SysUserController extends BaseController {
    @Autowired
    private ISysUserService userService;

    @Autowired
    private ISysRoleService roleService;

    @Autowired
    private ISysPostService postService;

    @Autowired
    private ISysPermissionService permissionService;

    @Autowired
    private ISysConfigService configService;

    @Autowired
    private TokenService tokenService;
    @Autowired
    private SysAsyncService sysAsyncService;

    @Autowired
    private ISysOrgService sysOrgService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private ISysUserOnlineService userOnlineService;

    /**
     * 获取用户列表
     */
    @RequiresPermissions("system:user:list")
    @GetMapping("/list")
    public TableDataInfo list(SysUser user) {
        startPage();
        List<SysUser> list = userService.selectUserList(user);
        return getDataTable(list);
    }

    /**
     * 获取用户列表，附带身份认证类型和实名认证状态
     *
     * @param user
     * @return
     */
    @RequiresPermissions("system:user:list")
    @GetMapping("/userList")
    public TableDataInfo userList(SysUser user) {
        return userService.selectUserInfoList(user);
    }
    @GetMapping("/getUserList")
    public TableDataInfo getUserList(SysUser user) {
        user.setUserType("0");
        user.setStatus("0");
        return userService.selectUserInfoList(user);
    }

    @Log(title = "用户管理", businessType = BusinessType.EXPORT)
    @RequiresPermissions("system:user:export")
    @PostMapping("/export")
    public void export(HttpServletResponse response, SysUser user) {
        List<SysUser> list = userService.getSysUserList(user);
        ExcelUtil<SysUser> util = new ExcelUtil<SysUser>(SysUser.class);
        util.exportExcel(response, list, "用户数据");
    }

    @Log(title = "用户管理", businessType = BusinessType.IMPORT)
    @RequiresPermissions("system:user:import")
    @PostMapping("/importData")
    public AjaxResult importData(MultipartFile file, boolean updateSupport) throws Exception {
        ExcelUtil<SysUser> util = new ExcelUtil<SysUser>(SysUser.class);
        List<SysUser> userList = util.importExcel(file.getInputStream());
        String operName = SecurityUtils.getUsername();
        String message = userService.importUser(userList, updateSupport, operName);
        return success(message);
    }

    @PostMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response) throws IOException {
        ExcelUtil<SysUser> util = new ExcelUtil<SysUser>(SysUser.class);
        util.importTemplateExcel(response, "用户数据");
    }

    /**
     * 获取当前用户信息
     */
//    @InnerAuth
    @GetMapping("/info/{username}")
    public R<LoginUser> info(@PathVariable("username") String username) {
        SysUser sysUser = userService.selectUserByUserName(username);
        if (StringUtils.isNull(sysUser)) {
            return R.fail("用户名或密码错误");
        }
        // 角色集合
        Set<String> roles = permissionService.getRolePermission(sysUser);
        // 权限集合
        Set<String> permissions = permissionService.getMenuPermission(sysUser);
        // 角色菜单数据权限
        permissionService.getRoleMenuList(sysUser);
        LoginUser sysUserVo = new LoginUser();
        sysUserVo.setSysUser(sysUser);
        sysUserVo.setRoles(roles);
        sysUserVo.setPermissions(permissions);
        return R.ok(sysUserVo);
    }

    @InnerAuth
    @GetMapping("/byNick/{nickName}")
    public R<SysUser> selectUserByNickName(@PathVariable("nickName") String nickName) {
        SysUser sysUser = userService.selectUserByNickName(nickName);
        return R.ok(sysUser);
    }
    @GetMapping("/byId/{userId}")
    public R<SysUser> info(@PathVariable("userId") Long userId) {
        SysUser sysUser = userService.selectUserById(userId);
        return R.ok(sysUser);
    }

    /**
     * 根据用户ID列表查询用户信息，供前端人员选择组件回显使用。
     */
    @GetMapping("/selectUserByIds/{userIds}")
    public AjaxResult selectUserByIds(@PathVariable("userIds") String userIds) {
        Long[] ids = Convert.toLongArray(userIds);
        if (ArrayUtils.isEmpty(ids)) {
            return success(Collections.emptyList());
        }
        return success(userService.selectUserByIds(Arrays.asList(ids)));
    }

    /**
     * 注册用户信息
     */
    @InnerAuth
    @PostMapping("/register")
    public R<Boolean> register(@RequestBody SysUser sysUser) {
        String username = sysUser.getUserName();
        if (!("true".equals(configService.selectConfigByKey("sys.account.registerUser")))) {
            return R.fail("当前系统没有开启注册功能！");
        }
        if (!userService.checkUserNameUnique(sysUser)) {
            return R.fail("保存用户'" + username + "'失败，注册账号已存在");
        }
        return R.ok(userService.registerUser(sysUser));
    }

    // 批量新增学生用户
    @InnerAuth
    @PostMapping("/saveStudentUserInfo")
    public AjaxResult saveUserInfo(@RequestBody List<SysUser> sysUserList) {
        return success(userService.saveUserInfo(sysUserList));
    }

    /**
     * 记录用户登录IP地址和登录时间
     */
    @InnerAuth
    @PutMapping("/recordlogin")
    public R<Boolean> recordlogin(@RequestBody SysUser sysUser) {
        return R.ok(userService.updateLoginInfo(sysUser));
    }

    /**
     * 获取用户信息
     *
     * @return 用户信息
     */
    @GetMapping("getInfo")
    public AjaxResult getInfo() {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        SysUser user = loginUser.getSysUser();
        // 角色菜单数据权限
        permissionService.getRoleMenuList(user);
        // 角色集合
        Set<String> roles = permissionService.getRolePermission(user);
        // 权限集合
        Set<String> permissions = permissionService.getMenuPermission(user);
        if (!loginUser.getPermissions().equals(permissions)) {
            loginUser.setPermissions(permissions);
            tokenService.refreshToken(loginUser);
        }
        AjaxResult ajax = AjaxResult.success();
        ajax.put("user", user);
        ajax.put("roles", roles);
        ajax.put("permissions", permissions);
        ajax.put("isDefaultModifyPwd", initPasswordIsModify(user.getPwdUpdateDate()));
        ajax.put("isPasswordExpired", passwordIsExpiration(user.getPwdUpdateDate()));
        //获取当前登录人可以审核的流程节点信息
        sysAsyncService.getCanAuditInfoByLoginUser();
        return ajax;
    }

    // 检查初始密码是否提醒修改
    public boolean initPasswordIsModify(Date pwdUpdateDate) {
        Integer initPasswordModify = Convert.toInt(configService.selectConfigByKey("sys.account.initPasswordModify"));
        return initPasswordModify != null && initPasswordModify == 1 && pwdUpdateDate == null;
    }

    // 检查密码是否过期
    public boolean passwordIsExpiration(Date pwdUpdateDate) {
        Integer passwordValidateDays = Convert.toInt(configService.selectConfigByKey("sys.account.passwordValidateDays"));
        if (passwordValidateDays != null && passwordValidateDays > 0) {
            if (StringUtils.isNull(pwdUpdateDate)) {
                // 如果从未修改过初始密码，直接提醒过期
                return true;
            }
            Date nowDate = DateUtils.getNowDate();
            return DateUtils.differentDaysByMillisecond(nowDate, pwdUpdateDate) > passwordValidateDays;
        }
        return false;
    }

    /**
     * 根据用户编号获取详细信息
     */
    @RequiresPermissions("system:user:query")
    @GetMapping(value = {"/", "/{userId}"})
    public AjaxResult getInfo(@PathVariable(value = "userId", required = false) Long userId) {
        AjaxResult ajax = AjaxResult.success();
        if (StringUtils.isNotNull(userId)) {
            userService.checkUserDataScope(userId);
            SysUser sysUser = userService.selectUserById(userId);
            ajax.put(AjaxResult.DATA_TAG, sysUser);
            ajax.put("postIds", postService.selectPostListByUserId(userId));
            ajax.put("roleIds", sysUser.getRoles().stream().map(SysRole::getRoleId).collect(Collectors.toList()));
        }
        List<SysRole> roles = roleService.selectRoleAll();
        ajax.put("roles", SysUser.isAdmin(userId) ? roles : roles.stream().filter(r -> !r.isAdmin()).collect(Collectors.toList()));
        ajax.put("posts", postService.selectPostAll());
        return ajax;
    }

    /**
     * 新增用户
     */
    @RequiresPermissions("system:user:add")
    @Log(title = "用户管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SysUser user) {
        sysOrgService.checkOrgDataScope(user.getOrgId());
        roleService.checkRoleDataScope(user.getRoleIds());
        if (!userService.checkUserNameUnique(user)) {
            return error("新增用户'" + user.getUserName() + "'失败，登录账号已存在");
        } else if (StringUtils.isNotEmpty(user.getPhonenumber()) && !userService.checkPhoneUnique(user)) {
            return error("新增用户'" + user.getUserName() + "'失败，手机号码已存在");
        } else if (StringUtils.isNotEmpty(user.getEmail()) && !userService.checkEmailUnique(user)) {
            return error("新增用户'" + user.getUserName() + "'失败，邮箱账号已存在");
        }
        user.setCreateBy(SecurityUtils.getUsername());
        user.setPassword(SecurityUtils.encryptPassword(user.getPassword()));
        user.setUserSources(TdConstants.USER_SOURCES_ADMIN);
        return toAjax(userService.insertUser(user));
    }

    /**
     * 修改用户
     */
    @RequiresPermissions("system:user:edit")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SysUser user) {
        userService.checkUserAllowed(user);
        userService.checkUserDataScope(user.getUserId());
        sysOrgService.checkOrgDataScope(user.getOrgId());
        roleService.checkRoleDataScope(user.getRoleIds());
        if (!userService.checkUserNameUnique(user)) {
            return error("修改用户'" + user.getUserName() + "'失败，登录账号已存在");
        } else if (StringUtils.isNotEmpty(user.getPhonenumber()) && !userService.checkPhoneUnique(user)) {
            return error("修改用户'" + user.getUserName() + "'失败，手机号码已存在");
        } else if (StringUtils.isNotEmpty(user.getEmail()) && !userService.checkEmailUnique(user)) {
            return error("修改用户'" + user.getUserName() + "'失败，邮箱账号已存在");
        }
        user.setUpdateBy(SecurityUtils.getUsername());
        return toAjax(userService.updateUser(user));
    }

    /**
     * 删除用户
     */
    @RequiresPermissions("system:user:remove")
    @Log(title = "用户管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{userIds}")
    public AjaxResult remove(@PathVariable Long[] userIds) {
        if (ArrayUtils.contains(userIds, SecurityUtils.getUserId())) {
            return error("当前用户不能删除");
        }
        // 查询用户name，进行会话强退
        Arrays.stream(userIds).toList().forEach(userId -> {
            SysUser sysUser = userService.selectUserById(userId);
            Collection<String> keys = redisService.keys(CacheConstants.LOGIN_TOKEN_KEY + "*");
            for (String key : keys) {
                LoginUser user = redisService.getCacheObject(key);
                if (Objects.nonNull(sysUser) && StringUtils.isNotEmpty(sysUser.getUserName()) && user.getUsername().equals(sysUser.getUserName())) {
                    SysUserOnline sysUserOnline = userOnlineService.selectOnlineByUserName(sysUser.getUserName(), user);
                    if (Objects.nonNull(sysUserOnline)) {
                        redisService.deleteObject(CacheConstants.LOGIN_TOKEN_KEY + sysUserOnline.getTokenId());
                    }
                }
            }
        });
        return toAjax(userService.deleteUserByIds(userIds));
    }

    @InnerAuth
    @PostMapping("/updateApplyInfoUser")
    public AjaxResult updateApplyInfoUser(@RequestBody SysUser user){
        return success(userService.updateApplyInfoUser(user));
    }

    @InnerAuth
    @PostMapping("/updateAddApplyInfoUser")
    public AjaxResult updateAddApplyInfoUser(@RequestBody List<SysUser> userList){
        return success(userService.updateAddApplyInfoUser(userList));
    }

    /**
     * 重置密码
     */
    @RequiresPermissions("system:user:edit")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PutMapping("/resetPwd")
    public AjaxResult resetPwd(@RequestBody SysUser user) {
        userService.checkUserAllowed(user);
        userService.checkUserDataScope(user.getUserId());
        user.setPassword(SecurityUtils.encryptPassword(user.getPassword()));
        user.setUpdateBy(SecurityUtils.getUsername());
        return toAjax(userService.resetPwd(user));
    }

    /**
     * 状态修改
     */
    @RequiresPermissions("system:user:edit")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public AjaxResult changeStatus(@RequestBody SysUser user) {
        userService.checkUserAllowed(user);
        userService.checkUserDataScope(user.getUserId());
        user.setUpdateBy(SecurityUtils.getUsername());
        return toAjax(userService.updateUserStatus(user));
    }

    /**
     * 根据用户编号获取授权角色
     */
    @RequiresPermissions("system:user:query")
    @GetMapping("/authRole/{userId}")
    public AjaxResult authRole(@PathVariable("userId") Long userId) {
        AjaxResult ajax = AjaxResult.success();
        SysUser user = userService.selectUserById(userId);
        List<SysRole> roles = roleService.selectRolesByUserId(userId);
        ajax.put("user", user);
        ajax.put("roles", SysUser.isAdmin(userId) ? roles : roles.stream().filter(r -> !r.isAdmin()).collect(Collectors.toList()));
        return ajax;
    }

    /**
     * 根据用户编号获取授权角色 角色区分是否是独立角色
     */
    @RequiresPermissions("system:user:query")
    @GetMapping("/authRole/{userId}/{exclusionFlag}")
    public AjaxResult authRoleAndExclusion(@PathVariable("userId") Long userId, @PathVariable("exclusionFlag") Boolean exclusionFlag) {
        AjaxResult ajax = AjaxResult.success();
        SysUser user = userService.selectUserById(userId);
        List<SysRole> roles = roleService.selectRolesByUserId(userId);
        ajax.put("user", user);
        List<SysRole> collect = roles.stream().filter(r -> !r.isAdmin() && exclusionFlag.equals(r.isExclusionFlag())).collect(Collectors.toList());
        ajax.put("roles", SysUser.isAdmin(userId) ? roles : collect);
        return ajax;
    }

    /**
     * 用户授权角色
     */
    @RequiresPermissions("system:user:edit")
    @Log(title = "用户管理", businessType = BusinessType.GRANT)
    @PutMapping("/authRole")
    public AjaxResult insertAuthRole(Long userId, Long[] roleIds) {
        userService.checkUserDataScope(userId);
        roleService.checkRoleDataScope(roleIds);
        //校验所选角色是否同时包含普通角色和独立角色  exclusion_flag 独立标识（1 是 0 否）
        roleService.checkIndependentRoleCoexistence(roleIds);
        userService.insertUserAuth(userId, roleIds);
        return success();
    }

    /**
     * 获取机构树列表
     */
    @RequiresPermissions("system:user:list")
    @GetMapping("/orgTree")
    public AjaxResult orgTree(SysOrg org) {
        return success(sysOrgService.selectOrgTreeList(org));
    }

    /**
     * 根据机构ID和角色ID查询用户列表
     *
     * @param orgId
     * @param roleId
     * @return
     */
    @GetMapping("/getUserList/{orgId}/{roleId}")
    public AjaxResult getUserListByOrgAndRoleId(@PathVariable("orgId") Long orgId, @PathVariable("roleId") Long roleId) {
        return success(userService.getUserListByOrgAndRoleId(orgId, roleId));
    }


    /**
     * 用户信息列表（用于用户组管理）
     *
     * @param map
     * @return
     */
    @GetMapping("/group/list")
    public TableDataInfo getUserListForUserGroup(@RequestParam Map<String, String> map) {
        startPage();
        List<Map<String, Object>> list = userService.getUserListForUserGroup(map);
        return getDataTable(list);
    }

    /**
     * 根据手机号或邮箱查用户
     * @param user
     * @return
     */
    @InnerAuth
    @PostMapping("/selectUserByPhoneCheck")
    public AjaxResult selectUserByPhoneCheck(@RequestBody SysUser user) {
        List<SysUser> sysUsers = userService.selectUserByPhoneCheck(user);
        return success(sysUsers);
    }

    /**
     * 注册微信用户信息
     */
    @InnerAuth
    @PostMapping("/registerWxSysUser")
    public R<Long> registerWxSysUser(@RequestBody SysUser sysUser) {
        return R.ok(userService.registerWxUser(sysUser));
    }

    /**
     * 查询微信用户信息
     */
    @InnerAuth
    @GetMapping("/queryWxSysUser")
    public R<SysUser> queryWxSysUser(@RequestParam String openId) {
        return R.ok(userService.selectWxUser(openId));
    }

    //刷新停用用户状态及密码
    @InnerAuth
    @GetMapping("/updateNoRegisterUser")
    public R<Void> updateNoRegisterUser(@RequestParam String updateSize) {
        userService.updateNoRegisterUser(updateSize);
        return R.ok();
    }
}
