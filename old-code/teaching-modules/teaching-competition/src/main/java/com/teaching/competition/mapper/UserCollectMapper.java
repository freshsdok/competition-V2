package com.teaching.competition.mapper;

import com.teaching.competition.domain.UserCollect;

import java.util.List;
import java.util.Map;

/**
 * 用户收藏信息Mapper接口
 * 
 * @author teaching
 * @date 2025-10-22
 */
public interface UserCollectMapper 
{
    /**
     * 查询用户收藏信息
     * 
     * @param collectId 用户收藏信息主键
     * @return 用户收藏信息
     */
    public UserCollect selectUserCollectByCollectId(Long collectId);

    /**
     * 查询收藏赛事信息数量
     *
     * @return 数量
     */
    public Integer selectCollectCompetitionCount(Map<String,Object> params);

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
     * 删除用户收藏信息
     * 
     * @param collectId 用户收藏信息主键
     * @return 结果
     */
    public int deleteUserCollectByCollectId(UserCollect userCollect);

    /**
     * 批量删除用户收藏信息
     * 
     * @param collectIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteUserCollectByCollectIds(Long[] collectIds);
}
