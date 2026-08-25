package com.teaching.flowable.service.impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.flowable.common.constant.ProcessConstants;
import com.teaching.flowable.common.constant.TaskConstants;
import com.teaching.flowable.core.domain.model.PageQuery;
import com.teaching.flowable.core.page.TableDataInfo;
import com.teaching.flowable.domain.WfCopy;
import com.teaching.flowable.domain.bo.WfCopyBo;
import com.teaching.flowable.domain.bo.WfTaskBo;
import com.teaching.flowable.domain.vo.WfCopyVo;
import com.teaching.flowable.mapper.WfCopyMapper;
import com.teaching.flowable.service.IWfCopyService;
import lombok.RequiredArgsConstructor;
import org.flowable.engine.HistoryService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.history.HistoricProcessInstanceQuery;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 流程抄送Service业务层处理
 *
 * @author KonBAI
 * @date 2022-05-19
 */
@RequiredArgsConstructor
@Service
public class WfCopyServiceImpl implements IWfCopyService {

    private final WfCopyMapper baseMapper;

    private final HistoryService historyService;
//    private final SysUserMapper sysUserMapper;

    /**
     * 查询流程抄送
     *
     * @param copyId 流程抄送主键
     * @return 流程抄送
     */
    @Override
    public WfCopyVo queryById(Long copyId) {
        return baseMapper.selectVoById(copyId);
    }

    /**
     * 查询流程抄送列表
     *
     * @param bo 流程抄送
     * @return 流程抄送
     */
    @Override
    public TableDataInfo<WfCopyVo> selectPageList(WfCopyBo bo, PageQuery pageQuery) {
        List<WfCopyVo> resultList = new ArrayList<>();
        LambdaQueryWrapper<WfCopy> lqw = buildQueryWrapper(bo);
        lqw.orderByDesc(WfCopy::getCreateTime);
        Page<WfCopyVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        List<WfCopyVo> records = result.getRecords();
        if (CollectionUtils.isNotEmpty(records)) {
            for (WfCopyVo record : records) {
                String instanceId = record.getInstanceId();
                HistoricProcessInstanceQuery historicProcessInstanceQuery = historyService.createHistoricProcessInstanceQuery().includeProcessVariables().processInstanceId(instanceId);
                if(StringUtils.isNotBlank(pageQuery.getIssueType())){
                    historicProcessInstanceQuery.variableValueEquals(ProcessConstants.ISSUE_TYPE, pageQuery.getIssueType());
                }
                if(StringUtils.isNotBlank(pageQuery.getUrgentLevel())){
                    historicProcessInstanceQuery.variableValueEquals(ProcessConstants.URGENT_LEVEL, pageQuery.getUrgentLevel());
                }
                //流程发起人筛选
                if(StringUtils.isNotBlank(pageQuery.getStartUserId())){
                    historicProcessInstanceQuery.startedBy(pageQuery.getStartUserId());
                }
                if(StringUtils.isNotBlank(pageQuery.getTraceabilityCode())){
                    historicProcessInstanceQuery.variableValueLike(ProcessConstants.TRACEBILITY_CODE, "%"+pageQuery.getTraceabilityCode()+"%");
                }
                HistoricProcessInstance historicProcessInstance = historicProcessInstanceQuery.singleResult();
                if(ObjectUtil.isNotNull(historicProcessInstance)){
                    Map<String, Object> processVariables = historicProcessInstance.getProcessVariables();
                    record.setIssueType(MapUtil.getStr(processVariables, ProcessConstants.ISSUE_TYPE));
                    record.setTraceabilityCode(MapUtil.getStr(processVariables, ProcessConstants.TRACEBILITY_CODE));
                    record.setUrgentLevel(MapUtil.getStr(processVariables, ProcessConstants.URGENT_LEVEL));
                    //流程发起人
                    Long startUserId = MapUtil.getLong(processVariables, TaskConstants.PROCESS_INITIATOR);
//                    record.setStartUserName(sysUserMapper.selectUserById(startUserId).getNickName());
                    record.setProcessStatus(MapUtil.getStr(processVariables, ProcessConstants.PROCESS_STATUS_KEY));
                    record.setContent(MapUtil.getStr(processVariables, "content"));
                    resultList.add(record);
                }
            }
        }
        return TableDataInfo.build(resultList);
    }

    /**
     * 查询流程抄送列表
     *
     * @param bo 流程抄送
     * @return 流程抄送
     */
    @Override
    public List<WfCopyVo> selectList(WfCopyBo bo) {
        LambdaQueryWrapper<WfCopy> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<WfCopy> buildQueryWrapper(WfCopyBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<WfCopy> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getUserId() != null, WfCopy::getUserId, bo.getUserId());
        lqw.like(StringUtils.isNotBlank(bo.getProcessName()), WfCopy::getProcessName, bo.getProcessName());
        lqw.like(StringUtils.isNotBlank(bo.getOriginatorName()), WfCopy::getOriginatorName, bo.getOriginatorName());
        return lqw;
    }

    @Override
    public Boolean makeCopy(WfTaskBo taskBo) {
        if (StringUtils.isBlank(taskBo.getCopyUserIds())) {
            // 若抄送用户为空，则不需要处理，返回成功
            return true;
        }
        HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(taskBo.getProcInsId()).singleResult();
        String[] ids = taskBo.getCopyUserIds().split(",");
        List<WfCopy> copyList = new ArrayList<>(ids.length);
        /*Long originatorId = LoginHelper.getUserId();
        String originatorName = LoginHelper.getNickName();*/
        String title = historicProcessInstance.getProcessDefinitionName() + "-" + taskBo.getTaskName();
        for (String id : ids) {
            Long userId = Long.valueOf(id);
            WfCopy copy = new WfCopy();
            copy.setTitle(title);
            copy.setProcessId(historicProcessInstance.getProcessDefinitionId());
            copy.setProcessName(historicProcessInstance.getProcessDefinitionName());
            copy.setDeploymentId(historicProcessInstance.getDeploymentId());
            copy.setInstanceId(taskBo.getProcInsId());
            copy.setTaskId(taskBo.getTaskId());
            copy.setUserId(userId);
            /*copy.setOriginatorId(originatorId);
            copy.setOriginatorName(originatorName);*/
            copy.setCreateTime(DateUtils.getNowDate());
            copyList.add(copy);
        }
        return baseMapper.insertBatch(copyList);
    }
}
