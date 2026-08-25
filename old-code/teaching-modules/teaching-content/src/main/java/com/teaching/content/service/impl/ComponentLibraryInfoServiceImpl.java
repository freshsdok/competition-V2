package com.teaching.content.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.common.core.utils.uuid.IdUtils;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.content.domain.ComponentLibraryInfo;
import com.teaching.content.mapper.ComponentDataSourceRelaMapper;
import com.teaching.content.mapper.ComponentLibraryInfoMapper;
import com.teaching.content.service.IComponentLibraryInfoService;
import com.teaching.system.api.model.LoginUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 组件库信息Service业务层处理
 *
 * @author teaching
 * @date 2025-10-13
 */
@Service
public class ComponentLibraryInfoServiceImpl implements IComponentLibraryInfoService {
    @Autowired
    private ComponentLibraryInfoMapper componentLibraryInfoMapper;
    @Autowired
    private ComponentDataSourceRelaMapper componentDataSourceRelaMapper;

    /**
     * 查询组件库信息
     *
     * @param componentId 组件库信息主键
     * @return 组件库信息
     */
    @Override
    public ComponentLibraryInfo selectComponentLibraryInfoByComponentId(String componentId) {
        //列表编辑详情时json中不返回主键信息
        ComponentLibraryInfo componentLibraryInfo = componentLibraryInfoMapper.selectComponentLibraryInfoByComponentId(componentId);
        removeIdFromJson(componentLibraryInfo);
        return componentLibraryInfo;
    }

    /**
     * 查询组件库信息列表
     *
     * @param componentLibraryInfo 组件库信息
     * @return 组件库信息
     */
    @Override
    public List<ComponentLibraryInfo> selectComponentLibraryInfoList(ComponentLibraryInfo componentLibraryInfo) {
        return componentLibraryInfoMapper.selectComponentLibraryInfoList(componentLibraryInfo);
    }

    /**
     * 查询组件库信息列表
     *
     * @param componentLibraryInfo 组件库信息
     * @return 组件库信息
     */
    @Override
    public Map<String, List<ComponentLibraryInfo>> selectComponentLibraryInfoListGroupClass(ComponentLibraryInfo componentLibraryInfo) {
        List<ComponentLibraryInfo> componentLibraryInfos = componentLibraryInfoMapper.selectComponentLibraryInfoList(componentLibraryInfo);
        //按照componentClassify分类返回各类的list
        return componentLibraryInfos.stream().collect(Collectors.groupingBy(ComponentLibraryInfo::getComponentClassify));
    }

    /**
     * 新增组件库信息
     *
     * @param componentLibraryInfo 组件库信息
     * @return 结果
     */
    @Override
    public int insertComponentLibraryInfo(ComponentLibraryInfo componentLibraryInfo) {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        componentLibraryInfo.setCreateBy(loginUser.getSysUser().getNickName());
        componentLibraryInfo.setUserId(loginUser.getUserid());
        componentLibraryInfo.setOrgId(loginUser.getSysUser().getOrgId());
        componentLibraryInfo.setComponentId(IdUtils.fastSimpleUUID());
        componentLibraryInfo.setCreateTime(DateUtils.getNowDate());
        addIdToJson(componentLibraryInfo);
        return componentLibraryInfoMapper.insertComponentLibraryInfo(componentLibraryInfo);
    }


    /**
     * 修改组件库信息
     *
     * @param componentLibraryInfo 组件库信息
     * @return 结果
     */
    @Override
    public int updateComponentLibraryInfo(ComponentLibraryInfo componentLibraryInfo) {
        componentLibraryInfo.setUpdateTime(DateUtils.getNowDate());
        return componentLibraryInfoMapper.updateComponentLibraryInfo(componentLibraryInfo);
    }

    /**
     * 批量删除组件库信息
     *
     * @param componentIds 需要删除的组件库信息主键
     * @return 结果
     */
    @Override
    public int deleteComponentLibraryInfoByComponentIds(String[] componentIds) {
        //校验是否已经被引用
        int i = componentDataSourceRelaMapper.checkComponentDataSourceRelaByComponentId(componentIds);
        if (i > 0) {
            throw new RuntimeException("组件已经被引用，不能删除");
        }
        return componentLibraryInfoMapper.deleteComponentLibraryInfoByComponentIds(componentIds);
    }

    /**
     * 删除组件库信息信息
     *
     * @param componentId 组件库信息主键
     * @return 结果
     */
    @Override
    public int deleteComponentLibraryInfoByComponentId(String componentId) {
        //校验是否已经被引用
        int i = componentDataSourceRelaMapper.checkComponentDataSourceRelaByComponentId(new String[]{componentId});
        if (i > 0) {
            throw new RuntimeException("组件已经被引用，不能删除");
        }
        return componentLibraryInfoMapper.deleteComponentLibraryInfoByComponentId(componentId);
    }

    /**
     * 组件内容json串中加上记录主键
     *
     * @param componentLibraryInfo 组件对象得有 componentJson和componentId值
     */
    private void addIdToJson(ComponentLibraryInfo componentLibraryInfo) {
        String componentJson = componentLibraryInfo.getComponentJson();
        if (StringUtils.isNotBlank(componentJson)) {
            JSONObject componentJsonObject = JSON.parseObject(componentJson);
            componentJsonObject.put("componentId", componentLibraryInfo.getComponentId());
            componentLibraryInfo.setComponentJson(componentJsonObject.toJSONString());
        }
    }

    /**
     * 组件内容json串中去掉记录主键
     *
     * @param componentLibraryInfo 组件对象得有
     */
    private void removeIdFromJson(ComponentLibraryInfo componentLibraryInfo) {
        String componentJson = componentLibraryInfo.getComponentJson();
        if (StringUtils.isNotBlank(componentJson)) {
            JSONObject componentJsonObject = JSON.parseObject(componentJson);
            componentJsonObject.remove("componentId");
            componentLibraryInfo.setComponentJson(componentJsonObject.toJSONString());
        }
    }
}
