package com.teaching.system.controller;

import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.web.page.TableDataInfo;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.InnerAuth;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.system.domain.NotificationInbox;
import com.teaching.system.domain.NotificationSender;
import com.teaching.system.service.INotificationSenderService;
import com.teaching.system.api.model.LoginUser;
import com.teaching.system.api.domain.SysUser;
import com.teaching.system.api.domain.NotificationSendDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * 站内信发送表 接口
 *
 * @author teaching
 */
@RestController
@RequestMapping("/notification/sender")
public class NotificationSenderController extends BaseController {

    @Autowired
    private INotificationSenderService notificationSenderService;

    /**
     * 分页查询发送记录列表
     */
    //@RequiresPermissions("system:notificationSender:list")
    @GetMapping("/list")
    public TableDataInfo list(NotificationSender notificationSender) {
        startPage();
        List<NotificationSender> list = notificationSenderService.selectNotificationSenderList(notificationSender);
        return getDataTable(list);
    }

    /**
     * 根据主键查询详情
     */
    //@RequiresPermissions("system:notificationSender:query")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        return success(notificationSenderService.selectNotificationSenderById(id));
    }

    /**
     * 新增发送记录
     */
    //@RequiresPermissions("system:notificationSender:add")
    @Log(title = "站内信发送", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody NotificationSender notificationSender) {
        notificationSender.setCreateBy(SecurityUtils.getUsername());
        if (notificationSender.getSenderUserId() == null) {
            notificationSender.setSenderUserId(0L);
        }
        if (notificationSender.getSendTime() == null) {
            notificationSender.setSendTime(DateUtils.getNowDate());
        }
        if (notificationSender.getDelFlag() == null) {
            notificationSender.setDelFlag("0");
        }
        return toAjax(notificationSenderService.insertNotificationSender(notificationSender));
    }


    /**
     * 修改发送记录
     */
    //@RequiresPermissions("system:notificationSender:edit")
    @Log(title = "站内信发送", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody NotificationSender notificationSender) {
        notificationSender.setUpdateBy(SecurityUtils.getUsername());
        return toAjax(notificationSenderService.updateNotificationSender(notificationSender));
    }

    /**
     * 删除发送记录
     */
    //@RequiresPermissions("system:notificationSender:remove")
    @Log(title = "站内信发送", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(notificationSenderService.deleteNotificationSenderByIds(ids));
    }

    /**
     * 当前用户未读站内信列表（发送表有、接收表无）
     *
     * 说明：系统/管理员发送后不写接收表；用户登录查询时，not exists 视为未读。
     */
    @GetMapping("/unread/list")
    public TableDataInfo unreadList() {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        SysUser sysUser = loginUser.getSysUser();
        Long userId = sysUser.getUserId();
        Long orgId = sysUser.getOrgId();

        startPage();
        List<NotificationSender> list = notificationSenderService.selectUnreadForCurrentUser(userId, orgId);
        return getDataTable(list);
    }

    /**
     * 查看站内信详情并标记已读（支持单条或多条）。
     * 路径参数 ids：单条传 {@code 1}，多条传 {@code 1,2,3}，统一返回有权限的详情列表。
     */
    @GetMapping("/view/{ids}")
    public AjaxResult view(@PathVariable("ids") String ids) {
        if (StringUtils.isBlank(ids)) {
            return success(Arrays.asList());
        }
        String[] parts = ids.split(",");
        Long[] idArr = Arrays.stream(parts)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::parseLong)
                .toArray(Long[]::new);
        if (idArr.length == 0) {
            return success(Arrays.asList());
        }
        LoginUser loginUser = SecurityUtils.getLoginUser();
        SysUser sysUser = loginUser.getSysUser();
        Long userId = sysUser.getUserId();
        Long orgId = sysUser.getOrgId();
        String username = loginUser.getUsername();

        List<NotificationSender> list = notificationSenderService.viewAndMarkReadBatch(idArr, userId, orgId, username);
        return success(list);
    }

    /**
     * 当前用户收件箱：全部删除（已读标记删除，未读插入已删除记录，收件箱清空）
     */
    @Log(title = "站内信收件箱", businessType = BusinessType.DELETE)
    @PostMapping("/inbox/delete-all")
    public AjaxResult inboxDeleteAll() {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        SysUser sysUser = loginUser.getSysUser();
        Long userId = sysUser.getUserId();
        Long orgId = sysUser.getOrgId();
        String username = loginUser.getUsername();
        notificationSenderService.markAllInboxDeletedForCurrentUser(userId, orgId, username);
        return success();
    }

    /**
     * 当前用户收件箱：全部已读（对所有未读站内信写入接收表）
     */
    @Log(title = "站内信收件箱", businessType = BusinessType.UPDATE)
    @PostMapping("/inbox/read-all")
    public AjaxResult inboxReadAll() {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        SysUser sysUser = loginUser.getSysUser();
        Long userId = sysUser.getUserId();
        Long orgId = sysUser.getOrgId();
        String username = loginUser.getUsername();
        notificationSenderService.markAllInboxReadForCurrentUser(userId, orgId, username);
        return success();
    }

    /**
     * 当前用户已读站内信列表（接收表存在）
     */
    @GetMapping("/read/list")
    public TableDataInfo readList() {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        SysUser sysUser = loginUser.getSysUser();
        Long userId = sysUser.getUserId();
        Long orgId = sysUser.getOrgId();

        startPage();
        List<NotificationInbox> list = notificationSenderService.selectReadInboxForCurrentUser(userId, orgId);
        return getDataTable(list);
    }

    /**
     * 当前用户全部收件箱（已读+未读），不展示已删除；支持按已读/未读、标题筛选。
     * isRead 为筛选条件（发送表无 isRead 字段，结果中的 is_read 由接收表得出）：
     * 不传或空或 all=全部，1=仅接收表有记录（已读），0=仅发送表有、接收表无（未读）。
     *
     * @param isRead 已读筛选：不传/空/all=全部，1=已读，0=未读（建议传字符串 "0"/"1"）
     * @param title  标题模糊筛选
     */
    @GetMapping("/inbox/list")
    public TableDataInfo inboxList(
            @RequestParam(required = false) String isRead,
            @RequestParam(required = false) String title) {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        SysUser sysUser = loginUser.getSysUser();
        Long userId = sysUser.getUserId();
        Long orgId = sysUser.getOrgId();
        // 规范化 isRead：trim 后空串视为不传（全部），保证 0/1 与 Mapper 比较一致
        if (isRead != null) {
            isRead = isRead.trim();
            if (isRead.isEmpty()) {
                isRead = null;
            }
        }

        startPage();
        List<NotificationInbox> list = notificationSenderService.selectAllInboxForCurrentUser(userId, orgId, isRead, title);
        return getDataTable(list);
    }

    /**
     * 内部调用：发送站内信（供其它微服务调用）
     */
    @InnerAuth
    @PostMapping("/inner/send")
    public AjaxResult innerSend(@RequestBody NotificationSendDTO dto) {
        NotificationSender sender = new NotificationSender();
        sender.setTitle(dto.getTitle());
        sender.setContent(dto.getContent());
        sender.setMessageType(dto.getMessageType());
        sender.setRelatedId(dto.getRelatedId());
        sender.setRelatedType(dto.getRelatedType());
        sender.setReceiverUserIds(dto.getReceiverUserIds());
        sender.setOrgId(dto.getOrgId());
        sender.setSenderUserId(dto.getSenderUserId());
        sender.setSendTime(DateUtils.getNowDate());
        sender.setCreateBy("flowable-service");
        notificationSenderService.insertNotificationSender(sender);
        return AjaxResult.success();
    }
}

