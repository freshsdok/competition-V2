package com.teaching.flowable.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.teaching.common.core.domain.R;
import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.InnerAuth;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.flowable.core.domain.ProcessQuery;
import com.teaching.flowable.core.domain.model.PageQuery;
import com.teaching.flowable.core.page.TableDataInfo;
import com.teaching.flowable.domain.WfReport;
import com.teaching.flowable.domain.bo.WfCopyBo;
import com.teaching.flowable.domain.vo.*;
import com.teaching.flowable.service.IWfCopyService;
import com.teaching.flowable.service.IWfProcessService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RuntimeService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 工作流流程管理
 *
 * @author KonBAI
 * @createTime 2022/3/24 18:54
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/process")
public class WfProcessController extends BaseController {

    private final IWfProcessService processService;
    private final IWfCopyService copyService;

    /**
     * 查询可发起流程列表
     *
     * @param pageQuery 分页参数
     */
    @GetMapping(value = "/list")
    @RequiresPermissions("workflow:process:startList")
    public TableDataInfo<WfDefinitionVo> startProcessList(ProcessQuery processQuery, PageQuery pageQuery) {
        return processService.selectPageStartProcessList(processQuery, pageQuery);
    }

    /**
     * C端发起退赛流程
     *
     * @param processQuery
     * @return
     */
    @GetMapping("/startProcess")
    public R<WfDefinitionVo> startProcess(ProcessQuery processQuery) {
        return R.ok(processService.selectStartProcess(processQuery));
    }

    /**
     * 我拥有的流程
     */
    @RequiresPermissions("workflow:process:ownList")
    @GetMapping(value = "/ownList")
    public TableDataInfo<WfTaskVo> ownProcessList(ProcessQuery processQuery, PageQuery pageQuery) {
        return processService.selectPageOwnProcessList(processQuery, pageQuery);
    }

    /**
     * 获取待办列表
     */
    @RequiresPermissions("workflow:process:todoList")
    @GetMapping(value = "/todoList")
    public TableDataInfo<WfTaskVo> todoProcessList(ProcessQuery processQuery, PageQuery pageQuery) {
        return processService.selectPageTodoProcessList(processQuery, pageQuery);
    }

    /**
     * 获取待接收列表
     */
    @RequiresPermissions("workflow:process:todoList")
    @GetMapping(value = "/waitingList")
    public TableDataInfo<WfTaskVo> waitingProcessList(ProcessQuery processQuery, PageQuery pageQuery) {
        return processService.selectPageWaitingProcessList(processQuery, pageQuery);
    }


    /**
     * 获取待签列表
     *
     * @param processQuery 流程业务对象
     * @param pageQuery    分页参数
     */
    @RequiresPermissions("workflow:process:claimList")
    @GetMapping(value = "/claimList")
    public TableDataInfo<WfTaskVo> claimProcessList(ProcessQuery processQuery, PageQuery pageQuery) {
        return processService.selectPageClaimProcessList(processQuery, pageQuery);
    }

    /**
     * 获取已办列表
     *
     * @param pageQuery 分页参数
     */
    @RequiresPermissions("workflow:process:finishedList")
    @GetMapping(value = "/finishedList")
    public TableDataInfo<WfTaskVo> finishedProcessList(ProcessQuery processQuery, PageQuery pageQuery) {
        return processService.selectPageFinishedProcessList(processQuery, pageQuery);
    }

    /**
     * 获取抄送列表
     *
     * @param copyBo    流程抄送对象
     * @param pageQuery 分页参数
     */
    @RequiresPermissions("workflow:process:copyList")
    @GetMapping(value = "/copyList")
    public TableDataInfo<WfCopyVo> copyProcessList(WfCopyBo copyBo, PageQuery pageQuery) {
//        copyBo.setUserId(getUserId());
        return copyService.selectPageList(copyBo, pageQuery);
    }


    /**
     * 导出可发起流程列表
     */
    @RequiresPermissions("workflow:process:startExport")
    @Log(title = "可发起流程", businessType = BusinessType.EXPORT)
    @PostMapping("/startExport")
    public void startExport(@Validated ProcessQuery processQuery, HttpServletResponse response) {
        List<WfDefinitionVo> list = processService.selectStartProcessList(processQuery);
//        ExcelUtil.exportExcel(list, "可发起流程", WfDefinitionVo.class, response);
    }

    /**
     * 导出我拥有流程列表
     */
    @RequiresPermissions("workflow:process:ownExport")
    @Log(title = "我拥有流程", businessType = BusinessType.EXPORT)
    @PostMapping("/ownExport")
    public void ownExport(@Validated ProcessQuery processQuery, HttpServletResponse response) {
        List<WfTaskVo> list = processService.selectOwnProcessList(processQuery);
        List<WfOwnTaskExportVo> listVo = BeanUtil.copyToList(list, WfOwnTaskExportVo.class);
        for (WfOwnTaskExportVo exportVo : listVo) {
            exportVo.setStatus(ObjectUtil.isNull(exportVo.getFinishTime()) ? "进行中" : "已完成");
        }
//        ExcelUtil.exportExcel(listVo, "我拥有流程", WfOwnTaskExportVo.class, response);
    }

    /**
     * 导出待办流程列表
     */
    @RequiresPermissions("workflow:process:todoExport")
    @Log(title = "待办流程", businessType = BusinessType.EXPORT)
    @PostMapping("/todoExport")
    public void todoExport(@Validated ProcessQuery processQuery, HttpServletResponse response) {
        List<WfTaskVo> list = processService.selectTodoProcessList(processQuery);
        List<WfTodoTaskExportVo> listVo = BeanUtil.copyToList(list, WfTodoTaskExportVo.class);
//        ExcelUtil.exportExcel(listVo, "待办流程", WfTodoTaskExportVo.class, response);
    }

    /**
     * 导出待签流程列表
     */
    @RequiresPermissions("workflow:process:claimExport")
    @Log(title = "待签流程", businessType = BusinessType.EXPORT)
    @PostMapping("/claimExport")
    public void claimExport(@Validated ProcessQuery processQuery, HttpServletResponse response) {
        List<WfTaskVo> list = processService.selectClaimProcessList(processQuery);
        List<WfClaimTaskExportVo> listVo = BeanUtil.copyToList(list, WfClaimTaskExportVo.class);
//        ExcelUtil.exportExcel(listVo, "待签流程", WfClaimTaskExportVo.class, response);
    }

    /**
     * 导出已办流程列表
     */
    @RequiresPermissions("workflow:process:finishedExport")
    @Log(title = "已办流程", businessType = BusinessType.EXPORT)
    @PostMapping("/finishedExport")
    public void finishedExport(@Validated ProcessQuery processQuery, HttpServletResponse response) {
        List<WfTaskVo> list = processService.selectFinishedProcessList(processQuery);
        List<WfFinishedTaskExportVo> listVo = BeanUtil.copyToList(list, WfFinishedTaskExportVo.class);
//        ExcelUtil.exportExcel(listVo, "已办流程", WfFinishedTaskExportVo.class, response);
    }

    /**
     * 导出抄送流程列表
     */
    @RequiresPermissions("workflow:process:copyExport")
    @Log(title = "抄送流程", businessType = BusinessType.EXPORT)
    @PostMapping("/copyExport")
    public void copyExport(WfCopyBo copyBo, HttpServletResponse response) {
//        copyBo.setUserId(getUserId());
        List<WfCopyVo> list = copyService.selectList(copyBo);
//        ExcelUtil.exportExcel(list, "抄送流程", WfCopyVo.class, response);
    }

    /**
     * 查询流程部署关联表单信息
     *
     * @param definitionId 流程定义id
     * @param deployId     流程部署id
     */
    @GetMapping("/getProcessForm")
//    @RequiresPermissions("workflow:process:start")
    public R<?> getForm(@RequestParam(value = "definitionId") String definitionId,
                        @RequestParam(value = "deployId") String deployId,
                        @RequestParam(value = "procInsId", required = false) String procInsId) {
        return R.ok(processService.selectFormContent(definitionId, deployId, procInsId));
    }

    /**
     * 根据流程定义id启动流程实例
     *
     * @param processDefId 流程定义id
     * @param variables    变量集合,json对象
     */
//    @RepeatSubmit()
//    @RequiresPermissions("workflow:process:start")
    @PostMapping("/start/{processDefId}")
    public R<String> start(@PathVariable(value = "processDefId") String processDefId, @RequestBody Map<String, Object> variables) {
        processService.startProcessByDefId(processDefId, variables);
        return R.ok("流程启动成功");

    }

    /**
     * 启动流程 根据流程分类
     * 接口发起流程
     *
     * @param variables
     * @param category
     * @return
     */
    @InnerAuth
    @PostMapping("/startByCategory/{category}/{teamCode}")
    public R<Void> startProcess(@RequestBody Map<String, Object> variables, @PathVariable String category, @PathVariable String teamCode) {
        processService.startProcess(variables, category, teamCode);
        return R.ok();

    }

    /**
     * 删除流程实例
     *
     * @param instanceIds 流程实例ID串
     */
    @DeleteMapping("/instance/{instanceIds}")
    public R<Void> delete(@PathVariable String[] instanceIds) {
        processService.deleteProcessByIds(instanceIds);
        return R.ok();
    }

    /**
     * 读取xml文件
     *
     * @param processDefId 流程定义ID
     */
    @GetMapping("/bpmnXml/{processDefId}")
    public R<String> getBpmnXml(@PathVariable(value = "processDefId") String processDefId) {
        return R.ok(null, processService.queryBpmnXmlById(processDefId));
    }

    /**
     * 查询流程详情信息
     *
     * @param procInsId 流程实例ID
     * @param taskId    任务ID
     */
    @GetMapping("/detail")
    public R detail(String procInsId, String taskId, String changeStatusFlag) {
        Long userId = SecurityUtils.getUserId();
        return R.ok(processService.queryProcessDetail(procInsId, taskId, userId, changeStatusFlag));
    }


    @Resource
    protected RuntimeService runtimeService;

    /**
     * 进行中的
     *
     * @param processQuery
     * @param pageQuery
     * @return
     */
    @GetMapping("/runList")
    public TableDataInfo<WfTaskVo> rList(ProcessQuery processQuery, PageQuery pageQuery) {
        return processService.queryPageRList(processQuery, pageQuery);
    }

    /**
     * 已结束的
     *
     * @param processQuery
     * @param pageQuery
     * @return
     */
    @GetMapping("/finishList")
    public TableDataInfo<WfTaskVo> fList(ProcessQuery processQuery, PageQuery pageQuery) {
        // 查询已结束的流程实例列表
        return processService.queryPageFList(processQuery, pageQuery);
    }

    /**
     * 挂起实例
     *
     * @param id
     * @return
     */
    @GetMapping("/suspend/{id}")
    public R suspend(@PathVariable String id) {
        //挂起
        runtimeService.suspendProcessInstanceById(id);
        return R.ok("操作成功");
    }

    /**
     * 激活实例
     *
     * @param id
     * @return
     */
    @GetMapping("/activate/{id}")
    public R activate(@PathVariable String id) {
        //激活
        runtimeService.activateProcessInstanceById(id);
        return R.ok("操作成功");
    }

    /**
     * 统一待办集成
     * 获取待办列表
     */
    @GetMapping(value = "/todoListIntegration")
    public Map<String, Object> todoProcessListIntegration(@RequestParam(value = "workno") String workno) {
        return processService.selectPageTodoProcessListIntegration(workno);
    }


    /**
     * 手动催办
     *
     * @param assigneeId 指定人时 人员id
     * @param procInsId  流程id
     * @param taskDefKey 当前节点id
     * @return
     */
    @GetMapping(value = "/hasten")
    public R hasten(String assigneeId, String procInsId, String taskDefKey) {
        int hasten = processService.hasten(assigneeId, procInsId, taskDefKey);
        if (hasten == -1) {
            return R.fail("当前流程待办节点今日已催办,不能重复催办!");
        } else if (hasten == -2) {
            return R.fail("催办失败,当前节点没有审核人,请联系管理员!");
        } else if (hasten == 0) {
            return R.fail("催办失败!");
        }
        return R.ok("催办成功!");
    }

    /**
     * 获取流程模型列表
     *
     * @return 模型id，模型名称，模型key
     */
    @GetMapping(value = "/getFlowModel")
    public List<Map<String, String>> getFlowModel() {
        return processService.getFlowModel();
    }

    /**
     * 流程审批时效信息列表 节点合并
     *
     * @param report
     * @param pageQuery
     * @return
     */
    @GetMapping(value = "/reportList")
    public TableDataInfo<WfReport> finishedReportList(WfReport report, PageQuery pageQuery) {
        return processService.getReportList1(report, pageQuery);
    }

    /**
     * 流程时效分析 节点不合并
     *
     * @param report
     * @return
     */
   /* @GetMapping(value = "/flowReportList")
    public TableDataInfo<WfReport> getFinishedReportList(WfReport report) {
        startPage();
        List<WfReport> list = processService.getReportList(report);
        return getDataTable(list);
    }*/

    /**
     * 流程审批时效信息导出
     *
     * @param response
     * @param report
     */
    /*@PostMapping("/export")
    public void export(HttpServletResponse response, WfReport report) {
        List<WfReport> list = processService.getReportList(report);
        ExcelUtil<WfReport> util = new ExcelUtil<WfReport>(WfReport.class);
        util.exportExcel(response, list, "流程审批时效分析");
    }*/

}
