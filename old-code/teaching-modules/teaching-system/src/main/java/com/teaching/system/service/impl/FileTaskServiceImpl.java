package com.teaching.system.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import com.teaching.common.core.constant.Constants;
import com.teaching.common.core.exception.GlobalException;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.common.redis.service.RedisService;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.system.domain.FileTaskConfig;
import com.teaching.system.domain.SysUserGroup;
import com.teaching.system.mapper.FileTaskConfigMapper;
import com.teaching.system.mapper.SysDictDataMapper;
import com.teaching.system.mapper.SysUserGroupMapper;
import com.teaching.system.service.FileTaskAudienceService;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.teaching.system.mapper.FileTaskMapper;
import com.teaching.system.domain.FileTask;
import com.teaching.system.service.IFileTaskService;
import org.springframework.transaction.annotation.Transactional;

/**
 * 文件分发任务Service业务层处理
 * 
 * @author teaching
 * @date 2026-01-07
 */
@Service
public class FileTaskServiceImpl implements IFileTaskService {

    private static final Logger logger = LoggerFactory.getLogger(FileTaskServiceImpl.class);

    @Autowired
    private FileTaskMapper fileTaskMapper;

    @Autowired
    private FileTaskConfigMapper fileTaskConfigMapper;

    @Autowired
    private SysUserGroupMapper sysUserGroupMapper;

    @Autowired
    private RedisService redisService;

    @Autowired
    private SysDictDataMapper sysDictDataMapper;

    @Autowired
    private FileTaskAudienceService fileTaskAudienceService;

    /**
     * 查询文件分发任务
     * 
     * @param id 文件分发任务主键
     * @return 文件分发任务
     */
    @Override
    public FileTask selectFileTaskById(Long id)
    {
        return fileTaskMapper.selectFileTaskById(id);
    }

    /**
     * 查询文件分发任务列表
     * 
     * @param fileTask 文件分发任务
     * @return 文件分发任务
     */
    @Override
    public List<FileTask> selectFileTaskList(FileTask fileTask) {
        //用户组名称  selectDictDataByType
        if(StringUtils.isNotEmpty(fileTask.getUserGroupName())){
            List<Map<String, Object>> stringObjectList = sysUserGroupMapper.selectSysUserGroupByUserGroupName(fileTask.getUserGroupName());
            if(CollectionUtils.isNotEmpty(stringObjectList)){
                StringBuffer sb = new StringBuffer();
                stringObjectList.stream().forEach(stringObjectMap -> {
                    sb.append(stringObjectMap.get("id").toString());
                });
                if(StringUtils.isNotEmpty(sb)){
                    fileTask.setUserGroupIds(String.join(",", sb.toString()));
                }
            }
        }
        List<FileTask> fileTasks = fileTaskMapper.selectFileTaskList(fileTask);
        if(CollectionUtils.isNotEmpty(fileTasks)){
            fileTasks.stream().forEach(fileTaskRes -> {
                Set<String> taskTypeNames = new HashSet<>();
                if(CollectionUtils.isNotEmpty(fileTaskRes.getFileTaskConfigList())){
                    fileTaskRes.getFileTaskConfigList().stream().forEach(fileTaskConfig -> {
                        String taskTypeName= sysDictDataMapper.selectDictLabel("task_type",fileTaskConfig.getTaskType());
                        taskTypeNames.add(taskTypeName);
                    });
                    fileTaskRes.setTaskType(String.join("/", taskTypeNames));
                }
                if(StringUtils.isNotEmpty(fileTaskRes.getUserGroupIds())){
                    List<String> ids = List.of(fileTaskRes.getUserGroupIds().split(","));
                    List<Long> userGroupIds = ids.stream().map(Long::parseLong).toList();
                    List<SysUserGroup> sysUserGroups = sysUserGroupMapper.selectSysUserGroupByIds(userGroupIds);
                    if(CollectionUtils.isNotEmpty(sysUserGroups)){
                        String userGroupNames = sysUserGroups.stream().map(SysUserGroup::getName).collect(Collectors.joining(","));
                        fileTaskRes.setUserGroupNames(userGroupNames);
                    }
                }
                if(StringUtils.isNotEmpty(fileTaskRes.getUserGroupIds())){
                    // 任务统计
                    // 获取已读量
                    Integer readCount = redisService.getCacheObject("fileTaskReadRecord:"+fileTaskRes.getId());
                    logger.info("获取已读量:"+readCount);
                    // 获取已下载量
                    Integer downCount = redisService.getCacheObject("fileDownRecord:"+fileTaskRes.getId());
                    logger.info("获取已下载量:"+downCount);
                    fileTaskRes.setReadCount(readCount == null ? 0 : readCount);
                    fileTaskRes.setDownCount(downCount == null ? 0 : downCount);
                }
            });
            fileTaskAudienceService.fillTaskUploadStatistics(fileTasks);
        }
        return fileTasks;
    }

    /**
     * 新增文件分发任务
     * 
     * @param fileTask 文件分发任务
     * @return 结果
     */
    @Override
    @Transactional
    public int insertFileTask(FileTask fileTask) {
        fileTask.setCreateBy(SecurityUtils.getLoginUser().getUsername());
        fileTask.setCreateTime(DateUtils.getNowDate());
        if(StringUtils.isEmpty(fileTask.getTaskStatus())){
            fileTask.setTaskStatus(Constants.TASK_STATUS_DRAFT);
        }
        fileTaskMapper.insertFileTask(fileTask);
        if(CollectionUtils.isNotEmpty(fileTask.getFileTaskConfigList())){
            List<FileTaskConfig> fileTaskConfigList = fileTask.getFileTaskConfigList();
            fileTaskConfigList.stream().forEach(fileTaskConfig -> {
                fileTaskConfig.setCreateBy(SecurityUtils.getLoginUser().getUsername());
                fileTaskConfig.setTaskId(fileTask.getId());
            });
        }
        fileTaskConfigMapper.batchInsertFileTaskConfig(fileTask.getFileTaskConfigList());
        return 1;
    }

    /**
     * 修改文件分发任务
     * 
     * @param fileTask 文件分发任务
     * @return 结果
     */
    @Transactional
    @Override
    public int updateFileTask(FileTask fileTask)
    {
        fileTask.setUpdateBy(SecurityUtils.getLoginUser().getUsername());
        fileTask.setUpdateTime(DateUtils.getNowDate());
        if(CollectionUtils.isNotEmpty(fileTask.getFileTaskConfigList())){
            List<FileTaskConfig> fileTaskConfigList = fileTask.getFileTaskConfigList();
            fileTaskConfigList.stream().forEach(fileTaskConfig -> {
                fileTaskConfig.setUpdateBy(SecurityUtils.getLoginUser().getUsername());
            });
        }
//        fileTaskConfigMapper.batchUpdateFileTaskConfig(fileTask.getFileTaskConfigList());
        if(CollectionUtils.isNotEmpty(fileTask.getFileTaskConfigList())){

            // 判断是否删除
            FileTaskConfig fileTaskConfig = new FileTaskConfig();
            fileTaskConfig.setTaskId(fileTask.getId());
            List<FileTaskConfig> fileTaskConfigs = fileTaskConfigMapper.selectFileTaskConfigList(fileTaskConfig);
            List<Long> deleteFileTaskConfigIds = new ArrayList<>();
            List<Long> fileTaskConfigIds = fileTaskConfigs.stream().map(FileTaskConfig::getId).toList();
            List<Long> updateFileTaskConfigIds = fileTask.getFileTaskConfigList().stream().
                    filter(fileTaskConfigRes -> fileTaskConfigRes.getId() != null).map(FileTaskConfig::getId).toList();
            deleteFileTaskConfigIds = fileTaskConfigIds.stream().filter(fileTaskConfigId -> !updateFileTaskConfigIds.contains(fileTaskConfigId)).toList();
            if(CollectionUtils.isNotEmpty(deleteFileTaskConfigIds)){
                fileTaskConfigMapper.deleteFileTaskConfigByIds(deleteFileTaskConfigIds.stream().toArray(Long[]::new));
            }
            fileTask.getFileTaskConfigList().stream().forEach(fileTaskConfigRes -> {
                if (Objects.nonNull(fileTaskConfigRes.getId())) {
                    fileTaskConfigMapper.updateFileTaskConfig(fileTaskConfigRes);
                } else {
                    fileTaskConfigMapper.insertFileTaskConfig(fileTaskConfigRes);
                }
            });
        }
        return fileTaskMapper.updateFileTask(fileTask);
    }

    @Override
    public int updateFileTaskStatus(FileTask fileTask) {
        fileTask.setUpdateTime(DateUtils.getNowDate());
        return fileTaskMapper.updateFileTask(fileTask);
    }

    /**
     * 批量删除文件分发任务
     * 
     * @param ids 需要删除的文件分发任务主键
     * @return 结果
     */
    @Override
    public int deleteFileTaskByIds(Long[] ids) {
        // 已发布的不能删除
        if(ids!=null && ids.length>0){
            List<FileTask> fileTasks = fileTaskMapper.selectFileTaskByIds(ids);
            if(CollectionUtils.isNotEmpty(fileTasks)){
                throw new GlobalException("已发布的任务不能删除");
            }
        }
        fileTaskConfigMapper.deleteFileTaskConfigByTaskIds(ids);
        return fileTaskMapper.deleteFileTaskByIds(ids);
    }

    /**
     * 删除文件分发任务信息
     * 
     * @param id 文件分发任务主键
     * @return 结果
     */
    @Override
    public int deleteFileTaskById(Long id) {
        fileTaskConfigMapper.deleteFileTaskConfigById(id);
        return fileTaskMapper.deleteFileTaskById(id);
    }
}
