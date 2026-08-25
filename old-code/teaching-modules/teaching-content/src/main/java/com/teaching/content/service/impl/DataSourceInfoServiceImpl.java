package com.teaching.content.service.impl;

import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.content.domain.DataSourceInfo;
import com.teaching.content.mapper.ComponentDataSourceRelaMapper;
import com.teaching.content.mapper.DataSourceInfoMapper;
import com.teaching.content.service.IDataSourceInfoService;
import com.teaching.system.api.model.LoginUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 数据源信息Service业务层处理
 *
 * @author teaching
 * @date 2025-10-13
 */
@Service
public class DataSourceInfoServiceImpl implements IDataSourceInfoService {
    @Autowired
    private DataSourceInfoMapper dataSourceInfoMapper;
    @Autowired
    private ComponentDataSourceRelaMapper componentDataSourceRelaMapper;

    /**
     * 查询数据源信息
     *
     * @param dataId 数据源信息主键
     * @return 数据源信息
     */
    @Override
    public DataSourceInfo selectDataSourceInfoByDataId(Long dataId) {
        return dataSourceInfoMapper.selectDataSourceInfoByDataId(dataId);
    }

    /**
     * 查询数据源信息列表
     *
     * @param dataSourceInfo 数据源信息
     * @return 数据源信息
     */
    @Override
    public List<DataSourceInfo> selectDataSourceInfoList(DataSourceInfo dataSourceInfo) {
        return dataSourceInfoMapper.selectDataSourceInfoList(dataSourceInfo);
    }

    /**
     * 新增数据源信息
     *
     * @param dataSourceInfo 数据源信息
     * @return 结果
     */
    @Override
    public int insertDataSourceInfo(DataSourceInfo dataSourceInfo) {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        dataSourceInfo.setCreateBy(loginUser.getSysUser().getNickName());
        dataSourceInfo.setCreateTime(DateUtils.getNowDate());
        dataSourceInfo.setUserId(loginUser.getUserid());
        dataSourceInfo.setOrgId(loginUser.getSysUser().getOrgId());
        dataSourceInfo.setCreateTime(DateUtils.getNowDate());
        return dataSourceInfoMapper.insertDataSourceInfo(dataSourceInfo);
    }

    /**
     * 修改数据源信息
     *
     * @param dataSourceInfo 数据源信息
     * @return 结果
     */
    @Override
    public int updateDataSourceInfo(DataSourceInfo dataSourceInfo) {
        dataSourceInfo.setUpdateTime(DateUtils.getNowDate());
        return dataSourceInfoMapper.updateDataSourceInfo(dataSourceInfo);
    }

    /**
     * 批量删除数据源信息
     *
     * @param dataIds 需要删除的数据源信息主键
     * @return 结果
     */
    @Override
    public int deleteDataSourceInfoByDataIds(Long[] dataIds) {
        //检查数据源是否被引用
        int i = componentDataSourceRelaMapper.checkComponentDataSourceRelaByDataIds(dataIds);
        if (i > 0) {
            throw new RuntimeException("数据源已经被引用，不能删除");
        }
        return dataSourceInfoMapper.deleteDataSourceInfoByDataIds(dataIds);
    }

    /**
     * 删除数据源信息信息
     *
     * @param dataId 数据源信息主键
     * @return 结果
     */
    @Override
    public int deleteDataSourceInfoByDataId(Long dataId) {
        //检查数据源是否被引用
        int i = componentDataSourceRelaMapper.checkComponentDataSourceRelaByDataIds(new Long[]{dataId});
        if (i > 0) {
            throw new RuntimeException("数据源已经被引用，不能删除");
        }
        return dataSourceInfoMapper.deleteDataSourceInfoByDataId(dataId);
    }
}
