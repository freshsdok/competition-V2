package com.teaching.competition.mapper;

import com.teaching.competition.domain.AwardPublicity;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * 获奖公示管理Mapper接口
 *
 * @author teaching
 * @date 2026-05-12
 */
public interface AwardPublicityMapper {
    /**
     * 查询获奖公示管理
     *
     * @param id 获奖公示管理主键
     * @return 获奖公示管理
     */
    public AwardPublicity selectAwardPublicityById(Long id);

    /**
     * 查询获奖公示管理列表
     *
     * @param awardPublicity 获奖公示管理
     * @return 获奖公示管理集合
     */
    public List<AwardPublicity> selectAwardPublicityList(AwardPublicity awardPublicity);

    public List<AwardPublicity> selectAwardPublicityByLeaderTeacherId(Long userId);

    /**
     * 新增获奖公示管理
     *
     * @param awardPublicity 获奖公示管理
     * @return 结果
     */
    public int insertAwardPublicity(AwardPublicity awardPublicity);

    /**
     * 批量新增获奖公示管理
     * @param awardPublicityList
     * @return
     */
    public int insertAwardPublicityBatch(List<AwardPublicity> awardPublicityList);

    /**
     * 修改获奖公示管理
     *
     * @param awardPublicity 获奖公示管理
     * @return 结果
     */
    public int updateAwardPublicity(AwardPublicity awardPublicity);
    /**
     * 修改获奖公示管理提示信息
     * @param awardPublicity
     * @return 结果
     */
    public int updateAwardPublicityTipInfo(AwardPublicity awardPublicity);

    /**
     * 修改获奖公示管理导入数据 仅修改update_time 和 update_by
     * @param awardPublicity
     * @return
     */
    public int updateAwardPublicityUpdateInfo(AwardPublicity awardPublicity);

    /**
     * 删除获奖公示管理
     *
     * @param id 获奖公示管理主键
     * @return 结果
     */
    public int deleteAwardPublicityById(Long id);

    /**
     * 批量删除获奖公示管理
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteAwardPublicityByIds(Long[] ids);

    /**
     * 逻辑批量删除获奖公示管理
     * @param ids
     * @return
     */
    public int batchLogicDeleteByTeamCodes(@Param("ids") Long[] ids,@Param("updateBy") String updateBy);
}
