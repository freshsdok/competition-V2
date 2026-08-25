package com.teaching.system.controller;

import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.redis.service.RedisService;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.system.api.RemoteFileService;
import com.teaching.system.domain.FileTask;
import com.teaching.system.domain.FileUploadManager;
import com.teaching.system.domain.FileUploadRecord;
import com.teaching.system.domain.FileUploadReq;
import com.teaching.system.service.IFileTaskUserService;
import com.teaching.system.service.IFileTaskNotificationService;
import com.teaching.system.service.IFileUploadManagerService;
import com.teaching.system.service.IFileUploadRecordService;
import io.jsonwebtoken.lang.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 用户端文件分发任务Controller
 * @Description:
 */
@RestController
@RequestMapping("/fileDistributeUserTask")
public class FileDistributeTaskUserController extends BaseController {

    @Autowired
    private IFileTaskUserService fileTaskUserService;

    @Autowired
    private IFileUploadRecordService fileUploadRecordService;

    @Autowired
    private RemoteFileService remoteFileService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private IFileUploadManagerService fileUploadManagerService;

    @Autowired
    private IFileTaskNotificationService fileTaskNotificationService;

    @GetMapping("/list")
    public AjaxResult list(FileTask fileTask) {
        List<FileTask> list = fileTaskUserService.selectFileTaskUserList(fileTask);
        return success(list);
    }

    @GetMapping("/unReadCount")
    public AjaxResult unReadCount(FileTask fileTask) {
        List<FileTask> list = fileTaskUserService.selectFileTaskUserList(fileTask);
        AtomicInteger unReadCount = new AtomicInteger();
        if(!Collections.isEmpty(list)){
            list.stream().forEach(fileTaskRes -> {
                if(!fileTaskRes.getReadCountFlag()){
                    unReadCount.getAndIncrement();
                }
            });
        }
        return success(unReadCount.get());
    }

    @GetMapping("/getSystemDate")
    public AjaxResult list() {
        return success(System.currentTimeMillis());
    }

    /**
     * 当前用户在任务内收到的有效通知标题列表。
     */
    @GetMapping("/{taskId}/notifications")
    public AjaxResult notificationList(@PathVariable Long taskId) {
        Long userId = SecurityUtils.getLoginUser().getSysUser().getUserId();
        return success(fileTaskNotificationService.selectUserList(taskId, userId));
    }

    /**
     * 当前用户查看任务通知正文。
     */
    @GetMapping("/{taskId}/notifications/{notificationId}")
    public AjaxResult notificationDetail(@PathVariable Long taskId,
                                         @PathVariable Long notificationId) {
        Long userId = SecurityUtils.getLoginUser().getSysUser().getUserId();
        return success(fileTaskNotificationService.selectUserDetail(taskId, notificationId, userId));
    }

    @Log(title = "用户端上传文件", businessType = BusinessType.INSERT)
    @PostMapping("/saveFileUploadRecordUser")
    public AjaxResult add(@RequestBody FileUploadRecord fileUploadRecord) {
        fileUploadRecord.setUserId(SecurityUtils.getLoginUser().getSysUser().getUserId());

        return toAjax(fileUploadRecordService.insertFileUploadRecord(fileUploadRecord));
    }

    @Log(title = "用户端文件上传任务总信息", businessType = BusinessType.INSERT)
    @PostMapping("/saveFileUploadManagerUser")
    public AjaxResult add(@RequestBody FileUploadReq fileUploadReq) {
        Long userId = SecurityUtils.getLoginUser().getSysUser().getUserId();
        Long id = fileUploadReq.getId();
        fileUploadReq.setUserId(userId);

        int rows = fileUploadRecordService.insertFileUploadManager(fileUploadReq);
        if(rows > 0){
            // 使用 setIfAbsent 原子操作，防止高并发下重复计数
            // 如果用户记录key不存在，则设置成功返回true，表示首次上传
            String userRecordKey = "fileUploadRecord:" + userId + ":" + id;
            Boolean isFirstUpload = redisService.setIfAbsent(userRecordKey, 1);

            if (Boolean.TRUE.equals(isFirstUpload)) {
                // 首次上传，任务计数+1（increment是原子操作）
                redisService.increment("fileUploadRecord:" + id, 1);
            }
        }

        return toAjax(rows);
    }

    @PostMapping("/updateSubmitStatus")
    public AjaxResult edit(@RequestBody FileUploadManager fileUploadManager)
    {
        fileUploadManager.setUserId(SecurityUtils.getLoginUser().getSysUser().getUserId());
        return toAjax(fileUploadManagerService.updateFileUploadManagerByTaskId(fileUploadManager));
    }

    @Log(title = "用户端重新上传文件", businessType = BusinessType.INSERT)
    @PostMapping("/updateFileUploadRecordUser")
    public AjaxResult updateFileUploadRecordUser(@RequestBody FileUploadRecord fileUploadRecord) {
        fileUploadRecord.setUserId(SecurityUtils.getLoginUser().getSysUser().getUserId());
        return toAjax(fileUploadRecordService.updateFileUploadRecord(fileUploadRecord));
    }

    // 文件下载记录
    @GetMapping("/fileDownloadRecord")
    public AjaxResult fileDownloadRecord(@RequestParam Long fileTaskId) {
        Long userId = SecurityUtils.getLoginUser().getSysUser().getUserId();

        // 使用 setIfAbsent 原子操作，防止高并发下重复计数
        String userRecordKey = "fileDownRecord:" + userId + ":" + fileTaskId;
        Boolean isFirstDownload = redisService.setIfAbsent(userRecordKey, 1);

        if (Boolean.TRUE.equals(isFirstDownload)) {
            // 首次下载，任务计数+1（increment是原子操作）
            redisService.increment("fileDownRecord:" + fileTaskId, 1);
        }

        return success();
    }

    // 文件任务已读记录
    @GetMapping("/fileTaskReadRecord")
    public AjaxResult fileTaskReadRecord(@RequestParam Long fileTaskId) {
        Long userId = SecurityUtils.getLoginUser().getSysUser().getUserId();

        // 使用 setIfAbsent 原子操作，防止高并发下重复计数
        String userRecordKey = "fileTaskReadRecord:" + userId + ":" + fileTaskId;
        Boolean isFirstRead = redisService.setIfAbsent(userRecordKey, 1);

        if (Boolean.TRUE.equals(isFirstRead)) {
            // 首次已读，任务计数+1（increment是原子操作）
            redisService.increment("fileTaskReadRecord:" + fileTaskId, 1);
        }

        return success();
    }
}
