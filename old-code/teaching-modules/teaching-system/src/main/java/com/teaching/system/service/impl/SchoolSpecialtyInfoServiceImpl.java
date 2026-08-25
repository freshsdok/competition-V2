package com.teaching.system.service.impl;

import com.teaching.common.core.utils.StringUtils;
import com.teaching.system.domain.SchoolSpecialtyInfo;
import com.teaching.system.mapper.SchoolSpecialtyInfoMapper;
import com.teaching.system.service.ISchoolSpecialtyInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 专业信息Service业务层处理
 *
 * @author teaching
 * @date 2025-12-03
 */
@Service
public class SchoolSpecialtyInfoServiceImpl implements ISchoolSpecialtyInfoService {
    @Autowired
    private SchoolSpecialtyInfoMapper schoolSpecialtyInfoMapper;

    /**
     * 查询专业信息
     *
     * @param id 专业信息主键
     * @return 专业信息
     */
    @Override
    public SchoolSpecialtyInfo selectSchoolSpecialtyInfoById(Long id) {
        return schoolSpecialtyInfoMapper.selectSchoolSpecialtyInfoById(id);
    }

    /**
     * 查询专业信息列表
     *
     * @param schoolSpecialtyInfo 专业信息
     * @return 专业信息
     */
    @Override
    public List<SchoolSpecialtyInfo> selectSchoolSpecialtyInfoList(SchoolSpecialtyInfo schoolSpecialtyInfo) {
        return schoolSpecialtyInfoMapper.selectSchoolSpecialtyInfoList(schoolSpecialtyInfo);
    }

    @Override
    public List<SchoolSpecialtyInfo> getSchoolSpecialtyInfoByMajorClassList10(SchoolSpecialtyInfo schoolSpecialtyInfo) {
        if (StringUtils.isNotBlank(schoolSpecialtyInfo.getMinorClass())) {
            return schoolSpecialtyInfoMapper.selectSchoolSpecialtyInfoByMajorClassList10(schoolSpecialtyInfo);
        }
        return null;
    }

    /**
     * 新增专业信息
     *
     * @param schoolSpecialtyInfo 专业信息
     * @return 结果
     */
    @Override
    public int insertSchoolSpecialtyInfo(SchoolSpecialtyInfo schoolSpecialtyInfo) {
        return schoolSpecialtyInfoMapper.insertSchoolSpecialtyInfo(schoolSpecialtyInfo);
    }

    /**
     * 修改专业信息
     *
     * @param schoolSpecialtyInfo 专业信息
     * @return 结果
     */
    @Override
    public int updateSchoolSpecialtyInfo(SchoolSpecialtyInfo schoolSpecialtyInfo) {
        return schoolSpecialtyInfoMapper.updateSchoolSpecialtyInfo(schoolSpecialtyInfo);
    }

    /**
     * 批量删除专业信息
     *
     * @param ids 需要删除的专业信息主键
     * @return 结果
     */
    @Override
    public int deleteSchoolSpecialtyInfoByIds(Long[] ids) {
        return schoolSpecialtyInfoMapper.deleteSchoolSpecialtyInfoByIds(ids);
    }

    /**
     * 删除专业信息信息
     *
     * @param id 专业信息主键
     * @return 结果
     */
    @Override
    public int deleteSchoolSpecialtyInfoById(Long id) {
        return schoolSpecialtyInfoMapper.deleteSchoolSpecialtyInfoById(id);
    }
}
