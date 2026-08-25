package com.teaching.system.controller;

import java.util.List;

import com.teaching.common.core.utils.poi.ExcelUtil;
import com.teaching.system.api.domain.AuthInfo;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.web.page.TableDataInfo;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.system.domain.SuggBack;
import com.teaching.system.service.ISuggBackService;

/**
 * 意见反馈信息操作处理
 *
 * @author teaching
 */
@RestController
@RequestMapping("/suggBack")
public class SuggBackController extends BaseController
{
    @Autowired
    private ISuggBackService suggBackService;

    /**
     * 获取意见反馈列表
     */
    @RequiresPermissions("system:suggBack:list")
    @GetMapping("/list")
    public TableDataInfo list(SuggBack suggBack)
    {
        try {
            startPage();
            // 先不使用分页，直接查询
            List<SuggBack> list = suggBackService.selectSuggBackList(suggBack);
            return getDataTable(list);
        } catch (Exception e) {
            e.printStackTrace();
            return getDataTable(new java.util.ArrayList<>());
        }
    }

    @RequiresPermissions("system:suggBack:export")
    @Log(title = "意见反馈列表", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, SuggBack suggBack)
    {
        List<SuggBack> list = suggBackService.selectSuggBackList(suggBack);
        ExcelUtil<SuggBack> util = new ExcelUtil<SuggBack>(SuggBack.class);
        util.exportExcel(response, list, "意见反馈信息");
    }

    /**
     * 根据意见反馈编号获取详细信息
     */
    @RequiresPermissions("system:suggBack:query")
    @GetMapping(value = "/{suggBackId}")
    public AjaxResult getInfo(@PathVariable Long suggBackId)
    {
        return success(suggBackService.selectSuggBackBySuggBackId(suggBackId));
    }

    /**
     * 新增意见反馈
     */
    //@RequiresPermissions("system:suggBack:add")
    @Log(title = "意见反馈", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SuggBack suggBack)
    {
        // 如果前端没有传递用户名称，使用当前登录用户名
        if (suggBack.getUserName() == null || suggBack.getUserName().isEmpty())
        {
            suggBack.setUserName(SecurityUtils.getUsername());
        }
        suggBack.setCreateBy(SecurityUtils.getUsername());
        suggBack.setUserId(SecurityUtils.getLoginUser().getUserid());
        return toAjax(suggBackService.insertSuggBack(suggBack));
    }

    /**
     * 修改意见反馈
     */
    @RequiresPermissions("system:suggBack:edit")
    @Log(title = "意见反馈", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SuggBack suggBack)
    {
        suggBack.setUpdateBy(SecurityUtils.getUsername());
        return toAjax(suggBackService.updateSuggBack(suggBack));
    }

    /**
     * 删除意见反馈
     */
    @RequiresPermissions("system:suggBack:remove")
    @Log(title = "意见反馈", businessType = BusinessType.DELETE)
    @DeleteMapping("/{suggBackIds}")
    public AjaxResult remove(@PathVariable Long[] suggBackIds)
    {
        return toAjax(suggBackService.deleteSuggBackBySuggBackIds(suggBackIds));
    }

    /**
     * 回复意见反馈
     */
    @RequiresPermissions("system:suggBack:reply")
    @Log(title = "回复意见反馈", businessType = BusinessType.UPDATE)
    @PutMapping("/reply")
    public AjaxResult reply(@RequestBody SuggBack suggBack)
    {
        suggBack.setUpdateBy(SecurityUtils.getUsername());
        // 状态由前端传递，不再自动设置
        return toAjax(suggBackService.replySuggBack(suggBack));
    }

    /**
     * 转交意见反馈
     */
    @RequiresPermissions("system:suggBack:transfer")
    @Log(title = "转交意见反馈", businessType = BusinessType.UPDATE)
    @PutMapping("/transfer")
    public AjaxResult transfer(@RequestBody SuggBack suggBack)
    {
        suggBack.setUpdateBy(SecurityUtils.getUsername());
        return toAjax(suggBackService.transferSuggBack(suggBack));
    }

    /**
     * 修改意见反馈处理状态
     */
    @RequiresPermissions("system:suggBack:edit")
    @Log(title = "修改意见反馈状态", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public AjaxResult changeStatus(@RequestBody SuggBack suggBack)
    {
        // 只更新状态，不进行完整的对象校验
        if (suggBack.getSuggBackId() == null)
        {
            return error("反馈ID不能为空");
        }
        if (suggBack.getDealStatus() == null)
        {
            return error("处理状态不能为空");
        }
        suggBack.setUpdateBy(SecurityUtils.getUsername());
        return toAjax(suggBackService.updateSuggBack(suggBack));
    }
}
