package com.teaching.wxApp.controller;

import com.teaching.common.core.constant.Constants;
import com.teaching.common.core.constant.SecurityConstants;
import com.teaching.common.core.domain.R;
import com.teaching.common.core.exception.GlobalException;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.web.page.TableDataInfo;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.system.api.RemoteUserService;
import com.teaching.system.api.domain.IdentityInfo;
import com.teaching.system.api.domain.NationwideCollegeInfo;
import com.teaching.system.api.domain.SysUser;
import com.teaching.system.api.model.LoginUser;
import com.teaching.wxApp.domain.WxQcCodeRecord;
import com.teaching.wxApp.service.IWxQcCodeRecordService;
import com.teaching.wxApp.service.MiniQrCodeService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Objects;


/**
 * 二维码生成记录Controller
 *
 * @author teaching
 * @date 2026-04-08
 */
@RestController
@RequestMapping("/wxQcCodeRecord")
public class WxQcCodeRecordController extends BaseController {

    @Autowired
    private IWxQcCodeRecordService wxQcCodeRecordService;
    @Autowired
    private MiniQrCodeService miniQrCodeService;

    @Autowired
    private RemoteUserService userService;

    /**
     * 查询二维码生成记录列表
     */
    @RequiresPermissions("wxApp:wxQcCodeRecord:list")
    @GetMapping("/list")
    public TableDataInfo list(WxQcCodeRecord wxQcCodeRecord) {
        startPage();
        List<WxQcCodeRecord> list = wxQcCodeRecordService.selectWxQcCodeRecordInfosList(wxQcCodeRecord);
        return getDataTable(list);
    }

    /**
     * 导出二维码生成记录列表
     */
    @RequiresPermissions("wxApp:wxQcCodeRecord:export")
    @Log(title = "二维码生成记录", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public AjaxResult export(WxQcCodeRecord wxQcCodeRecord) {
        List<WxQcCodeRecord> list = wxQcCodeRecordService.selectWxQcCodeRecordList(wxQcCodeRecord);
        return success(list);
    }

    /**
     * 获取二维码生成记录详细信息
     */
    @RequiresPermissions("wxApp:wxQcCodeRecord:query")
    @GetMapping(value = "/{recordId}")
    public AjaxResult getInfo(@PathVariable("recordId") Long recordId) {
        return success(wxQcCodeRecordService.selectWxQcCodeRecordByRecordId(recordId));
    }

    /**
     * 获取二维码生成记录的二维码内容
     * @param recordId
     * @return
     */
    @GetMapping(value = "/codeBase/{recordId}")
    public AjaxResult getWxQcCodeBaseByRecordId(@PathVariable("recordId") Long recordId) {
        return success(wxQcCodeRecordService.getWxQcCodeBaseByRecordId(recordId));
    }

    /**
     * 新增二维码生成记录
     * 流程：1.插入记录获取recordId -> 2.生成小程序码 -> 3.更新记录的二维码
     */
//    @RequiresPermissions("wxApp:wxQcCodeRecord:add")
    @Log(title = "二维码生成记录", businessType = BusinessType.INSERT)
    @PostMapping("/addWxQcCodeRecord")
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult add(@RequestBody WxQcCodeRecord wxQcCodeRecord) {
        // 获取当前登录用户信息
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser.getSysUser() == null) {
            return error("当前未登录");
        }
        // 获取当前用户身份
        Long userId = SecurityUtils.getLoginUser().getSysUser().getUserId();
        R<SysUser> userCenterInfo = userService.getUserCenterInfo(userId, SecurityConstants.INNER);
        if(R.isSuccess(userCenterInfo) && Objects.nonNull(userCenterInfo.getData())){
            SysUser data = userCenterInfo.getData();
            List<IdentityInfo> identityInfoList = data.getIdentityInfoList();
            if(CollectionUtils.isNotEmpty(identityInfoList)){
                List<String> certificationType = identityInfoList.stream().map(IdentityInfo::getCertificationType).toList();
                if(!certificationType.contains(Constants.IDENTITY_TYPE_TEACHER)){
                    throw new GlobalException("您不是教师身份，无法生成二维码");
                }
            }
        } else {
            throw new GlobalException("用户身份信息获取失败");
        }
        SysUser sysUser = loginUser.getSysUser();
        // 参数校验
        if (wxQcCodeRecord.getCodeConfigId() == null) {
            return error("二维码配置ID不能为空");
        }
        // 设置创建人信息
        wxQcCodeRecord.setUserId(sysUser.getUserId());
        wxQcCodeRecord.setCreateBy(sysUser.getUserName());
        wxQcCodeRecord.setCreateTime(DateUtils.getNowDate());
        wxQcCodeRecord.setCodeStatus("1");
        // 1. 先插入记录获取recordId
        int result = wxQcCodeRecordService.insertWxQcCodeRecord(wxQcCodeRecord);
        if (result <= 0 || wxQcCodeRecord.getRecordId() == null) {
            return error("保存二维码记录失败");
        }
        // 2. 生成小程序码
        // scene: 传递recordId用于扫码识别，最大32字符
//        String scene = "rid=" + wxQcCodeRecord.getRecordId();
        String scene = "rid_" + wxQcCodeRecord.getRecordId();
        // page: 微信小程序页面路径，不能带查询参数
        String page = "pages/scan/result";
        logger.info("开始生成小程序码，scene：{}，page：{}", scene, page);
        String codeBase64Str = miniQrCodeService.generateUnlimitedQrCode(
                scene,
                page,
                430  // 默认宽度
        );
        logger.info("小程序码生成成功，base64字符串长度为：{}", codeBase64Str.length());
        // 3. 更新二维码记录
        wxQcCodeRecord.setCodeBase64(codeBase64Str);
        wxQcCodeRecord.setUpdateBy(sysUser.getUserName());
        int updateResult = wxQcCodeRecordService.updateWxQcCodeRecord(wxQcCodeRecord);
        return toAjax(updateResult);
    }

    /**
     * 修改二维码生成记录
     */
//    @RequiresPermissions("wxApp:wxQcCodeRecord:edit")
    @Log(title = "二维码生成记录", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody WxQcCodeRecord wxQcCodeRecord) {
        return toAjax(wxQcCodeRecordService.updateWxQcCodeRecord(wxQcCodeRecord));
    }

    /**
     * 删除二维码生成记录
     */
    @RequiresPermissions("wxApp:wxQcCodeRecord:remove")
    @Log(title = "二维码生成记录", businessType = BusinessType.DELETE)
    @DeleteMapping("/{recordIds}")
    public AjaxResult remove(@PathVariable Long[] recordIds) {
        return toAjax(wxQcCodeRecordService.deleteWxQcCodeRecordByRecordIds(recordIds));
    }
}
