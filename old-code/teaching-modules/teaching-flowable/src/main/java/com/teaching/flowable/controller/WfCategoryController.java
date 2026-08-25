package com.teaching.flowable.controller;

import com.teaching.common.core.domain.R;
import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.flowable.core.domain.model.PageQuery;
import com.teaching.flowable.core.page.TableDataInfo;
import com.teaching.flowable.domain.WfCategory;
import com.teaching.flowable.domain.vo.WfCategoryVo;
import com.teaching.flowable.service.IWfCategoryService;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * 流程分类Controller
 *
 * @author KonBAI
 * @createTime 2022/3/10 00:12
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/category")
public class WfCategoryController extends BaseController {

    private final IWfCategoryService categoryService;

    /**
     * 查询流程分类列表
     */
    @RequiresPermissions("workflow:category:list")
    @GetMapping("/list")
    public TableDataInfo<WfCategoryVo> list(WfCategory category, PageQuery pageQuery) {
        return categoryService.queryPageList(category, pageQuery);
    }

    /**
     * 查询全部的流程分类列表
     */
    @GetMapping("/listAll")
    public R<List<WfCategoryVo>> listAll(WfCategory category) {
        return R.ok(categoryService.queryList(category));
    }


    /**
     * 获取流程分类详细信息
     *
     * @param categoryId 分类主键
     */
    @RequiresPermissions("workflow:category:query")
    @GetMapping("/{categoryId}")
    public R<WfCategoryVo> getInfo(@NotNull(message = "主键不能为空") @PathVariable("categoryId") Long categoryId) {
        return R.ok(categoryService.queryById(categoryId));
    }

    /**
     * 新增流程分类
     *
     * @return
     */
    @RequiresPermissions("workflow:category:add")
    @Log(title = "流程分类", businessType = BusinessType.INSERT)
//    @RepeatSubmit()
    @PostMapping()
    public AjaxResult add(@Validated @RequestBody WfCategory category) {
        if (!categoryService.checkCategoryCodeUnique(category)) {
            return error("新增流程分类'" + category.getCategoryName() + "'失败，流程编码已存在");
        }
        return toAjax(categoryService.insertCategory(category));
    }

    /**
     * 修改流程分类
     *
     * @return
     */
    @RequiresPermissions("workflow:category:edit")
    @Log(title = "流程分类", businessType = BusinessType.UPDATE)
//    @RepeatSubmit()
    @PutMapping()
    public AjaxResult edit(@Validated @RequestBody WfCategory category) {
        if (!categoryService.checkCategoryCodeUnique(category)) {
            return error("修改流程分类'" + category.getCategoryName() + "'失败，流程编码已存在");
        }
        return toAjax(categoryService.updateCategory(category));
    }

    /**
     * 删除流程分类
     *
     * @param categoryIds 分类主键串
     * @return
     */
    @RequiresPermissions("workflow:category:remove")
    @Log(title = "流程分类", businessType = BusinessType.DELETE)
    @DeleteMapping("/{categoryIds}")
    public AjaxResult remove(@NotEmpty(message = "主键不能为空") @PathVariable Long[] categoryIds) {
        return toAjax(categoryService.deleteWithValidByIds(Arrays.asList(categoryIds), true));
    }
}
