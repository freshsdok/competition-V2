package com.teaching.competition.service.impl;

import com.teaching.system.api.domain.UserCertificateOrigin;
import com.teaching.competition.mapper.UserCertificateOriginMapper;
import com.teaching.competition.service.IUserCertificateOriginService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户证书原表Service业务层处理
 *
 * @author teaching
 */
@Service
public class UserCertificateOriginServiceImpl implements IUserCertificateOriginService {
    @Autowired
    private UserCertificateOriginMapper userCertificateOriginMapper;

    /**
     * 查询用户证书原表
     *
     * @param certId 用户证书id
     * @return 用户证书原表
     */
    @Override
    public UserCertificateOrigin selectUserCertificateOriginById(Long certId) {
        return userCertificateOriginMapper.selectUserCertificateOriginById(certId);
    }

    /**
     * 查询用户证书原表列表
     *
     * @param userCertificateOrigin 用户证书原表
     * @return 用户证书原表
     */
    @Override
    public List<UserCertificateOrigin> selectUserCertificateOriginList(UserCertificateOrigin userCertificateOrigin) {
        return userCertificateOriginMapper.selectUserCertificateOriginList(userCertificateOrigin);
    }

    /**
     * 新增用户证书原表
     *
     * @param userCertificateOrigin 用户证书原表
     * @return 结果
     */
    @Override
    public int insertUserCertificateOrigin(UserCertificateOrigin userCertificateOrigin) {
        return userCertificateOriginMapper.insertUserCertificateOrigin(userCertificateOrigin);
    }

    /**
     * 修改用户证书原表
     *
     * @param userCertificateOrigin 用户证书原表
     * @return 结果
     */
    @Override
    public int updateUserCertificateOrigin(UserCertificateOrigin userCertificateOrigin) {
        return userCertificateOriginMapper.updateUserCertificateOrigin(userCertificateOrigin);
    }

    /**
     * 批量删除用户证书原表
     *
     * @param certIds 需要删除的用户证书原表主键
     * @return 结果
     */
    @Override
    public int deleteUserCertificateOriginByIds(Long[] certIds) {
        return userCertificateOriginMapper.deleteUserCertificateOriginByIds(certIds);
    }

    /**
     * 删除用户证书原表信息
     *
     * @param certId 用户证书原表主键
     * @return 结果
     */
    @Override
    public int deleteUserCertificateOriginById(Long certId) {
        return 1;
    }
}
