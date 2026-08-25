package com.teaching.content.mapper;

import com.teaching.content.domain.NewsInfo;
import com.teaching.content.domain.query.PublicNewsQuery;
import com.teaching.content.domain.vo.PublicNewsInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 资讯信息Mapper接口
 *
 * @author teaching
 * @date 2025-10-27
 */
@Mapper
public interface NewsInfoMapper {
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
    PublicNewsInfo selectPublicNewsInfoByNewsId(@Param("newsId") Long newsId);

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
     * 删除资讯信息
     *
     * @param newsId 资讯信息主键
     * @return 结果
     */
    public int deleteNewsInfoByNewsId(Long newsId);

    /**
     * 批量删除资讯信息
     *
     * @param newsIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteNewsInfoByNewsIds(Long[] newsIds);

    /**
     * 更新资讯状态
     *
     * @param newsInfo 资讯信息
     * @return 结果
     */
    public int updateNewsStatus(NewsInfo newsInfo);

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
}
