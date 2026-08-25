package com.teaching.system.controller;

import java.util.List;

import com.teaching.common.security.annotation.InnerAuth;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.system.api.domain.SysUser;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.system.api.domain.IdentityInfo;
import com.teaching.system.service.IIdentityInfoService;
import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.utils.poi.ExcelUtil;
import com.teaching.common.core.web.page.TableDataInfo;

/**
 * 身份认证信息Controller
 * 
 * @author teaching
 * @date 2025-10-13
 */
@RestController
@RequestMapping("/identityInfo")
public class IdentityInfoController extends BaseController {
    @Autowired
    private IIdentityInfoService identityInfoService;

    /**
     * 查询身份认证信息列表
     */
    @RequiresPermissions("system:identityInfo:list")
    @GetMapping("/list")
    public TableDataInfo list(IdentityInfo identityInfo) {
        startPage();
        List<IdentityInfo> list = identityInfoService.selectIdentityInfoList(identityInfo);
        return getDataTable(list);
    }

    /**
     * 导出身份认证信息列表
     */
    @RequiresPermissions("system:identityInfo:export")
    @Log(title = "身份认证信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, IdentityInfo identityInfo) {
        List<IdentityInfo> list = identityInfoService.selectIdentityInfoList(identityInfo);
        ExcelUtil<IdentityInfo> util = new ExcelUtil<IdentityInfo>(IdentityInfo.class);
        util.exportExcel(response, list, "身份认证信息数据");
    }

    /**
     * 获取身份认证信息详细信息(管理端)
     */
    @RequiresPermissions("system:identityInfo:list")
    @GetMapping(value = "/getIdentityInfoDetail/{authId}")
    public AjaxResult getInfo(@PathVariable Long authId) {
        return success(identityInfoService.selectIdentityInfoByAuthId(null,authId, null));
    }

    /**
     * 获取身份认证详情内部调用
     * @param authId
     * @return
     */
    @InnerAuth
    @GetMapping(value = "/getInnerIdentityInfoDetail/{authId}")
    public AjaxResult getInnerIdentityInfoDetail(@PathVariable Long authId) {
        return success(identityInfoService.selectIdentityInfoByAuthId(null,authId, null));
    }

    /**
     * 新增身份认证信息(用户端)
     */
    @Log(title = "身份认证信息", businessType = BusinessType.INSERT)
    @PostMapping("/saveIdentityInfo")
    public AjaxResult add(@RequestBody IdentityInfo identityInfo) {
        SysUser sysUser = SecurityUtils.getLoginUser().getSysUser();
        identityInfo.setUserId(sysUser.getUserId());
        return toAjax(identityInfoService.insertIdentityInfo(identityInfo));
    }

    /**
     * 修改身份认证信息(用户端)
     */
    @Log(title = "身份认证信息", businessType = BusinessType.UPDATE)
    @PostMapping("/updateIdentityInfo")
    public AjaxResult edit(@RequestBody IdentityInfo identityInfo) {
        return toAjax(identityInfoService.updateIdentityInfo(identityInfo));
    }

    /**
     * 修改身份认证状态(管理端)
     */
    @InnerAuth
    @PostMapping("/updateIdentityInfoStatus")
    public AjaxResult updateIdentityInfoStatus(@RequestBody IdentityInfo identityInfo) {
        return toAjax(identityInfoService.updateIdentityInfoStatus(identityInfo));
    }

    /**
     * 删除身份认证信息(管理端)
     */
    @RequiresPermissions("system:identityInfo:remove")
    @Log(title = "身份认证信息", businessType = BusinessType.DELETE)
	@DeleteMapping("/{authIds}")
    public AjaxResult remove(@PathVariable Long[] authIds) {
        return toAjax(identityInfoService.deleteIdentityInfoByAuthIds(authIds));
    }
}
