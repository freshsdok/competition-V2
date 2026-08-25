package com.teaching.flowable.controller;

import com.teaching.common.core.domain.R;
import com.teaching.common.core.validate.QueryGroup;
import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.flowable.core.domain.model.PageQuery;
import com.teaching.flowable.core.page.TableDataInfo;
import com.teaching.flowable.domain.WfDeployForm;
import com.teaching.flowable.domain.bo.WfFormBo;
import com.teaching.flowable.domain.vo.WfFormVo;
import com.teaching.flowable.service.IWfDeployFormService;
import com.teaching.flowable.service.IWfFormService;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

/**
 * 流程表单Controller
 *
 * @author KonBAI
 * @createTime 2022/3/7 22:07
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/form")
public class WfFormController extends BaseController {

    private final IWfFormService formService;

    private final IWfDeployFormService deployFormService;

    /**
     * 查询流程表单列表
     */
    @RequiresPermissions("workflow:form:list")
    @GetMapping("/list")
    public TableDataInfo<WfFormVo> list(@Validated(QueryGroup.class) WfFormBo bo, PageQuery pageQuery) {
        return formService.queryPageList(bo, pageQuery);
    }


    /**
     * 获取流程表单详细信息
     *
     * @param formId 主键
     */
    @RequiresPermissions("workflow:form:query")
    @GetMapping(value = "/{formId}")
    public R<WfFormVo> getInfo(@NotNull(message = "主键不能为空") @PathVariable("formId") Long formId) {
        return R.ok(formService.queryById(formId));
    }

    /**
     * 新增流程表单
     *
     * @return
     */
    @RequiresPermissions("workflow:form:add")
    @Log(title = "流程表单", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody WfFormBo bo) {
        return toAjax(formService.insertForm(bo));
    }

    /**
     * 修改流程表单
     *
     * @return
     */
    @RequiresPermissions("workflow:form:edit")
    @Log(title = "流程表单", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody WfFormBo bo) {
        return toAjax(formService.updateForm(bo));
    }

    /**
     * 删除流程表单
     *
     * @param formIds 主键串
     * @return
     */
    @RequiresPermissions("workflow:form:remove")
    @Log(title = "流程表单", businessType = BusinessType.DELETE)
    @DeleteMapping("/{formIds}")
    public AjaxResult remove(@NotEmpty(message = "主键不能为空") @PathVariable Long[] formIds) {
        return toAjax(formService.deleteWithValidByIds(Arrays.asList(formIds)) ? 1 : 0);
    }


    /**
     * 挂载流程表单
     *
     * @return
     */
    @Log(title = "流程表单", businessType = BusinessType.INSERT)
    @PostMapping("/addDeployForm")
    public AjaxResult addDeployForm(@RequestBody WfDeployForm deployForm) {
        return toAjax(deployFormService.insertWfDeployForm(deployForm));
    }
}
