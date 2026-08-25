package com.teaching.system.service;

import com.teaching.system.domain.NotificationSender;
import com.teaching.system.domain.NotificationInbox;

import java.util.List;

/**
 * 站内信发送表 服务接口
 *
 * @author teaching
 */
public interface INotificationSenderService {

    NotificationSender selectNotificationSenderById(Long id);

    List<NotificationSender> selectNotificationSenderList(NotificationSender notificationSender);

    int insertNotificationSender(NotificationSender notificationSender);

    int updateNotificationSender(NotificationSender notificationSender);

    int deleteNotificationSenderById(Long id);

    int deleteNotificationSenderByIds(Long[] ids);

    /**
     * 当前用户未读站内信列表（发送表存在、接收表不存在）
     */
    List<NotificationSender> selectUnreadForCurrentUser(Long userId, Long orgId);

    /**
     * 查看详情并标记为已读（首次查看时写入接收表）
     */
    NotificationSender viewAndMarkRead(Long id, Long userId, Long orgId, String username);

    /**
     * 当前用户已读站内信列表（接收表存在）
     */
    List<NotificationInbox> selectReadInboxForCurrentUser(Long userId, Long orgId);

    /**
     * 当前用户全部收件箱（已读+未读），支持按已读/未读、标题模糊筛选
     *
     * @param isRead 已读状态：1-已读，0-未读，null 则不过滤
     * @param title  标题模糊筛选，null 则不过滤
     */
    List<NotificationInbox> selectAllInboxForCurrentUser(Long userId, Long orgId, String isRead, String title);

    /**
     * 当前用户收件箱全部删除（已读的标记删除，未读的插入一条已删除记录，收件箱清空）
     */
    void markAllInboxDeletedForCurrentUser(Long userId, Long orgId, String username);

    /**
     * 当前用户收件箱全部已读（对所有未读站内信写入接收表）
     */
    void markAllInboxReadForCurrentUser(Long userId, Long orgId, String username);

    /**
     * 查看站内信详情并标记已读（支持多条），返回有权限的详情列表
     */
    List<NotificationSender> viewAndMarkReadBatch(Long[] ids, Long userId, Long orgId, String username);
}

