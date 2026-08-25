package com.teaching.system.service;

import java.util.List;
import java.util.Map;

import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.web.page.TableDataInfo;
import com.teaching.system.api.domain.SysUser;

/**
 * 用户 业务层
 *
 * @author teaching
 */
public interface ISysUserService
{
    /**
     * 根据条件分页查询用户列表
     *
     * @param user 用户信息
     * @return 用户信息集合信息
     */
    public List<SysUser> selectUserList(SysUser user);

    /**
     * 根据条件分页查询用户列表，附带身份认证信息和实名认证信息
     * @param user
     * @return
     */
    public TableDataInfo selectUserInfoList(SysUser user);

    /**
     * 导出用
     * @param user
     * @return
     */
    public List<SysUser> getSysUserList(SysUser user);

    /**
     * 根据条件分页查询已分配用户角色列表
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

    /**
     * 通过用户手机号或者邮箱或者用户名信息查询用户
     *
     * @param user 用户信息
     * @return 用户对象信息
     */
    public SysUser selectUserByUserInfo(SysUser user);

    /**
     * 通过用户ID查询用户
     *
     * @param userId 用户ID
     * @return 用户对象信息
     */
    public SysUser selectUserById(Long userId);

    /**
     * 根据用户ID列表查询用户。
     *
     * @param userIds 用户ID列表
     * @return 用户信息集合
     */
    public List<SysUser> selectUserByIds(List<Long> userIds);

    /**
     * 根据用户ID查询用户所属角色组
     *
     * @param userName 用户名
     * @return 结果
     */
    public String selectUserRoleGroup(String userName);

    /**
     * 根据用户ID查询用户所属岗位组
     *
     * @param userName 用户名
     * @return 结果
     */
    public String selectUserPostGroup(String userName);

    /**
     * 校验用户名称是否唯一
     *
     * @param user 用户信息
     * @return 结果
     */
    public boolean checkUserNameUnique(SysUser user);

    /**
     * 获取学生用户信息
     *
     * @param user 用户信息
     * @return 结果
     */
    public Boolean checkUserNameStudent(SysUser user);

    /**
     * 校验手机号码是否唯一
     *
     * @param user 用户信息
     * @return 结果
     */
    public boolean checkPhoneUnique(SysUser user);

    /**
     * 校验email是否唯一
     *
     * @param user 用户信息
     * @return 结果
     */
    public boolean checkEmailUnique(SysUser user);

    /**
     * 校验用户是否允许操作
     *
     * @param user 用户信息
     */
    public void checkUserAllowed(SysUser user);

    /**
     * 校验用户是否有数据权限
     *
     * @param userId 用户id
     */
    public void checkUserDataScope(Long userId);

    /**
     * 新增用户信息
     *
     * @param user 用户信息
     * @return 结果
     */
    public int insertUser(SysUser user);

    /**
     * 注册用户信息
     *
     * @param user 用户信息
     * @return 结果
     */
    public boolean registerUser(SysUser user);

    /**
     * 修改用户信息
     *
     * @param user 用户信息
     * @return 结果
     */
    public int updateUser(SysUser user);

    /**
     * 修改用户密码
     *
     * @param user 用户信息
     * @return 结果
     */
    public int updateUserPwd(SysUser user);

    public int updateUserStudent(SysUser user);

    /**
     * 用户授权角色
     *
     * @param userId 用户ID
     * @param roleIds 角色组
     */
    public void insertUserAuth(Long userId, Long[] roleIds);

    /**
     * 修改用户状态
     *
     * @param user 用户信息
     * @return 结果
     */
    public int updateUserStatus(SysUser user);


    /**
     * 修改用户基本信息
     *
     * @param user 用户信息
     * @return 结果
     */
    public boolean updateUserProfile(SysUser user);

    /**
     * 修改报名申请信息
     * @param user
     * @return
     */
    public SysUser updateApplyInfoUser(SysUser user);

    /**
     * 报名信息增减人修改
     * @param sysUsers
     * @return
     */
    public int updateAddApplyInfoUser(List<SysUser> sysUsers);

    /**
     * 修改用户头像
     *
     * @param userId 用户ID
     * @param avatar 头像地址
     * @return 结果
     */
    public boolean updateUserAvatar(Long userId, String avatar);

    /**
     * 更新用户登录信息（IP和登录时间）
     *
     * @param user 用户信息
     * @return 结果
     */
    public boolean updateLoginInfo(SysUser user);

    /**
     * 重置用户密码
     *
     * @param user 用户信息
     * @return 结果
     */
    public int resetPwd(SysUser user);

    /**
     * 重置用户密码
     *
     * @param userId 用户ID
     * @param password 密码
     * @return 结果
     */
    public int resetUserPwd(Long userId, String password);

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
     * 导入用户数据
     *
     * @param userList 用户数据列表
     * @param isUpdateSupport 是否更新支持，如果已存在，则进行更新数据
     * @param operName 操作用户
     * @return 结果
     */
    public String importUser(List<SysUser> userList, Boolean isUpdateSupport, String operName);

    /**
     * 根据机构ID和角色ID查询用户列表
     * @param orgId
     * @param roleId
     * @return
     */
    public List<Map<String,Object>> getUserListByOrgAndRoleId(Long orgId, Long roleId);

    /**
     * 获取提交人
     * @param type
     * @return
     */
    public List<Map<String, Object>> getSubmitUser(String type);

    Map<String,Map<String,Object>> saveUserInfo(List<SysUser> sysUsers);

    /**
     * 根据条件查询用户列表 用户组使用
     * @param map
     * @return
     */
    List<Map<String,Object>>getUserListForUserGroup(Map<String,String> map);

    /**
     * 根据条件查询用户列表 手机号验证使用
     * @param user
     * @return
     */
    List<SysUser> selectUserByPhoneCheck(SysUser user);

    /**
     * 注册微信用户信息
     *
     * @param user 用户信息
     * @return 结果
     */
    public Long registerWxUser(SysUser user);

    /**
     * 根据微信openId查询用户信息
     * @param openId
     * @return
     */
    public SysUser selectWxUser(String openId);

    public void updateNoRegisterUser(String updateSize);
}
