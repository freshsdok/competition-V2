package com.teaching.flowable.controller;

import com.teaching.common.core.utils.poi.ExcelUtil;
import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.web.page.TableDataInfo;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.flowable.service.IOperationFlowService;
import com.teaching.system.api.domain.OperationFlow;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 团队信息操作和流程关联Controller
 *
 * @author teaching
 * @date 2026-02-02
 */
@RestController
@RequestMapping("/flow")
public class OperationFlowController extends BaseController {
    @Autowired
    private IOperationFlowService operationFlowService;

    /**
     * 查询团队信息操作和流程关联列表
     */
    @RequiresPermissions("flowable:flow:list")
    @GetMapping("/list")
    public TableDataInfo list(OperationFlow operationFlow) {
        startPage();
        List<OperationFlow> list = operationFlowService.selectOperationFlowList(operationFlow);
        return getDataTable(list);
    }

    /**
     * 导出团队信息操作和流程关联列表
     */
    @RequiresPermissions("flowable:flow:export")
    @Log(title = "团队信息操作和流程关联", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, OperationFlow operationFlow) {
        List<OperationFlow> list = operationFlowService.selectOperationFlowList(operationFlow);
        ExcelUtil<OperationFlow> util = new ExcelUtil<OperationFlow>(OperationFlow.class);
        util.exportExcel(response, list, "团队信息操作和流程关联数据");
    }

    /**
     * 获取团队信息操作和流程关联详细信息  根据teamCode
     *
     * @param teamCode 团队code
     * @return List
     */
    @GetMapping(value = "/{teamCode}")
    public AjaxResult getInfo(@PathVariable("teamCode") String teamCode) {
        return success(operationFlowService.selectOperationFlowByTeamCode(teamCode));
    }

    @GetMapping(value = "/getTeamCodeOperatorType/{teamCode}")
    public AjaxResult getInnerInfo(@PathVariable("teamCode") String teamCode) {
        return success(operationFlowService.getTeamCodeOperatorType(teamCode));
    }

    /**
     * 根据teamCode获取是否有进行中的流程
     *
     * @param teamCode
     * @return true 有，false 没有
     */
    @GetMapping(value = "/getRunning/{teamCode}")
    public AjaxResult getOperationFlowList(@PathVariable("teamCode") String teamCode) {
        OperationFlow operationFlow = new OperationFlow();
        operationFlow.setFlowStatus("running");
        operationFlow.setTeamCode(teamCode);
        return success(CollectionUtils.isNotEmpty(operationFlowService.selectOperationFlowList(operationFlow)));
    }

    /**
     * 根据teamCode和flowType获取团队信息操作和流程关联详细信息
     *
     * @param teamCode 团队code
     * @param flowType 流程类型
     * @return 单个
     */
    @GetMapping(value = "/{teamCode}/{flowType}")
    public AjaxResult getInfoByTeamCodeAndFlowType(@PathVariable("teamCode") String teamCode, @PathVariable String flowType) {
        return success(operationFlowService.selectOperationFlowList(new OperationFlow(teamCode, flowType, null)));
    }

    /**
     * 根据teamCode和flowType获取流程信息
     *
     * @param teamCode
     * @return
     */
    @GetMapping(value = "/getFlowVariables/{teamCode}")
    public AjaxResult getFlowVariables(@PathVariable("teamCode") String teamCode) {
        return success(operationFlowService.getFlowVariables(teamCode));
    }

    /**
     * 新增团队信息操作和流程关联
     */
    @Log(title = "团队信息操作和流程关联", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody OperationFlow operationFlow) {
        return toAjax(operationFlowService.insertOperationFlow(operationFlow));
    }

    /**
     * 修改状态根据flowId
     */
    @RequiresPermissions("flowable:flow:edit")
    @Log(title = "团队信息操作和流程关联", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody OperationFlow operationFlow) {
        return toAjax(operationFlowService.updateStatusByFlowId(operationFlow));
    }

    /**
     * 删除团队信息操作和流程关联
     */
    @RequiresPermissions("flowable:flow:remove")
    @Log(title = "团队信息操作和流程关联", businessType = BusinessType.DELETE)
    @DeleteMapping("/{teamCodes}")
    public AjaxResult remove(@PathVariable String[] teamCodes) {
        return toAjax(operationFlowService.deleteOperationFlowByTeamCodes(teamCodes));
    }
}

