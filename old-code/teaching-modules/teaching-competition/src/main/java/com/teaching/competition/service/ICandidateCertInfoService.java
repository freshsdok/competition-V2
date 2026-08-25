package com.teaching.competition.service;

import com.teaching.competition.domain.CandidateCertInfo;
import com.teaching.competition.domain.CandidateCertInfoImport;

import java.util.List;

/**
 * 候选人证书Service接口
 *
 * @author teaching
 */
public interface ICandidateCertInfoService {
    /**
     * 查询候选人证书
     *
     * @param candidateId 候选人证书主键
     * @return 候选人证书
     */
    public CandidateCertInfo selectCandidateCertInfoById(Long candidateId);

    /**
     * 查询候选人证书列表
     *
     * @param candidateCertInfo 候选人证书
     * @return 候选人证书集合
     */
    public List<CandidateCertInfo> selectCandidateCertInfoList(CandidateCertInfo candidateCertInfo);

    /**
     * 新增候选人证书
     *
     * @param candidateCertInfo 候选人证书
     * @return 结果
     */
    public int insertCandidateCertInfo(CandidateCertInfo candidateCertInfo);

    /**
     * 批量新增候选人证书
     *
     * @param candidateCertInfoList 候选人证书列表
     * @return 结果
     */
    public int batchInsertCandidateCertInfo(List<CandidateCertInfo> candidateCertInfoList,Long certConfigId);

    /**
     * 修改候选人证书
     *
     * @param candidateCertInfo 候选人证书
     * @return 结果
     */
    public int updateCandidateCertInfo(CandidateCertInfo candidateCertInfo);

    /**
     * 删除候选人证书
     *
     * @param candidateId 候选人证书主键
     * @return 结果
     */
    public int deleteCandidateCertInfoById(Long candidateId);

    /**
     * 批量删除候选人证书
     *
     * @param candidateIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteCandidateCertInfoByIds(Long[] candidateIds);

    /**
     * 导入候选人信息
     *
     * @param candidateList 候选人信息列表
     * @param updateSupport 是否更新支持
     * @param operName 操作人
     * @return 结果
     */
    public String importCandidateCertInfo(List<CandidateCertInfoImport> candidateList, boolean updateSupport, String operName,Long candidateId);

    /**
     * 从获奖公示一键新增候选人证书
     *
     * @param candidateCertInfo 候选人证书
     * @return 结果
     */
    public int insertCandidateCertInfoFromAwards(CandidateCertInfo candidateCertInfo);
}
