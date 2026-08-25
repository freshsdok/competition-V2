package com.teaching.system.service.impl;

import com.teaching.common.core.constant.Constants;
import com.teaching.common.core.exception.GlobalException;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.common.core.utils.sign.RsaUtils;
import com.teaching.common.security.service.TokenService;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.system.api.domain.*;
import com.teaching.system.api.model.LoginUser;
import com.teaching.system.domain.SysUserRole;
import com.teaching.system.mapper.*;
import com.teaching.system.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;

@Service
public class PersonalCenterServiceImpl implements PersonalCenterService {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private ISysUserService userService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private IAuthInfoService authInfoService;

    @Autowired
    private IdentityInfoMapper identityInfoMapper;

    @Autowired
    private AuthInfoMapper authInfoMapper;

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private IOrderInfoService orderInfoService;


    @Override
    public boolean updateUserInfo(SysUser sysUser) {

        return sysUserMapper.updateUser(sysUser) > 0;
    }

    @Override
    public SysUser getUserCenterInfo(Long userId) {
        SysUser sysUserCurrent = sysUserMapper.selectUserById(userId);
        // 获取有无主账号信息
        SysUser sysUserMain = null;
        if (Objects.nonNull(sysUserCurrent) && StringUtils.isNotEmpty(sysUserCurrent.getSysUserId())) {
            sysUserMain = sysUserMapper.selectUserById(Long.parseLong(sysUserCurrent.getSysUserId()));
        } else {
            sysUserMain = sysUserCurrent;
        }
        if (sysUserMain != null) {
            IdentityInfo identityInfo = new IdentityInfo();
            identityInfo.setUserId(sysUserMain.getUserId());
//            identityInfo.setCheckStatus(Constants.IDENTITY_AUTH_PASS);
            List<IdentityInfo> identityInfoList = identityInfoMapper.selectIdentityInfoList(identityInfo);
            if(!CollectionUtils.isEmpty(identityInfoList)){
                sysUserMain.setIdentityInfoList(identityInfoList);
            }
            AuthInfo authInfo = authInfoMapper.selectAuthInfoByAuthId(sysUserMain.getUserId());
            try {
                if(Objects.nonNull(authInfo)){
                    authInfo.setIdCard(RsaUtils.encryptByPublicKey(authInfo.getIdCard()));
                    sysUserMain.setAuthStatus(authInfo.getAuthStatus());
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            sysUserMain.setAuthInfo(authInfo);
            // 获取未支付订单数量
            sysUserMain.setNoPayOrderNum(orderInfoService.getPendingCount(userId)+"");
        }
        return sysUserMain;
    }

    @Override
    public List<SysUser> getUserCenterInfoList(List<Long> userIdList) {
        if(CollectionUtils.isEmpty(userIdList)){
            return new ArrayList<>();
        }
        List<SysUser> sysUserList = sysUserMapper.selectUserByIds(userIdList);
        if (!CollectionUtils.isEmpty(sysUserList)) {
            sysUserList.stream().forEach(sysUser -> {
                IdentityInfo identityInfo = new IdentityInfo();
                identityInfo.setUserId(sysUser.getUserId());
//            identityInfo.setCheckStatus(Constants.IDENTITY_AUTH_PASS);
                List<IdentityInfo> identityInfoList = identityInfoMapper.selectIdentityInfoList(identityInfo);
                if(!CollectionUtils.isEmpty(identityInfoList)){
                    sysUser.setIdentityInfoList(identityInfoList);
                }
                AuthInfo authInfo = authInfoMapper.selectAuthInfoByAuthId(sysUser.getUserId());
                try {
                    if(Objects.nonNull(authInfo)){
                        authInfo.setIdCard(RsaUtils.encryptByPublicKey(authInfo.getIdCard()));
                        sysUser.setAuthStatus(authInfo.getAuthStatus());
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                sysUser.setAuthInfo(authInfo);
            });
        }
        return sysUserList;
    }

    @Override
    public List<SysUser> selectTeacherList(String userName) {
        return sysUserMapper.selectUserIdentityInfo(Constants.IDENTITY_TYPE_TEACHER,userName);
    }

    @Override
    public List<Map<String,Object>> getTeacherList(String schoolId) {
        if(StringUtils.isBlank(schoolId)){
            return null;
        }
        return sysUserMapper.selectTeacherListBySchoolId(schoolId);
    }

    @Override
    public void saveUserAuthorization(UserAuthorization userAuthorization) {
        SysRole sysRole = new SysRole();
        sysRole.setStudentFlag(userAuthorization.isStudentFlag());
        sysRole.setTeacherFlag(userAuthorization.isTeacherFlag());
        sysRole.setCompetitionFlag(userAuthorization.isCompetitionFlag());
        sysRole.setCaptainFlag(userAuthorization.isCaptainFlag());
        sysRole.setAuthFlag(userAuthorization.isAuthFlag());
        if(sysRole.isAuthFlag() || sysRole.isCompetitionFlag() || sysRole.isTeacherFlag() || sysRole.isCaptainFlag() || sysRole.isStudentFlag()){
            SysRole sysRoleUser = sysRoleMapper.selectRoleByFlag(sysRole);
            if (Objects.nonNull(sysRoleUser)) {
                SysUserRole sysUserRole = new SysUserRole();
                sysUserRole.setUserId(userAuthorization.getUserId());
                sysUserRole.setRoleId(sysRoleUser.getRoleId());
                sysUserRoleMapper.deleteUserRoleInfo(sysUserRole);
                sysUserRoleMapper.batchUserRole(Arrays.asList(sysUserRole));
            }
        }
    }

    @Override
    public void updateUserAuthorization(UserAuthorization userAuthorization) {
        SysRole sysRole = new SysRole();
        sysRole.setStudentFlag(userAuthorization.isStudentFlag());
        sysRole.setTeacherFlag(userAuthorization.isTeacherFlag());
        sysRole.setCompetitionFlag(userAuthorization.isCompetitionFlag());
        sysRole.setCaptainFlag(userAuthorization.isCaptainFlag());
        sysRole.setAuthFlag(userAuthorization.isAuthFlag());
        if(sysRole.isAuthFlag() || sysRole.isCompetitionFlag() || sysRole.isTeacherFlag() || sysRole.isCaptainFlag() || sysRole.isStudentFlag()){
            SysRole sysRoleUser = sysRoleMapper.selectRoleByFlag(sysRole);
            if (Objects.nonNull(sysRoleUser)) {
                SysUserRole sysUserRole = new SysUserRole();
                sysUserRole.setUserId(userAuthorization.getUserId());
                sysUserRole.setRoleId(sysRoleUser.getRoleId());
                sysUserRoleMapper.deleteUserRoleInfo(sysUserRole);
            }
        }
    }

}
