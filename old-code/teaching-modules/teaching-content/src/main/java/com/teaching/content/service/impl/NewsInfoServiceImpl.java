package com.teaching.content.service.impl;

import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.content.domain.NewsInfo;
import com.teaching.content.domain.query.PublicNewsQuery;
import com.teaching.content.domain.vo.PublicNewsInfo;
import com.teaching.content.mapper.NewsInfoMapper;
import com.teaching.content.service.INewsInfoService;
import com.teaching.system.api.domain.SysUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 资讯信息Service业务层处理
 *
 * @author teaching
 * @date 2025-10-27
 */
@Service
public class NewsInfoServiceImpl implements INewsInfoService {
    @Autowired
    private NewsInfoMapper newsInfoMapper;

    /**
     * 查询资讯信息
     *
     * @param newsId 资讯信息主键
     * @return 资讯信息
     */
    @Override
    public NewsInfo selectNewsInfoByNewsId(Long newsId) {
        return newsInfoMapper.selectNewsInfoByNewsId(newsId);
    }

    /**
     * 查询可公开展示的资讯详情。
     *
     * @param newsId 资讯信息主键
     * @return 可公开展示的资讯；不存在或不可公开时返回 {@code null}
     */
    @Override
    public PublicNewsInfo selectPublicNewsInfoByNewsId(Long newsId) {
        return newsInfoMapper.selectPublicNewsInfoByNewsId(newsId);
    }

    /**
     * 查询可公开展示的资讯列表。
     *
     * @param query 公开接口允许使用的查询条件
     * @return 可公开展示的资讯集合
     */
    @Override
    public List<PublicNewsInfo> selectPublicNewsInfoList(PublicNewsQuery query) {
        return newsInfoMapper.selectPublicNewsInfoList(query);
    }

    /**
     * 查询资讯信息列表
     *
     * @param newsInfo 资讯信息
     * @return 资讯信息
     */
    @Override
    public List<NewsInfo> selectNewsInfoList(NewsInfo newsInfo) {
        return newsInfoMapper.selectNewsInfoList(newsInfo);
    }

    /**
     * 新增资讯信息
     *
     * @param newsInfo 资讯信息
     * @return 结果
     */
    @Override
    public int insertNewsInfo(NewsInfo newsInfo) {
        // 获取当前登录用户信息
        com.teaching.system.api.model.LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser != null && loginUser.getSysUser() != null) {
            SysUser sysUser = loginUser.getSysUser();
            newsInfo.setCreateBy(sysUser.getNickName());
            newsInfo.setUserId(sysUser.getUserId());
            newsInfo.setOrgId(sysUser.getOrgId());
        }
        newsInfo.setCreateTime(DateUtils.getNowDate());
        // 默认删除标志为0（未删除）
        if (newsInfo.getDelFlag() == null || newsInfo.getDelFlag().isEmpty()) {
            newsInfo.setDelFlag("0");
        }
        // 默认状态为草稿（1）
        if (newsInfo.getNewsStatus() == null || newsInfo.getNewsStatus().isEmpty()) {
            newsInfo.setNewsStatus("1");
        }
        // 默认不置顶
        if (newsInfo.getIsTop() == null || newsInfo.getIsTop().isEmpty()) {
            newsInfo.setIsTop("0");
        }
        // 初始化阅读量和点赞数
        if (newsInfo.getReadingQuantity() == null) {
            newsInfo.setReadingQuantity(0);
        }
        if (newsInfo.getLikesNum() == null) {
            newsInfo.setLikesNum(0);
        }
        return newsInfoMapper.insertNewsInfo(newsInfo);
    }

    /**
     * 修改资讯信息
     *
     * @param newsInfo 资讯信息
     * @return 结果
     */
    @Override
    public int updateNewsInfo(NewsInfo newsInfo) {
        newsInfo.setUpdateTime(DateUtils.getNowDate());
        // 获取当前登录用户信息
        com.teaching.system.api.model.LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser != null && loginUser.getSysUser() != null) {
            SysUser sysUser = loginUser.getSysUser();
            newsInfo.setUpdateBy(sysUser.getNickName());
        }
        return newsInfoMapper.updateNewsInfo(newsInfo);
    }

    /**
     * 批量删除资讯信息
     *
     * @param newsIds 需要删除的资讯信息主键
     * @return 结果
     */
    @Override
    public int deleteNewsInfoByNewsIds(Long[] newsIds) {
        return newsInfoMapper.deleteNewsInfoByNewsIds(newsIds);
    }

    /**
     * 删除资讯信息信息
     *
     * @param newsId 资讯信息主键
     * @return 结果
     */
    @Override
    public int deleteNewsInfoByNewsId(Long newsId) {
        return newsInfoMapper.deleteNewsInfoByNewsId(newsId);
    }

    /**
     * 发布资讯
     * 状态流转：审核通过(4) -> 已发布(6)
     *
     * @param newsId 资讯ID
     * @return 结果
     */
    @Override
    public int publishNews(Long newsId) {
        // 验证当前状态是否为审核通过
        NewsInfo currentNews = newsInfoMapper.selectNewsInfoByNewsId(newsId);
        if (currentNews == null) {
            throw new RuntimeException("资讯不存在");
        }
        if (!"4".equals(currentNews.getNewsStatus())) {
            throw new RuntimeException("只有审核通过状态的资讯才能发布");
        }
        
        NewsInfo newsInfo = new NewsInfo();
        newsInfo.setNewsId(newsId);
        newsInfo.setNewsStatus("6");  // 已发布
        newsInfo.setPublishTime(new Date());
        newsInfo.setUpdateTime(DateUtils.getNowDate());
        // 获取当前登录用户信息
        com.teaching.system.api.model.LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser != null && loginUser.getSysUser() != null) {
            SysUser sysUser = loginUser.getSysUser();
            newsInfo.setUpdateBy(sysUser.getNickName());
        }
        return newsInfoMapper.updateNewsStatus(newsInfo);
    }

    /**
     * 下架资讯
     * 状态流转：已发布(6) -> 审核通过(4)
     *
     * @param newsId 资讯ID
     * @return 结果
     */
    @Override
    public int offlineNews(Long newsId) {
        // 验证当前状态是否为已发布
        NewsInfo currentNews = newsInfoMapper.selectNewsInfoByNewsId(newsId);
        if (currentNews == null) {
            throw new RuntimeException("资讯不存在");
        }
        if (!"6".equals(currentNews.getNewsStatus())) {
            throw new RuntimeException("只有已发布状态的资讯才能下架");
        }
        
        NewsInfo newsInfo = new NewsInfo();
        newsInfo.setNewsId(newsId);
        newsInfo.setNewsStatus("4");  // 审核通过，可再次发布
        newsInfo.setUpdateTime(DateUtils.getNowDate());
        // 获取当前登录用户信息
        com.teaching.system.api.model.LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser != null && loginUser.getSysUser() != null) {
            SysUser sysUser = loginUser.getSysUser();
            newsInfo.setUpdateBy(sysUser.getNickName());
        }
        return newsInfoMapper.updateNewsStatus(newsInfo);
    }

    /**
     * 提交审核
     * 状态流转：草稿(1) -> 审核中(3)
     *
     * @param newsId 资讯ID
     * @return 结果
     */
    @Override
    public int submitAudit(Long newsId) {
        // 验证当前状态是否为草稿
        NewsInfo currentNews = newsInfoMapper.selectNewsInfoByNewsId(newsId);
        if (currentNews == null) {
            throw new RuntimeException("资讯不存在");
        }
        String currentStatus = currentNews.getNewsStatus();
        
        // 如果状态已经是审核通过(4)，说明没有审核流程，外部接口已经将其设置为审核通过，直接返回成功
        if ("4".equals(currentStatus)) {
            return 1;
        }
        
        // 只有草稿状态才能提交审核
        if (!"1".equals(currentStatus)) {
            throw new RuntimeException("只有草稿状态的资讯才能提交审核");
        }
        
        NewsInfo newsInfo = new NewsInfo();
        newsInfo.setNewsId(newsId);
        newsInfo.setNewsStatus("3");  // 审核中
        newsInfo.setCheckStatus("3"); // 审核中
        newsInfo.setUpdateTime(DateUtils.getNowDate());
        // 获取当前登录用户信息
        com.teaching.system.api.model.LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser != null && loginUser.getSysUser() != null) {
            SysUser sysUser = loginUser.getSysUser();
            newsInfo.setUpdateBy(sysUser.getNickName());
        }
        return newsInfoMapper.updateNewsStatus(newsInfo);
    }

    /**
     * 增加阅读量
     *
     * @param newsId 资讯ID
     * @return 结果
     */
    @Override
    public int increaseReadingQuantity(Long newsId) {
        return newsInfoMapper.increaseReadingQuantity(newsId);
    }

    /**
     * 增加点赞数
     *
     * @param newsId 资讯ID
     * @return 结果
     */
    @Override
    public int increaseLikesNum(Long newsId) {
        return newsInfoMapper.increaseLikesNum(newsId);
    }

    /**
     * 修改资讯审核状态（跨服务调用）
     * 支持以下状态流转：
     * - 草稿(1)或驳回(5) -> 审核中(3)：checkStatus="3", newsStatus="3"
     * - 审核中(3) -> 审核通过(4)：checkStatus="4", newsStatus="4"
     * - 审核中(3) -> 审核驳回(5)：checkStatus="5", newsStatus="5"
     * 参考PageManagerInfoServiceImpl.updatePageManagerStatus实现
     *
     * @param newsInfo 资讯信息（包含newsId和checkStatus）
     * @return 结果
     */
    @Override
    public int updateNewsAuditStatus(NewsInfo newsInfo) {
        // 验证资讯是否存在
        NewsInfo currentNews = newsInfoMapper.selectNewsInfoByNewsId(newsInfo.getNewsId());
        if (currentNews == null) {
            throw new RuntimeException("资讯不存在");
        }
        
        // 根据审核结果更新状态
        String checkStatus = newsInfo.getCheckStatus();
        String newsStatus;
        String currentStatus = currentNews.getNewsStatus();
        
        if ("3".equals(checkStatus)) {
            // 提交审核：草稿(1)或驳回(5) -> 审核中(3)
            if (!"1".equals(currentStatus) && !"5".equals(currentStatus)) {
                throw new RuntimeException("只有草稿或驳回状态的资讯才能提交审核");
            }
            newsStatus = "3";
        } else if ("4".equals(checkStatus)) {
            // 审核通过：审核中(3) -> 审核通过(4)
            // 或者没有审核流程时：草稿(1) -> 审核通过(4)
            if (!"3".equals(currentStatus) && !"1".equals(currentStatus)) {
                throw new RuntimeException("只有审核中或草稿状态的资讯才能审核通过");
            }
            newsStatus = "4";
        } else if ("5".equals(checkStatus)) {
            // 审核驳回：审核中(3) -> 审核驳回(5)
            if (!"3".equals(currentStatus)) {
                throw new RuntimeException("只有审核中状态的资讯才能审核驳回");
            }
            newsStatus = "5";
        } else {
            throw new RuntimeException("无效的审核状态：" + checkStatus);
        }
        
        com.teaching.system.api.model.LoginUser loginUser = SecurityUtils.getLoginUser();
        NewsInfo updateNewsInfo = new NewsInfo();
        updateNewsInfo.setNewsId(newsInfo.getNewsId());
        updateNewsInfo.setCheckStatus(checkStatus);
        updateNewsInfo.setNewsStatus(newsStatus);
        updateNewsInfo.setUpdateTime(DateUtils.getNowDate());
        if (loginUser != null && loginUser.getSysUser() != null) {
            updateNewsInfo.setUpdateBy(loginUser.getSysUser().getNickName());
        }
        return newsInfoMapper.updateNewsStatus(updateNewsInfo);
    }
}
