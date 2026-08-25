package com.teaching.system.service.impl;

import com.github.pagehelper.PageInfo;
import com.teaching.common.core.constant.Constants;
import com.teaching.common.core.constant.HttpStatus;
import com.teaching.common.core.constant.TdConstants;
import com.teaching.common.core.constant.UserConstants;
import com.teaching.common.core.exception.ServiceException;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.core.utils.PageUtils;
import com.teaching.common.core.utils.SpringUtils;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.common.core.utils.bean.BeanValidators;
import com.teaching.common.core.web.page.PageDomain;
import com.teaching.common.core.web.page.TableDataInfo;
import com.teaching.common.core.web.page.TableSupport;
import com.teaching.common.datascope.annotation.DataScope;
import com.teaching.common.redis.service.RedisService;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.system.api.domain.*;
import com.teaching.system.domain.SysPost;
import com.teaching.system.domain.SysUserOrg;
import com.teaching.system.domain.SysUserRole;
import com.teaching.system.mapper.*;
import com.teaching.system.service.*;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 用户 业务层处理
 *
 * @author teaching
 */
@Service
public class SysUserServiceImpl implements ISysUserService {
    private static final Logger log = LoggerFactory.getLogger(SysUserServiceImpl.class);

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private SysRoleMapper roleMapper;

    @Autowired
    private SysPostMapper postMapper;

    @Autowired
    private SysUserRoleMapper userRoleMapper;

    @Autowired
    private SysUserPostMapper userPostMapper;

    @Autowired
    private ISysConfigService configService;

    @Autowired
    protected Validator validator;

    @Autowired
    private IdentityInfoMapper identityInfoMapper;

    @Autowired
    private AuthInfoMapper authInfoMapper;

    @Autowired
    private ISysOrgService orgService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private SysUserOrgMapper userOrgMapper;

    @Autowired
    private ISysDictTypeService dictTypeService;

    @Autowired
    private SysAsyncService sysAsyncService;

    /**
     * 根据条件分页查询用户列表
     *
     * @param user 用户信息
     * @return 用户信息集合信息
     */
    @Override
    @DataScope(orgAlias = "o", userAlias = "u")
    public List<SysUser> selectUserList(SysUser user) {
        List<SysUser> sysUserList = userMapper.selectUserList(user);
        if (!CollectionUtils.isEmpty(sysUserList)) {
            sysUserList.stream().forEach(sysUser -> {
                // 身份认证信息
                IdentityInfo identityInfo = new IdentityInfo();
                identityInfo.setUserId(sysUser.getUserId());
                identityInfo.setCheckStatus(Constants.IDENTITY_AUTH_PASS);
                sysUser.setIdentityInfoList(identityInfoMapper.selectIdentityInfoList(identityInfo));
                // 实名认证信息
                sysUser.setAuthInfo(authInfoMapper.selectAuthInfoByUserId(sysUser.getUserId()));
            });
        }
        return sysUserList;
    }

    /**
     * 根据条件分页查询用户列表，附带身份认证信息和实名认证信息
     *
     * @param user
     * @return
     */
    @Override
    @DataScope(orgAlias = "o", userAlias = "u")
    public TableDataInfo selectUserInfoList(SysUser user) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        Integer pageNum = pageDomain.getPageNum();
        Integer pageSize = pageDomain.getPageSize();
        List<SysUser> sysUsers = userMapper.selectUserWithAuthAndIdentity(user);
        List<SysUser> paginate = PageUtils.paginate(filter(sysUsers, user), pageNum, pageSize);
        TableDataInfo rspData = new TableDataInfo();
        rspData.setCode(HttpStatus.SUCCESS);
        rspData.setRows(paginate);
        rspData.setMsg("查询成功");
        rspData.setTotal(new PageInfo(filter(sysUsers,user)).getTotal());
        return rspData;
    }

    /**
     * 导出用
     *
     * @param user
     * @return
     */
    @Override
    @DataScope(orgAlias = "o", userAlias = "u")
    public List<SysUser> getSysUserList(SysUser user) {
        String exportType = user.getExportType();
        List<SysUser> sysUsers = userMapper.selectUserWithAuthAndIdentity(user);
        List<SysUser> filter = "all".equals(exportType) ? sysUsers : filter(sysUsers, user);
        if (CollectionUtils.isEmpty(filter)) {
            return filter;
        }
        List<SysDictData> dictInfos = dictTypeService.selectDictDataByType("certification_type");
        Map<String, SysDictData> collect = dictInfos.stream().
                collect(Collectors.toMap(SysDictData::getDictValue, SysDictData -> SysDictData));
        filter.forEach(sysUser -> {
            sysUser.setRealName(StringUtils.isNotBlank(sysUser.getNickName())?sysUser.getNickName():sysUser.getUserName());
            AuthInfo authInfo = sysUser.getAuthInfo();
            if (authInfo != null) {
                sysUser.setRealName(authInfo.getRealName());
                sysUser.setAuthStatus(authInfo.getAuthStatus());
            }
            String types = sysUser.getIdentityInfoList().stream().map(e -> {
                String certificationType = e.getCertificationType();
                if (StringUtils.isNotBlank(e.getInstitute())) {
                    sysUser.setInstitute(e.getInstitute());
                }
                return collect.get(certificationType).getDictLabel();
            }).collect(Collectors.joining(", "));
            sysUser.setIdentityTypes(types);
        });
        return filter;
    }

    /**
     * 查询过滤
     *
     * @param sysUsers
     * @param user
     * @return
     */
    private List<SysUser> filter(List<SysUser> sysUsers, SysUser user) {
        if(!CollectionUtils.isEmpty(sysUsers)){
            sysUsers.forEach(sysUser -> {
                sysUser.setRealName(StringUtils.isNotBlank(sysUser.getNickName())?sysUser.getNickName():sysUser.getUserName());
                AuthInfo authInfo = sysUser.getAuthInfo();
                if (authInfo != null) {
                    sysUser.setRealName(authInfo.getRealName());
                    sysUser.setAuthStatus(authInfo.getAuthStatus());
                }
                if (!CollectionUtils.isEmpty(sysUser.getIdentityInfoList())) {
                    for (IdentityInfo e : sysUser.getIdentityInfoList()) {
                        if (StringUtils.isNotBlank(e.getInstitute())) {
                            sysUser.setInstitute(e.getInstitute());
                            break;
                        }
                    }
                }
            });
        }
        String targetCertType = user.getCertificationType();
        if (StringUtils.isNotNull(targetCertType)) {
            sysUsers = sysUsers.stream()
                    .filter(u -> {
                        List<IdentityInfo> identityList = u.getIdentityInfoList();
                        if (CollectionUtils.isEmpty(identityList)) {
                            return false;
                        }
                        return identityList.stream()
                                .anyMatch(i -> targetCertType.equals(i.getCertificationType()));
                    })
                    .collect(Collectors.toList());
        }
        if (StringUtils.isNotBlank(user.getRealName())) {
            sysUsers = sysUsers.stream()
                    .filter(u -> u.getRealName().contains(user.getRealName().trim()))
                    .collect(Collectors.toList());
        }
        if (StringUtils.isNotBlank(user.getSchoolName())) {
            sysUsers = sysUsers.stream()
                    .filter(u -> {
                        String schoolName = u.getSchoolName();
                        if (StringUtils.isBlank(schoolName)) {
                            return false;
                        }
                        return schoolName.contains(user.getSchoolName().trim());
                    })
                    .collect(Collectors.toList());
        }
        if (StringUtils.isNotBlank(user.getPosition())) {
            sysUsers = sysUsers.stream()
                    .filter(u -> {
                        String position = u.getPosition();
                        if (StringUtils.isBlank(position)) {
                            return false;
                        }
                        return position.contains(user.getPosition().trim());
                    })
                    .collect(Collectors.toList());
        }
        return sysUsers;
    }

    /**
     * 根据条件分页查询已分配用户角色列表
     *
     * @param user 用户信息
     * @return 用户信息集合信息
     */
    @Override
    @DataScope(orgAlias = "o", userAlias = "u")
    public List<SysUser> selectAllocatedList(SysUser user) {
        return userMapper.selectAllocatedList(user);
    }

    /**
     * 根据条件分页查询未分配用户角色列表
     *
     * @param user 用户信息
     * @return 用户信息集合信息
     */
    @Override
    @DataScope(orgAlias = "o", userAlias = "u")
    public List<SysUser> selectUnallocatedList(SysUser user) {
        return userMapper.selectUnallocatedList(user);
    }

    /**
     * 通过用户名查询用户
     *
     * @param userName 用户名
     * @return 用户对象信息
     */
    @Override
    public SysUser selectUserByUserName(String userName) {
        return userMapper.selectUserByUserName(userName);
    }

    @Override
    public SysUser selectUserByNickName(String nickName) {
        return userMapper.selectUserByNickName(nickName);
    }

    @Override
    public SysUser selectUserByUserInfo(SysUser user) {
        return userMapper.selectUserByUserInfo(user);
    }

    /**
     * 通过用户ID查询用户
     *
     * @param userId 用户ID
     * @return 用户对象信息
     */
    @Override
    public SysUser selectUserById(Long userId) {
        return userMapper.selectUserById(userId);
    }

    @Override
    public List<SysUser> selectUserByIds(List<Long> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return Collections.emptyList();
        }
        List<Long> ids = userIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        return CollectionUtils.isEmpty(ids) ? Collections.emptyList() : userMapper.selectUserByIds(ids);
    }

    /**
     * 查询用户所属角色组
     *
     * @param userName 用户名
     * @return 结果
     */
    @Override
    public String selectUserRoleGroup(String userName) {
        List<SysRole> list = roleMapper.selectRolesByUserName(userName);
        if (CollectionUtils.isEmpty(list)) {
            return StringUtils.EMPTY;
        }
        return list.stream().map(SysRole::getRoleName).collect(Collectors.joining(","));
    }

    /**
     * 查询用户所属岗位组
     *
     * @param userName 用户名
     * @return 结果
     */
    @Override
    public String selectUserPostGroup(String userName) {
        List<SysPost> list = postMapper.selectPostsByUserName(userName);
        if (CollectionUtils.isEmpty(list)) {
            return StringUtils.EMPTY;
        }
        return list.stream().map(SysPost::getPostName).collect(Collectors.joining(","));
    }

    /**
     * 校验用户名称是否唯一
     *
     * @param user 用户信息
     * @return 结果
     */
    @Override
    public boolean checkUserNameUnique(SysUser user) {
        Long userId = StringUtils.isNull(user.getUserId()) ? -1L : user.getUserId();
        SysUser info = userMapper.checkUserNameUnique(user.getUserName());
        if (StringUtils.isNotNull(info) && info.getUserId().longValue() != userId.longValue()) {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    @Override
    public Boolean checkUserNameStudent(SysUser user) {
        if(StringUtils.isNotEmpty(user.getPhonenumber())){
            SysUser info = userMapper.checkUserNameStudentByPhone(user.getPhonenumber());
            if(Objects.nonNull(info) && "import".equals(info.getUserSources())){
                return true;
            }
            return false;
        }
        if(StringUtils.isNotEmpty(user.getEmail())){
            SysUser info = userMapper.checkUserNameStudentByEmail(user.getEmail());
            if(Objects.nonNull(info) &&"import".equals(info.getUserSources())){
                return true;
            }
            return false;
        }
        return false;
    }

    /**
     * 校验手机号码是否唯一
     *
     * @param user 用户信息
     * @return
     */
    @Override
    public boolean checkPhoneUnique(SysUser user) {
        Long userId = StringUtils.isNull(user.getUserId()) ? -1L : user.getUserId();
        SysUser info = userMapper.checkPhoneUnique(user.getPhonenumber());
        if(StringUtils.isNotNull(info) && "import".equals(info.getUserSources())){
            return UserConstants.UNIQUE;
        }
        if (StringUtils.isNotNull(info) && info.getUserId().longValue() != userId.longValue()) {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 校验email是否唯一
     *
     * @param user 用户信息
     * @return
     */
    @Override
    public boolean checkEmailUnique(SysUser user) {
        Long userId = StringUtils.isNull(user.getUserId()) ? -1L : user.getUserId();
        SysUser info = userMapper.checkEmailUnique(user.getEmail());
        if(StringUtils.isNotNull(info) && "import".equals(info.getUserSources())){
            return UserConstants.UNIQUE;
        }
        if (StringUtils.isNotNull(info) && info.getUserId().longValue() != userId.longValue()) {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 校验用户是否允许操作
     *
     * @param user 用户信息
     */
    @Override
    public void checkUserAllowed(SysUser user) {
        if (StringUtils.isNotNull(user.getUserId()) && user.isAdmin()) {
            throw new ServiceException("不允许操作超级管理员用户");
        }
    }

    /**
     * 校验用户是否有数据权限
     *
     * @param userId 用户id
     */
    @Override
    public void checkUserDataScope(Long userId) {
        if (!SysUser.isAdmin(SecurityUtils.getUserId())) {
            SysUser user = new SysUser();
            user.setUserId(userId);
            List<SysUser> users = SpringUtils.getAopProxy(this).selectUserList(user);
            if (StringUtils.isEmpty(users)) {
                throw new ServiceException("没有权限访问用户数据！");
            }
        }
    }

    /**
     * 新增保存用户信息
     *
     * @param user 用户信息
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertUser(SysUser user) {
        // 新增用户信息
        int rows = userMapper.insertUser(user);
        // 新增用户机构关联
        insertUserOrg(user);
        // 新增用户与角色管理
        insertUserRole(user);
        return rows;
    }

    /**
     * 注册用户信息
     *
     * @param user 用户信息
     * @return 结果
     */
    @Override
    public boolean registerUser(SysUser user) {
        boolean registered = userMapper.insertUser(user) > 0;
        if (registered && user.getUserId() != null && StringUtils.isNotBlank(user.getPhonenumber())) {
            sysAsyncService.bindTeacherCompetitionUser(user.getUserId(), user.getPhonenumber());
        }
        return registered;
    }

    /**
     * 修改保存用户信息
     *
     * @param user 用户信息
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateUser(SysUser user) {
        Long userId = user.getUserId();
//        // 删除用户与角色关联
//        userRoleMapper.deleteUserRoleByUserId(userId);
//        // 新增用户与角色管理
//        insertUserRole(user);
        if ("2".equals(user.getUserType())) {
            // 删除用户与机构关联
            userOrgMapper.deleteSysUserOrgByUserId(userId);
            // 新增用户与机构
            insertUserOrg(user);
        }
        return userMapper.updateUser(user);
    }

    @Override
    public int updateUserPwd(SysUser user) {
        return userMapper.updateUser(user);
    }

    @Override
    public int updateUserStudent(SysUser user) {
        return userMapper.updateUserStudent(user);
    }

    /**
     * 用户授权角色
     *
     * @param userId  用户ID
     * @param roleIds 角色组
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void insertUserAuth(Long userId, Long[] roleIds) {
        // 用户存在锁定角色，非超管不得修改删除
        // 记录非独立角色数
        AtomicInteger nonExclusionRoleCount = new AtomicInteger();
        Arrays.stream(roleIds).toList().forEach(roleId -> {
            SysRole role = roleMapper.selectRoleById(roleId);
            if (!role.isExclusionFlag()) {
                nonExclusionRoleCount.getAndIncrement();
            }
            if (role.isExclusionFlag() && nonExclusionRoleCount.get() != 0) {
                throw new ServiceException("独立的角色与非独立角色不能同时存在");
            }
        });
        userRoleMapper.deleteUserRoleByUserId(userId);
        insertUserRole(userId, roleIds);
    }

    /**
     * 修改用户状态
     *
     * @param user 用户信息
     * @return 结果
     */
    @Override
    public int updateUserStatus(SysUser user) {
        return userMapper.updateUserStatus(user.getUserId(), user.getStatus());
    }

    /**
     * 修改用户基本信息
     *
     * @param user 用户信息
     * @return 结果
     */
    @Override
    public boolean updateUserProfile(SysUser user) {
        return userMapper.updateUser(user) > 0;
    }

    @Override
    public SysUser updateApplyInfoUser(SysUser user) {
        // 修改手机号或者邮箱时已经存在，将已存在手机号对应得user_id更新报名表
        // 查老的数据
        SysUser sysUser = userMapper.selectUserByPhone(user.getPhonenumber(), user.getEmail());
        // 更新导入报名表时执行定时任务后得用户数据
        userMapper.updateUser(user);
        if(Objects.nonNull(sysUser)){
            // 返回发现修改手机号或邮箱时候查到修改得数据已经注册过，将注册过得用户id返回给报名表，进行更新对应到新得用户id
            return sysUser;
        }
        return null;
    }

    @Override
    public int updateAddApplyInfoUser(List<SysUser> sysUsers) {
        if(!CollectionUtils.isEmpty(sysUsers)){
            sysUsers.stream().forEach(sysUser -> {
                if ("2".equals(sysUser.getDelFlag())) {
                    userMapper.updateUser(sysUser);
                } else {
                    userMapper.insertUser(sysUser);
                }
            });
        }
        return 0;
    }

    /**
     * 修改用户头像
     *
     * @param userId 用户ID
     * @param avatar 头像地址
     * @return 结果
     */
    @Override
    public boolean updateUserAvatar(Long userId, String avatar) {
        return userMapper.updateUserAvatar(userId, avatar) > 0;
    }

    /**
     * 更新用户登录信息（IP和登录时间）
     *
     * @param user 用户信息
     * @return 结果
     */
    public boolean updateLoginInfo(SysUser user) {
        return userMapper.updateLoginInfo(user) > 0;
    }

    /**
     * 重置用户密码
     *
     * @param user 用户信息
     * @return 结果
     */
    @Override
    public int resetPwd(SysUser user) {
        return userMapper.resetUserPwd(user.getUserId(), user.getPassword());
    }

    /**
     * 重置用户密码
     *
     * @param userId   用户ID
     * @param password 密码
     * @return 结果
     */
    @Override
    public int resetUserPwd(Long userId, String password) {
        return userMapper.resetUserPwd(userId, password);
    }

    /**
     * 新增用户角色信息
     *
     * @param user 用户对象
     */
    public void insertUserRole(SysUser user) {
        this.insertUserRole(user.getUserId(), user.getRoleIds());
    }

    /**
     * 新增用户岗位信息
     *
     * @param user 用户对象
     */
    public void insertUserOrg(SysUser user) {
        Long[] posts = user.getOrgIds();
        if (StringUtils.isNotEmpty(posts)) {
            // 新增用户与岗位管理
            List<SysUserOrg> list = new ArrayList<SysUserOrg>();
            for (Long orgId : posts) {
                SysUserOrg up = new SysUserOrg();
                up.setUserId(user.getUserId());
                up.setOrgId(orgId);
                list.add(up);
            }
            userOrgMapper.batchUserOrg(list);
        }
    }

    /**
     * 新增用户角色信息
     *
     * @param userId  用户ID
     * @param roleIds 角色组
     */
    public void insertUserRole(Long userId, Long[] roleIds) {
        if (StringUtils.isNotEmpty(roleIds)) {
            // 新增用户与角色管理
            List<SysUserRole> list = new ArrayList<SysUserRole>();
            for (Long roleId : roleIds) {
                SysUserRole ur = new SysUserRole();
                ur.setUserId(userId);
                ur.setRoleId(roleId);
                list.add(ur);
            }
            userRoleMapper.batchUserRole(list);
        }
    }

    /**
     * 通过用户ID删除用户
     *
     * @param userId 用户ID
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteUserById(Long userId) {
        // 删除用户与角色关联
        userRoleMapper.deleteUserRoleByUserId(userId);
        // 删除用户与岗位表
        userPostMapper.deleteUserPostByUserId(userId);
        return userMapper.deleteUserById(userId);
    }

    /**
     * 批量删除用户信息
     *
     * @param userIds 需要删除的用户ID
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteUserByIds(Long[] userIds) {
        for (Long userId : userIds) {
            checkUserAllowed(new SysUser(userId));
            checkUserDataScope(userId);
        }
        // 删除用户与角色关联
        userRoleMapper.deleteUserRole(userIds);
        // 删除用户与岗位关联
        userPostMapper.deleteUserPost(userIds);
        return userMapper.deleteUserByIds(userIds);
    }

    /**
     * 导入用户数据
     *
     * @param userList        用户数据列表
     * @param isUpdateSupport 是否更新支持，如果已存在，则进行更新数据
     * @param operName        操作用户
     * @return 结果
     */
    @Override
    public String importUser(List<SysUser> userList, Boolean isUpdateSupport, String operName) {
        if (StringUtils.isNull(userList) || userList.size() == 0) {
            throw new ServiceException("导入用户数据不能为空！");
        }
        int successNum = 0;
        int failureNum = 0;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder failureMsg = new StringBuilder();
        for (SysUser user : userList) {
            try {
                // 验证是否存在这个用户
                SysUser u = userMapper.selectUserByUserName(user.getUserName());
                if (StringUtils.isNull(u)) {
                    BeanValidators.validateWithException(validator, user);
                    orgService.checkOrgDataScope(user.getOrgId());
                    String password = configService.selectConfigByKey("sys.user.initPassword");
                    user.setPassword(SecurityUtils.encryptPassword(password));
                    user.setCreateBy(operName);
                    // 设置默认用户类型为后台创建用户
                    if (StringUtils.isEmpty(user.getUserType())) {
                        user.setUserType("01");
                    }
                    userMapper.insertUser(user);
                    successNum++;
                    successMsg.append("<br/>" + successNum + "、账号 " + user.getUserName() + " 导入成功");
                } else if (isUpdateSupport) {
                    BeanValidators.validateWithException(validator, user);
                    checkUserAllowed(u);
                    checkUserDataScope(u.getUserId());
                    orgService.checkOrgDataScope(user.getOrgId());
                    user.setUserId(u.getUserId());
                    user.setOrgId(u.getOrgId());
                    user.setUpdateBy(operName);
                    userMapper.updateUser(user);
                    successNum++;
                    successMsg.append("<br/>" + successNum + "、账号 " + user.getUserName() + " 更新成功");
                } else {
                    failureNum++;
                    failureMsg.append("<br/>" + failureNum + "、账号 " + user.getUserName() + " 已存在");
                }
            } catch (Exception e) {
                failureNum++;
                String msg = "<br/>" + failureNum + "、账号 " + user.getUserName() + " 导入失败：";
                failureMsg.append(msg + e.getMessage());
                log.error(msg, e);
            }
        }
        if (failureNum > 0) {
            failureMsg.insert(0, "很抱歉，导入失败！共 " + failureNum + " 条数据格式不正确，错误如下：");
            throw new ServiceException(failureMsg.toString());
        } else {
            successMsg.insert(0, "恭喜您，数据已全部导入成功！共 " + successNum + " 条，数据如下：");
        }
        return successMsg.toString();
    }

    /**
     * 根据机构ID和角色ID查询用户列表
     *
     * @param orgId
     * @param roleId
     * @return
     */
    @Override
    public List<Map<String, Object>> getUserListByOrgAndRoleId(Long orgId, Long roleId) {
        return userMapper.selectUserListByOrgAndRoleId(orgId, roleId);
    }

    @Override
    public List<Map<String, Object>> getSubmitUser(String type) {
        return userMapper.selectSubmitUser("system".equals(type) ? type : type + ":task:submit");
    }

    @Transactional
    @Override
    public Map<String, Map<String, Object>> saveUserInfo(List<SysUser> sysUserList) {
        Map<String, Map<String, Object>> sysUserMapOuter = new HashMap();
        List<SysUser> sysUserListInner = new ArrayList<>();
        sysUserList.stream().forEach(sysUser -> {
            Map<String, Object> userMapInner = new HashMap();
            log.info("学生用户信息：{}", sysUser);
            try {
                // 如果查出多条先过滤 selectUserByPhoneCheck
                Integer userCount = userMapper.selectUserByPhoneCheck(sysUser.getPhonenumber(), sysUser.getEmail());
                if(userCount <2){
                    SysUser sysUserInner = userMapper.selectUserByPhone(sysUser.getPhonenumber(), sysUser.getEmail());
                    if (Objects.isNull(sysUserInner)) {
                        sysUser.setUserSources(TdConstants.USER_SOURCES_IMPORT);
                        sysUserListInner.add(sysUser);
                    } else {
                        AuthInfo authInfo = authInfoMapper.selectAuthInfoByUserId(sysUserInner.getUserId());
                        sysUserInner.setAuthInfo(authInfo);
//                        sysUserInner.setIdentityInfoList(identityInfoList);
                        userMapInner.put("userId", sysUserInner.getUserId());
                        userMapInner.put("authInfo", sysUser.getAuthInfo().getIdCard());
                        // 如果学生实名认证未通过，则定时任务执行后自动通过
                        if(Objects.nonNull(authInfo) && !authInfo.getAuthStatus().equals(Constants.AUTH_STATUS_PASS)){
                            authInfo.setAuthStatus(Constants.AUTH_STATUS_PASS);
                            authInfo.setUpdateTime(new Date());
                            authInfoMapper.updateAuthInfo(authInfo);
                            SysUser student = new SysUser();
                            student.setAuthStatus(Constants.AUTH_STATUS_PASS);
                            student.setUserId(sysUserInner.getUserId());
                            student.setSchool(sysUser.getSchool());
                            student.setSchoolName(sysUser.getSchoolName());
                            userMapper.updateUser(student);
                        }
                        if(Objects.isNull(authInfo)){
                            AuthInfo studentAuthInfo = sysUser.getAuthInfo();
                            studentAuthInfo.setUserId(sysUserInner.getUserId());
                            studentAuthInfo.setCreateTime(new Date());
                            authInfoMapper.insertAuthInfo(studentAuthInfo);
                            SysUser student = new SysUser();
                            student.setAuthStatus(Constants.AUTH_STATUS_PASS);
                            student.setUserId(sysUserInner.getUserId());
                            student.setSchool(sysUser.getSchool());
                            student.setSchoolName(sysUser.getSchoolName());
                            userMapper.updateUser(student);
                        }
                        // 如果学生身份认证未通过，则定时任务执行后自动通过
                        IdentityInfo identityInfo = new IdentityInfo();
                        identityInfo.setUserId(sysUserInner.getUserId());
                        identityInfo.setCheckStatus(Constants.IDENTITY_AUTH_PASS);
                        identityInfo.setCertificationType(Constants.IDENTITY_TYPE_STUDENT);
                        List<IdentityInfo> identityInfoList = identityInfoMapper.selectIdentityInfoList(identityInfo);
                        if(CollectionUtils.isEmpty(identityInfoList)){
                            sysUser.getIdentityInfo().setUserId(sysUserInner.getUserId());
                            // 自动赋予学生角色
                            UserAuthorization userAuthorization = new UserAuthorization();
                            userAuthorization.setUserId(sysUserInner.getUserId());
                            userAuthorization.setStudentFlag(true);
                            PersonalCenterService personalCenterService = SpringUtils.getBean(PersonalCenterService.class);
                            personalCenterService.saveUserAuthorization(userAuthorization);
                            identityInfoMapper.insertIdentityInfo(sysUser.getIdentityInfo());
                        } else if(!Constants.IDENTITY_AUTH_PASS.equals(identityInfoList.get(0).getCheckStatus())){
                            identityInfoList.get(0).setCheckStatus(Constants.IDENTITY_AUTH_PASS);
                            // 自动赋予学生角色
                            UserAuthorization userAuthorization = new UserAuthorization();
                            userAuthorization.setUserId(sysUserInner.getUserId());
                            userAuthorization.setStudentFlag(true);
                            PersonalCenterService personalCenterService = SpringUtils.getBean(PersonalCenterService.class);
                            personalCenterService.saveUserAuthorization(userAuthorization);
                            identityInfoMapper.updateIdentityInfo(identityInfoList.get(0));
                        }
                        sysUserMapOuter.put(sysUser.getAuthInfo().getIdCard(), userMapInner);
                    }
                } else {
                    log.info("用户信息重复：{}", sysUser);
                }
            } catch (Exception e) {
                log.error("查询学生用户信息异常：{}", e);
            }
        });
        if (!CollectionUtils.isEmpty(sysUserListInner)) {
            userMapper.batchInsertUser(sysUserListInner);
            List<AuthInfo> authInfoList = new ArrayList<>();
            List<IdentityInfo> identityInfoList = new ArrayList<>();
            sysUserListInner.stream().forEach(sysUser -> {
                Map<String, Object> userMapInner = new HashMap();
                // 自动赋予学生角色
                UserAuthorization userAuthorization = new UserAuthorization();
                userAuthorization.setUserId(sysUser.getUserId());
                userAuthorization.setStudentFlag(true);
                PersonalCenterService personalCenterService = SpringUtils.getBean(PersonalCenterService.class);
                personalCenterService.saveUserAuthorization(userAuthorization);
                // 自动实名认证成功
                sysUser.getAuthInfo().setUserId(sysUser.getUserId());
                authInfoList.add(sysUser.getAuthInfo());
                sysUser.getIdentityInfo().setUserId(sysUser.getUserId());
                identityInfoList.add(sysUser.getIdentityInfo());
                userMapInner.put("userId", sysUser.getUserId());
                userMapInner.put("authInfo", sysUser.getAuthInfo().getIdCard());
                sysUserMapOuter.put(sysUser.getAuthInfo().getIdCard(), userMapInner);
            });
            authInfoMapper.batchInsertAuthInfo(authInfoList);
            // 缺少学生证，学生自动注册身份认证为未认证
            identityInfoMapper.batchInsertIdentityInfo(identityInfoList);
        }
        log.info("学生用户信息返回：{}",sysUserMapOuter);
        return sysUserMapOuter;
    }

    /**
     * 根据条件查询用户列表 用户组使用
     * @param map
     * @return
     */
    @Override
    public List<Map<String, Object>> getUserListForUserGroup(Map<String, String> map) {
        //userType 2 C端 0 管理端
        return userMapper.selectUserListForUserGroup(map);
    }

    @Override
    public List<SysUser> selectUserByPhoneCheck(SysUser user) {
        List<SysUser> sysUsers = userMapper.selectUserByPhoneOrEmail(user);
        // 获取实名认证信息
//        if(org.apache.commons.collections4.CollectionUtils.isNotEmpty(sysUsers)){
//            sysUsers.stream().forEach(sysUser -> {
//                if(StringUtils.isNotEmpty(sysUser.getSysUserId())){
//                    sysUser.setAuthInfo(authInfoMapper.selectAuthInfoByUserId(Long.valueOf(sysUser.getSysUserId())));
//                } else {
//                    sysUser.setAuthInfo(authInfoMapper.selectAuthInfoByUserId(sysUser.getUserId()));
//                }
//            });
//        }
        return sysUsers;
    }

    @Override
    public Long registerWxUser(SysUser user) {
        userMapper.insertUser(user);
        return user.getUserId();
    }

    @Override
    public SysUser selectWxUser(String openId) {
        return userMapper.selectWxUserByOpenId(openId);
    }

    @Override
    public void updateNoRegisterUser(String updateSize) {
        List<SysUser> sysUsers = userMapper.selectUserNoRegisterUser();
        if (StringUtils.isEmpty(updateSize)) {
            updateSize = "100";
        }
        if(!CollectionUtils.isEmpty(sysUsers)){
            List<List<SysUser>> sysUsersBatch = new ArrayList<>();
            if (sysUsers.size() < Integer.valueOf(updateSize)) {
                sysUsersBatch = Arrays.asList(sysUsers);
            } else {
                sysUsersBatch = Arrays.asList(sysUsers.subList(0, Integer.valueOf(updateSize)));
            }
            for (List<SysUser> sysUserList : sysUsersBatch) {
                sysUserList.stream().forEach(sysUser -> {
                    // 获取身份证号
                    AuthInfo authInfo = authInfoMapper.selectAuthInfoByUserId(sysUser.getUserId());
                    if(Objects.nonNull(authInfo)){
                        // 身份证后6位
                        String idCard = authInfo.getIdCard();
                        String idCardLast6 = "";
                        if (idCard != null && idCard.length() >= 6) {
                            idCardLast6 = idCard.substring(idCard.length() - 6);
                        } else if (idCard != null) {
                            // 如果身份证号码长度不足6位，则取全部
                            idCardLast6 = String.format("%-6s", idCard).replace(' ', '0');
                        }
                        sysUser.setPassword(SecurityUtils.encryptPassword(idCardLast6));
                    }
                    sysUser.setStatus("0");
                    userMapper.updateUser(sysUser);
                });
            }
        }
    }
}
