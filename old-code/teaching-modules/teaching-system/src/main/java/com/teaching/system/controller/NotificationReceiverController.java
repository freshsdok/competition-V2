package com.teaching.system.controller;

import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.web.page.TableDataInfo;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.system.domain.NotificationReceiver;
import com.teaching.system.service.INotificationReceiverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 站内信接收表 接口
 *
 * @author teaching
 */
@RestController
@RequestMapping("/notification/receiver")
public class NotificationReceiverController extends BaseController {

    @Autowired
    private INotificationReceiverService notificationReceiverService;

    /**
     * 分页查询接收记录列表
     */
    @RequiresPermissions("system:notificationReceiver:list")
    @GetMapping("/list")
    public TableDataInfo list(NotificationReceiver notificationReceiver) {
        startPage();
        List<NotificationReceiver> list = notificationReceiverService.selectNotificationReceiverList(notificationReceiver);
        return getDataTable(list);
    }

    /**
     * 根据主键查询详情
     */
    @RequiresPermissions("system:notificationReceiver:query")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        return success(notificationReceiverService.selectNotificationReceiverById(id));
    }

    /**
     * 新增接收记录
     */
    @RequiresPermissions("system:notificationReceiver:add")
    @Log(title = "站内信接收", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody NotificationReceiver notificationReceiver) {
        notificationReceiver.setCreateBy(SecurityUtils.getUsername());
        return toAjax(notificationReceiverService.insertNotificationReceiver(notificationReceiver));
    }

    /**
     * 修改接收记录
     */
    @RequiresPermissions("system:notificationReceiver:edit")
    @Log(title = "站内信接收", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody NotificationReceiver notificationReceiver) {
        notificationReceiver.setUpdateBy(SecurityUtils.getUsername());
        return toAjax(notificationReceiverService.updateNotificationReceiver(notificationReceiver));
    }

    /**
     * 删除接收记录
     */
    @RequiresPermissions("system:notificationReceiver:remove")
    @Log(title = "站内信接收", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(notificationReceiverService.deleteNotificationReceiverByIds(ids));
    }
}

