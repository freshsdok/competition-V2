package com.teaching.competition.service.impl;

import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.competition.domain.UserCertificateHistory;
import com.teaching.competition.mapper.UserCertificateHistoryMapper;
import com.teaching.competition.service.IUserCertificateHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户证书历史Service业务层处理
 *
 * @author teaching
 * @date 2026-05-13
 */
@Service
public class UserCertificateHistoryServiceImpl implements IUserCertificateHistoryService {
    @Autowired
    private UserCertificateHistoryMapper userCertificateHistoryMapper;

    /**
     * 查询用户证书历史
     *
     * @param certId 用户证书历史主键
     * @return 用户证书历史
     */
    @Override
    public UserCertificateHistory selectUserCertificateHistoryById(Long certId) {
        return userCertificateHistoryMapper.selectUserCertificateHistoryById(certId);
    }

    /**
     * 查询用户证书历史列表
     *
     * @param userCertificateHistory 用户证书历史
     * @return 用户证书历史
     */
    @Override
    public List<UserCertificateHistory> selectUserCertificateHistoryList(UserCertificateHistory userCertificateHistory) {
        return userCertificateHistoryMapper.selectUserCertificateHistoryList(userCertificateHistory);
    }

    /**
     * 新增用户证书历史
     *
     * @param userCertificateHistory 用户证书历史
     * @return 结果
     */
    @Override
    public int insertUserCertificateHistory(UserCertificateHistory userCertificateHistory) {
        userCertificateHistory.setCreateTime(DateUtils.getNowDate());
        return userCertificateHistoryMapper.insertUserCertificateHistory(userCertificateHistory);
    }

    /**
     * 修改用户证书历史
     *
     * @param userCertificateHistory 用户证书历史
     * @return 结果
     */
    @Override
    public int updateUserCertificateHistory(UserCertificateHistory userCertificateHistory) {
        userCertificateHistory.setUpdateTime(DateUtils.getNowDate());
        return userCertificateHistoryMapper.updateUserCertificateHistory(userCertificateHistory);
    }

    /**
     * 批量删除用户证书历史
     *
     * @param certIds 需要删除的用户证书历史主键
     * @return 结果
     */
    @Override
    public int deleteUserCertificateHistoryByIds(Long[] certIds) {
        return userCertificateHistoryMapper.deleteUserCertificateHistoryByIds(certIds);
    }

    /**
     * 删除用户证书历史信息
     *
     * @param certId 用户证书历史主键
     * @return 结果
     */
    @Override
    public int deleteUserCertificateHistoryById(Long certId) {
        return userCertificateHistoryMapper.deleteUserCertificateHistoryById(certId);
    }
}
