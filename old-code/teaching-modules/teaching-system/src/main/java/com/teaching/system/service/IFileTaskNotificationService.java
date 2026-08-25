package com.teaching.system.service;

import com.teaching.system.domain.FileTask;
import com.teaching.system.domain.FileTaskNotificationForm;
import com.teaching.system.domain.vo.FileTaskNotificationVo;

import java.util.List;
import java.util.Map;

/**
 * 文件上传任务通知服务。
 */
public interface IFileTaskNotificationService {

    Map<String, Object> send(Long taskId, FileTaskNotificationForm form);

    List<FileTaskNotificationVo> selectAdminList(Long taskId);

    FileTaskNotificationVo selectAdminDetail(Long taskId, Long notificationId);

    void withdraw(Long taskId, Long notificationId);

    List<FileTaskNotificationVo> selectUserList(Long taskId, Long userId);

    FileTaskNotificationVo selectUserDetail(Long taskId, Long notificationId, Long userId);

    void fillNotificationCounts(List<FileTask> tasks, Long userId);
}
