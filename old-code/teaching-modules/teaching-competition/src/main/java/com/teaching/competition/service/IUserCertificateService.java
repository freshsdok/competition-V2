package com.teaching.competition.service;

import com.teaching.common.core.web.page.TableDataInfo;
import com.teaching.competition.domain.CompetitionCertificateQueryRequest;
import com.teaching.system.api.domain.UserCertificate;

import java.util.Date;
import java.util.List;

/**
 * 用户证书Service接口
 *
 * @author teaching
 */
public interface IUserCertificateService {
    /**
     * 查询用户证书
     *
     * @param certId 用户证书主键
     * @return 用户证书
     */
    public UserCertificate selectUserCertificateById(UserCertificate userCertificate);

    /**
     * 查询用户证书列表
     *
     * @param userCertificate 用户证书
     * @return 用户证书集合
     */
    public List<UserCertificate> selectUserCertificateList(UserCertificate userCertificate);

    /**
     * 查询公开大赛证书列表，以历史证书表为公开查询数据源。
     *
     * @param queryRequest 查询条件
     * @return 证书集合
     */
    public List<UserCertificate> selectCompetitionCertificateList(CompetitionCertificateQueryRequest queryRequest);
    public TableDataInfo getUserCertificateList(UserCertificate userCertificate);



    /**
     * 新增用户证书
     *
     * @param userCertificate 用户证书
     * @return 结果
     */
    public int insertUserCertificate(UserCertificate userCertificate);

    /**
     * 批量新增用户证书
     *
     * @param userCertificateList 用户证书列表
     * @return 结果
     */
    public int batchInsertUserCertificate(List<UserCertificate> userCertificateList, Date issuanceDate);

    /**
     * 修改用户证书
     *
     * @param userCertificate 用户证书
     * @return 结果
     */
    public int updateUserCertificate(UserCertificate userCertificate);

    /**
     * 删除用户证书
     *
     * @param certId 用户证书主键
     * @return 结果
     */
    public int deleteUserCertificateById(UserCertificate userCertificate);

    /**
     * 批量删除用户证书
     *
     * @param certIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteUserCertificateByIds(Long[] certIds);
}
