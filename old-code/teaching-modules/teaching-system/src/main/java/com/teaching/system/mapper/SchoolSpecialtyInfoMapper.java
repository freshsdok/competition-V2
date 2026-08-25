package com.teaching.system.mapper;


import java.util.List;

import com.teaching.system.domain.SchoolSpecialtyInfo;

/**
 * 专业信息Mapper接口
 *
 * @author teaching
 * @date 2025-12-03
 */
public interface SchoolSpecialtyInfoMapper {
    /**
     * 查询专业信息
     *
     * @param id 专业信息主键
     * @return 专业信息
     */
    public SchoolSpecialtyInfo selectSchoolSpecialtyInfoById(Long id);

    /**
     * 查询专业信息列表
     *
     * @param schoolSpecialtyInfo 专业信息
     * @return 专业信息集合
     */
    public List<SchoolSpecialtyInfo> selectSchoolSpecialtyInfoList(SchoolSpecialtyInfo schoolSpecialtyInfo);

    /**
     * 查询专业信息列表
     *
     * @param schoolSpecialtyInfo
     * @return
     */
    public List<SchoolSpecialtyInfo> selectSchoolSpecialtyInfoByMajorClassList10(SchoolSpecialtyInfo schoolSpecialtyInfo);

    /**
     * 新增专业信息
     *
     * @param schoolSpecialtyInfo 专业信息
     * @return 结果
     */
    public int insertSchoolSpecialtyInfo(SchoolSpecialtyInfo schoolSpecialtyInfo);

    /**
     * 修改专业信息
     *
     * @param schoolSpecialtyInfo 专业信息
     * @return 结果o
     */
    public int updateSchoolSpecialtyInfo(SchoolSpecialtyInfo schoolSpecialtyInfo);

    /**
     * 删除专业信息
     *
     * @param id 专业信息主键
     * @return 结果
     */
    public int deleteSchoolSpecialtyInfoById(Long id);

    /**
     * 批量删除专业信息
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteSchoolSpecialtyInfoByIds(Long[] ids);
}
