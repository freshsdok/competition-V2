package com.teaching.system.mapper;

import com.teaching.system.domain.NotificationReceiver;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 站内信接收表 Mapper
 *
 * @author teaching
 */
@Mapper
public interface NotificationReceiverMapper {

    /**
     * 根据主键查询
     *
     * @param id 主键
     * @return 接收记录
     */
    NotificationReceiver selectNotificationReceiverById(Long id);

    /**
     * 查询列表
     *
     * @param notificationReceiver 查询条件
     * @return 列表
     */
    List<NotificationReceiver> selectNotificationReceiverList(NotificationReceiver notificationReceiver);

    /**
     * 新增
     *
     * @param notificationReceiver 记录
     * @return 结果
     */
    int insertNotificationReceiver(NotificationReceiver notificationReceiver);

    /**
     * 首次阅读时插入（防重复）
     *
     * @param notificationReceiver 阅读记录
     * @return 影响行数（0/1）
     */
    int insertReadIfAbsent(NotificationReceiver notificationReceiver);

    /**
     * 修改
     *
     * @param notificationReceiver 记录
     * @return 结果
     */
    int updateNotificationReceiver(NotificationReceiver notificationReceiver);

    /**
     * 删除
     *
     * @param id 主键
     * @return 结果
     */
    int deleteNotificationReceiverById(Long id);

    /**
     * 批量删除
     *
     * @param ids 主键数组
     * @return 结果
     */
    int deleteNotificationReceiverByIds(Long[] ids);

    /**
     * 按接收人更新删除标识（用于收件箱全部删除）
     *
     * @param toUserId 接收人用户ID
     * @param delFlag  删除标识
     * @return 更新行数
     */
    int updateDelFlagByToUserId(@Param("toUserId") Long toUserId, @Param("delFlag") String delFlag);
}

