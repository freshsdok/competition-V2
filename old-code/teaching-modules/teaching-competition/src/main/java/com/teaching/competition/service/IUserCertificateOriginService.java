package com.teaching.competition.service;

import com.teaching.system.api.domain.UserCertificateOrigin;

import java.util.List;

/**
 * 用户证书原表Service接口
 *
 * @author teaching
 */
public interface IUserCertificateOriginService {
    /**
     * 查询用户证书原表
     *
     * @param certId 用户证书id
     * @return 用户证书原表
     */
    public UserCertificateOrigin selectUserCertificateOriginById(Long certId);

    /**
     * 查询用户证书原表列表
     *
     * @param userCertificateOrigin 用户证书原表
     * @return 用户证书原表集合
     */
    public List<UserCertificateOrigin> selectUserCertificateOriginList(UserCertificateOrigin userCertificateOrigin);

    /**
     * 新增用户证书原表
     *
     * @param userCertificateOrigin 用户证书原表
     * @return 结果
     */
    public int insertUserCertificateOrigin(UserCertificateOrigin userCertificateOrigin);

    /**
     * 修改用户证书原表
     *
     * @param userCertificateOrigin 用户证书原表
     * @return 结果
     */
    public int updateUserCertificateOrigin(UserCertificateOrigin userCertificateOrigin);

    /**
     * 批量删除用户证书原表
     *
     * @param certIds 需要删除的用户证书原表主键集合
     * @return 结果
     */
    public int deleteUserCertificateOriginByIds(Long[] certIds);

    /**
     * 删除用户证书原表信息
     *
     * @param certId 用户证书原表主键
     * @return 结果
     */
    public int deleteUserCertificateOriginById(Long certId);
}
