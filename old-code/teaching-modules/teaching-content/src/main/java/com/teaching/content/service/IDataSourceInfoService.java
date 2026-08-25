package com.teaching.content.service;

import java.util.List;

import com.teaching.content.domain.DataSourceInfo;

/**
 * 数据源信息Service接口
 *
 * @author teaching
 * @date 2025-10-13
 */
public interface IDataSourceInfoService {
    /**
     * 查询数据源信息
     *
     * @param dataId 数据源信息主键
     * @return 数据源信息
     */
    public DataSourceInfo selectDataSourceInfoByDataId(Long dataId);

    /**
     * 查询数据源信息列表
     *
     * @param dataSourceInfo 数据源信息
     * @return 数据源信息集合
     */
    public List<DataSourceInfo> selectDataSourceInfoList(DataSourceInfo dataSourceInfo);

    /**
     * 新增数据源信息
     *
     * @param dataSourceInfo 数据源信息
     * @return 结果
     */
    public int insertDataSourceInfo(DataSourceInfo dataSourceInfo);

    /**
     * 修改数据源信息
     *
     * @param dataSourceInfo 数据源信息
     * @return 结果
     */
    public int updateDataSourceInfo(DataSourceInfo dataSourceInfo);

    /**
     * 批量删除数据源信息
     *
     * @param dataIds 需要删除的数据源信息主键集合
     * @return 结果
     */
    public int deleteDataSourceInfoByDataIds(Long[] dataIds);

    /**
     * 删除数据源信息信息
     *
     * @param dataId 数据源信息主键
     * @return 结果
     */
    public int deleteDataSourceInfoByDataId(Long dataId);
}
