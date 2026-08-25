package com.teaching.system.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.teaching.common.core.constant.Constants;
import com.teaching.common.core.exception.GlobalException;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.core.utils.SpringUtils;
import com.teaching.common.redis.service.RedisLock;
import com.teaching.common.redis.service.RedisService;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.system.api.domain.AuthInfo;
import com.teaching.system.api.domain.SysUser;
import com.teaching.system.api.domain.UserAuthorization;
import com.teaching.system.mapper.AuthInfoMapper;
import com.teaching.system.mapper.SysUserMapper;
import com.teaching.system.service.AuthenticationService;
import com.teaching.system.service.IAuthInfoService;
import com.teaching.system.service.PersonalCenterService;
import org.apache.commons.collections4.MapUtils;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.redisson.api.RLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import utils.HttpUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 实名认证Service业务层处理
 *
 * @author teaching
 * @date 2025-10-13
 */
@Service
public class AuthInfoServiceImpl implements IAuthInfoService {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private AuthInfoMapper authInfoMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private RedisLock redisLock;

    /**
     * 查询实名认证
     *
     * @param authId 实名认证主键
     * @return 实名认证
     */
    @Override
    public AuthInfo selectAuthInfoByAuthId(Long userId) {
        return authInfoMapper.selectAuthInfoByAuthId(userId);
    }

    @Override
    public AuthInfo selectAuthInfoByUserId(Long userId) {
        return authInfoMapper.selectAuthInfoByUserId(userId);
    }

    /**
     * 查询实名认证列表
     *
     * @param authInfo 实名认证
     * @return 实名认证
     */
    @Override
    public List<AuthInfo> selectAuthInfoList(AuthInfo authInfo) {
        return authInfoMapper.selectAuthInfoList(authInfo);
    }

    @Override
    public List<AuthInfo> selectAuthInfoByUserName(String realName) {
        return authInfoMapper.selectAuthInfoByUserName(realName);
    }

    // 根据身份证及姓名查实名认证信息
    @Override
    public List<AuthInfo> selectAuthInfoByIdCard(AuthInfo authInfo) {
        return authInfoMapper.selectAuthInfoListByIdCard(authInfo);
    }

    /**
     * 新增实名认证
     *
     * @param authInfo 实名认证
     * @return 结果
     */
    @Override
    @Transactional
    public int insertAuthInfo(AuthInfo authInfo) throws Exception{
        String lockKey = "authUserId:"+authInfo.getUserId();
        RLock rLock = redisLock.getRLock(lockKey);
        try {
            if(rLock.tryLock(3, 30, TimeUnit.SECONDS)){
                // 当前登陆人id
                Long userId = SecurityUtils.getLoginUser().getSysUser().getUserId();
                // 同一个人不同得手机号或邮箱进行实名认证将现有得sys_user表已经实名认证过得用户id进行绑定
                authInfo.setAuthStatus(Constants.AUTH_STATUS_PASS);
                List<AuthInfo> authInfoList = authInfoMapper.selectAuthInfoList(authInfo);
                if (!CollectionUtils.isEmpty(authInfoList)) {
                    AuthInfo authInfoRes = authInfoList.get(0);
                    if(userId != authInfoRes.getUserId()){
                        // 不一样进行账号绑定
                        SysUser user = new SysUser();
                        user.setUserId(userId);
                        user.setAuthStatus(Constants.AUTH_STATUS_PASS);
                        user.setNickName(authInfoRes.getRealName());
                        // 判断时pc创建还是import创建,pc创建按照注册时间最早得为主，如果import创建，无论创建时间早晚，import不绑定pc注册得账号
                        // 当前登陆人id
                        SysUser sysUser = sysUserMapper.selectUserById(userId);
                        if(!"import".equals(sysUser.getUserSources())){
                            user.setSysUserId(authInfoRes.getUserId().toString());
                        }
                        // 获取主账号学校信息
                        SysUser sysUserMain = sysUserMapper.selectUserById(authInfoRes.getUserId());
                        user.setSchool(sysUserMain.getSchool());
                        user.setSchoolName(sysUserMain.getSchoolName());
                        sysUserMapper.updateUser(user);
                        // 认证成功
                        authInfo.setAuthTime(DateUtils.getNowDate());
                        authInfo.setCreateTime(DateUtils.getNowDate());
                        authInfo.setCreateBy(userId + "");
                        authInfo.setCountryName("CN");
                        authInfo.setUserId(userId);
                        authInfo.setAuthStatus(Constants.AUTH_STATUS_PASS);
                        // 将实名认证角色自动付给用户
                        UserAuthorization userAuthorization = new UserAuthorization();
                        userAuthorization.setUserId(userId);
                        userAuthorization.setAuthFlag(true);
                        PersonalCenterService personalCenterService = SpringUtils.getBean(PersonalCenterService.class);
                        personalCenterService.saveUserAuthorization(userAuthorization);
                        authInfoMapper.insertAuthInfo(authInfo);
                        return 2;
                    }
                    // 一样直接返回成功
                    return 1;
                }
                Map<String, Object> authenticationMap = authenticationService.authentication(authInfo.getRealName().trim(), authInfo.getIdCard().trim());
                // 修改用户表认证状态
                SysUser user = new SysUser();
                user.setUserId(userId);
                authInfo.setUserId(userId);
                if (Boolean.parseBoolean(String.valueOf(authenticationMap.get("isok")))) {
                    // 认证成功
                    authInfo.setAuthTime(DateUtils.getNowDate());
                    authInfo.setCreateTime(DateUtils.getNowDate());
                    authInfo.setCreateBy(userId + "");
                    authInfo.setCountryName("CN");
                    user.setAuthStatus(Constants.AUTH_STATUS_PASS);
                    user.setNickName(authInfo.getRealName());
                    if(Objects.nonNull(authenticationMap.get("IdCardInfor"))){
                        Map idCardInfor = (Map)authenticationMap.get("IdCardInfor");
                        user.setSex(idCardInfor.get("sex").toString().equals("男")? "0" : "1");
                    }
                    sysUserMapper.updateUser(user);
                    authInfo.setAuthStatus(Constants.AUTH_STATUS_PASS);
                    // 将实名认证角色自动付给用户
                    UserAuthorization userAuthorization = new UserAuthorization();
                    userAuthorization.setUserId(userId);
                    userAuthorization.setAuthFlag(true);
                    PersonalCenterService personalCenterService = SpringUtils.getBean(PersonalCenterService.class);
                    personalCenterService.saveUserAuthorization(userAuthorization);
                    return authInfoMapper.insertAuthInfo(authInfo);
                } else {
                    authInfo.setAuthTime(DateUtils.getNowDate());
                    authInfo.setCreateTime(DateUtils.getNowDate());
                    authInfo.setCreateBy(userId + "");
                    user.setAuthStatus(Constants.AUTH_STATUS_FAIL);
                    sysUserMapper.updateUser(user);
                    authInfo.setAuthStatus(Constants.AUTH_STATUS_FAIL);
                    authInfoMapper.insertAuthInfo(authInfo);
//                throw new GlobalException("认证失败");
                }
            }
        } catch (Exception e) {
            redisService.deleteObject("idCard:"+authInfo.getIdCard());
            logger.info("实名认证失败:" + e);
//            throw new GlobalException("认证失败");
        }finally {
            if (rLock.isHeldByCurrentThread()) {
                rLock.unlock();
            }
        }
        return 1;
    }

    /**
     * 修改实名认证
     *
     * @param authInfo 实名认证
     * @return 结果
     */
    @Override
    public int updateAuthInfo(AuthInfo authInfo) {
        authInfo.setUpdateTime(DateUtils.getNowDate());
        return authInfoMapper.updateAuthInfo(authInfo);
    }

    /**
     * 批量删除实名认证
     *
     * @param authIds 需要删除的实名认证主键
     * @return 结果
     */
    @Override
    public int deleteAuthInfoByAuthIds(String[] authIds) {
        return authInfoMapper.deleteAuthInfoByAuthIds(authIds);
    }

    /**
     * 删除实名认证信息
     *
     * @param authId 实名认证主键
     * @return 结果
     */
    @Override
    public int deleteAuthInfoByAuthId(String authId) {
        return authInfoMapper.deleteAuthInfoByAuthId(authId);
    }
}
