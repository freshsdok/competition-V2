package com.teaching.system.service.impl;

import cn.hutool.core.convert.Convert;
import com.teaching.common.core.constant.Constants;
import com.teaching.common.core.constant.TdConstants;
import com.teaching.common.core.exception.GlobalException;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.core.utils.SpringUtils;
import com.teaching.common.datascope.annotation.DataScope;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.system.api.OrderService;
import com.teaching.system.api.domain.IdentityInfo;
import com.teaching.system.api.domain.SysOrg;
import com.teaching.system.api.domain.SysUser;
import com.teaching.system.api.domain.UserAuthorization;
import com.teaching.system.api.domain.NationwideCollegeInfo;
import com.teaching.system.domain.SysAuditTask;
import com.teaching.system.domain.SysUserRole;
import com.teaching.system.mapper.*;
import com.teaching.system.service.IIdentityInfoService;
import com.teaching.system.service.ISysAuditTaskService;
import com.teaching.system.service.PersonalCenterService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 身份认证信息Service业务层处理
 *
 * @author teaching
 * @date 2025-10-13
 */
@Service
public class IdentityInfoServiceImpl implements IIdentityInfoService {
    @Autowired
    private IdentityInfoMapper identityInfoMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private OrderService orderService;

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private PersonalCenterService personalCenterService;

    @Autowired
    private SysOrgMapper sysOrgMapper;

    @Autowired
    private NationwideCollegeInfoMapper nationwideCollegeInfoMapper;
    @Autowired
    private SysAuditTaskMapper sysAuditTaskMapper;

    private static final Logger logger = LoggerFactory.getLogger(IdentityInfoServiceImpl.class);

    /**
     * 查询身份认证信息
     *
     * @param userId 身份认证信息主键
     * @return 身份认证信息
     */
    @Override
    public IdentityInfo selectIdentityInfoByAuthId(Long userId, Long authId, String certificationType) {
        IdentityInfo infoByAuthId = identityInfoMapper.selectIdentityInfoByAuthId(userId, authId, certificationType);
        if (infoByAuthId != null) {
            SysUser sysUser = sysUserMapper.selectUserById(infoByAuthId.getUserId());
            if (Objects.nonNull(sysUser)) {
                infoByAuthId.setUserName(sysUser.getUserName());
            }
        }
        return infoByAuthId;
    }

    /**
     * 查询身份认证信息列表
     *
     * @param identityInfo 身份认证信息
     * @return 身份认证信息
     */
    @Override
    @DataScope(orgAlias = "a", userAlias = "a")
    public List<IdentityInfo> selectIdentityInfoList(IdentityInfo identityInfo) {
        return identityInfoMapper.selectIdentityInfoList(identityInfo);
    }

    @Override
    public List<IdentityInfo> selectUserIdentityInfoList(IdentityInfo identityInfo) {
        List<IdentityInfo> identityInfoList = identityInfoMapper.selectIdentityInfoList(identityInfo);
        if (CollectionUtils.isNotEmpty(identityInfoList)) {
            for (IdentityInfo info : identityInfoList) {
                SysUser sysUser = sysUserMapper.selectUserById(info.getUserId());
                info.setUserName(sysUser.getUserName());
                //是学生认证时返回年级
                if ("1".equals(info.getCertificationType())) {
                    info.setClassInfo(calculateGrade(info.getEnrollmentYear()));
                }
                if(Constants.IDENTITY_AUTH_FAIL.equals(info.getCheckStatus())){
                    String refusalReasons = sysAuditTaskMapper.selectCheckOpinionByTaskId(TdConstants.TABLE_NAME_IDENTITY, info.getAuthId());
                    info.setRefusalReasons(refusalReasons);
                }
            }
        }
        return identityInfoList;
    }

    /**
     * 新增身份认证信息
     * 不能用事务注解，同一条数据新增及修改需要有执行顺序
     *
     * @param identityInfo 身份认证信息
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertIdentityInfo(IdentityInfo identityInfo) {
        if (StringUtils.isBlank(identityInfo.getCertificationType())) {
            throw new GlobalException("认证类型不能为空");
        }
        SysUser sysUser = sysUserMapper.selectUserById(identityInfo.getUserId());
        if (!Constants.AUTH_STATUS_PASS.equals(sysUser.getAuthStatus())) {
            throw new GlobalException("未进行实名认证");
        }
        IdentityInfo identityInfo1 = identityInfoMapper.selectInfoByUserAndType(identityInfo.getUserId(), identityInfo.getCertificationType());
        if (identityInfo1 != null) {
            throw new GlobalException("当前用户此类型认证，正在审核中，请勿重复认证");
        }
        // 根据学校名称查机构orgId
        if (StringUtils.isNotBlank(identityInfo.getSchool())) {
//            SysOrg org = sysOrgMapper.selectSysOrgByOrgName(identityInfo.getSchool());
            NationwideCollegeInfo nationwideCollegeInfo = nationwideCollegeInfoMapper.selectNationwideCollegeInfoById(identityInfo.getSchool());
            if (Objects.nonNull(nationwideCollegeInfo)) {
                identityInfo.setSchool(nationwideCollegeInfo.getId());
            }
        }
        // 根据公司名称查机构
//        if (StringUtils.isNotBlank(identityInfo.getCompanyName())) {
//            SysOrg org = sysOrgMapper.selectSysOrgByOrgName(identityInfo.getCompanyName());
//            if (Objects.nonNull(org)) {
//                identityInfo.setOrgId(org.getOrgId());
//            }
//        }
        identityInfo.setCreateBy(identityInfo.getUserId() + "");
        identityInfo.setCreateTime(DateUtils.getNowDate());
        identityInfo.setIdentityTime(DateUtils.getNowDate());
        identityInfoMapper.insertIdentityInfo(identityInfo);
        String checkStatus = insertSysAuditTask(identityInfo);
        identityInfo.setCheckStatus(checkStatus);
        return identityInfoMapper.updateIdentityInfo(identityInfo);
    }


    /**
     * 根据入学年份计算年级
     * 系统自动计算当前年级，以9月1日作为分界。（张仲：例如2024年入学的，如果当前时间是2025年9月1日0点之前则系统判断其为1年级，2025年9月1日0点之后的系统自动判断其为2年级）
     *
     * @param enrollmentYear 入学年份
     * @return 年级字符串
     */
    public static String calculateGrade(String enrollmentYear) {
        try {
            int enrollment = Integer.parseInt(enrollmentYear);
            LocalDate today = LocalDate.now();
            int currentYear = today.getYear();

            if (today.getMonthValue() < 9 || (today.getMonthValue() == 9 && today.getDayOfMonth() < 1)) {
                return Convert.numberToChinese((currentYear - enrollment), false) + "年级";
            } else {
                return Convert.numberToChinese((currentYear - enrollment + 1), false) + "年级";
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid enrollment year format", e);
        }
    }



    public String insertSysAuditTask(IdentityInfo identityInfo) {
        // 生成待审核任务id
        SysAuditTask sysAuditTask = new SysAuditTask(identityInfo.getAuthId(), identityInfo.getCertificationType());
        ISysAuditTaskService sysAuditTaskService = SpringUtils.getBean(ISysAuditTaskService.class);
        return sysAuditTaskService.authUserInsertSysAuditTask(sysAuditTask);
    }

    /**
     * 修改身份认证信息
     *
     * @param identityInfo 身份认证信息
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateIdentityInfo(IdentityInfo identityInfo) {
        SysUser sysUser = sysUserMapper.selectUserById(identityInfo.getUserId());
        if (!Constants.AUTH_STATUS_PASS.equals(sysUser.getAuthStatus())) {
            throw new GlobalException("未进行实名认证");
        }
        // 当有用户id和身份认证类型存在时，根据状态校验，存在未审核通过的记录，不能继续新增未审核通过记录
        IdentityInfo infoByAuthId = identityInfoMapper.selectInfoByUserAndType(identityInfo.getUserId(), identityInfo.getCertificationType());
        if (infoByAuthId != null) {
            throw new GlobalException("当前用户此类型认证，正在审核中，请勿重复认证");
        }
        identityInfo.setUpdateBy(sysUser.getUserId() + "");
        identityInfo.setUpdateTime(DateUtils.getNowDate());
        if (StringUtils.isNotBlank(identityInfo.getSchool())) {
//            SysOrg org = sysOrgMapper.selectSysOrgByOrgName(identityInfo.getSchool());
            NationwideCollegeInfo nationwideCollegeInfo = nationwideCollegeInfoMapper.selectNationwideCollegeInfoById(identityInfo.getSchool());
            if (Objects.nonNull(nationwideCollegeInfo)) {
                identityInfo.setSchool(nationwideCollegeInfo.getId());
            }
        }
        identityInfoMapper.updateIdentityInfo(identityInfo);
        String status = insertSysAuditTask(identityInfo);
        identityInfo.setCheckStatus(status);
        // 解除角色绑定
        identityInfo.setUserId(sysUser.getUserId());
        cancelRoleIdentityInfo(identityInfo);
        return identityInfoMapper.updateIdentityInfo(identityInfo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public int updateIdentityInfoStatus(IdentityInfo identityInfo) {
        SysUser sysUser = new SysUser();
        IdentityInfo infoByAuthId = identityInfoMapper.selectIdentityInfoByAuthId(null, identityInfo.getAuthId(), null);
        if (Constants.CHECK_PASS.equals(identityInfo.getCheckStatus())) {
            identityInfo.setIdentityTime(DateUtils.getNowDate());
            identityInfo.setCheckStatus(Constants.IDENTITY_AUTH_PASS);
            sysUser.setUserId(infoByAuthId.getUserId());
            // 根据学校名称查机构orgId
            if (StringUtils.isNotBlank(infoByAuthId.getSchool())) {
                NationwideCollegeInfo nationwideCollegeInfo = nationwideCollegeInfoMapper.selectNationwideCollegeInfoById(infoByAuthId.getSchool());
                if (nationwideCollegeInfo != null) {
                    identityInfo.setOrgId(Long.parseLong(nationwideCollegeInfo.getId()));
                    sysUser.setOrgId(Long.parseLong(nationwideCollegeInfo.getId()));
                    sysUser.setSchoolName(nationwideCollegeInfo.getSchoolName());
                    sysUser.setSchool(nationwideCollegeInfo.getId());
                }
            }
            // 根据公司名称查机构
            if (StringUtils.isNotBlank(infoByAuthId.getCompanyName())) {
                SysOrg org = sysOrgMapper.selectSysOrgByOrgName(infoByAuthId.getCompanyName());
                if (org != null) {
                    identityInfo.setOrgId(org.getOrgId());
                    sysUser.setOrgId(org.getOrgId());
                }
            }
            //
            giveRoleIdentityInfo(infoByAuthId);
        } else if (Constants.CHECK_REJECT.equals(identityInfo.getCheckStatus())) {
            identityInfo.setIdentityTime(DateUtils.getNowDate());
            identityInfo.setCheckStatus(Constants.IDENTITY_AUTH_FAIL);
            sysUser.setUserId(infoByAuthId.getUserId());
            sysUser.setOrgId(infoByAuthId.getOrgId());
            // 解除角色绑定
            cancelRoleIdentityInfo(infoByAuthId);
        } else {
            sysUser.setUserId(SecurityUtils.getLoginUser().getSysUser().getUserId());
            identityInfo.setCheckStatus(Constants.IDENTITY_NO_CHECK);
        }
        sysUserMapper.updateUser(sysUser);
        return identityInfoMapper.updateIdentityInfo(identityInfo);
    }

    public void giveRoleIdentityInfo(IdentityInfo identityInfo) {
        // 若果是学生将学生角色自动付给用户
        UserAuthorization userAuthorization = new UserAuthorization();
        if (identityInfo.getCertificationType().equals(Constants.IDENTITY_TYPE_STUDENT)) {
            userAuthorization.setUserId(identityInfo.getUserId());
            userAuthorization.setStudentFlag(true);
            personalCenterService.saveUserAuthorization(userAuthorization);
        }
        // 如果是老师将老师角色自动付给用户
        if (identityInfo.getCertificationType().equals(Constants.IDENTITY_TYPE_TEACHER)) {
            userAuthorization.setUserId(identityInfo.getUserId());
            userAuthorization.setTeacherFlag(true);
            personalCenterService.saveUserAuthorization(userAuthorization);
        }
    }

    public void cancelRoleIdentityInfo(IdentityInfo identityInfo) {
        // 解除学生角色绑定
        UserAuthorization userAuthorization = new UserAuthorization();
        if (identityInfo.getCertificationType().equals(Constants.IDENTITY_TYPE_STUDENT)) {
            userAuthorization.setUserId(identityInfo.getUserId());
            userAuthorization.setStudentFlag(true);
            personalCenterService.updateUserAuthorization(userAuthorization);
        }
        // 解除老师角色绑定
        if (identityInfo.getCertificationType().equals(Constants.IDENTITY_TYPE_TEACHER)) {
            userAuthorization.setUserId(identityInfo.getUserId());
            userAuthorization.setTeacherFlag(true);
            personalCenterService.updateUserAuthorization(userAuthorization);
        }
    }

    /**
     * 批量删除身份认证信息
     *
     * @param authIds 需要删除的身份认证信息主键
     * @return 结果
     */
    @Override
    public int deleteIdentityInfoByAuthIds(Long[] authIds) {
        return identityInfoMapper.deleteIdentityInfoByAuthIds(authIds);
    }

    /**
     * 删除身份认证信息信息
     *
     * @param authId 身份认证信息主键
     * @return 结果
     */
    @Override
    public int deleteIdentityInfoByAuthId(Long authId) {
        return identityInfoMapper.deleteIdentityInfoByAuthId(authId);
    }

    /**
     * 根据学校id查询教师姓名列表
     * @param schoolId
     * @return
     */
    @Override
    public List<Map<String,Object>> getTeacherNameBySchoolId(String schoolId) {
        return identityInfoMapper.selectTeacherNameBySchoolId(schoolId);
    }
}
