package com.teaching.content.service.impl;

import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.content.domain.ContentBannerInfo;
import com.teaching.content.mapper.ContentBannerInfoMapper;
import com.teaching.content.service.IContentBannerInfoService;
import com.teaching.system.api.domain.SysUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * banner图管理Service业务层处理
 *
 * @author teaching
 * @date 2025-10-22
 */
@Service
public class ContentBannerInfoServiceImpl implements IContentBannerInfoService {
    @Autowired
    private ContentBannerInfoMapper contentBannerInfoMapper;

    /**
     * 查询banner图管理
     *
     * @param id banner图管理主键
     * @return banner图管理
     */
    @Override
    public ContentBannerInfo selectContentBannerInfoById(Long id) {
        return contentBannerInfoMapper.selectContentBannerInfoById(id);
    }

    /**
     * 查询banner图管理列表
     *
     * @param contentBannerInfo banner图管理
     * @return banner图管理
     */
    @Override
    public List<ContentBannerInfo> selectContentBannerInfoList(ContentBannerInfo contentBannerInfo) {
        return contentBannerInfoMapper.selectContentBannerInfoList(contentBannerInfo);
    }

    @Override
    public List<Map<String, Object>> getContentBannerInfoListByPc(ContentBannerInfo contentBannerInfo) {
        return contentBannerInfoMapper.selectContentBannerInfoListByPc(contentBannerInfo);
    }

    /**
     * 新增banner图管理
     *
     * @param contentBannerInfo banner图管理
     * @return 结果
     */
    @Override
    public int insertContentBannerInfo(ContentBannerInfo contentBannerInfo) {
        SysUser sysUser = SecurityUtils.getLoginUser().getSysUser();
        contentBannerInfo.setCreateTime(DateUtils.getNowDate());
        contentBannerInfo.setCreateBy(sysUser.getNickName());
        contentBannerInfo.setUserId(sysUser.getUserId());
        contentBannerInfo.setOrgId(sysUser.getOrgId());
        return contentBannerInfoMapper.insertContentBannerInfo(contentBannerInfo);
    }

    /**
     * 修改banner图管理
     *
     * @param contentBannerInfo banner图管理
     * @return 结果
     */
    @Override
    public int updateContentBannerInfo(ContentBannerInfo contentBannerInfo) {
        SysUser sysUser = SecurityUtils.getLoginUser().getSysUser();
        contentBannerInfo.setUpdateTime(DateUtils.getNowDate());
        contentBannerInfo.setUpdateBy(sysUser.getNickName());
        return contentBannerInfoMapper.updateContentBannerInfo(contentBannerInfo);
    }

    /**
     * 批量删除banner图管理
     *
     * @param ids 需要删除的banner图管理主键
     * @return 结果
     */
    @Override
    public int deleteContentBannerInfoByIds(Long[] ids) {
        return contentBannerInfoMapper.deleteContentBannerInfoByIds(ids);
    }

    /**
     * 删除banner图管理信息
     *
     * @param id banner图管理主键
     * @return 结果
     */
    @Override
    public int deleteContentBannerInfoById(Long id) {
        return contentBannerInfoMapper.deleteContentBannerInfoById(id);
    }
}
