package com.teaching.competition.controller;

import java.util.List;

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.competition.domain.UserCollect;
import com.teaching.competition.service.IUserCollectService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.utils.poi.ExcelUtil;
import com.teaching.common.core.web.page.TableDataInfo;

/**
 * 用户收藏信息Controller
 * 
 * @author teaching
 * @date 2025-10-22
 */
@RestController
@RequestMapping("/collect")
public class UserCollectController extends BaseController
{
    @Autowired
    private IUserCollectService userCollectService;

    /**
     * 查询用户收藏信息列表
     */
    @GetMapping("/list")
    public TableDataInfo list(UserCollect userCollect)
    {
        startPage();
        List<UserCollect> list = userCollectService.selectUserCollectList(userCollect);
        return getDataTable(list);
    }

    /**
     * 校验用户是否收藏
     */
    @GetMapping("/checkCollect")
    public AjaxResult checkCollect(UserCollect userCollect) {
        userCollect.setUserId(SecurityUtils.getLoginUser().getSysUser().getUserId());
        List<UserCollect> list = userCollectService.selectUserCollectList(userCollect);
        if (CollectionUtils.isNotEmpty(list)) {
            return success(true);
        }
        return success(false);
    }

    /**
     * 导出用户收藏信息列表
     */
    @RequiresPermissions("system:collect:export")
    @Log(title = "用户收藏信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, UserCollect userCollect)
    {
        List<UserCollect> list = userCollectService.selectUserCollectList(userCollect);
        ExcelUtil<UserCollect> util = new ExcelUtil<UserCollect>(UserCollect.class);
        util.exportExcel(response, list, "用户收藏信息数据");
    }

    /**
     * 获取用户收藏信息详细信息
     */
    @RequiresPermissions("system:collect:query")
    @GetMapping(value = "/{collectId}")
    public AjaxResult getInfo(@PathVariable("collectId") Long collectId)
    {
        return success(userCollectService.selectUserCollectByCollectId(collectId));
    }

    /**
     * 新增用户收藏信息
     */
    @Log(title = "用户收藏信息", businessType = BusinessType.INSERT)
    @PostMapping("/saveUserCollect")
    public AjaxResult add(@RequestBody UserCollect userCollect)
    {
        userCollect.setUserId(SecurityUtils.getLoginUser().getSysUser().getUserId());
        return toAjax(userCollectService.insertUserCollect(userCollect));
    }

    /**
     * 修改用户收藏信息
     */
    @RequiresPermissions("system:collect:edit")
    @Log(title = "用户收藏信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody UserCollect userCollect)
    {
        return toAjax(userCollectService.updateUserCollect(userCollect));
    }

    /**
     * 删除用户收藏信息
     */
    @Log(title = "用户收藏信息", businessType = BusinessType.DELETE)
	@PostMapping("/removeUserCollect")
    public AjaxResult removeUserCollect(@RequestBody UserCollect userCollect)
    {
        userCollect.setUserId(SecurityUtils.getLoginUser().getSysUser().getUserId());
        return toAjax(userCollectService.deleteUserCollectByCollectId(userCollect));
    }
}
