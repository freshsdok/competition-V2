package com.teaching.competition.service;

import com.teaching.competition.domain.AwardDetails;
import com.teaching.competition.domain.AwardPublicity;

import java.util.List;
import java.util.Map;

/**
 * 获奖公示管理Service接口
 *
 * @author teaching
 * @date 2026-05-12
 */
public interface IAwardPublicityService {
    /**
     * 查询获奖公示管理
     *
     * @param id 获奖公示管理主键
     * @return 获奖公示管理
     */
    public AwardPublicity selectAwardPublicityById(Long id);

    /**
     * 导入获奖公示管理数据
     * @param list
     * @param importType
     * @return
     */
    public Map<String,Object> importData(List<AwardDetails> list, String importType, Long competitionSeriesId, String competitionName, Long awardPublicityId);

    /**
     * 查询获奖公示管理列表
     *
     * @param awardPublicity 获奖公示管理
     * @return 获奖公示管理集合
     */
    public List<AwardPublicity> selectAwardPublicityList(AwardPublicity awardPublicity);

    /**
     * 新增获奖公示管理
     *
     * @param awardPublicity 获奖公示管理
     * @return 结果
     */
    public int insertAwardPublicity(AwardPublicity awardPublicity);

    /**
     * 修改获奖公示管理
     *
     * @param awardPublicity 获奖公示管理
     * @return 结果
     */
    public int updateAwardPublicity(AwardPublicity awardPublicity);

    /**
     * 修改获奖公示管理 提示信息
     * @param awardPublicity
     * @return
     */
    public int updateAwardPublicityTipInfo(AwardPublicity awardPublicity);

    /**
     * 批量删除获奖公示管理
     *
     * @param ids 需要删除的获奖公示管理主键集合
     * @return 结果
     */
    public int deleteAwardPublicityByIds(Long[] ids);

    /**
     * 删除获奖公示管理信息
     *
     * @param id 获奖公示管理主键
     * @return 结果
     */
    public int deleteAwardPublicityById(Long id);
}
