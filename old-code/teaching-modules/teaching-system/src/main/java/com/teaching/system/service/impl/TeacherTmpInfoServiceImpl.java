package com.teaching.system.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.teaching.common.core.constant.Constants;
import com.teaching.common.core.constant.TdConstants;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.system.api.domain.AuthInfo;
import com.teaching.system.api.domain.IdentityInfo;
import com.teaching.system.api.domain.NationwideCollegeInfo;
import com.teaching.system.api.domain.SysUser;
import com.teaching.system.mapper.*;
import com.teaching.system.service.IAuthInfoService;
import com.teaching.system.service.IIdentityInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.teaching.system.domain.TeacherTmpInfo;
import com.teaching.system.service.ITeacherTmpInfoService;
import org.springframework.transaction.annotation.Transactional;

/**
 * 教师导入临时Service业务层处理
 *
 * @author teaching
 * @date 2025-12-19
 */
@Slf4j
@Service
public class TeacherTmpInfoServiceImpl implements ITeacherTmpInfoService
{
    @Autowired
    private TeacherTmpInfoMapper teacherTmpInfoMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private AuthInfoMapper authInfoMapper;

    @Autowired
    private IdentityInfoMapper identityInfoMapper;

    @Autowired
    private NationwideCollegeInfoMapper collegeInfoMapper;

    @Autowired
    private IAuthInfoService authInfoService;

    @Autowired
    private IIdentityInfoService identityInfoService;

    /**
     * 查询教师导入临时
     *
     * @param id 教师导入临时主键
     * @return 教师导入临时
     */
    @Override
    public TeacherTmpInfo selectTeacherTmpInfoById(String id)
    {
        return teacherTmpInfoMapper.selectTeacherTmpInfoById(id);
    }

    /**
     * 查询教师导入临时列表
     *
     * @param teacherTmpInfo 教师导入临时
     * @return 教师导入临时
     */
    @Override
    public List<TeacherTmpInfo> selectTeacherTmpInfoList(TeacherTmpInfo teacherTmpInfo)
    {
        return teacherTmpInfoMapper.selectTeacherTmpInfoList(teacherTmpInfo);
    }

    /**
     * 新增教师导入临时
     *
     * @param teacherTmpInfo 教师导入临时
     * @return 结果
     */
    @Override
    public int insertTeacherTmpInfo(TeacherTmpInfo teacherTmpInfo)
    {
        return teacherTmpInfoMapper.insertTeacherTmpInfo(teacherTmpInfo);
    }

    /**
     * 修改教师导入临时
     *
     * @param teacherTmpInfo 教师导入临时
     * @return 结果
     */
    @Override
    public int updateTeacherTmpInfo(TeacherTmpInfo teacherTmpInfo)
    {
        return teacherTmpInfoMapper.updateTeacherTmpInfo(teacherTmpInfo);
    }

    /**
     * 批量删除教师导入临时
     *
     * @param ids 需要删除的教师导入临时主键
     * @return 结果
     */
    @Override
    public int deleteTeacherTmpInfoByIds(String[] ids)
    {
        return teacherTmpInfoMapper.deleteTeacherTmpInfoByIds(ids);
    }

    /**
     * 删除教师导入临时信息
     *
     * @param id 教师导入临时主键
     * @return 结果
     */
    @Override
    public int deleteTeacherTmpInfoById(String id)
    {
        return teacherTmpInfoMapper.deleteTeacherTmpInfoById(id);
    }

    @Override
//    @Transactional
    public int saveTeacherTmpInfo() throws Exception {
        TeacherTmpInfo teacherTmpInfo = new TeacherTmpInfo();
        List<TeacherTmpInfo> teacherTmpInfos = teacherTmpInfoMapper.selectTeacherTmpInfoList(teacherTmpInfo);
        log.info("查询到临时数据：" + teacherTmpInfos.size());
        if(CollectionUtils.isNotEmpty(teacherTmpInfos)){
            for (TeacherTmpInfo tmpInfo : teacherTmpInfos) {
                SysUser user = new SysUser();
                user.setUserName(tmpInfo.getUserName());
                user.setUserType("2");
                user.setNickName(tmpInfo.getRealName());
                user.setPassword(SecurityUtils.encryptPassword(tmpInfo.getPwd()));
                user.setEmail(tmpInfo.getEmail());
                user.setPhonenumber(tmpInfo.getPhone());
                NationwideCollegeInfo nationwideCollegeInfo = collegeInfoMapper.selectNationwideCollegeInfoBySchoolName(tmpInfo.getSchoolName());
                if(Objects.nonNull(nationwideCollegeInfo)){
                    user.setSchool(nationwideCollegeInfo.getId());
                }
                user.setSchoolName(tmpInfo.getSchoolName());
                user.setCreateTime(DateUtils.getNowDate());
                SysUser sysUser = sysUserMapper.selectUserByUserInfo(user);
                if(Objects.nonNull(sysUser)){
                    user.setUserId(sysUser.getUserId());
                    log.info("用户已存在：" + JSONObject.toJSONString(tmpInfo));
                } else {
                    user.setUserSources(TdConstants.USER_SOURCES_IMPORT);
                    sysUserMapper.insertUser(user);
                }
                AuthInfo authInfo = new AuthInfo();
                authInfo.setIdCard(tmpInfo.getIdCard());
                authInfo.setIdCardType("1");
                authInfo.setRealName(tmpInfo.getRealName());
                authInfo.setUserId(user.getUserId());
                AuthInfo authInfRes = new AuthInfo();
                BeanUtils.copyProperties(authInfo,authInfRes);
                authInfRes.setAuthStatus(Constants.AUTH_STATUS_PASS);
                List<AuthInfo> authInfoList = authInfoMapper.selectAuthInfoList(authInfRes);
                if(CollectionUtils.isNotEmpty(authInfoList)){
                    log.info("实名认证已存在：" + JSONObject.toJSONString(authInfoList));
                } else {
                    authInfoService.insertAuthInfo(authInfo);
                }
                IdentityInfo identityInfo = new IdentityInfo();
                identityInfo.setUserId(user.getUserId());
                if(Objects.nonNull(nationwideCollegeInfo)){
                    identityInfo.setSchool(nationwideCollegeInfo.getId());
                }
                identityInfo.setInstitute(tmpInfo.getInstitute());
                identityInfo.setPosition(tmpInfo.getPosition());
                identityInfo.setCertificationType("teacher");
                identityInfo.setIdentityTime(tmpInfo.getRegistTime());
                identityInfo.setCheckStatus("4");
                identityInfo.setCreateTime(DateUtils.getNowDate());
                identityInfo.setWorkCardUrl(tmpInfo.getTeacherUrl());
                IdentityInfo identityInfoRes = new IdentityInfo();
                identityInfoRes.setUserId(user.getUserId());
                identityInfoRes.setCertificationType("teacher");
                identityInfoRes.setCheckStatus(Constants.IDENTITY_AUTH_PASS);
                List<IdentityInfo> identityInfoList = identityInfoMapper.selectIdentityInfoList(identityInfoRes);
                if(CollectionUtils.isNotEmpty(identityInfoList)){
                    log.info("教师信息已存在：" + JSONObject.toJSONString(identityInfoList));
                } else {
                    identityInfoMapper.insertIdentityInfo(identityInfo);
                    identityInfoService.updateIdentityInfoStatus(identityInfo);
                }
            }
        }
        return 0;
    }
}
