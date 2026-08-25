package com.teaching.system.service;

import com.teaching.system.domain.SchoolSpecialtyInfo;

import java.util.List;

/**
 * 专业信息Service接口
 *
 * @author teaching
 * @date 2025-12-03
 */
public interface ISchoolSpecialtyInfoService {
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
     * @param schoolSpecialtyInfo
     * @return
     */
    public List<SchoolSpecialtyInfo> getSchoolSpecialtyInfoByMajorClassList10(SchoolSpecialtyInfo schoolSpecialtyInfo);

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
     * @return 结果
     */
    public int updateSchoolSpecialtyInfo(SchoolSpecialtyInfo schoolSpecialtyInfo);

    /**
     * 批量删除专业信息
     *
     * @param ids 需要删除的专业信息主键集合
     * @return 结果
     */
    public int deleteSchoolSpecialtyInfoByIds(Long[] ids);

    /**
     * 删除专业信息信息
     *
     * @param id 专业信息主键
     * @return 结果
     */
    public int deleteSchoolSpecialtyInfoById(Long id);
}
