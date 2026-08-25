package com.teaching.content.service.impl;

import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.core.exception.ServiceException;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.content.domain.NoticeInfo;
import com.teaching.content.domain.query.PublicNoticeQuery;
import com.teaching.content.domain.vo.PublicNoticeInfo;
import com.teaching.content.mapper.NoticeInfoMapper;
import com.teaching.content.service.INoticeInfoService;
import com.teaching.content.util.NoticeContentSecurityValidator;
import com.teaching.system.api.CompetitionService;
import com.teaching.common.core.constant.SecurityConstants;
import com.teaching.common.core.domain.R;
import com.teaching.system.api.domain.SysUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通知公告信息Service业务层处理
 *
 * @author teaching
 * @date 2025-10-27
 */
@Service
public class NoticeInfoServiceImpl implements INoticeInfoService {
    @Autowired
    private NoticeInfoMapper noticeInfoMapper;

    @Autowired(required = false)
    private CompetitionService competitionService;

    /**
     * 查询通知公告信息
     *
     * @param noticeId 通知公告信息主键
     * @return 通知公告信息
     */
    @Override
    public NoticeInfo selectNoticeInfoByNoticeId(Long noticeId) {
        return noticeInfoMapper.selectNoticeInfoByNoticeId(noticeId);
    }

    /**
     * 查询可公开展示的通知公告详情。
     *
     * @param noticeId 通知公告信息主键
     * @return 可公开展示的通知公告；不存在或不可公开时返回 {@code null}
     */
    @Override
    public PublicNoticeInfo selectPublicNoticeInfoByNoticeId(Long noticeId) {
        PublicNoticeInfo noticeInfo = noticeInfoMapper.selectPublicNoticeInfoByNoticeId(noticeId);
        return isSafeForPublicRead(noticeInfo) ? noticeInfo : null;
    }

    /**
     * 查询可公开展示的通知公告列表。
     *
     * @param query 公开接口允许使用的查询条件
     * @return 可公开展示的通知公告集合
     */
    @Override
    public List<PublicNoticeInfo> selectPublicNoticeInfoList(PublicNoticeQuery query) {
        List<PublicNoticeInfo> noticeInfos = noticeInfoMapper.selectPublicNoticeInfoList(query);
        if (noticeInfos == null) {
            return Collections.emptyList();
        }
        noticeInfos.removeIf(noticeInfo -> !isSafeForPublicRead(noticeInfo));
        return noticeInfos;
    }

    private boolean isSafeForPublicRead(PublicNoticeInfo noticeInfo) {
        if (noticeInfo == null) {
            return false;
        }
        try {
            NoticeContentSecurityValidator.validateRichText(noticeInfo.getNoticeContent());
            NoticeContentSecurityValidator.validateResourceUrl(noticeInfo.getNoticeImage());
            return true;
        } catch (ServiceException ignored) {
            // 历史脏数据不应继续通过匿名接口暴露；详情按不存在处理，列表直接排除。
            return false;
        }
    }

    /**
     * 查询通知公告信息列表
     *
     * @param noticeInfo 通知公告信息
     * @return 通知公告信息
     */
    @Override
    public List<NoticeInfo> selectNoticeInfoList(NoticeInfo noticeInfo) {
        List<NoticeInfo> list = noticeInfoMapper.selectNoticeInfoList(noticeInfo);

        Map<String, Object> params = noticeInfo.getParams();
        Object ruleParam = params != null ? params.get("rule") : null;
        boolean rule = ruleParam != null && "true".equalsIgnoreCase(String.valueOf(ruleParam));
        if (!rule) {
            return list;
        }

        if (competitionService == null) {
            return list;
        }

        Map<String, Object> query = new HashMap<>();
        Object competitionSeriesId = params != null ? params.get("competitionSeriesId") : null;
        Object competitionTrackId = params != null ? params.get("competitionTrackId") : null;
        Object secondLevelCode = params != null ? params.get("secondLevelCode") : null;
        if (competitionSeriesId != null) {
            query.put("competitionSeriesId", competitionSeriesId);
        }
        if (competitionTrackId != null) {
            query.put("competitionTrackId", competitionTrackId);
        }
        if (secondLevelCode != null) {
            query.put("secondLevelCode", secondLevelCode);
        }
        query.put("rulerStatus", "1");
        query.put("delFlag", "0");

        R<List<Map<String, Object>>> ruleResp = competitionService.listCertExchangeRuleInner(query, SecurityConstants.INNER);
        if (ruleResp != null && R.isSuccess(ruleResp) && ruleResp.getData() != null) {
            for (Map<String, Object> item : ruleResp.getData()) {
                NoticeInfo n = new NoticeInfo();
                n.setType("cert");
                Object ruleIdObj = item.get("ruleId");
                if (ruleIdObj != null) {
                    try {
                        n.setNoticeId(Long.valueOf(String.valueOf(ruleIdObj)));
                    } catch (NumberFormatException ignored) {
                    }
                }
                Object ruleName = item.get("ruleName");
                if (ruleName == null) {
                    ruleName = item.get("rulerName");
                }
                if (ruleName != null) {
                    n.setNoticeTitle(String.valueOf(ruleName));
                }
                Object applyDesc = item.get("applyDesc");
                if (applyDesc != null) {
                    n.setNoticeAbstract(String.valueOf(applyDesc));
                }
                Object icon = item.get("icon");
                if (icon != null) {
                    n.setNoticeImage(String.valueOf(icon));
                }
                Object createTime = item.get("createTime");
                if (createTime instanceof Date) {
                    n.setPublishTime((Date) createTime);
                }
                list.add(n);
            }
        }

        return list;
    }

    /**
     * 新增通知公告信息
     *
     * @param noticeInfo 通知公告信息
     * @return 结果
     */
    @Override
    public int insertNoticeInfo(NoticeInfo noticeInfo) {
        NoticeContentSecurityValidator.validate(noticeInfo);
        // 获取当前登录用户信息
        com.teaching.system.api.model.LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser != null && loginUser.getSysUser() != null) {
            SysUser sysUser = loginUser.getSysUser();
            noticeInfo.setCreateBy(sysUser.getNickName());
            noticeInfo.setUserId(sysUser.getUserId());
            noticeInfo.setOrgId(sysUser.getOrgId());
        }
        noticeInfo.setCreateTime(DateUtils.getNowDate());
        // 默认删除标志为0（未删除）
        if (noticeInfo.getDelFlag() == null || noticeInfo.getDelFlag().isEmpty()) {
            noticeInfo.setDelFlag("0");
        }
        // 默认状态为草稿（1）
        if (noticeInfo.getNoticeStatus() == null || noticeInfo.getNoticeStatus().isEmpty()) {
            noticeInfo.setNoticeStatus("1");
        }
        return noticeInfoMapper.insertNoticeInfo(noticeInfo);
    }

    /**
     * 修改通知公告信息
     *
     * @param noticeInfo 通知公告信息
     * @return 结果
     */
    @Override
    public int updateNoticeInfo(NoticeInfo noticeInfo) {
        NoticeContentSecurityValidator.validate(noticeInfo);
        noticeInfo.setUpdateTime(DateUtils.getNowDate());
        // 获取当前登录用户信息
        com.teaching.system.api.model.LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser != null && loginUser.getSysUser() != null) {
            SysUser sysUser = loginUser.getSysUser();
            noticeInfo.setUpdateBy(sysUser.getNickName());
        }
        return noticeInfoMapper.updateNoticeInfo(noticeInfo);
    }

    /**
     * 批量删除通知公告信息
     *
     * @param noticeIds 需要删除的通知公告信息主键
     * @return 结果
     */
    @Override
    public int deleteNoticeInfoByNoticeIds(Long[] noticeIds) {
        return noticeInfoMapper.deleteNoticeInfoByNoticeIds(noticeIds);
    }

    /**
     * 删除通知公告信息信息
     *
     * @param noticeId 通知公告信息主键
     * @return 结果
     */
    @Override
    public int deleteNoticeInfoByNoticeId(Long noticeId) {
        return noticeInfoMapper.deleteNoticeInfoByNoticeId(noticeId);
    }

    /**
     * 发布通知公告
     * 状态流转：审核通过(4) -> 已发布(6)
     *
     * @param noticeId 通知公告ID
     * @return 结果
     */
    @Override
    public int publishNotice(Long noticeId) {
        // 验证当前状态是否为审核通过
        NoticeInfo currentNotice = noticeInfoMapper.selectNoticeInfoByNoticeId(noticeId);
        if (currentNotice == null) {
            throw new RuntimeException("通知公告不存在");
        }
        if (!"4".equals(currentNotice.getNoticeStatus())) {
            throw new RuntimeException("只有审核通过状态的通知公告才能发布");
        }
        
        // 使用前端传入的 publishTime，如果前端没有传入，则使用当前时间
        // 注意：这里不需要再设置 publishTime，因为前端已经传入了正确的发布时间
        NoticeInfo noticeInfo = new NoticeInfo();
        noticeInfo.setNoticeId(noticeId);
        noticeInfo.setNoticeStatus("6");  // 已发布
        noticeInfo.setUpdateTime(DateUtils.getNowDate());
        // 获取当前登录用户信息
        com.teaching.system.api.model.LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser != null && loginUser.getSysUser() != null) {
            SysUser sysUser = loginUser.getSysUser();
            noticeInfo.setUpdateBy(sysUser.getNickName());
        }
        return noticeInfoMapper.updateNoticeStatus(noticeInfo);
    }

    /**
     * 下架通知公告
     * 状态流转：已发布(6) -> 审核通过(4)
     *
     * @param noticeId 通知公告ID
     * @return 结果
     */
    @Override
    public int offlineNotice(Long noticeId) {
        // 验证当前状态是否为已发布
        NoticeInfo currentNotice = noticeInfoMapper.selectNoticeInfoByNoticeId(noticeId);
        if (currentNotice == null) {
            throw new RuntimeException("通知公告不存在");
        }
        if (!"6".equals(currentNotice.getNoticeStatus())) {
            throw new RuntimeException("只有已发布状态的通知公告才能下架");
        }
        
        NoticeInfo noticeInfo = new NoticeInfo();
        noticeInfo.setNoticeId(noticeId);
        noticeInfo.setNoticeStatus("4");  // 审核通过
        noticeInfo.setUpdateTime(DateUtils.getNowDate());
        // 获取当前登录用户信息
        com.teaching.system.api.model.LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser != null && loginUser.getSysUser() != null) {
            SysUser sysUser = loginUser.getSysUser();
            noticeInfo.setUpdateBy(sysUser.getNickName());
        }
        return noticeInfoMapper.updateNoticeStatus(noticeInfo);
    }

    /**
     * 提交审核
     * 状态流转：草稿(1) -> 审核中(3)
     *
     * @param noticeId 通知公告ID
     * @return 结果
     */
    @Override
    public int submitAudit(Long noticeId) {
        // 验证当前状态是否为草稿
        NoticeInfo currentNotice = noticeInfoMapper.selectNoticeInfoByNoticeId(noticeId);
        if (currentNotice == null) {
            throw new RuntimeException("通知公告不存在");
        }
        String currentStatus = currentNotice.getNoticeStatus();
        
        // 如果状态已经是审核通过(4)，说明没有审核流程，外部接口已经将其设置为审核通过，直接返回成功
        if ("4".equals(currentStatus)) {
            return 1;
        }
        
        // 只有草稿状态才能提交审核
        if (!"1".equals(currentStatus)) {
            throw new RuntimeException("只有草稿状态的通知公告才能提交审核");
        }
        
        NoticeInfo noticeInfo = new NoticeInfo();
        noticeInfo.setNoticeId(noticeId);
        noticeInfo.setNoticeStatus("3");  // 审核中
        noticeInfo.setCheckStatus("3"); // 审核中
        noticeInfo.setUpdateTime(DateUtils.getNowDate());
        // 获取当前登录用户信息
        com.teaching.system.api.model.LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser != null && loginUser.getSysUser() != null) {
            SysUser sysUser = loginUser.getSysUser();
            noticeInfo.setUpdateBy(sysUser.getNickName());
        }
        return noticeInfoMapper.updateNoticeStatus(noticeInfo);
    }

    /**
     * 修改通知公告审核状态（跨服务调用）
     * 支持以下状态流转：
     * - 草稿(1)或驳回(5) -> 审核中(3)：checkStatus="3", noticeStatus="3"
     * - 审核中(3) -> 审核通过(4)：checkStatus="4", noticeStatus="4"
     * - 审核中(3) -> 审核驳回(5)：checkStatus="5", noticeStatus="5"
     * 参考PageManagerInfoServiceImpl.updatePageManagerStatus实现
     *
     * @param noticeInfo 通知公告信息（包含noticeId和checkStatus）
     * @return 结果
     */
    @Override
    public int updateNoticeAuditStatus(NoticeInfo noticeInfo) {
        // 验证通知公告是否存在
        NoticeInfo currentNotice = noticeInfoMapper.selectNoticeInfoByNoticeId(noticeInfo.getNoticeId());
        if (currentNotice == null) {
            throw new RuntimeException("通知公告不存在");
        }
        
        // 根据审核结果更新状态
        String checkStatus = noticeInfo.getCheckStatus();
        String noticeStatus;
        String currentStatus = currentNotice.getNoticeStatus();
        
        if ("3".equals(checkStatus)) {
            // 提交审核：草稿(1)或驳回(5) -> 审核中(3)
            if (!"1".equals(currentStatus) && !"5".equals(currentStatus)) {
                throw new RuntimeException("只有草稿或驳回状态的通知公告才能提交审核");
            }
            noticeStatus = "3";
        } else if ("4".equals(checkStatus)) {
            // 审核通过：审核中(3) -> 审核通过(4)
            // 或者没有审核流程时：草稿(1) -> 审核通过(4)
            if (!"3".equals(currentStatus) && !"1".equals(currentStatus)) {
                throw new RuntimeException("只有审核中或草稿状态的通知公告才能审核通过");
            }
            noticeStatus = "4";
        } else if ("5".equals(checkStatus)) {
            // 审核驳回：审核中(3) -> 审核驳回(5)
            if (!"3".equals(currentStatus)) {
                throw new RuntimeException("只有审核中状态的通知公告才能审核驳回");
            }
            noticeStatus = "5";
        } else {
            throw new RuntimeException("无效的审核状态：" + checkStatus);
        }
        
        com.teaching.system.api.model.LoginUser loginUser = SecurityUtils.getLoginUser();
        NoticeInfo updateNoticeInfo = new NoticeInfo();
        updateNoticeInfo.setNoticeId(noticeInfo.getNoticeId());
        updateNoticeInfo.setCheckStatus(checkStatus);
        updateNoticeInfo.setNoticeStatus(noticeStatus);
        updateNoticeInfo.setUpdateTime(DateUtils.getNowDate());
        if (loginUser != null && loginUser.getSysUser() != null) {
            updateNoticeInfo.setUpdateBy(loginUser.getSysUser().getNickName());
        }
        return noticeInfoMapper.updateNoticeStatus(updateNoticeInfo);
    }
}
