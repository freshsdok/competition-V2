package com.teaching.competition.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson2.JSONArray;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.competition.domain.CompetitionCheckDataPackage;
import com.teaching.competition.domain.CompetitionCheckInfo;
import com.teaching.competition.mapper.CompetitionCheckDataPackageMapper;
import com.teaching.competition.mapper.CompetitionCheckInfoMapper;
import com.teaching.competition.service.ICompetitionCheckDataPackageService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 校验包Service业务层处理
 *
 * @author teaching
 * @date 2025-12-18
 */
@Service
public class CompetitionCheckDataPackageServiceImpl implements ICompetitionCheckDataPackageService {
    @Autowired
    private CompetitionCheckDataPackageMapper competitionCheckDataPackageMapper;
    @Autowired
    private CompetitionCheckInfoMapper competitionCheckInfoMapper;

    /**
     * 查询校验包
     *
     * @param packageId 校验包主键
     * @return 校验包
     */
    @Override
    public CompetitionCheckDataPackage selectCompetitionCheckDataPackageByPackageId(Long packageId) {
        CompetitionCheckDataPackage competitionCheckDataPackage = competitionCheckDataPackageMapper.selectCompetitionCheckDataPackageByPackageId(packageId);
        setCheckInfoList(competitionCheckDataPackage);
        return competitionCheckDataPackage;
    }

    /**
     * 查询校验包列表
     *
     * @param competitionCheckDataPackage 校验包
     * @return 校验包
     */
    @Override
    public List<CompetitionCheckDataPackage> selectCompetitionCheckDataPackageList(CompetitionCheckDataPackage competitionCheckDataPackage) {
        List<CompetitionCheckDataPackage> competitionCheckDataPackages = competitionCheckDataPackageMapper.selectCompetitionCheckDataPackageList(competitionCheckDataPackage);
//        competitionCheckDataPackages.forEach(this::setCheckInfoList);
        return competitionCheckDataPackages;
    }

    /**
     * 新增校验包
     *
     * @param competitionCheckDataPackage 校验包
     * @return 结果
     */
    @Override
    public int insertCompetitionCheckDataPackage(CompetitionCheckDataPackage competitionCheckDataPackage) {
        competitionCheckDataPackage.setPackageJson(getJsonArrayStr(competitionCheckDataPackage.getCheckInfoList()));
        competitionCheckDataPackage.setCreateTime(DateUtils.getNowDate());
        return competitionCheckDataPackageMapper.insertCompetitionCheckDataPackage(competitionCheckDataPackage);
    }


    /**
     * 修改校验包
     *
     * @param competitionCheckDataPackage 校验包
     * @return 结果
     */
    @Override
    public int updateCompetitionCheckDataPackage(CompetitionCheckDataPackage competitionCheckDataPackage) {
        competitionCheckDataPackage.setUpdateTime(DateUtils.getNowDate());
        competitionCheckDataPackage.setPackageJson(getJsonArrayStr(competitionCheckDataPackage.getCheckInfoList()));
        return competitionCheckDataPackageMapper.updateCompetitionCheckDataPackage(competitionCheckDataPackage);
    }

    /**
     * 批量删除校验包
     *
     * @param packageIds 需要删除的校验包主键
     * @return 结果
     */
    @Override
    public int deleteCompetitionCheckDataPackageByPackageIds(Long[] packageIds) {
        return competitionCheckDataPackageMapper.deleteCompetitionCheckDataPackageByPackageIds(packageIds);
    }

    /**
     * 删除校验包信息
     *
     * @param packageId 校验包主键
     * @return 结果
     */
    @Override
    public int deleteCompetitionCheckDataPackageByPackageId(Long packageId) {
        return competitionCheckDataPackageMapper.deleteCompetitionCheckDataPackageByPackageId(packageId);
    }

    /**
     * 获取校验项信息json字符串
     *
     * @param checkInfoList
     * @return
     */
    private String getJsonArrayStr(List<CompetitionCheckInfo> checkInfoList) {
        if (CollectionUtils.isEmpty(checkInfoList)) {
            return null;
        }
        List<Long> checkItemIds = checkInfoList.stream()
                .map(CompetitionCheckInfo::getCheckItemId)
                .collect(Collectors.toList());

        List<CompetitionCheckInfo> competitionCheckInfos =
                competitionCheckInfoMapper.selectCompetitionCheckInfoByCheckItemIds(checkItemIds);
        JSONArray jsonArray = new JSONArray(filterCheckInfoList(competitionCheckInfos));
        return jsonArray.toString();
    }


    /**
     * 过滤校验项信息
     *
     * @param checkInfoList
     * @return
     */
    private List<JSONObject> filterCheckInfoList(List<CompetitionCheckInfo> checkInfoList) {
        return checkInfoList.stream()
                .map(info -> {
                    JSONObject jsonObj = (JSONObject) JSONObject.toJSON(info);
                    jsonObj.remove("createBy");
                    jsonObj.remove("createTime");
                    jsonObj.remove("updateBy");
                    jsonObj.remove("updateTime");
                    jsonObj.remove("version");
                    jsonObj.remove("delFlag");
                    jsonObj.remove("searchValue");
                    jsonObj.remove("params");
                    return jsonObj;
                }).collect(Collectors.toList());
    }


    /**
     * 设置校验包中校验项详情
     * @param competitionCheckDataPackage
     */
    private void setCheckInfoList(CompetitionCheckDataPackage competitionCheckDataPackage) {
        String packageJson = competitionCheckDataPackage.getPackageJson();
        if(StringUtils.isNotBlank(packageJson)){
            //json 转成list
            List<CompetitionCheckInfo> checkInfoList = JSONObject.parseArray(packageJson, CompetitionCheckInfo.class);
            competitionCheckDataPackage.setCheckInfoList(checkInfoList);
            competitionCheckDataPackage.setPackageJson(null);
        }
    }
}
