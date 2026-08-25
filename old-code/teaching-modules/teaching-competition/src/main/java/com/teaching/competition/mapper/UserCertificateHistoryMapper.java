package com.teaching.competition.mapper;

import com.teaching.competition.domain.UserCertificateHistory;

import java.util.List;

/**
 * 用户证书历史Mapper接口
 *
 * @author teaching
 * @date 2026-05-13
 */
public interface UserCertificateHistoryMapper {
    /**
     * 查询用户证书历史
     *
     * @param certId 用户证书历史主键
     * @return 用户证书历史
     */
    public UserCertificateHistory selectUserCertificateHistoryById(Long certId);

    /**
     * 查询用户证书历史列表
     *
     * @param userCertificateHistory 用户证书历史
     * @return 用户证书历史集合
     */
    public List<UserCertificateHistory> selectUserCertificateHistoryList(UserCertificateHistory userCertificateHistory);

    /**
     * 新增用户证书历史
     *
     * @param userCertificateHistory 用户证书历史
     * @return 结果
     */
    public int insertUserCertificateHistory(UserCertificateHistory userCertificateHistory);

    /**
     * 修改用户证书历史
     *
     * @param userCertificateHistory 用户证书历史
     * @return 结果
     */
    public int updateUserCertificateHistory(UserCertificateHistory userCertificateHistory);

    /**
     * 删除用户证书历史
     *
     * @param certId 用户证书历史主键
     * @return 结果
     */
    public int deleteUserCertificateHistoryById(Long certId);

    /**
     * 批量删除用户证书历史
     *
     * @param certIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteUserCertificateHistoryByIds(Long[] certIds);
}
