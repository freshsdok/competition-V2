package com.teaching.system.mapper;

import com.teaching.system.domain.NotificationSender;
import com.teaching.system.domain.NotificationInbox;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 站内信发送表 Mapper
 *
 * @author teaching
 */
@Mapper
public interface NotificationSenderMapper {

    /**
     * 根据主键查询
     *
     * @param id 主键
     * @return 发送记录
     */
    NotificationSender selectNotificationSenderById(Long id);

    /**
     * 查询列表
     *
     * @param notificationSender 查询条件
     * @return 列表
     */
    List<NotificationSender> selectNotificationSenderList(NotificationSender notificationSender);

    /**
     * 查询当前用户未读站内信（只存在于发送表，接收表无记录）
     *
     * @param userId 用户ID
     * @param orgId  机构ID（为空则不按机构过滤）
     * @return 未读消息列表
     */
    List<NotificationSender> selectUnreadForUser(@Param("userId") Long userId, @Param("orgId") Long orgId);

    /**
     * 查询当前用户可见的站内信详情
     *
     * @param id     发送表主键
     * @param userId 用户ID
     * @param orgId  机构ID（为空则不按机构过滤）
     * @return 消息详情
     */
    NotificationSender selectVisibleById(@Param("id") Long id, @Param("userId") Long userId, @Param("orgId") Long orgId);

    /**
     * 已读列表（接收表存在）
     */
    List<NotificationInbox> selectReadInboxForUser(@Param("userId") Long userId, @Param("orgId") Long orgId);

    /**
     * 全部收件箱（已读 + 未读），支持按已读/未读、标题模糊筛选
     */
    List<NotificationInbox> selectAllInboxForUser(@Param("userId") Long userId, @Param("orgId") Long orgId,
                                                  @Param("isRead") String isRead, @Param("title") String title);

    /**
     * 新增
     *
     * @param notificationSender 记录
     * @return 结果
     */
    int insertNotificationSender(NotificationSender notificationSender);

    /**
     * 修改
     *
     * @param notificationSender 记录
     * @return 结果
     */
    int updateNotificationSender(NotificationSender notificationSender);

    /**
     * 删除
     *
     * @param id 主键
     * @return 结果
     */
    int deleteNotificationSenderById(Long id);

    /**
     * 批量删除
     *
     * @param ids 主键数组
     * @return 结果
     */
    int deleteNotificationSenderByIds(Long[] ids);
}

