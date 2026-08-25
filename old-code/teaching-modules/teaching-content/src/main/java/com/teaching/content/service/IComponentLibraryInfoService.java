package com.teaching.content.service;

import java.util.List;
import java.util.Map;

import com.teaching.content.domain.ComponentLibraryInfo;

/**
 * 组件库信息Service接口
 *
 * @author teaching
 * @date 2025-10-13
 */
public interface IComponentLibraryInfoService {
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
     * 按类型分组
     *
     * @param componentLibraryInfo
     * @return
     */
    public Map<String, List<ComponentLibraryInfo>> selectComponentLibraryInfoListGroupClass(ComponentLibraryInfo componentLibraryInfo);

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
     * 批量删除组件库信息
     *
     * @param componentIds 需要删除的组件库信息主键集合
     * @return 结果
     */
    public int deleteComponentLibraryInfoByComponentIds(String[] componentIds);

    /**
     * 删除组件库信息信息
     *
     * @param componentId 组件库信息主键
     * @return 结果
     */
    public int deleteComponentLibraryInfoByComponentId(String componentId);
}
