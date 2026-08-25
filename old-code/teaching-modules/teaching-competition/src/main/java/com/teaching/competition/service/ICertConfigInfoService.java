package com.teaching.competition.service;

import com.teaching.system.api.domain.CertConfigInfo;

import java.util.List;

/**
 * 证书配置信息Service接口
 *
 * @author teaching
 */
public interface ICertConfigInfoService {
    /**
     * 查询证书配置信息
     *
     * @param certConfigId 证书配置信息主键
     * @return 证书配置信息
     */
    public CertConfigInfo selectCertConfigInfoById(Long certConfigId);

    /**
     * 查询证书配置信息列表
     *
     * @param certConfigInfo 证书配置信息
     * @return 证书配置信息集合
     */
    public List<CertConfigInfo> selectCertConfigInfoList(CertConfigInfo certConfigInfo);

    /**
     * 新增证书配置信息
     *
     * @param certConfigInfo 证书配置信息
     * @return 结果
     */
    public int insertCertConfigInfo(CertConfigInfo certConfigInfo);

    /**
     * 修改证书配置信息
     *
     * @param certConfigInfo 证书配置信息
     * @return 结果
     */
    public int updateCertConfigInfo(CertConfigInfo certConfigInfo);

    /**
     * 删除证书配置信息
     *
     * @param certConfigId 证书配置信息主键
     * @return 结果
     */
    public int deleteCertConfigInfoById(Long certConfigId);

    /**
     * 批量删除证书配置信息
     *
     * @param certConfigIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteCertConfigInfoByIds(Long[] certConfigIds);
}