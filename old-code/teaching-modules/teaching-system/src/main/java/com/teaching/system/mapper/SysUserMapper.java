package com.teaching.system.mapper;

import com.teaching.system.api.domain.SysUser;
import com.teaching.system.domain.SpecialistSysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 用户表 数据层
 *
 * @author teaching
 */
@Mapper
public interface SysUserMapper {
    /**
     * 根据条件分页查询用户列表
     *
     * @param sysUser 用户信息
     * @return 用户信息集合信息
     */
    public List<SysUser> selectUserList(SysUser sysUser);

    // 获取专家用户信息
    public List<SpecialistSysUser> selectSpecialUserList(SpecialistSysUser specialistSysUserReq);

    /**
     * 根据条件查询用户列表 返回一对一实名认证状态、一对多身份认证类型
     *
     * @param sysUser
     * @return
     */
    public List<SysUser> selectUserWithAuthAndIdentity(SysUser sysUser);

    /**
     * 根据条件分页查询已配用户角色列表
     *
     * @param user 用户信息
     * @return 用户信息集合信息
     */
    public List<SysUser> selectAllocatedList(SysUser user);

    /**
     * 根据条件分页查询未分配用户角色列表
     *
     * @param user 用户信息
     * @return 用户信息集合信息
     */
    public List<SysUser> selectUnallocatedList(SysUser user);

    /**
     * 通过用户名查询用户
     *
     * @param userName 用户名
     * @return 用户对象信息
     */
    public SysUser selectUserByUserName(String userName);

    public SysUser selectUserByNickName(String nickName);

    public SysUser selectUserByUserInfo(SysUser user);

    public SysUser selectWxUserByOpenId(String openId);

    /**
     * 通过用户ID查询用户
     *
     * @param userId 用户ID
     * @return 用户对象信息
     */
    public SysUser selectUserById(Long userId);

    public List<SysUser> selectUserNoRegisterUser();

    public SysUser selectUserByPhone(@Param("phone") String phone, @Param("email") String email);

    public List<SysUser> selectUserByPhoneOrEmail(SysUser user);

    public List<SysUser> selectUserByPhoneAndName(@Param("phone") String phone, @Param("name") String name);

    public Integer selectUserByPhoneCheck(@Param("phone") String phone, @Param("email") String email);

    public List<SysUser> selectUserByIds(@Param("list") List<Long> userIdList);

    /**
     * 新增用户信息
     *
     * @param user 用户信息
     * @return 结果
     */
    public int insertUser(SysUser user);

    /**
     * 批量插入用户信息
     *
     * @return 插入结果
     */
    public int batchInsertUser(@Param("userList") List<SysUser> userList);

    /**
     * 修改用户信息
     *
     * @param user 用户信息
     * @return 结果
     */
    public int updateUser(SysUser user);

    public int updateUserStudent(SysUser user);

    /**
     * 修改用户头像
     *
     * @param userId 用户ID
     * @param avatar 头像地址
     * @return 结果
     */
    public int updateUserAvatar(@Param("userId") Long userId, @Param("avatar") String avatar);

    /**
     * 修改用户状态
     *
     * @param userId 用户ID
     * @param status 状态
     * @return 结果
     */
    public int updateUserStatus(@Param("userId") Long userId, @Param("status") String status);

    /**
     * 更新用户登录信息（IP和登录时间）
     *
     * @param user 用户信息
     * @return 结果
     */
    public int updateLoginInfo(SysUser user);

    /**
     * 重置用户密码
     *
     * @param userId   用户ID
     * @param password 密码
     * @return 结果
     */
    public int resetUserPwd(@Param("userId") Long userId, @Param("password") String password);

    /**
     * 通过用户ID删除用户
     *
     * @param userId 用户ID
     * @return 结果
     */
    public int deleteUserById(Long userId);

    /**
     * 批量删除用户信息
     *
     * @param userIds 需要删除的用户ID
     * @return 结果
     */
    public int deleteUserByIds(Long[] userIds);

    /**
     * 校验用户名称是否唯一
     *
     * @param userName 用户名称
     * @return 结果
     */
    public SysUser checkUserNameUnique(String userName);

    /**
     * 校验手机号码是否唯一
     *
     * @param phonenumber 手机号码
     * @return 结果
     */
    public SysUser checkPhoneUnique(String phonenumber);

    public SysUser checkUserNameStudentByPhone(String phonenumber);

    public SysUser checkUserNameStudentByEmail(String email);

    /**
     * 校验email是否唯一
     *
     * @param email 用户邮箱
     * @return 结果
     */
    public SysUser checkEmailUnique(String email);

    public List<SysUser> selectUserIdentityInfo(String certificationType, String userName);

    /**
     * 根据机构ID和角色ID查询用户列表
     *
     * @param orgId
     * @param roleId
     * @return
     */
    public List<Map<String, Object>> selectUserListByOrgAndRoleId(@Param("orgId") Long orgId, @Param("roleId") Long roleId);

    /**
     * 查询提交审核的用户
     *
     * @param submit 对应提交审核的权限字符串
     * @return
     */
    public List<Map<String, Object>> selectSubmitUser(@Param("submit") String submit);

    /**
     * 根据角色key获取审核用户名称
     *
     * @param rolekey
     * @return
     */
    public String selectAuditUserNamesByRoleKey(String rolekey);

    /**
     * 根据机构ID获取审核用户名称
     *
     * @param orgId
     * @return
     */
    public String selectAuditUserNamesByOrgId(String orgId);

    /**
     * 根据机构ID和角色key获取审核用户名称
     *
     * @param orgId
     * @param roleKey
     * @return
     */
    public String selectAuditUserNamesByOrgAndRoleKey(@Param("orgId") long orgId, @Param("roleKey") String roleKey);

    /**
     * 根据学校ID获取教师列表
     *
     * @param schoolId
     * @return
     */
    List<Map<String, Object>> selectTeacherListBySchoolId(String schoolId);

    /**
     * 获取用户信息 用户组管理使用
     *
     * @param map
     * @return
     */
    List<Map<String, Object>> selectUserListForUserGroup(Map<String, String> map);

    /**
     * 根据用户ids获取用户信息列表
     *
     * @param userIds
     * @return
     */
    List<Map<String, Object>> selectUserListByUserGroupIds(@Param("userIdList") List<Long> userIds);
}
