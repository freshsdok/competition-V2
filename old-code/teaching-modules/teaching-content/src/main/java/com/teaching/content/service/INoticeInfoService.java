package com.teaching.content.service;

import com.teaching.content.domain.NoticeInfo;
import com.teaching.content.domain.query.PublicNoticeQuery;
import com.teaching.content.domain.vo.PublicNoticeInfo;

import java.util.List;

/**
 * 通知公告信息Service接口
 *
 * @author teaching
 * @date 2025-10-27
 */
public interface INoticeInfoService {
    /**
     * 查询通知公告信息
     *
     * @param noticeId 通知公告信息主键
     * @return 通知公告信息
     */
    public NoticeInfo selectNoticeInfoByNoticeId(Long noticeId);

    /**
     * 查询可公开展示的通知公告详情。
     *
     * @param noticeId 通知公告信息主键
     * @return 可公开展示的通知公告；不存在或不可公开时返回 {@code null}
     */
    PublicNoticeInfo selectPublicNoticeInfoByNoticeId(Long noticeId);

    /**
     * 查询可公开展示的通知公告列表。
     *
     * @param query 公开接口允许使用的查询条件
     * @return 可公开展示的通知公告集合
     */
    List<PublicNoticeInfo> selectPublicNoticeInfoList(PublicNoticeQuery query);

    /**
     * 查询通知公告信息列表
     *
     * @param noticeInfo 通知公告信息
     * @return 通知公告信息集合
     */
    public List<NoticeInfo> selectNoticeInfoList(NoticeInfo noticeInfo);

    /**
     * 新增通知公告信息
     *
     * @param noticeInfo 通知公告信息
     * @return 结果
     */
    public int insertNoticeInfo(NoticeInfo noticeInfo);

    /**
     * 修改通知公告信息
     *
     * @param noticeInfo 通知公告信息
     * @return 结果
     */
    public int updateNoticeInfo(NoticeInfo noticeInfo);

    /**
     * 批量删除通知公告信息
     *
     * @param noticeIds 需要删除的通知公告信息主键集合
     * @return 结果
     */
    public int deleteNoticeInfoByNoticeIds(Long[] noticeIds);

    /**
     * 删除通知公告信息信息
     *
     * @param noticeId 通知公告信息主键
     * @return 结果
     */
    public int deleteNoticeInfoByNoticeId(Long noticeId);

    /**
     * 发布通知公告
     *
     * @param noticeId 通知公告ID
     * @return 结果
     */
    public int publishNotice(Long noticeId);

    /**
     * 下架通知公告
     *
     * @param noticeId 通知公告ID
     * @return 结果
     */
    public int offlineNotice(Long noticeId);

    /**
     * 提交审核
     *
     * @param noticeId 通知公告ID
     * @return 结果
     */
    public int submitAudit(Long noticeId);

    /**
     * 修改通知公告审核状态（跨服务调用）
     *
     * @param noticeInfo 通知公告信息（包含noticeId和checkStatus）
     * @return 结果
     */
    public int updateNoticeAuditStatus(NoticeInfo noticeInfo);
}
