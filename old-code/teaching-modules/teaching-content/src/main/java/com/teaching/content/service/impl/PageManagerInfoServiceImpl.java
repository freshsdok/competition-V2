package com.teaching.content.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.teaching.common.core.constant.TdConstants;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.content.domain.DataSourceInfo;
import com.teaching.content.mapper.DataSourceInfoMapper;
import com.teaching.content.mapper.PageManagerInfoMapper;
import com.teaching.content.service.IPageManagerInfoService;
import com.teaching.system.api.domain.PageInfo;
import com.teaching.system.api.domain.PageManagerInfo;
import com.teaching.system.api.model.LoginUser;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * 页面管理信息Service业务层处理
 *
 * @author teaching
 * @date 2025-10-14
 */
@Service
public class PageManagerInfoServiceImpl implements IPageManagerInfoService {
    @Autowired
    private PageManagerInfoMapper pageManagerInfoMapper;
    @Autowired
    private DataSourceInfoMapper dataSourceInfoMapper;

    /**
     * 查询页面管理信息
     *
     * @param pageId 页面管理信息主键
     * @return 页面管理信息
     */
    @Override
    public PageManagerInfo selectPageManagerInfoByPageId(Long pageId) {
        PageManagerInfo pageManagerInfo = pageManagerInfoMapper.selectPageManagerInfoByPageId(pageId);
        if(Objects.isNull(pageManagerInfo)){
            return null;
        }
        String pageContent = pageManagerInfo.getPageContent();
        pageManagerInfo.setPageContent(replaceContent(pageContent));
        return pageManagerInfo;
    }

    /**
     * 根据类型和url查询页面管理信息
     * @param pt
     * @param url
     * @return
     */
    @Override
    public PageManagerInfo getInfoByTypeAndUrl(String pt, String url) {
        PageManagerInfo pageManagerInfo = pageManagerInfoMapper.selectInfoByTypeAndUrl(pt,url);
        if(Objects.isNull(pageManagerInfo)){
            return null;
        }
        String pageContent = pageManagerInfo.getPageContent();
        pageManagerInfo.setPageContent(replaceContent(pageContent));
        return pageManagerInfo;
    }

    //补充信息
    public String replaceContent(String pageContent){
        String replacedContent = pageContent;
        if (StringUtils.isNotBlank(pageContent)) {
            // 页面回显组装配置的组件componentId对应的数据源接口地址interfaceUrl
//            List<Map<String, String>> maps = componentDataSourceRelaMapper.selectDataSourceUrlByPageId(pageId);
            JSONArray jsonArray = JSONArray.parseArray(pageContent);
            jsonArray.forEach(obj -> {
                JSONObject jsonObject = (JSONObject) obj;
                Long dataSourceId = jsonObject.getLong("dataSourceId");
                if (jsonObject.containsKey("dataSourceId") && dataSourceId != null) {
                    DataSourceInfo componentId = dataSourceInfoMapper.selectDataSourceInfoByDataId(dataSourceId);
                    jsonObject.put("dataSourceUrl", componentId == null ? "" : componentId.getInterfaceUrl());
                }
                Long dataSourceTwoId = jsonObject.getLong("dataSourceTwoId");
                if (jsonObject.containsKey("dataSourceTwoId") && dataSourceTwoId != null) {
                    DataSourceInfo componentTwoId = dataSourceInfoMapper.selectDataSourceInfoByDataId(dataSourceTwoId);
                    jsonObject.put("dataSourceTwoUrl", componentTwoId == null ? "" : componentTwoId.getInterfaceUrl());
                }
            });
            replacedContent =  jsonArray.toJSONString();
        }
        return replacedContent;
    }
    /**
     * 查询页面管理信息列表
     *
     * @param pageManagerInfo 页面管理信息
     * @return 页面管理信息
     */
    @Override
    public List<PageManagerInfo> selectPageManagerInfoList(PageManagerInfo pageManagerInfo) {
        List<PageManagerInfo> pageManagerInfos = pageManagerInfoMapper.selectPageManagerInfoList2(pageManagerInfo);
        return pageManagerInfos;
    }

    /**
     * 新增页面管理信息
     * 页面内容是json数组
     *
     * @param pageManagerInfo 页面管理信息
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertPageManagerInfo(PageManagerInfo pageManagerInfo) {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        pageManagerInfo.setCreateBy(loginUser.getSysUser().getNickName());
        pageManagerInfo.setUserId(loginUser.getUserid());
        pageManagerInfo.setOrgId(loginUser.getSysUser().getOrgId());
        pageManagerInfo.setCreateTime(DateUtils.getNowDate());
        Long version = pageManagerInfoMapper.selectMaxVersionByDisplayPlatformAndUrl(pageManagerInfo.getDisplayPlatform(), pageManagerInfo.getUrl());
        pageManagerInfo.setVersion(version == null ? 1L : version + 1L);
        return pageManagerInfoMapper.insertPageManagerInfo(pageManagerInfo);
    }

    @Override
    public int copyPageManagerInfo(Long pageId) {
        PageManagerInfo pageManagerInfo = pageManagerInfoMapper.selectPageManagerInfoByPageId(pageId);
        pageManagerInfo.setPageId(null);
        pageManagerInfo.setCheckStatus(TdConstants.CHECK_STATUS_DSH);
        pageManagerInfo.setPublishStatus(TdConstants.PUBLISH_STATUS_CG);
        return insertPageManagerInfo(pageManagerInfo);
    }

    /**
     * 修改页面管理信息
     *
     * @param pageManagerInfo 页面管理信息
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updatePageManagerInfo(PageManagerInfo pageManagerInfo) {
        pageManagerInfo.setUpdateTime(DateUtils.getNowDate());
        return pageManagerInfoMapper.updatePageManagerInfo(pageManagerInfo);
    }

    /**
     * 修改页面内容信息
     *
     * @param pageManagerInfo 页面管理信息
     * @return 更新记录数
     */
    @Override
    public int updatePageManagerContentInfo(PageManagerInfo pageManagerInfo) {
        // 参数校验
        if (pageManagerInfo == null || pageManagerInfo.getPageId() == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        // 查询现有页面信息
        PageManagerInfo existingInfo = pageManagerInfoMapper.selectPageManagerInfoByPageId(pageManagerInfo.getPageId());
        if (existingInfo == null) {
            throw new RuntimeException("页面不存在");
        }
        // 检查状态是否允许修改
        if (isPageLocked(existingInfo)) {
            throw new RuntimeException("当前页面已发布或审核中，不允许修改");
        }
        // 获取当前用户
        LoginUser loginUser = SecurityUtils.getLoginUser();
        // 准备更新内容
        PageManagerInfo updateContent = new PageManagerInfo(pageManagerInfo.getPageContent());
        updateContent.setCheckStatus(TdConstants.CHECK_STATUS_DSH);
        updateContent.setPublishStatus(TdConstants.PUBLISH_STATUS_CG);
        updateContent.setPageId(pageManagerInfo.getPageId());
        // 更新修改信息
        pageManagerInfo.setUpdateTime(DateUtils.getNowDate());
        pageManagerInfo.setUpdateBy(loginUser.getSysUser().getNickName());
        return pageManagerInfoMapper.updatePageManagerInfo(updateContent);
    }

    /**
     * 检查页面是否处于锁定状态
     * 0草稿&审核中3
     * 2已下架&审核中3
     * 1已发布
     * 以上情况不可以修改
     */
    private boolean isPageLocked(PageManagerInfo info) {
        String checkStatus = info.getCheckStatus();
        String publishStatus = info.getPublishStatus();
        return (TdConstants.CHECK_STATUS_SHZ.equals(checkStatus) && TdConstants.PUBLISH_STATUS_CG.equals(publishStatus)) ||
                (TdConstants.CHECK_STATUS_SHZ.equals(checkStatus) && TdConstants.PUBLISH_STATUS_YXJ.equals(publishStatus)) ||
                TdConstants.PUBLISH_STATUS_YFB.equals(publishStatus);
    }


    /**
     * 修改页面基本信息
     *
     * @param pageManagerInfo
     * @return
     */
    @Override
    public int updatePageManagerBaseInfo(PageManagerInfo pageManagerInfo) {
        PageManagerInfo pageManger = new PageManagerInfo();
        pageManger.setUrl(pageManagerInfo.getUrl());
        pageManger.setDisplayPlatform(pageManagerInfo.getDisplayPlatform());
        pageManger.setPublishStatus(TdConstants.PUBLISH_STATUS_YFB);
        List<PageManagerInfo> pageManagerInfos = pageManagerInfoMapper.selectPageManagerInfoList(pageManger);
        if (TdConstants.PUBLISH_STATUS_YXJ.equals(pageManagerInfo.getPublishStatus()) && CollectionUtils.isNotEmpty(pageManagerInfos) && pageManagerInfos.size() == 1) {
            throw new RuntimeException("当前页面已发布，不允许下架");
        }
        LoginUser loginUser = SecurityUtils.getLoginUser();
        pageManagerInfo.setUpdateTime(DateUtils.getNowDate());
        pageManagerInfo.setUpdateBy(loginUser.getSysUser().getNickName());
        pageManagerInfo.setPageContent(null);
        return pageManagerInfoMapper.updatePageManagerInfo(pageManagerInfo);
    }

    /**
     * 修改页面管理审核状态
     *
     * @param pageInfo
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public int updatePageManagerStatus(PageInfo pageInfo) {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        PageManagerInfo pageManagerInfo = new PageManagerInfo(pageInfo.getPageId(), pageInfo.getCheckStatus(), pageInfo.getApplyReason());
        pageManagerInfo.setUpdateTime(DateUtils.getNowDate());
        pageManagerInfo.setUpdateBy(loginUser.getSysUser().getNickName());
        return pageManagerInfoMapper.updatePageManagerInfo(pageManagerInfo);
    }

    /**
     * 批量删除页面管理信息
     *
     * @param pageIds 需要删除的页面管理信息主键
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deletePageManagerInfoByPageIds(Long[] pageIds) {
//        componentDataSourceRelaMapper.deleteComponentDataSourceRelaByPageIds(pageIds);
        return pageManagerInfoMapper.deletePageManagerInfoByPageIds(pageIds);
    }

    /**
     * 删除页面管理信息信息
     *
     * @param pageInfo 页面管理信息
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deletePageManagerInfoByPageId(PageManagerInfo pageInfo) {
        /*PageManagerInfo pageManger = new PageManagerInfo();
        pageManger.setUrl(pageInfo.getUrl());
        pageManger.setDisplayPlatform(pageInfo.getDisplayPlatform());
        List<PageManagerInfo> pageManagerInfos = pageManagerInfoMapper.selectPageManagerInfoList(pageManger);
        if (CollectionUtils.isNotEmpty(pageManagerInfos) && pageManagerInfos.size() == 1) {
            throw new RuntimeException("当前页面已发布，不允许删除");
        }*/
        return pageManagerInfoMapper.deletePageManagerInfoByPageId(pageInfo.getPageId());
    }
}
