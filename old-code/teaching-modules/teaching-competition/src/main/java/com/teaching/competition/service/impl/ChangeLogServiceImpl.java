package com.teaching.competition.service.impl;


import cn.hutool.core.map.MapUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.competition.mapper.ChangeLogMapper;
import com.teaching.competition.service.IChangeLogService;
import com.teaching.system.api.domain.ChangeLog;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.util.*;

/**
 * 参赛信息变动日志Service业务层处理
 *
 * @author teaching
 * @date 2026-01-28
 */
@Service
public class ChangeLogServiceImpl implements IChangeLogService {
    private static final Logger log = LoggerFactory.getLogger(ChangeLogServiceImpl.class);
    @Autowired
    private ChangeLogMapper changeLogMapper;

    /**
     * 查询参赛信息变动日志
     *
     * @param id 参赛信息变动日志主键
     * @return 参赛信息变动日志
     */
    @Override
    public ChangeLog selectChangeLogById(Long id) {
        return changeLogMapper.selectChangeLogById(id);
    }

    /**
     * 查询参赛信息变动日志列表
     *
     * @param changeLog 参赛信息变动日志
     * @return 参赛信息变动日志
     */
    @Override
    public List<ChangeLog> selectChangeLogList(ChangeLog changeLog) {
        List<ChangeLog> changeLogs = changeLogMapper.selectChangeLogList(changeLog);
        if (CollectionUtils.isNotEmpty(changeLogs)) {
            changeLogs.forEach(this::castJsonToMapAndMark);
        }
        return changeLogs;
    }

    /**
     * 新增参赛信息变动日志
     *
     * @param changeLog 参赛信息变动日志
     * @return 结果
     */
    @Override
    public int insertChangeLog(ChangeLog changeLog) {
        changeLog.setCreateTime(DateUtils.getNowDate());
        return changeLogMapper.insertChangeLog(changeLog);
    }

    /**
     * 修改参赛信息变动日志
     *
     * @param changeLog 参赛信息变动日志
     * @return 结果
     */
    @Override
    public int updateChangeLog(ChangeLog changeLog) {
        changeLog.setUpdateTime(DateUtils.getNowDate());
        return changeLogMapper.updateChangeLog(changeLog);
    }

    /**
     * 批量删除参赛信息变动日志
     *
     * @param ids 需要删除的参赛信息变动日志主键
     * @return 结果
     */
    @Override
    public int deleteChangeLogByIds(Long[] ids) {
        return changeLogMapper.deleteChangeLogByIds(ids);
    }

    /**
     * 删除参赛信息变动日志信息
     *
     * @param id 参赛信息变动日志主键
     * @return 结果
     */
    @Override
    public int deleteChangeLogById(Long id) {
        return changeLogMapper.deleteChangeLogById(id);
    }


    /**
     * 将json字符串转换为map集合 并标记出值不一样的key
     *
     * @param changeLog
     */
    private void castJsonToMapAndMark(ChangeLog changeLog) {
        String oldData = changeLog.getOldData();
        String newData = changeLog.getNewData();
        List<Map<String, Object>> oldList = null;
        List<Map<String, Object>> newList = null;
        if (StringUtils.isNotBlank(oldData)) {
            if ("retired".equals(changeLog.getChangeType())) {
                JSONArray jsonArray = new JSONArray(oldData);
                JSONArray competitionApplyInfoList = jsonArray.getJSONObject(0).get("competitionApplyInfoList", JSONArray.class);
                JSONArray teacher = jsonArray.getJSONObject(0).get("guideTeacherApplyInfoList", JSONArray.class);
                oldList = convertJsonArrayToMap(competitionApplyInfoList.toString());
                oldList.addAll(convertJsonArrayToMap(teacher.toString()));
            } else {
                oldList = convertJsonArrayToMap(oldData);
            }
        }
        if (StringUtils.isNotBlank(newData)) {
            newList = convertJsonArrayToMap(newData);
        }
        if (!"retired".equals(changeLog.getChangeType())) {
            mark(oldList, newList, changeLog.getChangeType());
        }
        changeLog.setOldDataMap(oldList);
        changeLog.setOldData(null);
        changeLog.setNewDataMap(newList);
        changeLog.setNewData(null);
    }

    /**
     * 将JSON数组字符串转换为List<Map<String, Object>>，保留空值
     *
     * @param jsonArrayStr JSON数组字符串
     * @return 转换后的List<Map < String, Object>>
     */
    public static List<Map<String, Object>> convertJsonArrayToMap(String jsonArrayStr) {
        if (jsonArrayStr == null || jsonArrayStr.trim().isEmpty()) {
            return null;
        }

        // 将JSON字符串解析为JSONArray
        JSONArray jsonArray = JSONUtil.parseArray(jsonArrayStr);
        List<Map<String, Object>> resultList = new ArrayList<>();

        // 遍历JSONArray中的每个JSONObject
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);

            // 将JSONObject转换为Map，第二个参数false表示不忽略空值
            Map<String, Object> map = jsonObject.toBean(Map.class, false);
            resultList.add(map);
        }

        return resultList;
    }

    /**
     * 标记出值不一样的key
     *
     * @param oldList
     * @param newList
     */
    public static void mark(List<Map<String, Object>> oldList, List<Map<String, Object>> newList, String changeType) {
        if (CollectionUtils.isNotEmpty(newList)) {
            if (oldList.size() > newList.size()) {
                List<String> keys = new ArrayList<>();
                //找到oldList中比newLit多的数据
                for (Map<String, Object> stringObjectMap : oldList) {
                    if (!newList.contains(stringObjectMap)) {
                        stringObjectMap.forEach((k, v) -> {
                            keys.add(k);
                        });
                        stringObjectMap.put("keys", keys);
                    }
                }
                return;
            }
            newList.forEach(map -> {
                String idKey = "changeTeacherPromoted".equals(changeType) ? "applyId" : "memberId";
                String memberId = MapUtil.getStr(map, idKey);
                Optional<Map<String, Object>> memberId1 = oldList.stream().filter(m -> MapUtil.getStr(m, idKey).equals(memberId)).findFirst();
                List<String> keys = new ArrayList<>();
                if (memberId1.isPresent()) {
                    map.forEach((k, v) -> {
                        if (!(v == null ? "" : v).equals(memberId1.get().get(k)) || (!memberId1.get().containsKey(k))) {
                            keys.add(k);
                        }
                    });
                }
                if (!memberId1.isPresent()) {
                    map.forEach((k, v) -> {
                        keys.add(k);
                    });
                }
                map.put("keys", keys);
            });
        }
    }

}
