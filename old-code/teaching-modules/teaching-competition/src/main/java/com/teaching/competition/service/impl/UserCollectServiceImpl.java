package com.teaching.competition.service.impl;

import java.util.List;
import java.util.Map;

import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.redis.service.RedisService;
import com.teaching.competition.domain.UserCollect;
import com.teaching.competition.mapper.UserCollectMapper;
import com.teaching.competition.service.IUserCollectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 用户收藏信息Service业务层处理
 * 
 * @author teaching
 * @date 2025-10-22
 */
@Service
public class UserCollectServiceImpl implements IUserCollectService
{
    @Autowired
    private UserCollectMapper userCollectMapper;

    @Autowired
    private RedisService redisService;

    /**
     * 查询用户收藏信息
     * 
     * @param collectId 用户收藏信息主键
     * @return 用户收藏信息
     */
    @Override
    public UserCollect selectUserCollectByCollectId(Long collectId)
    {
        return userCollectMapper.selectUserCollectByCollectId(collectId);
    }

    /**
     * 查询用户收藏信息列表
     * 
     * @param userCollect 用户收藏信息
     * @return 用户收藏信息
     */
    @Override
    public List<UserCollect> selectUserCollectList(UserCollect userCollect)
    {
        return userCollectMapper.selectUserCollectList(userCollect);
    }

    /**
     * 新增用户收藏信息
     * 
     * @param userCollect 用户收藏信息
     * @return 结果
     */
    @Override
    public int insertUserCollect(UserCollect userCollect)
    {
        userCollect.setCreateTime(DateUtils.getNowDate());
        return userCollectMapper.insertUserCollect(userCollect);
    }

    /**
     * 修改用户收藏信息
     * 
     * @param userCollect 用户收藏信息
     * @return 结果
     */
    @Override
    public int updateUserCollect(UserCollect userCollect)
    {
        userCollect.setUpdateTime(DateUtils.getNowDate());
        return userCollectMapper.updateUserCollect(userCollect);
    }

    /**
     * 批量删除用户收藏信息
     * 
     * @param collectIds 需要删除的用户收藏信息主键
     * @return 结果
     */
    @Override
    public int deleteUserCollectByCollectIds(Long[] collectIds)
    {
        return userCollectMapper.deleteUserCollectByCollectIds(collectIds);
    }

    /**
     * 删除用户收藏信息信息
     * 
     * @param collectId 用户收藏信息主键
     * @return 结果
     */
    @Override
    public int deleteUserCollectByCollectId(UserCollect userCollect)
    {
        return userCollectMapper.deleteUserCollectByCollectId(userCollect);
    }

    @Override
    public Integer selectCollectCompetitionCount(Map<String,Object> params) {
        return userCollectMapper.selectCollectCompetitionCount(params);
    }

    /**
     * 赛事分享
     * @param competitionId
     * @return
     */
    @Override
    public Long shareCompetition(Map<String,Object> params) {
        String competitionId = params.get("competitionId") + "";
        String competitionSeriesId = params.get("competitionSeriesId") + "";
        // 用redis自增功能
        Long num = null;
        if(redisService.hasKey(competitionId+competitionSeriesId+"")){
            num = redisService.increment(competitionId+competitionSeriesId + "", 1);
        } else {
            redisService.setCacheObject(competitionId+competitionSeriesId + "", 1);
        }
        return num;
    }

    @Override
    public Integer shareCompetitionCount(Map<String,Object> params) {
        String competitionId = params.get("competitionId") + "";
        String competitionSeriesId = params.get("competitionSeriesId") + "";
        Integer cacheObject = redisService.getCacheObject(competitionId+competitionSeriesId + "");
        return cacheObject;
    }
}
