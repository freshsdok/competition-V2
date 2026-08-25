package com.teaching.competition.service.impl;

import com.teaching.competition.domain.CertPlayerInfo;
import com.teaching.competition.mapper.CertPlayerInfoMapper;
import com.teaching.competition.service.ICertPlayerInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 证书人员Service业务层处理
 *
 * @author teaching
 */
@Service
public class CertPlayerInfoServiceImpl implements ICertPlayerInfoService {
    @Autowired
    private CertPlayerInfoMapper certPlayerInfoMapper;

    /**
     * 查询证书人员
     *
     * @param relaId 证书人员主键
     * @return 证书人员
     */
    @Override
    public CertPlayerInfo selectCertPlayerInfoById(Long relaId) {
        return certPlayerInfoMapper.selectCertPlayerInfoById(relaId);
    }

    /**
     * 查询证书人员列表
     *
     * @param certPlayerInfo 证书人员
     * @return 证书人员集合
     */
    @Override
    public List<CertPlayerInfo> selectCertPlayerInfoList(CertPlayerInfo certPlayerInfo) {
        return certPlayerInfoMapper.selectCertPlayerInfoList(certPlayerInfo);
    }

    /**
     * 新增证书人员
     *
     * @param certPlayerInfo 证书人员
     * @return 结果
     */
    @Override
    public int insertCertPlayerInfo(CertPlayerInfo certPlayerInfo) {
        return certPlayerInfoMapper.insertCertPlayerInfo(certPlayerInfo);
    }

    /**
     * 批量新增证书人员
     *
     * @param certPlayerInfoList 证书人员列表
     * @return 结果
     */
    @Override
    public int batchInsertCertPlayerInfo(List<CertPlayerInfo> certPlayerInfoList) {
        return certPlayerInfoMapper.batchInsertCertPlayerInfo(certPlayerInfoList);
    }

    /**
     * 修改证书人员
     *
     * @param certPlayerInfo 证书人员
     * @return 结果
     */
    @Override
    public int updateCertPlayerInfo(CertPlayerInfo certPlayerInfo) {
        return certPlayerInfoMapper.updateCertPlayerInfo(certPlayerInfo);
    }

    /**
     * 删除证书人员
     *
     * @param relaId 证书人员主键
     * @return 结果
     */
    @Override
    public int deleteCertPlayerInfoById(Long relaId) {
        return certPlayerInfoMapper.deleteCertPlayerInfoById(relaId);
    }

    /**
     * 批量删除证书人员
     *
     * @param relaIds 需要删除的数据主键集合
     * @return 结果
     */
    @Override
    public int deleteCertPlayerInfoByIds(Long[] relaIds) {
        return certPlayerInfoMapper.deleteCertPlayerInfoByIds(relaIds);
    }
}
