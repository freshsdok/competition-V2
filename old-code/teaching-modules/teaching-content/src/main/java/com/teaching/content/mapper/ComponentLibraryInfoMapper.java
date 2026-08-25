package com.teaching.content.mapper;


import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import com.teaching.content.domain.ComponentLibraryInfo;

/**
 * 组件库信息Mapper接口
 *
 * @author teaching
 * @date 2025-10-13
 */
@Mapper
public interface ComponentLibraryInfoMapper {
    /**
     * 查询组件库信息
     *
     * @param componentId 组件库信息主键
     * @return 组件库信息
     */
    public ComponentLibraryInfo selectComponentLibraryInfoByComponentId(String componentId);

    /**
     * 查询组件库信息列表
     *
     * @param componentLibraryInfo 组件库信息
     * @return 组件库信息集合
     */
    public List<ComponentLibraryInfo> selectComponentLibraryInfoList(ComponentLibraryInfo componentLibraryInfo);

    /**
     * 新增组件库信息
     *
     * @param componentLibraryInfo 组件库信息
     * @return 结果
     */
    public int insertComponentLibraryInfo(ComponentLibraryInfo componentLibraryInfo);

    /**
     * 修改组件库信息
     *
     * @param componentLibraryInfo 组件库信息
     * @return 结果
     */
    public int updateComponentLibraryInfo(ComponentLibraryInfo componentLibraryInfo);

    /**
     * 删除组件库信息
     *
     * @param componentId 组件库信息主键
     * @return 结果
     */
    public int deleteComponentLibraryInfoByComponentId(String componentId);

    /**
     * 批量删除组件库信息
     *
     * @param componentIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteComponentLibraryInfoByComponentIds(String[] componentIds);
}
