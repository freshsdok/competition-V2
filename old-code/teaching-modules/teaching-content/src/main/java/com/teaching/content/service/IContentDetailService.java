package com.teaching.content.service;

import com.teaching.content.domain.ContentDetail;

import java.util.List;
import java.util.Map;

/**
 * 内容详情Service接口
 *
 * @author teaching
 * @date 2025-12-10
 */
public interface IContentDetailService {
    /**
     * 查询内容详情
     *
     * @param detailId 内容详情主键
     * @return 内容详情
     */
    public ContentDetail selectContentDetailByDetailId(Long detailId);

    /**
     * 查询内容详情列表
     *
     * @param contentDetail 内容详情
     * @return 内容详情集合
     */
    public List<ContentDetail> selectContentDetailList(ContentDetail contentDetail);

    /**
     * 根据栏目ID查询详情
     *
     * @param columnId 栏目ID
     * @return 内容详情
     */
    public ContentDetail selectContentDetailByColumnId(Long columnId);

    /**
     * 新增内容详情
     *
     * @param contentDetail 内容详情
     * @return 结果
     */
    public int insertContentDetail(ContentDetail contentDetail);

    /**
     * 修改内容详情
     *
     * @param contentDetail 内容详情
     * @return 结果
     */
    public int updateContentDetail(ContentDetail contentDetail);

    /**
     * 删除内容详情
     *
     * @param detailId 内容详情主键
     * @return 结果
     */
    public int deleteContentDetailByDetailId(Long detailId);

    /**
     * 批量删除内容详情
     *
     * @param detailIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteContentDetailByDetailIds(Long[] detailIds);

    /**
     * 根据栏目ID删除详情
     *
     * @param columnId 栏目ID
     * @return 结果
     */
    public int deleteContentDetailByColumnId(Long columnId);

    /**
     * 获取"维护公告"信息
     * @return
     */
    public List<Map<String, Object>> getNotices();
}
