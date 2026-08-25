package com.teaching.content.service;

import com.teaching.content.domain.NewsInfo;
import com.teaching.content.domain.query.PublicNewsQuery;
import com.teaching.content.domain.vo.PublicNewsInfo;

import java.util.List;

/**
 * 资讯信息Service接口
 *
 * @author teaching
 * @date 2025-10-27
 */
public interface INewsInfoService {
    /**
     * 查询资讯信息
     *
     * @param newsId 资讯信息主键
     * @return 资讯信息
     */
    public NewsInfo selectNewsInfoByNewsId(Long newsId);

    /**
     * 查询可公开展示的资讯详情。
     *
     * @param newsId 资讯信息主键
     * @return 可公开展示的资讯；不存在或不可公开时返回 {@code null}
     */
    PublicNewsInfo selectPublicNewsInfoByNewsId(Long newsId);

    /**
     * 查询可公开展示的资讯列表。
     *
     * @param query 公开接口允许使用的查询条件
     * @return 可公开展示的资讯集合
     */
    List<PublicNewsInfo> selectPublicNewsInfoList(PublicNewsQuery query);

    /**
     * 查询资讯信息列表
     *
     * @param newsInfo 资讯信息
     * @return 资讯信息集合
     */
    public List<NewsInfo> selectNewsInfoList(NewsInfo newsInfo);

    /**
     * 新增资讯信息
     *
     * @param newsInfo 资讯信息
     * @return 结果
     */
    public int insertNewsInfo(NewsInfo newsInfo);

    /**
     * 修改资讯信息
     *
     * @param newsInfo 资讯信息
     * @return 结果
     */
    public int updateNewsInfo(NewsInfo newsInfo);

    /**
     * 批量删除资讯信息
     *
     * @param newsIds 需要删除的资讯信息主键集合
     * @return 结果
     */
    public int deleteNewsInfoByNewsIds(Long[] newsIds);

    /**
     * 删除资讯信息信息
     *
     * @param newsId 资讯信息主键
     * @return 结果
     */
    public int deleteNewsInfoByNewsId(Long newsId);

    /**
     * 发布资讯
     *
     * @param newsId 资讯ID
     * @return 结果
     */
    public int publishNews(Long newsId);

    /**
     * 下架资讯
     *
     * @param newsId 资讯ID
     * @return 结果
     */
    public int offlineNews(Long newsId);

    /**
     * 提交审核
     *
     * @param newsId 资讯ID
     * @return 结果
     */
    public int submitAudit(Long newsId);

    /**
     * 增加阅读量
     *
     * @param newsId 资讯ID
     * @return 结果
     */
    public int increaseReadingQuantity(Long newsId);

    /**
     * 增加点赞数
     *
     * @param newsId 资讯ID
     * @return 结果
     */
    public int increaseLikesNum(Long newsId);

    /**
     * 修改资讯审核状态（跨服务调用）
     *
     * @param newsInfo 资讯信息（包含newsId和checkStatus）
     * @return 结果
     */
    public int updateNewsAuditStatus(NewsInfo newsInfo);
}
