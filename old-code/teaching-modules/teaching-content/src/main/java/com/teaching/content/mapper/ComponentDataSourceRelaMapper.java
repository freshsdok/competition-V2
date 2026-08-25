package com.teaching.content.mapper;

import com.teaching.system.api.domain.ComponentDataSourceRela;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import java.util.Map;

/**
 * 页面、组件、数据源关联关系Mapper接口
 *
 * @author teaching
 * @date 2025-10-14
 */
@Mapper
public interface ComponentDataSourceRelaMapper {
    /**
     * 根据主键查询页面、组件、数据源关联关系
     *
     * @param relaId 页面、组件、数据源关联关系主键
     * @return 页面、组件、数据源关联关系
     */
    public ComponentDataSourceRela selectComponentDataSourceRelaByRelaId(Long relaId);

    /**
     * 根据页面ID查询页面、组件、数据源关联关系
     *
     * @param pageId
     * @return
     */
    public List<ComponentDataSourceRela> selectComponentDataSourceRelaByPageId(Long pageId);

    /**
     * 根据页面ID查询数据源接口地址
     *
     * @param pageId 页面id
     * @return 组件id对应的数据源url 集合
     */
    public List<Map<String, String>> selectDataSourceUrlByPageId(Long pageId);

    /**
     * 查询页面、组件、数据源关联关系列表
     *
     * @param componentDataSourceRela 页面、组件、数据源关联关系
     * @return 页面、组件、数据源关联关系集合
     */
    public List<ComponentDataSourceRela> selectComponentDataSourceRelaList(ComponentDataSourceRela componentDataSourceRela);

    /**
     * 新增页面、组件、数据源关联关系
     *
     * @param componentDataSourceRela 页面、组件、数据源关联关系
     * @return 结果
     */
    public int insertComponentDataSourceRela(ComponentDataSourceRela componentDataSourceRela);

    /**
     * 批量新增
     *
     * @param list
     * @return
     */
    public int insertComponentDataSourceRelaBatch(List<ComponentDataSourceRela> list);

    /**
     * 修改页面、组件、数据源关联关系
     *
     * @param componentDataSourceRela 页面、组件、数据源关联关系
     * @return 结果
     */
    public int updateComponentDataSourceRela(ComponentDataSourceRela componentDataSourceRela);

    /**
     * 删除页面、组件、数据源关联关系
     *
     * @param relaId 页面、组件、数据源关联关系主键
     * @return 结果
     */
    public int deleteComponentDataSourceRelaByRelaId(Long relaId);

    /**
     * 删除页面、组件、数据源关联关系 通过页面id
     *
     * @param pageId 页面id
     * @return
     */
    public int deleteComponentDataSourceRelaByPageId(Long pageId);

    /**
     * 批量删除页面、组件、数据源关联关系
     *
     * @param relaIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteComponentDataSourceRelaByRelaIds(Long[] relaIds);

    /**
     * 批量删除页面、组件、数据源关联关系 通过页面ids
     *
     * @param pageIds
     * @return
     */
    public int deleteComponentDataSourceRelaByPageIds(Long[] pageIds);

    /**
     * 校验组件是否已经被引用
     *
     * @param componentIds
     * @return
     */
    public int checkComponentDataSourceRelaByComponentId(String[] componentIds);

    /**
     * 校验数据源是否已经被引用
     *
     * @param dataIds
     * @return
     */
    public int checkComponentDataSourceRelaByDataIds(Long[] dataIds);
}
