package com.teaching.system.service.impl;

import com.teaching.common.core.utils.StringUtils;
import com.teaching.common.core.utils.uuid.IdUtils;
import com.teaching.system.api.domain.NationwideCollegeInfo;
import com.teaching.system.mapper.NationwideCollegeInfoMapper;
import com.teaching.system.service.INationwideCollegeInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 全国院校信息Service业务层处理
 *
 * @author teaching
 * @date 2025-12-03
 */
@Service
public class NationwideCollegeInfoServiceImpl implements INationwideCollegeInfoService {
    @Autowired
    private NationwideCollegeInfoMapper nationwideCollegeInfoMapper;

    /**
     * 查询全国院校信息
     *
     * @param id 全国院校信息主键
     * @return 全国院校信息
     */
    @Override
    public NationwideCollegeInfo selectNationwideCollegeInfoById(String id) {
        return nationwideCollegeInfoMapper.selectNationwideCollegeInfoById(id);
    }

    @Override
    public NationwideCollegeInfo selectNationwideCollegeInfoByName(String name) {
        return nationwideCollegeInfoMapper.selectNationwideCollegeInfoBySchoolName(name);
    }

    @Override
    public List<Map<String, Object>> selectAllNationwideCollegeProvince() {
        return nationwideCollegeInfoMapper.selectAllNationwideCollegeProvince();
    }

    /**
     * 查询全国院校信息列表
     *
     * @param nationwideCollegeInfo 全国院校信息
     * @return 全国院校信息
     */
    @Override
    public List<NationwideCollegeInfo> selectNationwideCollegeInfoList(NationwideCollegeInfo nationwideCollegeInfo) {
        return nationwideCollegeInfoMapper.selectNationwideCollegeInfoList(nationwideCollegeInfo);
    }

    @Override
    public List<NationwideCollegeInfo> getNationwideCollegeInfoByNameLimit10(NationwideCollegeInfo nationwideCollegeInfo) {
        if(StringUtils.isNotBlank(nationwideCollegeInfo.getSchoolName())||StringUtils.isNotBlank(nationwideCollegeInfo.getId())){
            return nationwideCollegeInfoMapper.selectNationwideCollegeInfoByNameLimit10(nationwideCollegeInfo);
        }
        return null;
    }

    /**
     * 新增全国院校信息
     *
     * @param nationwideCollegeInfo 全国院校信息
     * @return 结果
     */
    @Override
    public int insertNationwideCollegeInfo(NationwideCollegeInfo nationwideCollegeInfo) {
        nationwideCollegeInfo.setId(IdUtils.getRandomCode());
        return nationwideCollegeInfoMapper.insertNationwideCollegeInfo(nationwideCollegeInfo);
    }


    /**
     * 修改全国院校信息
     *
     * @param nationwideCollegeInfo 全国院校信息
     * @return 结果
     */
    @Override
    public int updateNationwideCollegeInfo(NationwideCollegeInfo nationwideCollegeInfo) {
        return nationwideCollegeInfoMapper.updateNationwideCollegeInfo(nationwideCollegeInfo);
    }

    /**
     * 批量删除全国院校信息
     *
     * @param ids 需要删除的全国院校信息主键
     * @return 结果
     */
    @Override
    public int deleteNationwideCollegeInfoByIds(String[] ids) {
        return nationwideCollegeInfoMapper.deleteNationwideCollegeInfoByIds(ids);
    }

    /**
     * 删除全国院校信息信息
     *
     * @param id 全国院校信息主键
     * @return 结果
     */
    @Override
    public int deleteNationwideCollegeInfoById(String id) {
        return nationwideCollegeInfoMapper.deleteNationwideCollegeInfoById(id);
    }
}
