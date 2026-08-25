package com.teaching.competition.controller;

import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.web.page.TableDataInfo;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.system.api.domain.UserCertificateOrigin;
import com.teaching.competition.service.IUserCertificateOriginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户证书原表Controller
 *
 * @author teaching
 */
@RestController
@RequestMapping("/competition/userCertificateOrigin")
public class UserCertificateOriginController extends BaseController {
    @Autowired
    private IUserCertificateOriginService userCertificateOriginService;

    /**
     * 查询用户证书原表列表
     */
    @GetMapping("/list")
    public TableDataInfo list(UserCertificateOrigin userCertificateOrigin) {
        startPage();
        List<UserCertificateOrigin> list = userCertificateOriginService.selectUserCertificateOriginList(userCertificateOrigin);
        return getDataTable(list);
    }

    /**
     * 导出用户证书原表列表
     */
    @PostMapping("/export")
    public AjaxResult export(UserCertificateOrigin userCertificateOrigin) {
        List<UserCertificateOrigin> list = userCertificateOriginService.selectUserCertificateOriginList(userCertificateOrigin);
        return success(list);
    }

    /**
     * 获取用户证书原表详细信息
     */
    @GetMapping(value = "/{certId}")
    public AjaxResult getInfo(@PathVariable("certId") Long certId) {
        return success(userCertificateOriginService.selectUserCertificateOriginById(certId));
    }

    /**
     * 新增用户证书原表
     */
    @PostMapping
    public AjaxResult add(@RequestBody UserCertificateOrigin userCertificateOrigin) {
        return toAjax(userCertificateOriginService.insertUserCertificateOrigin(userCertificateOrigin));
    }

    /**
     * 修改用户证书原表
     */
    @RequiresPermissions("competition:userCertificateOrigin:edit")
    @PutMapping
    public AjaxResult edit(@RequestBody UserCertificateOrigin userCertificateOrigin) {
        return toAjax(userCertificateOriginService.updateUserCertificateOrigin(userCertificateOrigin));
    }

    /**
     * 删除用户证书原表
     */
    @DeleteMapping("/{certIds}")
    public AjaxResult remove(@PathVariable Long[] certIds) {
        return toAjax(userCertificateOriginService.deleteUserCertificateOriginByIds(certIds));
    }
}
