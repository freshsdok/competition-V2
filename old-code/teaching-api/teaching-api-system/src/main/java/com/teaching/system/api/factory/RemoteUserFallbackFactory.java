package com.teaching.system.api.factory;

import com.teaching.common.core.domain.R;
import com.teaching.system.api.RemoteUserService;
import com.teaching.system.api.domain.*;
import com.teaching.system.api.model.LoginUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 用户服务降级处理
 *
 * @author teaching
 */
@Component
public class RemoteUserFallbackFactory implements FallbackFactory<RemoteUserService> {
    private static final Logger log = LoggerFactory.getLogger(RemoteUserFallbackFactory.class);

    @Override
    public RemoteUserService create(Throwable throwable) {
        log.error("用户服务调用失败:{}", throwable.getMessage());
        return new RemoteUserService() {
            @Override
            public R<LoginUser> getUserInfo(String username, String source) {
                return R.fail("获取用户失败:" + throwable.getMessage());
            }

            @Override
            public R<SysUser> getUserInfoByNickName(String nickName, String source) {
                return R.fail("获取用户失败:" + throwable.getMessage());
            }

            @Override
            public R<SysUser> getUserInfoById(Long userId, String source) {
                return R.fail("获取用户失败:" + throwable.getMessage());
            }

            @Override
            public R<SysRole> getRoleInfoById(Long roleId, String source) {
                return R.fail("获取角色失败:" + throwable.getMessage());
            }

            @Override
            public R<SysUser> getUserCenterInfo(Long userId, String source) {
                return R.fail("根据用户id获取用户失败:" + throwable.getMessage());
            }

            @Override
            public R<List<SysUser>> getUserCenterInfoList(List<Long> userIdList, String source) {
                return R.fail("批量获取用户信息:" + throwable.getMessage());
            }

            @Override
            public R<Boolean> registerUserInfo(SysUser sysUser, String source) {
                return R.fail("注册用户失败:" + throwable.getMessage());
            }

            @Override
            public R<Boolean> recordUserLogin(SysUser sysUser, String source) {
                return R.fail("记录用户登录信息失败:" + throwable.getMessage());
            }

            @Override
            public R<List<SysDictData>> dictType(String dictType, String source) {
                return R.fail("获取字段数据失败:" + throwable.getMessage());
            }

            @Override
            public R<Integer> updateIdentityInfoStatus(IdentityInfo identityInfo, String source) {
                return R.fail("修改身份认证状态失败:" + throwable.getMessage());
            }

            @Override
            public R<IdentityInfo> getInnerIdentityInfoDetail(Long authId, String source) {
                return R.fail("获取身份认证详情信息失败:" + throwable.getMessage());
            }

            @Override
            public R<Void> saveUserAuthorization(UserAuthorization userAuthorization, String source) {
                return R.fail("比赛用户赋权失败:" + throwable.getMessage());
            }

            @Override
            public R<NationwideCollegeInfo> getNationwideCollegeInfoInfo(String id, String source) {
                return R.fail("获取学校信息失败:" + throwable.getMessage());
            }

            @Override
            public R<NationwideCollegeInfo> getNationwideCollegeInfoInfoByName(String name, String source) {
                return R.fail("根据学校名称获取学校信息失败:" + throwable.getMessage());
            }

            @Override
            public R<Map<String,Map<String,Object>>> saveStudentUserInfo(List<SysUser> sysUserList, String source) {
                return R.fail("保存学生用户信息失败:" + throwable.getMessage());
            }

            @Override
            public R<List<NationwideCollegeInfo>> getDoubleFirstClassUniversityPlan(String source) {
                return R.fail("获取双一流院校信息失败:" + throwable.getMessage());
            }

            @Override
            public R<Map<String, Object>> saveInnerAuthInfo(AuthInfo authInfo, String source) {
                return R.fail("实名认证失败:" + throwable.getMessage());
            }

            @Override
            public R<List<AuthInfo>> selectAuthInfoByIdCard(AuthInfo authInfo, String source) {
                return R.fail("查询实名认证信息失败:" + throwable.getMessage());
            }

            @Override
            public R<Void> insertTeacherTmpInfo(String source) {
                return R.fail("教师录入系统失败失败:" + throwable.getMessage());
            }

            @Override
            public R<List<AuthInfo>> selectAuthInfoByName(String name, String source) {
                return R.fail("教师信息查询失败:" + throwable.getMessage());
            }
            @Override
            public R<Void> updateUserIdsByUserGroup(Long userGroupId, String source) {
                return R.fail("更新用户组下的人员信息失败:" + throwable.getMessage());
            }

            @Override
            public R<SysUser> updateApplyInfoUser(SysUser user, String source) {
                return R.fail("修改报名个人信息失败:" + throwable.getMessage());
            }

            @Override
            public R<Integer> updateAddApplyInfoUser(List<SysUser> userList, String source) {
                return R.fail("增删报名个人信息失败:" + throwable.getMessage());
            }

            @Override
            public R<List<SysUser>> selectUserByPhoneCheck(SysUser user, String source) {
                return R.fail("根据手机号或邮箱查询个人信息失败:" + throwable.getMessage());
            }

            @Override
            public R<Long> saveOssExportFile(Map<String,Object> fileParam, String source) {
                return R.fail("保存文件到oss失败:" + throwable.getMessage());
            }

            @Override
            public R<Void> updateExportManageInner(Map<String, Object> fileParam, String source) {
                return R.fail("更新文件列表信息失败:" + throwable.getMessage());
            }

            @Override
            public R<Long> registerWxSysUser(SysUser sysUser, String source) {
                return R.fail("保存微信用户信息失败:" + throwable.getMessage());
            }

            @Override
            public R<SysUser> queryWxSysUser(String openId, String source) {
                return R.fail("查询微信用户信息失败:" + throwable.getMessage());
            }

            @Override
            public R<Void> handlePdf(String fileTaskId, String source) {
                return R.fail("处理pdf失败:" + throwable.getMessage());
            }

            @Override
            public R<Void> updateNoRegisterUser(String updateSize, String source) {
                return R.fail("刷新用户状态及密码失败:" + throwable.getMessage());
            }

            @Override
            public R<String> getGroupNames(List<Long> groupIds, String source) {
                return R.fail("获取用户组名称失败:" + throwable.getMessage());
            }
        };
    }
}
