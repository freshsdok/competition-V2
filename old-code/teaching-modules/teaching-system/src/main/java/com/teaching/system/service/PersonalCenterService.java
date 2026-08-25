package com.teaching.system.service;

import com.teaching.system.api.domain.IdentityInfo;
import com.teaching.system.api.domain.SysUser;
import com.teaching.system.api.domain.UserAuthorization;

import java.util.List;
import java.util.Map;

/**
 *  个人中心服务
 */
public interface PersonalCenterService {

    /**
     *  更新用户信息
     * @param sysUser
     * @return
     */
    boolean updateUserInfo(SysUser sysUser);

    /**
     *  获取用户中心信息
     * @param userId
     * @return
     */
    SysUser getUserCenterInfo(Long userId);

    List<SysUser> getUserCenterInfoList(List<Long> userIdList);

    /**
     *  获取教师信息列表
     * @return
     */
    List<SysUser> selectTeacherList(String userName);

    /**
     *  根据学校ID获取教师列表
     * @param schoolId
     * @return
     */
    List<Map<String,Object>>getTeacherList(String schoolId);

    /**
     *  保存用户授权信息
     * @param userAuthorization
     */
    public void saveUserAuthorization(UserAuthorization userAuthorization);

    /**
     *  重新认证删除历史认证信息成功后，自动赋予角色
     * @param userAuthorization
     */
    public void updateUserAuthorization(UserAuthorization userAuthorization);
}
