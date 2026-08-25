package com.teaching.competition.mapper;

import com.teaching.competition.domain.CandidateCertInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 候选人证书Mapper接口
 *
 * @author teaching
 */
@Mapper
public interface CandidateCertInfoMapper {
    /**
     * 查询候选人证书
     *
     * @param certConfigId 候选人证书主键
     * @return 候选人证书
     */
    public List<CandidateCertInfo> selectCandidateCertInfoById(Long certConfigId, Long memberId);

    public List<CandidateCertInfo> selectCandidateCertInfoListByUser(Map<String, Object> params);

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
    public int batchInsertCandidateCertInfo(List<CandidateCertInfo> candidateCertInfoList);

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

    public int deleteCandidateCertInfoByCertConfigId(Long certConfigId);

    public int deleteCandidateCertInfoByMemberId(Long[] memberIds);

    public int deleteCandidateCertInfoByCartId(@Param("idCardList") List<String> idCardList);

    /**
     * 批量删除候选人证书
     *
     * @param candidateIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteCandidateCertInfoByIds(Long[] candidateIds);
}
