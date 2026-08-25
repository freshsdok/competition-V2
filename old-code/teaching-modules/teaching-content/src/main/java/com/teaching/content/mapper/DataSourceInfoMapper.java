package com.teaching.content.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import com.teaching.content.domain.DataSourceInfo;

/**
 * 数据源信息Mapper接口
 *
 * @author teaching
 * @date 2025-10-13
 */
@Mapper
public interface DataSourceInfoMapper {
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
     * 删除数据源信息
     *
     * @param dataId 数据源信息主键
     * @return 结果
     */
    public int deleteDataSourceInfoByDataId(Long dataId);

    /**
     * 批量删除数据源信息
     *
     * @param dataIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteDataSourceInfoByDataIds(Long[] dataIds);
}
