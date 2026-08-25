package com.teaching.content.service.impl;

import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.content.domain.ContentDetail;
import com.teaching.content.mapper.ContentDetailMapper;
import com.teaching.content.service.IContentDetailService;
import com.teaching.system.api.domain.SysUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 内容详情Service业务层处理
 *
 * @author teaching
 * @date 2025-12-10
 */
@Service
public class ContentDetailServiceImpl implements IContentDetailService {
    @Autowired
    private ContentDetailMapper contentDetailMapper;

    /**
     * 查询内容详情
     *
     * @param detailId 内容详情主键
     * @return 内容详情
     */
    @Override
    public ContentDetail selectContentDetailByDetailId(Long detailId) {
        return contentDetailMapper.selectContentDetailByDetailId(detailId);
    }

    /**
     * 查询内容详情列表
     *
     * @param contentDetail 内容详情
     * @return 内容详情集合
     */
    @Override
    public List<ContentDetail> selectContentDetailList(ContentDetail contentDetail) {
        return contentDetailMapper.selectContentDetailList(contentDetail);
    }

    /**
     * 根据栏目ID查询详情
     *
     * @param columnId 栏目ID
     * @return 内容详情
     */
    @Override
    public ContentDetail selectContentDetailByColumnId(Long columnId) {
        ContentDetail contentDetail = new ContentDetail();
        contentDetail.setColumnId(columnId);
        contentDetail.setDelFlag("0");  // 只查询未删除的详情
        List<ContentDetail> details = contentDetailMapper.selectContentDetailList(contentDetail);
        if (details != null && !details.isEmpty()) {
            return details.get(0);
        }
        return null;
    }

    /**
     * 新增内容详情
     *
     * @param contentDetail 内容详情
     * @return 结果
     */
    @Override
    public int insertContentDetail(ContentDetail contentDetail) {
        // 获取当前登录用户信息
        com.teaching.system.api.model.LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser != null && loginUser.getSysUser() != null) {
            SysUser sysUser = loginUser.getSysUser();
            contentDetail.setCreateBy(sysUser.getNickName());
        }
        contentDetail.setCreateTime(DateUtils.getNowDate());
        // 默认删除标志为0（未删除）
        if (contentDetail.getDelFlag() == null || contentDetail.getDelFlag().isEmpty()) {
            contentDetail.setDelFlag("0");
        }
        return contentDetailMapper.insertContentDetail(contentDetail);
    }

    /**
     * 修改内容详情
     *
     * @param contentDetail 内容详情
     * @return 结果
     */
    @Override
    public int updateContentDetail(ContentDetail contentDetail) {
        // 获取当前登录用户信息
        com.teaching.system.api.model.LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser != null && loginUser.getSysUser() != null) {
            SysUser sysUser = loginUser.getSysUser();
            contentDetail.setUpdateBy(sysUser.getNickName());
        }
        contentDetail.setUpdateTime(DateUtils.getNowDate());
        return contentDetailMapper.updateContentDetail(contentDetail);
    }

    /**
     * 删除内容详情
     *
     * @param detailId 内容详情主键
     * @return 结果
     */
    @Override
    public int deleteContentDetailByDetailId(Long detailId) {
        return contentDetailMapper.deleteContentDetailByDetailId(detailId);
    }

    /**
     * 批量删除内容详情
     *
     * @param detailIds 需要删除的数据主键集合
     * @return 结果
     */
    @Override
    public int deleteContentDetailByDetailIds(Long[] detailIds) {
        return contentDetailMapper.deleteContentDetailByDetailIds(detailIds);
    }

    /**
     * 根据栏目ID删除详情
     *
     * @param columnId 栏目ID
     * @return 结果
     */
    @Override
    public int deleteContentDetailByColumnId(Long columnId) {
        // 先查询该栏目下的所有详情
        ContentDetail contentDetail = new ContentDetail();
        contentDetail.setColumnId(columnId);
        List<ContentDetail> details = contentDetailMapper.selectContentDetailList(contentDetail);

        // 如果有详情，则批量删除
        if (details != null && !details.isEmpty()) {
            Long[] detailIds = new Long[details.size()];
            for (int i = 0; i < details.size(); i++) {
                detailIds[i] = details.get(i).getDetailId();
            }
            return contentDetailMapper.deleteContentDetailByDetailIds(detailIds);
        }
        return 0;
    }

    /**
     * 获取"维护公告"信息
     *
     * @return
     */
    @Override
    public List<Map<String, Object>> getNotices() {
        return contentDetailMapper.selectNotices();
    }
}
