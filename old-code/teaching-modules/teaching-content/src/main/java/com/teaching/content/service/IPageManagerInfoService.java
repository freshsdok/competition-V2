package com.teaching.content.service;

import com.teaching.system.api.domain.PageInfo;
import com.teaching.system.api.domain.PageManagerInfo;

import java.util.List;

/**
 * 页面管理信息Service接口
 *
 * @author teaching
 * @date 2025-10-14
 */
public interface IPageManagerInfoService {
    /**
     * 查询页面管理信息
     *
     * @param pageId 页面管理信息主键
     * @return 页面管理信息
     */
    public PageManagerInfo selectPageManagerInfoByPageId(Long pageId);

    /**
     * 根据类型和url查询页面管理信息
     * @param pt
     * @param url
     * @return
     */
    public PageManagerInfo getInfoByTypeAndUrl(String pt,String url);

    /**
     * 查询页面管理信息列表
     *
     * @param pageManagerInfo 页面管理信息
     * @return 页面管理信息集合
     */
    public List<PageManagerInfo> selectPageManagerInfoList(PageManagerInfo pageManagerInfo);

    /**
     * 新增页面管理信息
     *
     * @param pageManagerInfo 页面管理信息
     * @return 结果
     */
    public int insertPageManagerInfo(PageManagerInfo pageManagerInfo);

    /**
     * 复制页面管理信息
     *
     * @param pageId
     * @return
     */
    public int copyPageManagerInfo(Long pageId);

    /**
     * 修改页面管理信息
     *
     * @param pageManagerInfo 页面管理信息
     * @return 结果
     */
    public int updatePageManagerInfo(PageManagerInfo pageManagerInfo);

    /**
     * 修改页面内容信息
     *
     * @param pageManagerInfo
     * @return
     */
    public int updatePageManagerContentInfo(PageManagerInfo pageManagerInfo);

    /**
     * 修改页面基本信息
     *
     * @param pageManagerInfo
     * @return
     */
    public int updatePageManagerBaseInfo(PageManagerInfo pageManagerInfo);

    /**
     * 修改页面管理审核状态
     *
     * @param pageManagerInfo
     * @return
     */
    public int updatePageManagerStatus(PageInfo pageManagerInfo);

    /**
     * 批量删除页面管理信息
     *
     * @param pageIds 需要删除的页面管理信息主键集合
     * @return 结果
     */
    public int deletePageManagerInfoByPageIds(Long[] pageIds);

    /**
     * 删除页面管理信息信息
     *
     * @param pageInfo 页面管理信息
     * @return 结果
     */
    public int deletePageManagerInfoByPageId(PageManagerInfo pageInfo);
}
