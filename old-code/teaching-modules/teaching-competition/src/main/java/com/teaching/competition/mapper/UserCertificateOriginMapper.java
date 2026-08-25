package com.teaching.competition.mapper;

import com.teaching.system.api.domain.UserCertificate;
import com.teaching.system.api.domain.UserCertificateOrigin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户证书原表Mapper接口
 *
 * @author teaching
 */
@Mapper
public interface UserCertificateOriginMapper {
    /**
     * 查询用户证书原表
     *
     * @param certId 用户证书id
     * @return 用户证书原表
     */
    public UserCertificateOrigin selectUserCertificateOriginById(Long certId);

    public List<UserCertificateOrigin> selectUserCertificateOriginByStageId(UserCertificateOrigin userCertificateOrigin);

    public List<UserCertificateOrigin> selectUserCertificateOriginByCetCode(@Param("certCodeList") List<String> certCodeList);

    public UserCertificateOrigin selectUserCertificateOriginByIdCard(Long certId,String idCard);

    /**
     * 查询用户证书原表列表
     *
     * @param userCertificateOrigin 用户证书原表
     * @return 用户证书原表集合
     */
    public List<UserCertificateOrigin> selectUserCertificateOriginList(UserCertificateOrigin userCertificateOrigin);

    public List<UserCertificate> selectUserCertificateListFromOrigin(UserCertificate userCertificate);

    /**
     * 查询当前报名负责人名下获证团队的不重复学生证书编号。
     */
    public List<String> selectGuidedCertificateCodes(@Param("userId") Long userId);

    /**
     * 查询当前报名负责人名下的获证团队数量。
     */
    public long countGuidedCertificateTeams(@Param("userId") Long userId);

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
     * 删除用户证书原表
     *
     * @param certId 用户证书id
     * @return 结果
     */
    public int deleteUserCertificateOriginById(UserCertificateOrigin userCertificateOrigin);

    /**
     * 批量删除用户证书原表
     *
     * @param certIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteUserCertificateOriginByIds(Long[] certIds);
}
