package com.teaching.system.service;

import com.teaching.system.domain.NotificationReceiver;

import java.util.List;

/**
 * 站内信接收表 服务接口
 *
 * @author teaching
 */
public interface INotificationReceiverService {

    NotificationReceiver selectNotificationReceiverById(Long id);

    List<NotificationReceiver> selectNotificationReceiverList(NotificationReceiver notificationReceiver);

    int insertNotificationReceiver(NotificationReceiver notificationReceiver);

    int updateNotificationReceiver(NotificationReceiver notificationReceiver);

    int deleteNotificationReceiverById(Long id);

    int deleteNotificationReceiverByIds(Long[] ids);
}

