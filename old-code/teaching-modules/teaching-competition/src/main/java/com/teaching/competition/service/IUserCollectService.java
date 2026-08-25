package com.teaching.competition.service;

import com.teaching.competition.domain.UserCollect;

import java.util.List;
import java.util.Map;

/**
 * 用户收藏信息Service接口
 * 
 * @author teaching
 * @date 2025-10-22
 */
public interface IUserCollectService 
{
    /**
     * 查询用户收藏信息
     * 
     * @param collectId 用户收藏信息主键
     * @return 用户收藏信息
     */
    public UserCollect selectUserCollectByCollectId(Long collectId);

    /**
     * 查询用户收藏信息列表
     * 
     * @param userCollect 用户收藏信息
     * @return 用户收藏信息集合
     */
    public List<UserCollect> selectUserCollectList(UserCollect userCollect);

    /**
     * 新增用户收藏信息
     * 
     * @param userCollect 用户收藏信息
     * @return 结果
     */
    public int insertUserCollect(UserCollect userCollect);

    /**
     * 修改用户收藏信息
     * 
     * @param userCollect 用户收藏信息
     * @return 结果
     */
    public int updateUserCollect(UserCollect userCollect);

    /**
     * 批量删除用户收藏信息
     * 
     * @param collectIds 需要删除的用户收藏信息主键集合
     * @return 结果
     */
    public int deleteUserCollectByCollectIds(Long[] collectIds);

    /**
     * 删除用户收藏信息信息
     * 
     * @param collectId 用户收藏信息主键
     * @return 结果
     */
    public int deleteUserCollectByCollectId(UserCollect userCollect);

    /**
     * 查询赛事收藏信息数量
     * @return
     */
    public Integer selectCollectCompetitionCount(Map<String,Object> params);

    /**
     * 赛事分享
     * @param competitionId
     * @return
     *
     */
    public Long shareCompetition(Map<String,Object> params);


    /**
     * 赛事分享总数
     * @param competitionId
     * @return
     *
     */
    public Integer shareCompetitionCount(Map<String,Object> params);
}
