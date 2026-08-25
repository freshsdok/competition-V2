package com.teaching.competition.service.impl;

import com.teaching.competition.domain.CertOrgInfo;
import com.teaching.competition.mapper.CertOrgInfoMapper;
import com.teaching.competition.service.ICertOrgInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 证书颁发机构Service业务层处理
 *
 * @author teaching
 */
@Service
public class CertOrgInfoServiceImpl implements ICertOrgInfoService {
    @Autowired
    private CertOrgInfoMapper certOrgInfoMapper;

    /**
     * 查询证书颁发机构
     *
     * @param orgId 证书颁发机构主键
     * @return 证书颁发机构
     */
    @Override
    public CertOrgInfo selectCertOrgInfoById(Long orgId) {
        return certOrgInfoMapper.selectCertOrgInfoById(orgId);
    }

    /**
     * 查询证书颁发机构列表
     *
     * @param certOrgInfo 证书颁发机构
     * @return 证书颁发机构集合
     */
    @Override
    public List<CertOrgInfo> selectCertOrgInfoList(CertOrgInfo certOrgInfo) {
        return certOrgInfoMapper.selectCertOrgInfoList(certOrgInfo);
    }

    /**
     * 新增证书颁发机构
     *
     * @param certOrgInfo 证书颁发机构
     * @return 结果
     */
    @Override
    public int insertCertOrgInfo(CertOrgInfo certOrgInfo) {
        return certOrgInfoMapper.insertCertOrgInfo(certOrgInfo);
    }

    /**
     * 修改证书颁发机构
     *
     * @param certOrgInfo 证书颁发机构
     * @return 结果
     */
    @Override
    public int updateCertOrgInfo(CertOrgInfo certOrgInfo) {
        return certOrgInfoMapper.updateCertOrgInfo(certOrgInfo);
    }

    /**
     * 删除证书颁发机构
     *
     * @param orgId 证书颁发机构主键
     * @return 结果
     */
    @Override
    public int deleteCertOrgInfoById(Long orgId) {
        return certOrgInfoMapper.deleteCertOrgInfoById(orgId);
    }

    /**
     * 批量删除证书颁发机构
     *
     * @param orgIds 需要删除的数据主键集合
     * @return 结果
     */
    @Override
    public int deleteCertOrgInfoByIds(Long[] orgIds) {
        return certOrgInfoMapper.deleteCertOrgInfoByIds(orgIds);
    }
}