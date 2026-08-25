package com.teaching.system.controller;


import com.teaching.common.core.domain.R;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.core.utils.ip.IpUtils;
import com.teaching.common.core.utils.sign.RsaUtils;
import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.redis.service.RedisService;
import com.teaching.common.security.annotation.InnerAuth;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.common.security.service.TokenService;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.system.api.RemoteFileService;
import com.teaching.system.api.domain.IdentityInfo;
import com.teaching.system.api.domain.SysUser;
import com.teaching.system.api.domain.UserAuthorization;
import com.teaching.system.api.model.LoginUser;
import com.teaching.system.domain.SysSenderMessageLog;
import com.teaching.system.mapper.SysUserMapper;
import com.teaching.system.service.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.teaching.common.core.web.domain.AjaxResult.error;

/**
 * 用户端服务
 */
@RestController
@RequestMapping("/personalCenter")
public class PersonalCenterController extends BaseController {

    // 判断用户名是邮箱还是手机号
    public static final String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
    // 手机号正则
    public static final String phoneRegex = "^1\\d{10}$";

    @Autowired
    private PersonalCenterService personalCenterService;

    @Autowired
    private ISysUserService userService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private RemoteFileService remoteFileService;

    @Autowired
    private IIdentityInfoService identityInfoService;

    @Autowired
    private ISysPermissionService permissionService;
    @Autowired
    private SysAsyncService sysAsyncService;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private RedisService redisService;

    @Autowired
    private ISysSenderMessageLogService senderMessageLogService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 已登录用户修改手机号或邮箱前的可用性检查。
     *
     * <p>匿名注册接口不返回账号是否存在；个人中心在认证后使用本接口，
     * 保留原有的即时查重体验。</p>
     */
    @GetMapping("/checkUserAccountAvailable")
    public AjaxResult checkUserAccountAvailable(SysUser user) {
        String account = user == null ? null : user.getUserName();
        if (StringUtils.isBlank(account)) {
            return success(false);
        }
        account = account.trim();

        SysUser candidate = new SysUser();
        candidate.setUserId(SecurityUtils.getUserId());
        if (account.matches(phoneRegex)) {
            candidate.setPhonenumber(account);
            return success(userService.checkPhoneUnique(candidate));
        }
        if (account.matches(emailRegex)) {
            candidate.setEmail(account);
            return success(userService.checkEmailUnique(candidate));
        }
        return success(false);
    }

    @Log(title = "修改用户信息", businessType = BusinessType.UPDATE)
    @PostMapping("/updateUserInfo")
    public AjaxResult updateUserInfo(@RequestBody SysUser user) {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        SysUser currentUser = loginUser.getSysUser();
        user.setUserId(currentUser.getUserId());
        if(personalCenterService.updateUserInfo(user)){
            // 更新缓存用户信息
            tokenService.setLoginUser(loginUser);
            return success();
        } else {
            return error("修改个人信息异常，请联系管理员");
        }
    }

    /**
     * 修改用户
     */
    @Log(title = "修改用户信息手机号邮箱", businessType = BusinessType.UPDATE)
    @PostMapping("/updateUserInfoPhoneOrEmail")
    public AjaxResult updateUserInfoPhoneOrEmail(@RequestBody SysUser user) {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        SysUser currentUser = loginUser.getSysUser();
        user.setUserId(currentUser.getUserId());
        // 验证手机号和邮箱验证码
        String cachedCode = "";
        if(com.teaching.common.core.utils.StringUtils.isNotEmpty(user.getPhonenumber()) && user.getPhonenumber().matches(phoneRegex)){
            // 校验手机号唯一性
            if (!userService.checkPhoneUnique(user)) {
                insertSenderMessageLog(currentUser.getNickName(),"","修改手机号","失败","验证手机失败，该手机号已被使用！");
                return error("验证手机失败，该手机号已被使用！");
            }
            cachedCode = redisService.getCacheObject("verificationCode_"+user.getPhonenumber());
        }

        // 校验邮箱唯一性
        if (com.teaching.common.core.utils.StringUtils.isNotEmpty(user.getEmail()) && user.getEmail().matches(emailRegex)) {
            if(!userService.checkEmailUnique(user)){
                insertSenderMessageLog(currentUser.getNickName(),"","修改邮箱","失败","验证邮箱失败，该邮箱已被使用！");
                return error("验证邮箱失败，该邮箱已被使用！");
            }
            // 从Redis中获取验证码
            ValueOperations valueOperations = redisTemplate.opsForValue();
            cachedCode = (String) valueOperations.get("EMAIL_CODE:" + user.getEmail());
        }

        if (com.teaching.common.core.utils.StringUtils.isEmpty(cachedCode)) {
            return error("验证码已过期，请重新获取");
        }
        if (!cachedCode.equals(user.getMsgCode())) {
            return error("验证码错误");
        }
        // 修改手机号和邮箱的时候进行筛重
        String redisKey = "";
        if(StringUtils.isNotBlank(user.getPhonenumber())){
            redisKey = "update_phone" + currentUser.getUserId();
            if(redisService.hasKey(redisKey)){
                String updateDate = redisService.getCacheObject(redisKey);
                String detail = "您已于"+updateDate.substring(0,4)+"年"+updateDate.substring(5,7)+"月"+updateDate.substring(8,10)+"日修改过手机号，" +
                        "本年度不允许再次修改。您可以在"+DateUtils.getNextYear()+"年1月1日后重新修改您的手机号";
                insertSenderMessageLog(currentUser.getNickName(),"","修改手机号","失败",detail);
                return error(detail);
            }
            insertSenderMessageLog(currentUser.getNickName(),"","修改手机号","成功","成功");
        }
        if(StringUtils.isNotBlank(user.getEmail())){
            redisKey = "update_email" + currentUser.getUserId();
            if(redisService.hasKey(redisKey)){
                String updateDate = redisService.getCacheObject(redisKey);
                String detail = "您已于"+updateDate.substring(0,4)+"年"+updateDate.substring(5,7)+"月"+updateDate.substring(8,10)+"日修改过邮箱，" +
                        "本年度不允许再次修改。您可以在"+DateUtils.getNextYear()+"年1月1日后重新修改您的邮箱";
                insertSenderMessageLog(currentUser.getNickName(),"","修改邮箱","失败",detail);
                return error(detail);
            }
            // 记录验证成功日志
            insertSenderMessageLog(currentUser.getNickName(),"","修改邮箱","成功","成功");
        }
        if (personalCenterService.updateUserInfo(user)) {
            // 修改手机号或邮箱一年只能改一次
            redisService.setCacheObject(redisKey, DateUtils.getDate(), DateUtils.getRemainingDays(), TimeUnit.DAYS);
            // 更新缓存用户信息
            tokenService.setLoginUser(loginUser);
            return success();
        }
        return error("修改个人信息异常，请联系管理员");
    }

    private void insertSenderMessageLog(String userName, String code, String type,String result,String reason){
        SysSenderMessageLog sysSenderMessageLog = new SysSenderMessageLog();
        sysSenderMessageLog.setVerifyType(type);
        sysSenderMessageLog.setVerifyCode(code);
        sysSenderMessageLog.setOperator(userName);
        sysSenderMessageLog.setOperationTime(DateUtils.getNowDate());
        sysSenderMessageLog.setVerifyResult(result);
        sysSenderMessageLog.setOperationIp(IpUtils.getIpAddr());
        sysSenderMessageLog.setReason(reason);
        senderMessageLogService.insertSysSenderMessageLog(sysSenderMessageLog);
    }

    /**
     * 重置密码
     */
    @Log(title = "个人信息", businessType = BusinessType.UPDATE)
    @PostMapping("/updatePwd")
    public AjaxResult updatePwd(@RequestBody Map<String, String> params) throws Exception {
        String oldPassword = RsaUtils.decryptByPrivateKey(params.get("oldPassword"));
        String newPassword = RsaUtils.decryptByPrivateKey(params.get("newPassword"));
        LoginUser loginUser = SecurityUtils.getLoginUser();
        Long userId = loginUser.getUserid();
        String password = loginUser.getSysUser().getPassword();
        if (!SecurityUtils.matchesPassword(oldPassword, password))
        {
            return error("修改密码失败，旧密码错误");
        }
        if (SecurityUtils.matchesPassword(newPassword, password))
        {
            return error("新密码不能与旧密码相同");
        }
        newPassword = SecurityUtils.encryptPassword(newPassword);
        if (userService.resetUserPwd(userId, newPassword) > 0)
        {
            // 更新缓存用户密码&密码最后更新时间
            loginUser.getSysUser().setPwdUpdateDate(DateUtils.getNowDate());
            loginUser.getSysUser().setPassword(newPassword);
            tokenService.setLoginUser(loginUser);
            return success();
        }
        return error("修改密码异常，请联系管理员");
    }

    @GetMapping("/getUserCenterInfo")
    public R<?> getUserCenterInfo(@RequestParam(required = false) Long userId) {
        if (userId == null) {
            SysUser sysUser = SecurityUtils.getLoginUser().getSysUser();
            userId = sysUser.getUserId();
        }
        SysUser userCenterInfo = personalCenterService.getUserCenterInfo(userId);
        // 角色集合
        Set<String> roles = permissionService.getRolePermission(userCenterInfo);
        // 权限集合
        Set<String> permissions = permissionService.getMenuPermission(userCenterInfo);
        userCenterInfo.setPermissions(permissions);
        //pc登录后判断登陆人所属用户组
        sysAsyncService.getUserGroupByLoginUser(userId);
        return R.ok(userCenterInfo);
    }

    @InnerAuth
    @GetMapping("/getInnerUserCenterInfo")
    public AjaxResult getInnerUserCenterInfo(@RequestParam(required = false) Long userId) {
        if (userId == null) {
            SysUser sysUser = SecurityUtils.getLoginUser().getSysUser();
            userId = sysUser.getUserId();
        }
        return success(personalCenterService.getUserCenterInfo(userId));
    }

    //批量获取用户信息
    @InnerAuth
    @PostMapping("/getUserCenterInfoList")
    public AjaxResult getUserCenterInfoList(@RequestBody List<Long> userIdList) {
        return success(personalCenterService.getUserCenterInfoList(userIdList));
    }

//    获取用户身份认证信息
    @GetMapping("/list")
    public AjaxResult list(IdentityInfo identityInfo) {
        Long userId = SecurityUtils.getLoginUser().getSysUser().getUserId();
        identityInfo.setUserId(userId);
        List<IdentityInfo> list = identityInfoService.selectUserIdentityInfoList(identityInfo);
        return success(list);
    }

    // 获取教师信息列表
    @GetMapping("/teacherList")
    public AjaxResult teacherList(@RequestParam String userName) {
        List<SysUser> list = personalCenterService.selectTeacherList(userName);
        return success(list);
    }

    /**
     * 根据学校ID获取教师列表
     * @param schoolId
     * @return
     */
    @GetMapping("/pc/getTeachers")
    public AjaxResult getTeachers(@RequestParam String schoolId) {
        return success(personalCenterService.getTeacherList(schoolId));
    }

    // 用户授权
    @InnerAuth
    @PostMapping("/saveUserAuthorization")
    public AjaxResult saveUserAuthorization(@RequestBody UserAuthorization userAuthorization) {
        personalCenterService.saveUserAuthorization(userAuthorization);
        return success();
    }
}
