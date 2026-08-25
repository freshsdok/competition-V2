package com.teaching.content.mapper;

import com.teaching.content.domain.ContentBannerInfo;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import java.util.Map;

/**
 * banner图管理Mapper接口
 *
 * @author teaching
 * @date 2025-10-22
 */
@Mapper
public interface ContentBannerInfoMapper {
    /**
     * 查询banner图管理
     *
     * @param id banner图管理主键
     * @return banner图管理
     */
    public ContentBannerInfo selectContentBannerInfoById(Long id);

    /**
     * 查询banner图管理列表
     *
     * @param contentBannerInfo banner图管理
     * @return banner图管理集合
     */
    public List<ContentBannerInfo> selectContentBannerInfoList(ContentBannerInfo contentBannerInfo);

    /**
     * 查询banner图管理列表  PC页面使用
     * @param contentBannerInfo
     * @return
     */
    public List<Map<String, Object>> selectContentBannerInfoListByPc(ContentBannerInfo contentBannerInfo);

    /**
     * 新增banner图管理
     *
     * @param contentBannerInfo banner图管理
     * @return 结果
     */
    public int insertContentBannerInfo(ContentBannerInfo contentBannerInfo);

    /**
     * 修改banner图管理
     *
     * @param contentBannerInfo banner图管理
     * @return 结果
     */
    public int updateContentBannerInfo(ContentBannerInfo contentBannerInfo);

    /**
     * 删除banner图管理
     *
     * @param id banner图管理主键
     * @return 结果
     */
    public int deleteContentBannerInfoById(Long id);

    /**
     * 批量删除banner图管理
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteContentBannerInfoByIds(Long[] ids);
}
