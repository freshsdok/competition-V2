package com.teaching.flowable.service;


import com.teaching.flowable.core.FormConf;
import com.teaching.flowable.core.domain.ProcessQuery;
import com.teaching.flowable.core.domain.model.PageQuery;
import com.teaching.flowable.core.page.TableDataInfo;
import com.teaching.flowable.domain.LedgerInfo;
import com.teaching.flowable.domain.LedgerInfoZb;
import com.teaching.flowable.domain.WfReport;
import com.teaching.flowable.domain.vo.WfDefinitionVo;
import com.teaching.flowable.domain.vo.WfDetailVo;
import com.teaching.flowable.domain.vo.WfTaskVo;

import java.util.List;
import java.util.Map;

/**
 * @author KonBAI
 * @createTime 2022/3/24 18:57
 */
public interface IWfProcessService {

    /**
     * 查询可发起流程列表
     *
     * @param pageQuery 分页参数
     * @return
     */
    TableDataInfo<WfDefinitionVo> selectPageStartProcessList(ProcessQuery processQuery, PageQuery pageQuery);

    /**
     * 查询C端可发起的流程根据类型
     * @param processQuery
     * @return
     */
    public WfDefinitionVo selectStartProcess(ProcessQuery processQuery);

    /**
     * 查询可发起流程列表
     */
    List<WfDefinitionVo> selectStartProcessList(ProcessQuery processQuery);

    /**
     * 查询我的流程列表
     *
     * @param pageQuery 分页参数
     */
    TableDataInfo<WfTaskVo> selectPageOwnProcessList(ProcessQuery processQuery, PageQuery pageQuery);

    /**
     * 查询我的流程列表
     */
    List<WfTaskVo> selectOwnProcessList(ProcessQuery processQuery);

    /**
     * 查询代办任务列表
     *
     * @param pageQuery 分页参数
     */
    TableDataInfo<WfTaskVo> selectPageTodoProcessList(ProcessQuery processQuery, PageQuery pageQuery);

    TableDataInfo<WfTaskVo> selectPageTodoProcessList2(ProcessQuery processQuery, PageQuery pageQuery);

    /**
     * 查询代接收任务列表
     *
     * @param processQuery
     * @param pageQuery
     * @return
     */
    TableDataInfo<WfTaskVo> selectPageWaitingProcessList(ProcessQuery processQuery, PageQuery pageQuery);

    TableDataInfo<WfTaskVo> selectPageWaitingProcessList2(ProcessQuery processQuery, PageQuery pageQuery);

    /**
     * 待办转未统一待办
     *
     * @param workno
     * @return
     */
    Map<String, Object> selectPageTodoProcessListIntegration(String workno);

    /**
     * 查询代办任务列表
     */
    List<WfTaskVo> selectTodoProcessList(ProcessQuery processQuery);

    /**
     * 查询待签任务列表
     *
     * @param pageQuery 分页参数
     */
    TableDataInfo<WfTaskVo> selectPageClaimProcessList(ProcessQuery processQuery, PageQuery pageQuery);

    /**
     * 查询待签任务列表
     */
    List<WfTaskVo> selectClaimProcessList(ProcessQuery processQuery);

    /**
     * 查询已办任务列表
     *
     * @param pageQuery 分页参数
     */
    TableDataInfo<WfTaskVo> selectPageFinishedProcessList(ProcessQuery processQuery, PageQuery pageQuery);

    /**
     * 查询已办任务列表
     */
    List<WfTaskVo> selectFinishedProcessList(ProcessQuery processQuery);

    /**
     * 查询流程部署关联表单信息
     *
     * @param definitionId 流程定义ID
     * @param deployId     部署ID
     */
    FormConf selectFormContent(String definitionId, String deployId, String procInsId);

    /**
     * 启动流程实例
     *
     * @param procDefId 流程定义ID
     * @param variables 扩展参数
     */
    void startProcessByDefId(String procDefId, Map<String, Object> variables);

    /**
     * 启动流程
     */
    void startProcess(Map<String, Object> variables,String category,String teamCode);

    /**
     * 通过DefinitionKey启动流程
     *
     * @param procDefKey 流程定义Key
     * @param variables  扩展参数
     */
    void startProcessByDefKey(String procDefKey, Map<String, Object> variables);

    /**
     * 删除流程实例
     */
    void deleteProcessByIds(String[] instanceIds);


    /**
     * 读取xml文件
     *
     * @param processDefId 流程定义ID
     */
    String queryBpmnXmlById(String processDefId);


    /**
     * 查询流程任务详情信息
     *
     * @param procInsId 流程实例ID
     * @param taskId    任务ID
     */
    WfDetailVo queryProcessDetail(String procInsId, String taskId, Long userId, String changeStatusFlag);

    /**
     * 进行中的流程实例
     *
     * @param processQuery
     * @param pageQuery
     * @return
     */
    TableDataInfo<WfTaskVo> queryPageRList(ProcessQuery processQuery, PageQuery pageQuery);

    /**
     * 已完成的流程实例
     *
     * @param processQuery
     * @param pageQuery
     * @return
     */
    TableDataInfo<WfTaskVo> queryPageFList(ProcessQuery processQuery, PageQuery pageQuery);


    /**
     * 手动催办
     *
     * @param assigneeIds 指定的人员id
     * @param procInsId   流程id
     * @param taskDefKey  当前节点
     */
    int hasten(String assigneeIds, String procInsId, String taskDefKey);

    /**
     * 报表查询 节点合并
     *
     * @param report
     * @param pageQuery
     * @return
     */
    TableDataInfo<WfReport> getReportList1(WfReport report, PageQuery pageQuery);

    /**
     * 报表查询 节点不合并
     *
     * @param report
     * @return
     */
    List<WfReport> getReportList(WfReport report);

    /**
     * 获取流程模型
     *
     * @return 流程id，流程名称，流程key
     */
    List<Map<String, String>> getFlowModel();



}
