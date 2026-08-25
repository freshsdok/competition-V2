package com.teaching.competition.mapper;

import com.teaching.competition.domain.CompetitionCertificateQueryRequest;
import com.teaching.system.api.domain.UserCertificate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户证书Mapper接口
 *
 * @author teaching
 */
@Mapper
public interface UserCertificateMapper {
    /**
     * 查询用户证书
     *
     * @param certId 用户证书主键
     * @return 用户证书
     */
    public UserCertificate selectUserCertificateById(UserCertificate userCertificate);

    public List<UserCertificate> selectUserCertificateByIdCard(Long certConfigId,String idCard);

    public List<UserCertificate> selectUserCertificateByUserId(UserCertificate userCertificate);

    public List<UserCertificate> selectUserCertificateByCertCode(@Param("certCodeList") List<String> certCodeList);

    public List<UserCertificate> selectUserCertificateByCertId(@Param("certIdList") List<Long> certIdList);

    /**
     * 查询用户证书列表
     *
     * @param userCertificate 用户证书
     * @return 用户证书集合
     */
    public List<UserCertificate> selectUserCertificateList(UserCertificate userCertificate);

    /**
     * 查询公开大赛证书列表，以历史证书表为数据源。
     *
     * @param queryRequest 查询条件
     * @return 证书集合
     */
    public List<UserCertificate> selectCompetitionCertificateList(CompetitionCertificateQueryRequest queryRequest);

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
    public int batchInsertUserCertificate(List<UserCertificate> userCertificateList);

    /**
     * 修改用户证书
     *
     * @param userCertificate 用户证书
     * @return 结果
     */
    public int updateUserCertificate(UserCertificate userCertificate);

    public int updateUserCertificateByCertConfigId(UserCertificate userCertificate);

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
