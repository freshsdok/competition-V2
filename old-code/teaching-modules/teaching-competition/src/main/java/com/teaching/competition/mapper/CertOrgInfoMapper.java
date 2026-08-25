package com.teaching.competition.mapper;

import com.teaching.competition.domain.CertOrgInfo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 证书颁发机构Mapper接口
 *
 * @author teaching
 */
@Mapper
public interface CertOrgInfoMapper {
    /**
     * 查询证书颁发机构
     *
     * @param orgId 证书颁发机构主键
     * @return 证书颁发机构
     */
    public CertOrgInfo selectCertOrgInfoById(Long orgId);

    public CertOrgInfo selectCertOrgInfoByOrgCode(String orgCode);

    /**
     * 查询证书颁发机构列表
     *
     * @param certOrgInfo 证书颁发机构
     * @return 证书颁发机构集合
     */
    public List<CertOrgInfo> selectCertOrgInfoList(CertOrgInfo certOrgInfo);

    /**
     * 新增证书颁发机构
     *
     * @param certOrgInfo 证书颁发机构
     * @return 结果
     */
    public int insertCertOrgInfo(CertOrgInfo certOrgInfo);

    /**
     * 修改证书颁发机构
     *
     * @param certOrgInfo 证书颁发机构
     * @return 结果
     */
    public int updateCertOrgInfo(CertOrgInfo certOrgInfo);

    /**
     * 删除证书颁发机构
     *
     * @param orgId 证书颁发机构主键
     * @return 结果
     */
    public int deleteCertOrgInfoById(Long orgId);

    /**
     * 批量删除证书颁发机构
     *
     * @param orgIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteCertOrgInfoByIds(Long[] orgIds);
}