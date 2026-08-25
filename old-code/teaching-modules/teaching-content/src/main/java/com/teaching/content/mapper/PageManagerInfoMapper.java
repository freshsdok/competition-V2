package com.teaching.content.mapper;

import com.teaching.system.api.domain.PageManagerInfo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

/**
 * 页面管理信息Mapper接口
 *
 * @author teaching
 * @date 2025-10-14
 */
@Mapper
public interface PageManagerInfoMapper {
    /**
     * 查询页面管理信息
     *
     * @param pageId 页面管理信息主键
     * @return 页面管理信息
     */
    public PageManagerInfo selectPageManagerInfoByPageId(Long pageId);

    /**
     * 查询页面管理信息列表
     *
     * @param pageManagerInfo 页面管理信息
     * @return 页面管理信息集合
     */
    public List<PageManagerInfo> selectPageManagerInfoList(PageManagerInfo pageManagerInfo);

    /**
     * 查询页面管理信息列表2 标记生效版本
     * @param pageManagerInfo
     * @return
     */
    public List<PageManagerInfo> selectPageManagerInfoList2(PageManagerInfo pageManagerInfo);

    /**
     * 新增页面管理信息
     *
     * @param pageManagerInfo 页面管理信息
     * @return 结果
     */
    public int insertPageManagerInfo(PageManagerInfo pageManagerInfo);

    /**
     * 修改页面管理信息
     *
     * @param pageManagerInfo 页面管理信息
     * @return 结果
     */
    public int updatePageManagerInfo(PageManagerInfo pageManagerInfo);

    /**
     * 删除页面管理信息
     *
     * @param pageId 页面管理信息主键
     * @return 结果
     */
    public int deletePageManagerInfoByPageId(Long pageId);

    /**
     * 批量删除页面管理信息
     *
     * @param pageIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deletePageManagerInfoByPageIds(Long[] pageIds);

    /**
     * 根据展示平台和url查询最大版本号
     * @param displayPlatform
     * @param url
     * @return
     */
    public Long selectMaxVersionByDisplayPlatformAndUrl(@Param("displayPlatform") String displayPlatform, @Param("url") String url);

    /**
     * 根据类型和url查询页面管理信息
     * @param displayPlatform
     * @param url
     * @return
     */
    public PageManagerInfo selectInfoByTypeAndUrl(@Param("displayPlatform") String displayPlatform, @Param("url") String url);
}
