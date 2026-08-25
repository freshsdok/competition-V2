package com.teaching.system.controller;

import com.alibaba.fastjson2.JSONObject;
import com.teaching.common.core.constant.Constants;
import com.teaching.common.core.constant.TdConstants;
import com.teaching.common.core.domain.R;
import com.teaching.common.core.exception.GlobalException;
import com.teaching.common.core.sms.SmsUtil;
import com.teaching.common.core.utils.ClientInfoUtils;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.common.core.utils.ip.IpUtils;
import com.teaching.common.core.utils.sign.RsaUtils;
import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.redis.service.RedisService;
import com.teaching.common.security.service.TokenService;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.system.api.domain.AuthInfo;
import com.teaching.system.api.domain.IdentityInfo;
import com.teaching.system.api.domain.SysLogininfor;
import com.teaching.system.api.domain.SysUser;
import com.teaching.system.api.model.LoginUser;
import com.teaching.system.domain.SysSenderMessageLog;
import com.teaching.system.security.PcCaptchaRateLimiter;
import com.teaching.system.security.PcLoginClientIpResolver;
import com.teaching.system.security.PcLoginRateLimiter;
import com.teaching.system.service.*;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 用户认证接口（面向PC端和小程序端用户）
 *
 * @author teaching
 */
@RestController
@RequestMapping("/auth")
public class UserAuthController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(UserAuthController.class);
    private static final String LOGIN_FAILURE_MESSAGE = "用户名或密码错误";
    private static final String RESET_FAILURE_MESSAGE = "账号或验证码无效";
    private static final String CAPTCHA_DISPATCH_MESSAGE =
            "如果账号信息有效，验证码将发送至对应邮箱或手机";
    private static final String DUMMY_PASSWORD_HASH =
            SecurityUtils.encryptPassword("pc-login-dummy-password");
    private static final SecureRandom CAPTCHA_RANDOM = new SecureRandom();
    private static final int HTTP_TOO_MANY_REQUESTS = 429;
    private static final int HTTP_SERVICE_UNAVAILABLE = 503;

    @Autowired
    private ISysUserService userService;

    @Autowired
    private SysAsyncService sysAsyncService;

    @Autowired
    private ISysConfigService configService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${spring.mail.username}")
    private String sender;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private RedisService redisService;

    @Autowired
    private IIdentityInfoService identityInfoService;

    @Autowired
    private ISysRoleService roleService;

    @Autowired
    private ISysPermissionService permissionService;

    @Autowired
    private ISysSenderMessageLogService senderMessageLogService;

    @Autowired
    private IAuthInfoService authInfoService;

    @Autowired
    private PcLoginRateLimiter pcLoginRateLimiter;

    @Autowired
    private PcLoginClientIpResolver pcLoginClientIpResolver;

    @Autowired
    private PcCaptchaRateLimiter pcCaptchaRateLimiter;

    @Autowired
    @Qualifier("pcCaptchaTaskExecutor")
    private TaskExecutor pcCaptchaTaskExecutor;

    @Autowired(required = false)
    private ISysLogininforService logininforService;

    // 判断用户名是邮箱还是手机号
    public static final String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
    // 手机号正则
    public static final String phoneRegex = "^1\\d{10}$";

    /**
     * 发送邮箱或者手机验证码
     */
    @PostMapping("/pc/sendCode")
    public R<?> sendCode(@RequestBody Map<String, String> params,
                         HttpServletRequest request, HttpServletResponse response) {
        String userName = params == null || StringUtils.isBlank(params.get("userName"))
                ? "" : params.get("userName").trim();
        String clientIp = pcLoginClientIpResolver.resolve(request);
        PcCaptchaRateLimiter.SendStatus sendStatus =
                pcCaptchaRateLimiter.consume(userName, clientIp);
        if (!sendStatus.isAvailable()) {
            return captchaRateLimitUnavailable(response);
        }
        if (sendStatus.isBlocked()) {
            return captchaRateLimited(response, sendStatus.getRetryAfterSeconds());
        }

        // Registration legitimately targets an address that does not exist in
        // the user table. Syntax validation plus the shared account/IP limiter
        // prevents this endpoint from bypassing captcha dispatch protection.
        if (userName.matches(emailRegex) || userName.matches(phoneRegex)) {
            scheduleCaptcha(userName);
        }
        return R.ok(CAPTCHA_DISPATCH_MESSAGE);
    }

    /**
     * 验证码登录获取验证码
     * @param user
     * @return
     */
    @PostMapping("/pc/captcha")
    public R<?> captcha(@RequestBody SysUser user, HttpServletRequest request,
                        HttpServletResponse response) {
        final String userName = StringUtils.isBlank(user.getUserName())
                ? "" : user.getUserName().trim();
        String clientIp = pcLoginClientIpResolver.resolve(request);

        PcCaptchaRateLimiter.SendStatus sendStatus =
                pcCaptchaRateLimiter.consume(userName, clientIp);
        if (!sendStatus.isAvailable()) {
            log.warn("PC captcha dispatch denied because the rate-limit backend is unavailable");
            return captchaRateLimitUnavailable(response);
        }
        if (sendStatus.isBlocked()) {
            log.info("PC captcha request rate-limited: clientIp={}", clientIp);
            return captchaRateLimited(response, sendStatus.getRetryAfterSeconds());
        }

        boolean supportedAccount = userName.matches(emailRegex) || userName.matches(phoneRegex);
        SysUser sysUser = null;
        if (supportedAccount) {
            SysUser lookup = new SysUser();
            lookup.setUserName(userName);
            sysUser = userService.selectUserByUserInfo(lookup);
        }
        boolean eligibleUser = sysUser != null
                && "2".equals(sysUser.getUserType())
                && "0".equals(sysUser.getStatus());

        if (eligibleUser) {
            scheduleCaptcha(userName);
        } else {
            log.info("PC captcha dispatch suppressed for an ineligible account");
        }

        return R.ok(CAPTCHA_DISPATCH_MESSAGE);
    }

    private void scheduleCaptcha(String userName) {
        try {
            pcCaptchaTaskExecutor.execute(() -> dispatchCaptcha(userName));
        } catch (RuntimeException e) {
            // Queue saturation and executor failures are internal only; the
            // public response must not reveal provider or account state.
            log.error("Unable to schedule PC captcha delivery", e);
        }
    }

    private void dispatchCaptcha(String userName) {
        try {
            if (userName.matches(emailRegex)) {
                getEmailCode(userName);
            } else if (userName.matches(phoneRegex)) {
                getPhoneCode(userName);
            }
        } catch (RuntimeException e) {
            log.error("PC captcha delivery failed", e);
        }
    }

    private R<?> getEmailCode(String email) {
        // 验证邮箱格式
        if (!email.matches("^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$")) {
            return R.fail("邮箱格式不正确");
        }

        // 生成6位随机验证码
        String code = String.valueOf(CAPTCHA_RANDOM.nextInt(900000) + 100000);

        // 存储验证码到Redis
        ValueOperations valueOperations = redisTemplate.opsForValue();
        valueOperations.set("EMAIL_CODE:" + email, code, 10, TimeUnit.MINUTES);

        // 发送邮件
        try {
            // 发送验证码邮件，发件人信息从配置中获取
            org.springframework.mail.javamail.JavaMailSenderImpl mailSenderImpl = (org.springframework.mail.javamail.JavaMailSenderImpl) mailSender;
            jakarta.mail.internet.MimeMessage mimeMessage = mailSenderImpl.createMimeMessage();
            org.springframework.mail.javamail.MimeMessageHelper helper = new org.springframework.mail.javamail.MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(sender);
            helper.setTo(email);
            helper.setSubject("邮箱注册验证码");

            // 格式化邮件内容，使验证码更明显（加粗并显示为蓝色）
            String text = "<!DOCTYPE html>" +
                    "<html>" +
                    "<head><meta charset='UTF-8'></head>" +
                    "<body>" +
                    "<p>亲爱的用户：</p>" +
                    "<p>您的邮箱验证码是：<span style='font-weight:bold;color:blue;font-size:20px;'>" + code + "</span>,请在10分钟内完成验证。</p>" +
                    "<p>注意：请勿泄露邮件验证码</p>" +
                    "<p>-----------------------------------------------------------------------</p>" +
                    "<p>如果这不是您的操作，请忽略此邮件。</p>" +
                    "<br>" +
                    "<hr>" +
//                    "<p>天津大学教育平台</p>" +
                    "</body>" +
                    "</html>";

            helper.setText(text, true);
            mailSender.send(mimeMessage);
            // 记录验证信息日志
            insertSenderMessageLog(email,null,"获取邮箱验证码","成功","成功");
            log.info("发送验证码邮件成功: to={}", email);
        } catch (Exception e) {
            insertSenderMessageLog(email,null,"获取邮箱验证码","发送邮件验证码失败",e.getMessage());
            log.error("发送邮件验证码失败", e);
            // 即使邮件发送失败，也要删除Redis中的验证码
            redisTemplate.delete("EMAIL_CODE:" + email);
            return R.fail("邮件发送失败，请稍后重试");
        }

        return R.ok("验证码已发送至您的邮箱");
    }

    // 获取手机短信验证码
    public R<?> getPhoneCode(String phone) {
        // 获取短信验证码
        R<Map<String, Object>> phoneRes = SmsUtil.sendVerificationCode(phone);
        if(phoneRes.getCode() == Constants.SUCCESS){
            Map<String, Object> data = phoneRes.getData();
            if(Boolean.parseBoolean(data.get("success").toString())){
                // 放redis 有效期5分钟
                redisService.setCacheObject("verificationCode_"+phone, data.get("verificationCode").toString(), 5L, TimeUnit.MINUTES);
                // 记录验证信息日志
                insertSenderMessageLog(phone,null,"获取手机验证码","成功","成功");
                return R.ok("验证码已发送至您的手机");
            }
        }
        insertSenderMessageLog(phone,null,"获取验证码失败","失败","失败");
        return R.fail("获取验证码失败");
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

    @Log(title = "验证手机或者邮箱", businessType = BusinessType.INSERT)
    @PostMapping("/pc/checkRegisterInfo")
    public R<?> checkPhone(@RequestBody SysUser user) throws Exception {
        String cachedCode = "";
        if(StringUtils.isNotEmpty(user.getUserName()) && user.getUserName().matches(phoneRegex)){
            // 校验手机号唯一性
//            user.setPhonenumber(user.getUserName());
//            if (!userService.checkPhoneUnique(user)) {
//                return success(userService.checkPhoneUnique(user));
//            }
            cachedCode = redisService.getCacheObject("verificationCode_"+user.getUserName());
        }

        // 校验邮箱唯一性
        if (StringUtils.isNotEmpty(user.getUserName()) && user.getUserName().matches(emailRegex)) {
//            user.setEmail(user.getUserName());
//            if(!userService.checkEmailUnique(user)){
//                return success(userService.checkEmailUnique(user));
//            }
            // 从Redis中获取验证码
            ValueOperations valueOperations = redisTemplate.opsForValue();
            cachedCode = (String) valueOperations.get("EMAIL_CODE:" + user.getUserName());
        }

        if (StringUtils.isEmpty(cachedCode)) {
            return R.fail("验证码已过期，请重新获取");
        }
        if (!cachedCode.equals(user.getMsgCode())) {
            return R.fail("验证码错误");
        }
        return R.ok();
    }

    /**
     * 用户注册
     * {
     *   "phonenumber": "18539567395",
     *   "password": "123456",
     *   "nickName": "18539567395",
     *   "userName": "18539567395",
     *   "email": "example@example.com",    // 邮箱注册时提供
     *   "msgCode": "123456"               // 邮箱注册时提供验证码
     * }
     */
    @Log(title = "用户注册", businessType = BusinessType.INSERT)
    @PostMapping("/pc/register")
    public R<?> register(@RequestBody SysUser user) throws Exception {
        // 检查注册功能是否开启
//        if (!("true".equals(configService.selectConfigByKey("sys.account.registerUser")))) {
//            return R.fail("当前系统没有开启注册功能！");
//        }
        if(StringUtils.isEmpty(user.getPhonenumber())&& StringUtils.isEmpty(user.getEmail())){
            return R.fail("注册失败，请输入手机或邮箱！");
        }
        // 如果提供了邮箱，则必须提供邮箱验证码并验证
        if (StringUtils.isNotEmpty(user.getEmail())) {
            String emailCode = user.getMsgCode();
            if (StringUtils.isEmpty(emailCode)) {
                return R.fail("邮箱注册必须提供验证码");
            }

            // 从Redis中获取验证码
            ValueOperations valueOperations = redisTemplate.opsForValue();
            String cachedCode = (String) valueOperations.get("EMAIL_CODE:" + user.getEmail());

            if (StringUtils.isEmpty(cachedCode)) {
                return R.fail("验证码已过期，请重新获取");
            }

            if (!cachedCode.equals(emailCode)) {
                return R.fail("验证码错误");
            }

            // 验证成功后删除验证码
            redisTemplate.delete("EMAIL_CODE:" + user.getEmail());
            user.setUserName(user.getEmail());
        }
        // 手机号验证码认证
        if(StringUtils.isNotEmpty(user.getPhonenumber())){
            String code = user.getMsgCode();
            if (StringUtils.isEmpty(code)) {
                return R.fail("手机注册必须提供验证码");
            }
            String phoneCode = redisService.getCacheObject("verificationCode_"+user.getPhonenumber());
            if (StringUtils.isEmpty(phoneCode)) {
                return R.fail("验证码已过期，请重新获取");
            }
            if (!phoneCode.equals(code)) {
                return R.fail("验证码错误");
            }
            // 验证成功后删除验证码
            redisService.deleteObject("verificationCode_"+user.getPhonenumber());
            user.setUserName(user.getPhonenumber());
        }

        // 只有通过邮箱/手机验证码后才执行唯一性检查，避免注册接口被用作账号枚举器。
        if (!userService.checkUserNameUnique(user)) {
            return R.fail("注册失败，该用户账号已被使用！");
        }
        if (StringUtils.isNotEmpty(user.getPhonenumber()) && !userService.checkPhoneUnique(user)) {
            return R.fail("注册失败，该手机号已被使用！");
        }
        if (StringUtils.isNotEmpty(user.getEmail()) && !userService.checkEmailUnique(user)) {
            return R.fail("注册失败，该邮箱已被使用！");
        }

        // PC 公共注册只能创建 C 端用户，不能由客户端选择账号类型。
        user.setUserType("2");

        // 设置默认昵称
        if (StringUtils.isEmpty(user.getNickName())) {
            user.setNickName(user.getUserName());
        }
        // 设置默认密码更新时间
        user.setPwdUpdateDate(new java.util.Date());
        // 加密密码
        user.setPassword(SecurityUtils.encryptPassword(RsaUtils.decryptByPrivateKey(user.getPassword())));

        // 注册用户
        user.setUserSources(TdConstants.USER_SOURCES_PC);
        // 校验是否学生账号  checkUserNameStudent
        if(userService.checkUserNameStudent(user)){
            SysUser userInner = new SysUser();
            userInner.setStatus("0");
            userInner.setPhonenumber(user.getPhonenumber());
            userInner.setEmail(user.getEmail());
            userInner.setPassword(user.getPassword());
            userService.updateUserStudent(userInner);
            bindTeacherCompetitionRegistration(user);
            return  R.ok("注册成功");
        }
        boolean result = userService.registerUser(user);
        return result ? R.ok("注册成功") : R.fail("注册失败，请稍后重试");
    }

    private void bindTeacherCompetitionRegistration(SysUser registeredUser) {
        if (StringUtils.isBlank(registeredUser.getPhonenumber())) {
            return;
        }
        SysUser lookup = new SysUser();
        lookup.setUserName(registeredUser.getPhonenumber());
        SysUser storedUser = userService.selectUserByUserInfo(lookup);
        if (storedUser != null && storedUser.getUserId() != null) {
            sysAsyncService.bindTeacherCompetitionUser(storedUser.getUserId(), storedUser.getPhonenumber());
        }
    }

    /**
     * 忘记密码
     */
    @Log(title = "忘记密码", businessType = BusinessType.UPDATE)
    @PostMapping("/pc/resetPwd")
    public R<?> updatePwd(@RequestBody SysUser user) throws Exception {
        String userName = user.getUserName();
        if (StringUtils.isBlank(userName)) {
            return R.fail(RESET_FAILURE_MESSAGE);
        }
        userName = userName.trim();
        user.setUserName(userName);
        if (StringUtils.isEmpty(user.getPassword())) {
            return R.fail("密码不能为空");
        }

        SysUser sysUser = userService.selectUserByUserInfo(user);
        boolean eligibleUser = sysUser != null
                && "2".equals(sysUser.getUserType())
                && "0".equals(sysUser.getStatus());
        String submittedCode = user.getMsgCode();
        String cachedCode;
        String cacheKey;

        if (userName.matches(emailRegex)) {
            cacheKey = "EMAIL_CODE:" + userName;
            ValueOperations valueOperations = redisTemplate.opsForValue();
            cachedCode = (String) valueOperations.get(cacheKey);
        } else if (userName.matches(phoneRegex)) {
            cacheKey = "verificationCode_" + userName;
            cachedCode = redisService.getCacheObject(cacheKey);
        } else {
            insertSenderMessageLog(userName, null, "重置密码", "失败", "账号格式无效");
            return R.fail(RESET_FAILURE_MESSAGE);
        }

        if (!eligibleUser
                || StringUtils.isEmpty(submittedCode)
                || StringUtils.isEmpty(cachedCode)
                || !cachedCode.equals(submittedCode)) {
            insertSenderMessageLog(userName, null, "重置密码", "失败", "用户或验证码无效");
            return R.fail(RESET_FAILURE_MESSAGE);
        }

        insertSenderMessageLog(userName, null, "重置密码", "成功", "成功");
        if (userName.matches(emailRegex)) {
            redisTemplate.delete(cacheKey);
        } else {
            redisService.deleteObject(cacheKey);
        }

        user.setUserId(sysUser.getUserId());
        user.setPassword(SecurityUtils.encryptPassword(RsaUtils.decryptByPrivateKey(user.getPassword())));
        return R.ok(userService.updateUserPwd(user));
    }

    /**
     * 微信扫码注册
     */
    @Log(title = "微信扫码注册", businessType = BusinessType.INSERT)
    @PostMapping("/pc/wechat/register")
    public R<?> wechatRegister(@RequestBody Map<String, Object> params) {
        // 获取参数
        String username = (String) params.get("username");
        String password = (String) params.get("password");
        String wechatOpenId = (String) params.get("wechatOpenId");
        String wechatUnionId = (String) params.get("wechatUnionId");
        String nickName = (String) params.get("nickName");
        String avatar = (String) params.get("avatar");

        // 参数校验
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password) ||
            StringUtils.isEmpty(wechatOpenId)) {
            return R.fail("用户名、密码和微信OpenId不能为空");
        }

        // 构造用户对象
        SysUser user = new SysUser();
        user.setUserName(username);
        user.setPassword(password);
        user.setNickName(StringUtils.isNotEmpty(nickName) ? nickName : username);

        // 设置头像
        if (StringUtils.isNotEmpty(avatar)) {
            user.setAvatar(avatar);
        }

        // 设置用户类型为微信小程序注册用户
        user.setUserType("03");

        // 校验用户名唯一性
        if (!userService.checkUserNameUnique(user)) {
            return R.fail("注册失败，该用户名已被使用！");
        }

        // 校验手机号唯一性（如果提供了手机号）
        if (StringUtils.isNotEmpty(user.getPhonenumber()) && !userService.checkPhoneUnique(user)) {
            return R.fail("注册失败，该手机号已被使用！");
        }

        // 校验邮箱唯一性（如果提供了邮箱）
        if (StringUtils.isNotEmpty(user.getEmail()) && !userService.checkEmailUnique(user)) {
            return R.fail("注册失败，该邮箱已被使用！");
        }

        // 设置默认密码更新时间
        user.setPwdUpdateDate(new java.util.Date());

        // 加密密码
        user.setPassword(SecurityUtils.encryptPassword(user.getPassword()));

        // 注册用户
        boolean result = userService.registerUser(user);

        if (result) {
            // 这里需要在用户表或者单独的微信绑定表中保存微信相关信息
            // 例如: wechat_open_id, wechat_union_id 等字段

            // 构造登录用户信息
            SysUser sysUser = userService.selectUserByUserName(username);
            LoginUser loginUser = new LoginUser();
            loginUser.setSysUser(sysUser);
            loginUser.setUserid(sysUser.getUserId());
            loginUser.setUsername(sysUser.getUserName());

            // 生成token
            return R.ok(tokenService.createToken(loginUser));
        }

        return R.fail("注册失败，请稍后重试");
    }

    // 用户登录查询该用户身份认证信息,配置白名单不做登录认证校验 未做任何认证，则选择认证身份跳过
    @GetMapping("/pc/getIdentityInfoList")
    public AjaxResult list(IdentityInfo identityInfo) {
        // 只获取认证通过后的身份
        identityInfo.setCheckStatus(Constants.IDENTITY_AUTH_PASS);
        List<IdentityInfo> list = identityInfoService.selectUserIdentityInfoList(identityInfo);
        return success(list);
    }

    /**
     * 检测用户账号是否已存在
     */
    @GetMapping("/pc/checkUserAccount")
    public AjaxResult checkUserAccount(SysUser user) {
        // 公开预检不返回账号是否存在；最终唯一性检查在验证码校验后执行。
        return success(true);
    }

    /**
     * 用户名密码登录
     */
    @Log(title = "用户密码登录", isSaveResponseData = false)
    @PostMapping("/pc/login")
    public R<?> login(@RequestBody SysUser user, HttpServletRequest request,
                      HttpServletResponse response) {
        String userName = StringUtils.isBlank(user.getUserName())
                ? "" : user.getUserName().trim();
        user.setUserName(userName);
        String clientIp = pcLoginClientIpResolver.resolve(request);

        PcLoginRateLimiter.LimitStatus currentLimit =
                pcLoginRateLimiter.reserveAttempt(userName, clientIp);
        if (!currentLimit.isAvailable()) {
            recordPcLoginAudit(userName, clientIp, request, "1", "RATE_LIMIT_BACKEND_UNAVAILABLE");
            return rateLimitUnavailable(response);
        }
        if (currentLimit.isBlocked()) {
            recordPcLoginAudit(userName, clientIp, request, "1", "RATE_LIMITED");
            return rateLimited(response, currentLimit.getRetryAfterSeconds());
        }

        boolean reservationCompleted = false;
        try {
            String rawPassword = "";
            boolean malformedPassword = false;
            if (StringUtils.isNotEmpty(user.getPassword())) {
                try {
                    rawPassword = RsaUtils.decryptByPrivateKey(user.getPassword());
                } catch (Exception e) {
                    malformedPassword = true;
                }
            }

            SysUser sysUser = StringUtils.isBlank(userName)
                    ? null : userService.selectUserByUserInfo(user);
            boolean eligibleUser = sysUser != null
                    && "2".equals(sysUser.getUserType())
                    && "0".equals(sysUser.getStatus())
                    && StringUtils.isNotEmpty(sysUser.getPassword());
            String passwordHash = eligibleUser ? sysUser.getPassword() : DUMMY_PASSWORD_HASH;
            boolean passwordMatches = SecurityUtils.matchesPassword(rawPassword, passwordHash);

            if (!eligibleUser || malformedPassword || !passwordMatches) {
                String internalReason = loginFailureReason(userName, sysUser, malformedPassword);
                recordPcLoginAudit(userName, clientIp, request, "1", internalReason);
                PcLoginRateLimiter.LimitStatus failureLimit =
                        pcLoginRateLimiter.recordFailure(userName, clientIp);
                reservationCompleted = failureLimit.isAvailable();
                if (!failureLimit.isAvailable()) {
                    return rateLimitUnavailable(response);
                }
                if (failureLimit.isBlocked()) {
                    return rateLimited(response, failureLimit.getRetryAfterSeconds());
                }
                return R.fail(LOGIN_FAILURE_MESSAGE);
            }

            if (!pcLoginRateLimiter.recordSuccess(userName, clientIp)) {
                recordPcLoginAudit(userName, clientIp, request, "1", "RATE_LIMIT_BACKEND_UNAVAILABLE");
                return rateLimitUnavailable(response);
            }
            reservationCompleted = true;

            //实名认证信息
            AuthInfo authInfo = authInfoService.selectAuthInfoByUserId(sysUser.getUserId());
            try {
                if(Objects.nonNull(authInfo)){
                    authInfo.setIdCard(RsaUtils.encryptByPublicKey(authInfo.getIdCard()));
                    sysUser.setAuthInfo(authInfo);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            // 构造登录用户信息
            LoginUser loginUser = new LoginUser();
            loginUser.setSysUser(sysUser);
            loginUser.setUserid(sysUser.getUserId());
            loginUser.setUsername(sysUser.getUserName());
            // 生成token
            Map<String, Object> token = tokenService.createToken(loginUser);
            recordPcLoginAudit(userName, clientIp, request, "0", "LOGIN_SUCCESS");
            return R.ok(token);
        } finally {
            if (!reservationCompleted) {
                pcLoginRateLimiter.releaseReservation(userName, clientIp);
            }
        }
    }

    private String loginFailureReason(String userName, SysUser sysUser, boolean malformedPassword) {
        if (StringUtils.isBlank(userName)) {
            return "ACCOUNT_EMPTY";
        }
        if (malformedPassword) {
            return "PASSWORD_PAYLOAD_INVALID";
        }
        if (sysUser == null) {
            return "ACCOUNT_NOT_FOUND";
        }
        if (!"2".equals(sysUser.getUserType())) {
            return "ACCOUNT_TYPE_REJECTED";
        }
        if (!"0".equals(sysUser.getStatus())) {
            return "ACCOUNT_STATUS_REJECTED";
        }
        return "PASSWORD_INVALID";
    }

    private R<?> rateLimited(HttpServletResponse response, long retryAfterSeconds) {
        if (response != null) {
            response.setStatus(HTTP_TOO_MANY_REQUESTS);
            response.setHeader("Retry-After", String.valueOf(Math.max(1L, retryAfterSeconds)));
        }
        return R.fail(HTTP_TOO_MANY_REQUESTS, "登录尝试过于频繁，请稍后重试");
    }

    private R<?> rateLimitUnavailable(HttpServletResponse response) {
        if (response != null) {
            response.setStatus(HTTP_SERVICE_UNAVAILABLE);
        }
        return R.fail(HTTP_SERVICE_UNAVAILABLE, "登录服务暂不可用，请稍后重试");
    }

    private R<?> captchaRateLimited(HttpServletResponse response, long retryAfterSeconds) {
        if (response != null) {
            response.setStatus(HTTP_TOO_MANY_REQUESTS);
            response.setHeader("Retry-After", String.valueOf(Math.max(1L, retryAfterSeconds)));
        }
        return R.fail(HTTP_TOO_MANY_REQUESTS, "验证码请求过于频繁，请稍后重试");
    }

    private R<?> captchaRateLimitUnavailable(HttpServletResponse response) {
        if (response != null) {
            response.setStatus(HTTP_SERVICE_UNAVAILABLE);
        }
        return R.fail(HTTP_SERVICE_UNAVAILABLE, "验证码服务暂不可用，请稍后重试");
    }

    private void recordPcLoginAudit(String userName, String clientIp,
                                    HttpServletRequest request, String status, String reason) {
        String normalizedUserName = StringUtils.substring(
                StringUtils.isBlank(userName) ? "<empty>"
                        : userName.replace('\r', '_').replace('\n', '_'),
                0, 50);
        log.info("PC login audit: account={}, clientIp={}, status={}, reason={}",
                normalizedUserName, clientIp, status, reason);
        if (logininforService == null) {
            return;
        }
        try {
            String userAgent = ClientInfoUtils.getUserAgent(request);
            SysLogininfor loginInfo = new SysLogininfor();
            loginInfo.setUserName(normalizedUserName);
            loginInfo.setIpaddr(StringUtils.substring(clientIp, 0, 128));
            loginInfo.setStatus(status);
            loginInfo.setMsg(reason);
            loginInfo.setBrowser(ClientInfoUtils.parseBrowser(userAgent));
            loginInfo.setOs(ClientInfoUtils.parseOS(userAgent));
            logininforService.insertLogininfor(loginInfo);
        } catch (RuntimeException e) {
            log.error("Failed to persist PC login audit event", e);
        }
    }

    /**
     * 用户名密码登录
     */
    @Log(title = "邮箱或者手机号登录", isSaveResponseData = false)
    @PostMapping("/pc/userInfoLogin")
    public R<?> emailLogin(@RequestBody SysUser user, HttpServletRequest request,
                           HttpServletResponse response) {
        String userName = StringUtils.isBlank(user.getUserName())
                ? "" : user.getUserName().trim();
        user.setUserName(userName);
        String clientIp = pcLoginClientIpResolver.resolve(request);

        PcLoginRateLimiter.LimitStatus currentLimit =
                pcLoginRateLimiter.reserveAttempt(userName, clientIp);
        if (!currentLimit.isAvailable()) {
            recordPcLoginAudit(userName, clientIp, request, "1", "RATE_LIMIT_BACKEND_UNAVAILABLE");
            return rateLimitUnavailable(response);
        }
        if (currentLimit.isBlocked()) {
            recordPcLoginAudit(userName, clientIp, request, "1", "RATE_LIMITED");
            return rateLimited(response, currentLimit.getRetryAfterSeconds());
        }

        boolean reservationCompleted = false;
        try {
            SysUser sysUser = StringUtils.isBlank(userName)
                    ? null : userService.selectUserByUserInfo(user);
            boolean eligibleUser = sysUser != null
                    && "2".equals(sysUser.getUserType())
                    && "0".equals(sysUser.getStatus());

            String cacheKey = null;
            String cachedCode = null;
            if (userName.matches(emailRegex)) {
                cacheKey = "EMAIL_CODE:" + userName;
                ValueOperations valueOperations = redisTemplate.opsForValue();
                cachedCode = (String) valueOperations.get(cacheKey);
            } else if (userName.matches(phoneRegex)) {
                cacheKey = "verificationCode_" + userName;
                cachedCode = redisService.getCacheObject(cacheKey);
            }

            boolean validCode = StringUtils.isNotEmpty(user.getMsgCode())
                    && StringUtils.isNotEmpty(cachedCode)
                    && cachedCode.equals(user.getMsgCode());
            if (!eligibleUser || !validCode) {
                String reason = otpLoginFailureReason(userName, sysUser, validCode);
                recordPcLoginAudit(userName, clientIp, request, "1", reason);
                PcLoginRateLimiter.LimitStatus failureLimit =
                        pcLoginRateLimiter.recordFailure(userName, clientIp);
                reservationCompleted = failureLimit.isAvailable();
                if (!failureLimit.isAvailable()) {
                    return rateLimitUnavailable(response);
                }
                if (failureLimit.isBlocked()) {
                    return rateLimited(response, failureLimit.getRetryAfterSeconds());
                }
                return R.fail(RESET_FAILURE_MESSAGE);
            }

            if (userName.matches(emailRegex)) {
                redisTemplate.delete(cacheKey);
                user.setEmail(userName);
            } else {
                redisService.deleteObject(cacheKey);
                user.setPhonenumber(userName);
            }

            if (!pcLoginRateLimiter.recordSuccess(userName, clientIp)) {
                recordPcLoginAudit(userName, clientIp, request, "1",
                        "RATE_LIMIT_BACKEND_UNAVAILABLE");
                return rateLimitUnavailable(response);
            }
            reservationCompleted = true;

            //实名认证信息
            AuthInfo authInfo = authInfoService.selectAuthInfoByUserId(sysUser.getUserId());
            try {
                if(Objects.nonNull(authInfo)){
                    authInfo.setIdCard(RsaUtils.encryptByPublicKey(authInfo.getIdCard()));
                    sysUser.setAuthInfo(authInfo);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            // 构造登录用户信息
            LoginUser loginUser = new LoginUser();
            loginUser.setSysUser(sysUser);
            loginUser.setUserid(sysUser.getUserId());
            loginUser.setUsername(sysUser.getUserName());
            Map<String, Object> token = tokenService.createToken(loginUser);
            recordPcLoginAudit(userName, clientIp, request, "0", "OTP_LOGIN_SUCCESS");
            return R.ok(token);
        } finally {
            if (!reservationCompleted) {
                pcLoginRateLimiter.releaseReservation(userName, clientIp);
            }
        }
    }

    private String otpLoginFailureReason(String userName, SysUser sysUser, boolean validCode) {
        if (StringUtils.isBlank(userName)) {
            return "ACCOUNT_EMPTY";
        }
        if (sysUser == null) {
            return "ACCOUNT_NOT_FOUND";
        }
        if (!"2".equals(sysUser.getUserType())) {
            return "ACCOUNT_TYPE_REJECTED";
        }
        if (!"0".equals(sysUser.getStatus())) {
            return "ACCOUNT_STATUS_REJECTED";
        }
        return validCode ? "OTP_LOGIN_REJECTED" : "OTP_INVALID";
    }

    /**
     * 退出登录
     */
    @Log(title = "退出登录")
    @DeleteMapping("/logout")
    public R<?> logout(HttpServletRequest request) {
        // 获取登录用户信息
        LoginUser loginUser = tokenService.getLoginUser(request);
        if (StringUtils.isNotNull(loginUser)) {
            // 删除用户缓存记录
            tokenService.delLoginUser(loginUser.getToken());
        }
        return R.ok("退出成功");
    }

    /**
     * 获取用户信息
     */
    @GetMapping("/info")
    public R<?> getInfo(HttpServletRequest request) {
        LoginUser loginUser = tokenService.getLoginUser(request);
        if (StringUtils.isNull(loginUser)) {
            return R.fail("未登录");
        }
        // 角色集合
        Set<String> roles = permissionService.getRolePermission(loginUser.getSysUser());
        // 权限集合
        Set<String> permissions = permissionService.getMenuPermission(loginUser.getSysUser());
        loginUser.setRoles(roles);
        loginUser.setPermissions(permissions);
        // 角色菜单数据权限
        permissionService.getRoleMenuList(loginUser.getSysUser());
        loginUser.getSysUser().setPermissions(permissions);
        return R.ok(loginUser.getSysUser());
    }

    /**
     * 刷新token
     */
    @PostMapping("/refresh")
    public R<?> refresh(HttpServletRequest request) {
        LoginUser loginUser = tokenService.getLoginUser(request);
        if (StringUtils.isNull(loginUser)) {
            return R.fail("未登录");
        }
        // 刷新令牌有效期
        tokenService.refreshToken(loginUser);
        return R.ok(tokenService.createToken(loginUser));
    }
}
