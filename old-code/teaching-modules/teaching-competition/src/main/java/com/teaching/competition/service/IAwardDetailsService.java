package com.teaching.competition.service;

import com.teaching.competition.domain.AwardDetails;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 获奖公示明细Service接口
 *
 * @author teaching
 * @date 2026-05-12
 */
public interface IAwardDetailsService {
    /**
     * 查询获奖公示明细
     *
     * @param id 获奖公示明细主键
     * @return 获奖公示明细
     */
    public AwardDetails selectAwardDetailsById(Long id);

    /**
     * 查询获奖公示明细列表
     *
     * @param awardDetails 获奖公示明细
     * @return 获奖公示明细集合
     */
    public List<AwardDetails> selectAwardDetailsList(AwardDetails awardDetails);

    /**
     * 查询获奖公示带报名信息明细列表
     *
     * @param awardDetails 获奖公示明细
     * @return 获奖公示明细集合
     */
    public List<AwardDetails> selectAwardDetailsCompetitionApplyInfoList(AwardDetails awardDetails);

    public Integer selectAwardDetailsSum(Long awardPublicityId);

    /**
     * 新增获奖公示明细
     *
     * @param awardDetails 获奖公示明细
     * @return 结果
     */
    public int insertAwardDetails(AwardDetails awardDetails);

    /**
     * 批量新增获奖公示明细
     *
     * @param detailList
     * @return
     */
    public Map<String, Object> insertAwardDetailsBatch(List<AwardDetails> detailList);

    /**
     * 根据团队编码批量删除获奖公示明细
     *
     * @param teamCodes
     * @return
     */
    public void batchLogicDeleteByTeamCodes(Set<String> teamCodes, Long awardPublicityId);

    /**
     * 修改获奖公示明细
     *
     * @param awardDetails 获奖公示明细
     * @return 结果
     */
    public int updateAwardDetails(List<AwardDetails> awardDetails);

    /**
     * 批量删除获奖公示明细
     *
     * @param ids 需要删除的获奖公示明细主键集合
     * @return 结果
     */
    public int deleteAwardDetailsByIds(Long[] ids);

    /**
     * 根据获奖公示ID批量逻辑删除获奖公示明细
     *
     * @param ids
     * @param updateBy
     * @return
     */
    public int batchLogicDeleteByAwardPublicityId(Long[] ids, String updateBy);

    /**
     * 删除获奖公示明细信息
     *
     * @param id 获奖公示明细主键
     * @return 结果
     */
    public int deleteAwardDetailsById(Long id);
}
