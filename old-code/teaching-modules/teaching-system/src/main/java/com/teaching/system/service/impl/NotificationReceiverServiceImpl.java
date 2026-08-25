package com.teaching.system.service.impl;

import com.teaching.system.domain.NotificationReceiver;
import com.teaching.system.mapper.NotificationReceiverMapper;
import com.teaching.system.service.INotificationReceiverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 站内信接收表 服务实现
 *
 * @author teaching
 */
@Service
public class NotificationReceiverServiceImpl implements INotificationReceiverService {

    @Autowired
    private NotificationReceiverMapper notificationReceiverMapper;

    @Override
    public NotificationReceiver selectNotificationReceiverById(Long id) {
        return notificationReceiverMapper.selectNotificationReceiverById(id);
    }

    @Override
    public List<NotificationReceiver> selectNotificationReceiverList(NotificationReceiver notificationReceiver) {
        return notificationReceiverMapper.selectNotificationReceiverList(notificationReceiver);
    }

    @Override
    public int insertNotificationReceiver(NotificationReceiver notificationReceiver) {
        return notificationReceiverMapper.insertNotificationReceiver(notificationReceiver);
    }

    @Override
    public int updateNotificationReceiver(NotificationReceiver notificationReceiver) {
        return notificationReceiverMapper.updateNotificationReceiver(notificationReceiver);
    }

    @Override
    public int deleteNotificationReceiverById(Long id) {
        return notificationReceiverMapper.deleteNotificationReceiverById(id);
    }

    @Override
    public int deleteNotificationReceiverByIds(Long[] ids) {
        return notificationReceiverMapper.deleteNotificationReceiverByIds(ids);
    }
}

