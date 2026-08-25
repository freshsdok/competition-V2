package com.teaching.system.service.impl;

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.teaching.common.core.exception.GlobalException;
import com.teaching.common.redis.service.RedisService;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.system.domain.FileTask;
import com.teaching.system.domain.FileUploadManager;
import com.teaching.system.domain.FileUploadRecord;
import com.teaching.system.domain.SysUserGroupCompetitionRelation;
import com.teaching.system.mapper.*;
import com.teaching.system.service.FileTaskAudienceService;
import com.teaching.system.service.IFileTaskUserService;
import com.teaching.system.service.IFileTaskNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FileTaskUserServiceImpl implements IFileTaskUserService {

    private static final Logger logger = LoggerFactory.getLogger(FileTaskUserServiceImpl.class);

    @Autowired
    private FileTaskMapper fileTaskMapper;

    @Autowired
    private FileTaskConfigMapper fileTaskConfigMapper;

    @Autowired
    private FileUploadRecordMapper fileUploadRecordMapper;

    @Autowired
    private FileUploadManagerMapper fileUploadManagerMapper;

    @Autowired
    private RedisService redisService;

    @Autowired
    private SysUserGroupMapper sysUserGroupMapper;

    @Autowired
    private FileTaskAudienceService fileTaskAudienceService;

    @Autowired
    private IFileTaskNotificationService fileTaskNotificationService;

    @Override
    public List<FileTask> selectFileTaskUserList(FileTask fileTask) {
        Long userId = SecurityUtils.getLoginUser().getSysUser().getUserId();
        // userGroup:info 由登录后的异步任务维护，可能缺失或陈旧，不能作为任务访问依据。
        // 先查询已发布任务，再以权威的正向 groupUserIds 集合做动态受众过滤。
        fileTask.setUserGroupIds(null);
        // 只获取已发布任务
        fileTask.setTaskStatus("2");
        List<FileTask> fileTasks = fileTaskMapper.selectFileTaskList(fileTask);
        if (fileTasks == null) {
            return new ArrayList<>();
        }
        fileTasks = fileTasks.stream()
                .filter(task -> fileTaskAudienceService.isCurrentRecipient(task, userId))
                .collect(Collectors.toList());
        if(CollectionUtils.isNotEmpty(fileTasks)){
            fileTasks.stream().forEach(fileTaskRes -> {
                logger.info("PC获取任务数据库用户组id列表：{}", fileTaskRes.getUserGroupIds());
                // 获取赛事信息
                List<String> taskUserGroupIdsStr =  Arrays.asList(fileTaskRes.getUserGroupIds().split( ","));
                List<Long> taskUserGroupIdList = taskUserGroupIdsStr.stream().map(Long::parseLong).toList();
                if(CollectionUtils.isNotEmpty(taskUserGroupIdList)){
                    List<List<SysUserGroupCompetitionRelation>> sysUserGroupCompetitionRelationList = new ArrayList<>();
                    taskUserGroupIdList.stream().forEach(userGroupId -> {
                        List<SysUserGroupCompetitionRelation> sysUserGroupCompetitionRelations = sysUserGroupMapper.selectSysUserGroupCompetitionRelationList(userGroupId);
                        sysUserGroupCompetitionRelationList.add(sysUserGroupCompetitionRelations);
                    });
                    fileTaskRes.setSysUserGroupCompetitionRelationList(sysUserGroupCompetitionRelationList);
                }
                fileTaskRes.getFileTaskConfigList().stream().forEach(fileTaskConfig -> {
                    // 获取用户是否已经上传文件
                    FileUploadRecord fileUploadRecord = new FileUploadRecord();
                    fileUploadRecord.setFileTaskId(fileTaskConfig.getId());
                    fileUploadRecord.setUserId(userId);
                    List<FileUploadRecord> fileUploadRecords = fileUploadRecordMapper.selectFileUploadRecordListByUser(fileUploadRecord);
                    if(CollectionUtils.isNotEmpty(fileUploadRecords)){
                        fileUploadRecords.stream().forEach(fileUploadRecordRes -> {
                            if(!"delete".equals(fileUploadRecordRes.getUploadOperationType())){
                                fileTaskConfig.setFileUploadRecord(fileUploadRecords.get(0));
                            }
                        });
                    }
                });
//                FileUploadManager fileUploadManager = new FileUploadManager();
//                fileUploadManager.setFileTaskId(fileTaskRes.getId());
//                fileUploadManager.setUserId(userId);
                // 判断当前用户是否已经保存过文件
//                List<FileUploadManager> fileUploadManagers = fileUploadManagerMapper.selectFileUploadListByUser(fileUploadManager);
//                if(CollectionUtils.isNotEmpty(fileUploadManagers) && fileUploadManagers.get(0).getSubmitStatus()!=null
//                        && fileUploadManagers.get(0).getSubmitStatus()){
//                    fileTaskRes.setSubmitStatus(true);
//                } else {
//                    fileTaskRes.setSubmitStatus(false);
//                }
                // 判断当前用户是否已读该任务
                if(redisService.hasKey("fileTaskReadRecord:" + userId + ":" + fileTaskRes.getId())){
                    fileTaskRes.setReadCountFlag(true);
                } else {
                    fileTaskRes.setReadCountFlag(false);
                }
            });
            fileTaskNotificationService.fillNotificationCounts(fileTasks, userId);
        }
        return fileTasks;
    }
}
