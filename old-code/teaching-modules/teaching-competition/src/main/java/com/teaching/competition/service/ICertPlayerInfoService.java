package com.teaching.competition.service;

import com.teaching.competition.domain.CertPlayerInfo;

import java.util.List;

/**
 * 证书人员Service接口
 *
 * @author teaching
 */
public interface ICertPlayerInfoService {
    /**
     * 查询证书人员
     *
     * @param relaId 证书人员主键
     * @return 证书人员
     */
    public CertPlayerInfo selectCertPlayerInfoById(Long relaId);

    /**
     * 查询证书人员列表
     *
     * @param certPlayerInfo 证书人员
     * @return 证书人员集合
     */
    public List<CertPlayerInfo> selectCertPlayerInfoList(CertPlayerInfo certPlayerInfo);

    /**
     * 新增证书人员
     *
     * @param certPlayerInfo 证书人员
     * @return 结果
     */
    public int insertCertPlayerInfo(CertPlayerInfo certPlayerInfo);

    /**
     * 批量新增证书人员
     *
     * @param certPlayerInfoList 证书人员列表
     * @return 结果
     */
    public int batchInsertCertPlayerInfo(List<CertPlayerInfo> certPlayerInfoList);

    /**
     * 修改证书人员
     *
     * @param certPlayerInfo 证书人员
     * @return 结果
     */
    public int updateCertPlayerInfo(CertPlayerInfo certPlayerInfo);

    /**
     * 删除证书人员
     *
     * @param relaId 证书人员主键
     * @return 结果
     */
    public int deleteCertPlayerInfoById(Long relaId);

    /**
     * 批量删除证书人员
     *
     * @param relaIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteCertPlayerInfoByIds(Long[] relaIds);
}
