package com.teaching.system.service;

import com.teaching.system.api.domain.NationwideCollegeInfo;

import java.util.List;
import java.util.Map;

/**
 * 全国院校信息Service接口
 *
 * @author teaching
 * @date 2025-12-03
 */
public interface INationwideCollegeInfoService {
    /**
     * 查询全国院校信息
     *
     * @param id 全国院校信息主键
     * @return 全国院校信息
     */
    public NationwideCollegeInfo selectNationwideCollegeInfoById(String id);

    public NationwideCollegeInfo selectNationwideCollegeInfoByName(String name);

    public List<Map<String, Object>> selectAllNationwideCollegeProvince();

    /**
     * 查询全国院校信息列表
     *
     * @param nationwideCollegeInfo 全国院校信息
     * @return 全国院校信息集合
     */
    public List<NationwideCollegeInfo> selectNationwideCollegeInfoList(NationwideCollegeInfo nationwideCollegeInfo);

    /**
     * 根据院校名称查询院校信息，最多返回10条记录
     * @param nationwideCollegeInfo
     * @return
     */
    public List<NationwideCollegeInfo> getNationwideCollegeInfoByNameLimit10(NationwideCollegeInfo nationwideCollegeInfo);

    /**
     * 新增全国院校信息
     *
     * @param nationwideCollegeInfo 全国院校信息
     * @return 结果
     */
    public int insertNationwideCollegeInfo(NationwideCollegeInfo nationwideCollegeInfo);

    /**
     * 修改全国院校信息
     *
     * @param nationwideCollegeInfo 全国院校信息
     * @return 结果
     */
    public int updateNationwideCollegeInfo(NationwideCollegeInfo nationwideCollegeInfo);

    /**
     * 批量删除全国院校信息
     *
     * @param ids 需要删除的全国院校信息主键集合
     * @return 结果
     */
    public int deleteNationwideCollegeInfoByIds(String[] ids);

    /**
     * 删除全国院校信息信息
     *
     * @param id 全国院校信息主键
     * @return 结果
     */
    public int deleteNationwideCollegeInfoById(String id);
}
