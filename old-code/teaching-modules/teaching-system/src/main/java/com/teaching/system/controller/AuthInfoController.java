package com.teaching.system.controller;

import java.util.List;

import com.teaching.common.core.utils.sign.RsaUtils;
import com.teaching.common.security.annotation.InnerAuth;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.system.api.domain.SysUser;
import com.teaching.system.service.AuthenticationService;
import com.teaching.system.service.ISysUserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.system.api.domain.AuthInfo;
import com.teaching.system.service.IAuthInfoService;
import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.utils.poi.ExcelUtil;
import com.teaching.common.core.web.page.TableDataInfo;

/**
 * 实名认证Controller
 *
 * @author teaching
 * @date 2025-10-13
 */
@RestController
@RequestMapping("/authInfo")
public class AuthInfoController extends BaseController
{
    @Autowired
    private IAuthInfoService authInfoService;

    @Autowired
    private ISysUserService userService;

    @Autowired
    private AuthenticationService authenticationService;

    /**
     * 查询实名认证列表
     */
    @RequiresPermissions("system:info:list")
    @GetMapping("/list")
    public TableDataInfo list(AuthInfo authInfo)
    {
        startPage();
        List<AuthInfo> list = authInfoService.selectAuthInfoList(authInfo);
        return getDataTable(list);
    }

    @InnerAuth
    @GetMapping("/selectAuthInfoByName")
    public AjaxResult selectAuthInfoByName(@RequestParam String realName) {
        List<AuthInfo> authInfoRes = authInfoService.selectAuthInfoByUserName(realName);
        return success(authInfoRes);
    }

    @InnerAuth
    @PostMapping("/selectAuthInfoByIdCard")
    public AjaxResult selectAuthInfoByIdCard(@RequestBody AuthInfo authInfo) {
        return success(authInfoService.selectAuthInfoByIdCard(authInfo));
    }

    /**
     * 导出实名认证列表
     */
    @RequiresPermissions("system:info:export")
    @Log(title = "实名认证导出", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, AuthInfo authInfo)
    {
        List<AuthInfo> list = authInfoService.selectAuthInfoList(authInfo);
        ExcelUtil<AuthInfo> util = new ExcelUtil<AuthInfo>(AuthInfo.class);
        util.exportExcel(response, list, "实名认证数据");
    }

    /**
     * 用户端获取实名认证详细信息
     */
    @GetMapping(value = "/getAuthInfo")
    public AjaxResult getAuthInfo() {
        return success(authInfoService.selectAuthInfoByAuthId(SecurityUtils.getLoginUser().getSysUser().getUserId()));
    }

    /**
     * 实名认证
     * {
     *   "idCard": "410105200011112222",
     *   "authStatus": "1",
     *   "userId": 0,
     *   "realName": "测试真名字"
     * }
     */
    @Log(title = "用户端进行实名认证", businessType = BusinessType.INSERT)
    @PostMapping("/saveAuthInfo")
    public AjaxResult saveAuthInfo(@RequestBody AuthInfo authInfo) throws Exception {
//        authInfo.setUserId(SecurityUtils.getLoginUser().getSysUser().getUserId());
        authInfo.setIdCard(RsaUtils.decryptByPrivateKey(authInfo.getIdCard()));
        return success(authInfoService.insertAuthInfo(authInfo));
    }

    @InnerAuth
    @PostMapping("/saveInnerAuthInfo")
    public AjaxResult saveInnerAuthInfo(@RequestBody AuthInfo authInfo) throws Exception {
        authInfo.setUserId(SecurityUtils.getLoginUser().getSysUser().getUserId());
        return success(authenticationService.authentication(authInfo.getRealName().trim(), authInfo.getIdCard().trim()));
    }

    /**
     * 修改实名认证
     */
    @RequiresPermissions("system:info:edit")
    @Log(title = "重新实名认证", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody AuthInfo authInfo)
    {
        return toAjax(authInfoService.updateAuthInfo(authInfo));
    }

    /**
     * 删除实名认证
     */
    @RequiresPermissions("system:info:remove")
    @Log(title = "实名认证", businessType = BusinessType.DELETE)
	@DeleteMapping("/{authIds}")
    public AjaxResult remove(@PathVariable String[] authIds)
    {
        return toAjax(authInfoService.deleteAuthInfoByAuthIds(authIds));
    }
}
