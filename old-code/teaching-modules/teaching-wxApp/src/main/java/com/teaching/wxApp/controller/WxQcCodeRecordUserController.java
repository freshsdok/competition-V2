package com.teaching.wxApp.controller;

import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.page.TableDataInfo;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.wxApp.domain.WxQcCodeConfig;
import com.teaching.wxApp.domain.WxQcCodeRecord;
import com.teaching.wxApp.service.IWxQcCodeConfigService;
import com.teaching.wxApp.service.IWxQcCodeRecordService;
import com.teaching.wxApp.service.WxQcCodeRecordUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * pc端二维码接口
 *
 * @author teaching
 * @date 2026-04-08
 */
@RestController
@RequestMapping("/wxQcCodeRecordUser")
public class WxQcCodeRecordUserController extends BaseController {

    @Autowired
    private WxQcCodeRecordUserService wxQcCodeRecordUserService;

    @Autowired
    private IWxQcCodeRecordService wxQcCodeRecordService;

    /**
     * 查询二维码配置列表
     */
    @GetMapping("/list")
    public TableDataInfo list(WxQcCodeConfig wxQcCodeConfig) {
        startPage();
        List<WxQcCodeConfig> list = wxQcCodeRecordUserService.selectWxQcCodeConfigPcList(wxQcCodeConfig);
        return getDataTable(list);
    }

    /**
     * 查询二维码生成记录列表
     */
    @GetMapping("/wxQcCodeRecordList")
    public TableDataInfo list(WxQcCodeRecord wxQcCodeRecord) {
        startPage();
        wxQcCodeRecord.setUserId(SecurityUtils.getLoginUser().getUserid());
        System.out.println("userId::"+SecurityUtils.getLoginUser().getUserid());
        List<WxQcCodeRecord> list = wxQcCodeRecordService.selectWxQcCodeRecordList(wxQcCodeRecord);
        return getDataTable(list);
    }



}
