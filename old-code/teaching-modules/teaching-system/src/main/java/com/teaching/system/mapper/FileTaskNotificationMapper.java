package com.teaching.system.mapper;

import com.teaching.system.domain.FileTaskNotification;
import com.teaching.system.domain.vo.FileTaskNotificationCountVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文件上传任务通知数据访问。
 */
@Mapper
public interface FileTaskNotificationMapper {

    int insert(FileTaskNotification notification);

    List<FileTaskNotification> selectAdminList(@Param("taskId") Long taskId);

    FileTaskNotification selectByTaskAndId(@Param("taskId") Long taskId,
                                           @Param("notificationId") Long notificationId);

    int withdraw(@Param("taskId") Long taskId,
                 @Param("notificationId") Long notificationId,
                 @Param("withdrawUserId") Long withdrawUserId,
                 @Param("updateBy") String updateBy);

    List<FileTaskNotification> selectUserList(@Param("taskId") Long taskId,
                                              @Param("userId") Long userId);

    FileTaskNotification selectUserDetail(@Param("taskId") Long taskId,
                                          @Param("notificationId") Long notificationId,
                                          @Param("userId") Long userId);

    List<FileTaskNotificationCountVo> countActiveByTaskIdsAndUser(
            @Param("taskIds") List<Long> taskIds, @Param("userId") Long userId);
}
