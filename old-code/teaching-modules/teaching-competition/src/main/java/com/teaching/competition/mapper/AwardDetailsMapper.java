package com.teaching.competition.mapper;

import com.teaching.competition.domain.AwardDetails;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * 获奖公示明细Mapper接口
 *
 * @author teaching
 * @date 2026-05-12
 */
public interface AwardDetailsMapper {
    /**
     * 查询获奖公示明细
     *
     * @param id 获奖公示明细主键
     * @return 获奖公示明细
     */
    public AwardDetails selectAwardDetailsById(Long id);

    public Integer selectAwardDetailsSum(Long awardPublicityId);

    /**
     * 查询获奖公示明细列表
     *
     * @param awardDetails 获奖公示明细
     * @return 获奖公示明细集合
     */
    public List<AwardDetails> selectAwardDetailsList(AwardDetails awardDetails);

    /**
     * 查询获奖公示明细列表
     *
     * @param awardDetails 获奖公示明细
     * @return 获奖公示明细集合
     */
    public List<AwardDetails> selectAwardDetailsCompetitionApplyInfoList(AwardDetails awardDetails);
//    public int selectAwardDetailsCompetitionApplyInfoList_COUNT(AwardDetails awardDetails);
    /**
     * 新增获奖公示明细
     *
     * @param awardDetails 获奖公示明细
     * @return 结果
     */
    public int insertAwardDetails(AwardDetails awardDetails);

    /**
     * 批量新增获奖公示明细
     * @param list
     * @return
     */
    public int insertAwardDetailsBatch(List<AwardDetails> list);

    /**
     * 根据团队编码批量删除获奖公示明细
     * @param teamCodes
     * @return
     */
    int batchDeleteByTeamCodes(List<String> teamCodes);

    /**
     * 根据团队编码批量逻辑删除获奖公示明细
     * @param teamCodes
     * @return
     */
    int batchLogicDeleteByTeamCodes(@Param("teamCodes") List<String> teamCodes, @Param("updateBy") String updateBy, @Param("awardPublicityId") Long awardPublicityId);

    /**
     * 根据获奖公示ID批量逻辑删除获奖公示明细
     * @param awardPublicityIds
     * @param updateBy
     * @return
     */
    int batchLogicDeleteByAwardPublicityId(@Param("awardPublicityIds") Long[] awardPublicityIds, @Param("updateBy") String updateBy);

    /**
     * 修改获奖公示明细
     *
     * @param awardDetails 获奖公示明细
     * @return 结果
     */
    public int updateAwardDetails(AwardDetails awardDetails);

    /**
     * 批量修改获奖公示明细
     *
     * @param awardDetailsList 获奖公示明细列表
     * @return 结果
     */
    public int batchUpdateAwardDetails(List<AwardDetails> awardDetailsList);

    /**
     * 删除获奖公示明细
     *
     * @param id 获奖公示明细主键
     * @return 结果
     */
    public int deleteAwardDetailsById(Long id);

    /**
     * 批量删除获奖公示明细
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteAwardDetailsByIds(Long[] ids);
}
