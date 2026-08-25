package com.teaching.system.service.impl;

import com.teaching.system.domain.NotificationSender;
import com.teaching.system.domain.NotificationReceiver;
import com.teaching.system.domain.NotificationInbox;
import com.teaching.system.mapper.NotificationReceiverMapper;
import com.teaching.system.mapper.NotificationSenderMapper;
import com.teaching.system.service.INotificationSenderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 站内信发送表 服务实现
 *
 * @author teaching
 */
@Service
public class NotificationSenderServiceImpl implements INotificationSenderService {

    @Autowired
    private NotificationSenderMapper notificationSenderMapper;

    @Autowired
    private NotificationReceiverMapper notificationReceiverMapper;

    @Override
    public NotificationSender selectNotificationSenderById(Long id) {
        return notificationSenderMapper.selectNotificationSenderById(id);
    }

    @Override
    public List<NotificationSender> selectNotificationSenderList(NotificationSender notificationSender) {
        return notificationSenderMapper.selectNotificationSenderList(notificationSender);
    }

    @Override
    public int insertNotificationSender(NotificationSender notificationSender) {
        return notificationSenderMapper.insertNotificationSender(notificationSender);
    }

    @Override
    public int updateNotificationSender(NotificationSender notificationSender) {
        return notificationSenderMapper.updateNotificationSender(notificationSender);
    }

    @Override
    public int deleteNotificationSenderById(Long id) {
        return notificationSenderMapper.deleteNotificationSenderById(id);
    }

    @Override
    public int deleteNotificationSenderByIds(Long[] ids) {
        return notificationSenderMapper.deleteNotificationSenderByIds(ids);
    }

    @Override
    public List<NotificationSender> selectUnreadForCurrentUser(Long userId, Long orgId) {
        return notificationSenderMapper.selectUnreadForUser(userId, orgId);
    }

    @Override
    public NotificationSender viewAndMarkRead(Long id, Long userId, Long orgId, String username) {
        NotificationSender sender = notificationSenderMapper.selectVisibleById(id, userId, orgId);
        if (sender == null) {
            return null;
        }

        NotificationReceiver receiver = new NotificationReceiver();
        receiver.setNotificationId(id);
        receiver.setToUserId(userId);
        receiver.setOrgId(orgId);
        receiver.setCreateBy(username);
        // 通过 insertReadIfAbsent 写入已读记录（防重复）
        notificationReceiverMapper.insertReadIfAbsent(receiver);
        return sender;
    }

    @Override
    public List<NotificationInbox> selectReadInboxForCurrentUser(Long userId, Long orgId) {
        return notificationSenderMapper.selectReadInboxForUser(userId, orgId);
    }

    @Override
    public List<NotificationInbox> selectAllInboxForCurrentUser(Long userId, Long orgId, String isRead, String title) {
        return notificationSenderMapper.selectAllInboxForUser(userId, orgId, isRead, title);
    }

    @Override
    public void markAllInboxDeletedForCurrentUser(Long userId, Long orgId, String username) {
        // 1) 已有接收记录的，全部标记为已删除
        notificationReceiverMapper.updateDelFlagByToUserId(userId, "1");
        // 2) 未读的（尚无接收记录）插入一条 del_flag=1 的接收记录，收件箱不再显示
        List<NotificationSender> unreadList = notificationSenderMapper.selectUnreadForUser(userId, orgId);
        for (NotificationSender sender : unreadList) {
            NotificationReceiver receiver = new NotificationReceiver();
            receiver.setNotificationId(sender.getId());
            receiver.setToUserId(userId);
            receiver.setOrgId(orgId);
            receiver.setCreateBy(username);
            receiver.setDelFlag("1");
            receiver.setIsRead("0");
            notificationReceiverMapper.insertReadIfAbsent(receiver);
        }
    }

    @Override
    public void markAllInboxReadForCurrentUser(Long userId, Long orgId, String username) {
        List<NotificationSender> unreadList = notificationSenderMapper.selectUnreadForUser(userId, orgId);
        for (NotificationSender sender : unreadList) {
            NotificationReceiver receiver = new NotificationReceiver();
            receiver.setNotificationId(sender.getId());
            receiver.setToUserId(userId);
            receiver.setOrgId(orgId);
            receiver.setCreateBy(username);
            receiver.setIsRead("1");
            receiver.setDelFlag("0");
            notificationReceiverMapper.insertReadIfAbsent(receiver);
        }
    }

    @Override
    public List<NotificationSender> viewAndMarkReadBatch(Long[] ids, Long userId, Long orgId, String username) {
        if (ids == null || ids.length == 0) {
            return Collections.emptyList();
        }
        List<NotificationSender> list = new ArrayList<>(ids.length);
        for (Long id : ids) {
            NotificationSender sender = viewAndMarkRead(id, userId, orgId, username);
            if (sender != null) {
                list.add(sender);
            }
        }
        return list;
    }
}

