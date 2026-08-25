package com.teaching.system.api;

import com.teaching.common.core.constant.SecurityConstants;
import com.teaching.common.core.constant.ServiceNameConstants;
import com.teaching.common.core.domain.R;
import com.teaching.system.api.domain.*;
import com.teaching.system.api.factory.RemoteUserFallbackFactory;
import com.teaching.system.api.model.LoginUser;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 用户服务
 *
 * @author teaching
 */
@FeignClient(contextId = "remoteUserService", value = ServiceNameConstants.SYSTEM_SERVICE, fallbackFactory = RemoteUserFallbackFactory.class)
public interface RemoteUserService {
    /**
     * 通过用户名查询用户信息
     *
     * @param username 用户名
     * @param source   请求来源
     * @return 结果
     */
    @GetMapping("/user/info/{username}")
    public R<LoginUser> getUserInfo(@PathVariable("username") String username, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);


    @GetMapping("/user/byNick/{nickName}")
    public R<SysUser> getUserInfoByNickName(@PathVariable("nickName") String nickName, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);


    /**
     * 通过用户id查询用户信息
     * @param userId
     * @param source
     * @return
     */
    @GetMapping("/user/byId/{userId}")
    public R<SysUser> getUserInfoById(@PathVariable("userId") Long userId, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * 通过角色id查询角色信息
     * @param roleId
     * @param source
     * @return
     */
    @GetMapping("/role/byId/{roleId}")
    public R<SysRole> getRoleInfoById(@PathVariable("roleId") Long roleId, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * 通过用户id查询用户信息
     *
     * @param userId 用户名
     * @param source 请求来源
     * @return 结果
     */
    @GetMapping("/personalCenter/getInnerUserCenterInfo")
    public R<SysUser> getUserCenterInfo(@RequestParam Long userId, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    // 批量获取用户信息
    @PostMapping("/personalCenter/getUserCenterInfoList")
    public R<List<SysUser>> getUserCenterInfoList(@RequestBody List<Long> userIdList, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);
    /**
     * 注册用户信息
     *
     * @param sysUser 用户信息
     * @param source  请求来源
     * @return 结果
     */
    @PostMapping("/user/register")
    public R<Boolean> registerUserInfo(@RequestBody SysUser sysUser, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * 记录用户登录IP地址和登录时间
     *
     * @param sysUser 用户信息
     * @param source  请求来源
     * @return 结果
     */
    @PutMapping("/user/recordlogin")
    public R<Boolean> recordUserLogin(@RequestBody SysUser sysUser, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * 获取字段数据
     */
    @GetMapping("/dict/data/type/{dictType}")
    public R<List<SysDictData>> dictType(@PathVariable("dictType") String dictType, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * 修改身份认证状态
     */
    @PostMapping("/identityInfo/updateIdentityInfoStatus")
    public R<Integer> updateIdentityInfoStatus(@RequestBody IdentityInfo identityInfo, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * 查询身份认证信息列表
     */
    @PostMapping("/identityInfo/getInnerIdentityInfoDetail/{authId}")
    public R<IdentityInfo> getInnerIdentityInfoDetail(@PathVariable Long authId, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * 用户授权
     */
    @PostMapping("/personalCenter/saveUserAuthorization")
    public R<Void> saveUserAuthorization(@RequestBody UserAuthorization userAuthorization, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * 获取院校信息
     */
    @GetMapping("/school/getNationwideCollegeInfoInfo/{id}")
    public R<NationwideCollegeInfo> getNationwideCollegeInfoInfo(@PathVariable("id") String id, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * 获取院校信息
     */
    @GetMapping("/school/getNationwideCollegeInfoInfoByName/{name}")
    public R<NationwideCollegeInfo> getNationwideCollegeInfoInfoByName(@PathVariable("name") String name, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * 批量新增学生用户
     */
    @PostMapping("/user/saveStudentUserInfo")
    public R<Map<String,Map<String,Object>>> saveStudentUserInfo(@RequestBody List<SysUser> sysUserList, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * 获取双一流院校信息
     *
     * @param source
     * @return
     */
    @GetMapping("/school/getDoubleFirstClassUniversityPlan")
    public R<List<NationwideCollegeInfo>> getDoubleFirstClassUniversityPlan(@RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    // 远程调用实名认证
    @PostMapping("/authInfo/saveInnerAuthInfo")
    public R<Map<String, Object>> saveInnerAuthInfo(@RequestBody AuthInfo authInfo, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    // 查询实名认证信息
    @PostMapping("/authInfo/selectAuthInfoByIdCard")
    public R<List<AuthInfo>> selectAuthInfoByIdCard(@RequestBody AuthInfo authInfo, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    // 远程调用实名认证
    @PostMapping("/teacherTmpInfo/insertTeacherTmpInfo")
    public R<Void> insertTeacherTmpInfo(@RequestHeader(SecurityConstants.FROM_SOURCE) String source);


    @GetMapping("/authInfo/selectAuthInfoByName")
    public R<List<AuthInfo>> selectAuthInfoByName(@RequestParam String realName,@RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * 更新用户组下的人员信息
     * @param groupId
     * @param source
     * @return
     */
    @GetMapping( "/userGroup/updateUserIdsByUserGroup")
    public R<Void> updateUserIdsByUserGroup(@RequestParam(required = false) Long groupId, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    @GetMapping( "/userGroup/getGroupNames/ids")
    public R<String> getGroupNames(@RequestParam List<Long> groupIds, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    // 修改报名个人信息
    @PostMapping( "/user/updateApplyInfoUser")
    public R<SysUser> updateApplyInfoUser(@RequestBody SysUser user, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    // 增删报名个人信息
    @PostMapping( "/user/updateAddApplyInfoUser")
    public R<Integer> updateAddApplyInfoUser(@RequestBody List<SysUser> userList, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    // 查询个人信息
    @PostMapping( "/user/selectUserByPhoneCheck")
    public R<List<SysUser>> selectUserByPhoneCheck(@RequestBody SysUser user, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    // 保存导出文件列表
    @PostMapping( "/exportManage/saveOssExportFile")
    public R<Long> saveOssExportFile(@RequestBody Map<String,Object> fileParam, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    // 更新导出文件列表
    @PostMapping( "/exportManage/updateExportManageInner")
    public R<Void> updateExportManageInner(@RequestBody Map<String,Object> fileParam, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    // 保存微信小程序用户信息
    @PostMapping( "/user/registerWxSysUser")
    public R<Long> registerWxSysUser(@RequestBody SysUser sysUser, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    // 查询微信小程序用户信息
    @GetMapping( "/user/queryWxSysUser")
    public R<SysUser> queryWxSysUser(@RequestParam String openId, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);
    //处理pdf
    @GetMapping( "/fileUploadManager/byFileTaskId/{fileTaskId}")
    public R<Void> handlePdf(@PathVariable String fileTaskId, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);


    @GetMapping( "/user/updateNoRegisterUser")
    public R<Void> updateNoRegisterUser(@RequestParam String updateSize, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);
}
