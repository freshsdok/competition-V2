package com.teaching.flowable.service.impl;

import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.io.IoUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.flowable.core.domain.ProcessQuery;
import com.teaching.flowable.core.domain.model.PageQuery;
import com.teaching.flowable.core.page.TableDataInfo;
import com.teaching.flowable.domain.WfCopy;
import com.teaching.flowable.domain.WfDeployForm;
import com.teaching.flowable.domain.vo.WfDeployVo;
import com.teaching.flowable.mapper.WfCopyMapper;
import com.teaching.flowable.mapper.WfDeployFormMapper;
import com.teaching.flowable.service.IWfDeployService;
import com.teaching.flowable.utils.ProcessUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.flowable.common.engine.api.FlowableException;
import org.flowable.common.engine.impl.db.SuspensionState;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.repository.ProcessDefinitionQuery;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author KonBAI
 * @createTime 2022/6/30 9:04
 */
@RequiredArgsConstructor
@Service
public class WfDeployServiceImpl implements IWfDeployService {

    private final RepositoryService repositoryService;
    private final WfDeployFormMapper deployFormMapper;
    private final RuntimeService runtimeService;
    protected final HistoryService historyService;

    @Override
    public TableDataInfo<WfDeployVo> queryPageList(ProcessQuery processQuery, PageQuery pageQuery) {
        // 流程定义列表数据查询
        ProcessDefinitionQuery processDefinitionQuery = repositoryService.createProcessDefinitionQuery()
                .latestVersion()
                .orderByProcessDefinitionKey()
                .asc();
        // 构建搜索条件
        ProcessUtils.buildProcessSearch(processDefinitionQuery, processQuery);
        long pageTotal = processDefinitionQuery.count();
        if (pageTotal <= 0) {
            return TableDataInfo.build();
        }
        int offset = pageQuery.getPageSize() * (pageQuery.getPageNum() - 1);
        List<ProcessDefinition> definitionList = processDefinitionQuery.listPage(offset, pageQuery.getPageSize());

        List<WfDeployVo> deployVoList = new ArrayList<>(definitionList.size());
        for (ProcessDefinition processDefinition : definitionList) {
            String deploymentId = processDefinition.getDeploymentId();
            Deployment deployment = repositoryService.createDeploymentQuery().deploymentId(deploymentId).singleResult();
            WfDeployVo vo = new WfDeployVo();
            vo.setDefinitionId(processDefinition.getId());
            vo.setProcessKey(processDefinition.getKey());
            vo.setProcessName(StringUtils.isNotBlank(deployment.getName())?deployment.getName():processDefinition.getName());
            vo.setVersion(processDefinition.getVersion());
            vo.setCategory(processDefinition.getCategory());
            vo.setDeploymentId(processDefinition.getDeploymentId());
            vo.setSuspended(processDefinition.isSuspended());
            vo.setStatus(StringUtils.isBlank(processDefinition.getDescription()) ? "0" : processDefinition.getDescription());
            // 流程部署信息
            vo.setCategory(deployment.getCategory());
            vo.setDeploymentTime(deployment.getDeploymentTime());
            deployVoList.add(vo);
        }
        Page<WfDeployVo> page = new Page<>();
        page.setRecords(deployVoList);
        page.setTotal(pageTotal);
        return TableDataInfo.build(page);
    }

    @Override
    public TableDataInfo<WfDeployVo> queryPublishList(String processKey, PageQuery pageQuery) {
        // 创建查询条件
        ProcessDefinitionQuery processDefinitionQuery = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(processKey)
                .orderByProcessDefinitionVersion()
                .desc();
        long pageTotal = processDefinitionQuery.count();
        if (pageTotal <= 0) {
            return TableDataInfo.build();
        }
        // 根据查询条件，查询所有版本
        int offset = pageQuery.getPageSize() * (pageQuery.getPageNum() - 1);
        List<ProcessDefinition> processDefinitionList = processDefinitionQuery
                .listPage(offset, pageQuery.getPageSize());
        List<WfDeployVo> deployVoList = processDefinitionList.stream().map(item -> {
            Deployment deployment = repositoryService.createDeploymentQuery().deploymentId(item.getDeploymentId()).singleResult();
            WfDeployVo vo = new WfDeployVo();
            vo.setDefinitionId(item.getId());
            vo.setProcessKey(item.getKey());
            vo.setProcessName(deployment!=null&&StringUtils.isNotBlank(deployment.getName())?deployment.getName():item.getName());
            vo.setVersion(item.getVersion());
            vo.setCategory(item.getCategory());
            vo.setDeploymentId(item.getDeploymentId());
            vo.setSuspended(item.isSuspended());
            return vo;
        }).collect(Collectors.toList());
        Page<WfDeployVo> page = new Page<>();
        page.setRecords(deployVoList);
        page.setTotal(pageTotal);
        return TableDataInfo.build(page);
    }

    /**
     * 激活或挂起流程
     *
     * @param state        状态
     * @param definitionId 流程定义ID
     */
    @Override
    public void updateState(String definitionId, String state) {
        if (SuspensionState.ACTIVE.toString().equals(state)) {
            // 激活
            repositoryService.activateProcessDefinitionById(definitionId, true, null);
        } else if (SuspensionState.SUSPENDED.toString().equals(state)) {
            // 挂起
            repositoryService.suspendProcessDefinitionById(definitionId, true, null);
        }
    }

    @Override
    public String queryBpmnXmlById(String definitionId) {
        InputStream inputStream = repositoryService.getProcessModel(definitionId);
        try {
            return IoUtil.readUtf8(inputStream);
        } catch (IORuntimeException exception) {
            throw new RuntimeException("加载xml文件异常");
        }
    }

    private final WfCopyMapper baseMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByIds(List<String> deployIds) {
        for (String deployId : deployIds) {
            // 根据部署ID查询所有流程定义
            List<ProcessDefinition> definitions = repositoryService.createProcessDefinitionQuery()
                    .deploymentId(deployId)
                    .list();

            for (ProcessDefinition definition : definitions) {
                // 统计指定流程定义的活跃实例数量
                long activeCount = runtimeService.createProcessInstanceQuery()
                        .processDefinitionId(definition.getId())
                        .count();
                if (activeCount > 0) {
                    throw new FlowableException("存在运行中的流程实例，禁止删除部署");
                }
                long count = historyService.createHistoricProcessInstanceQuery().processDefinitionId(definition.getId()).count();
                if (count > 0) {
                    throw new FlowableException("存在历史流程实例，禁止删除部署");
                }
            }
            repositoryService.deleteDeployment(deployId, true);
            deployFormMapper.delete(new LambdaQueryWrapper<WfDeployForm>().eq(WfDeployForm::getDeployId, deployId));
            baseMapper.delete(new LambdaQueryWrapper<WfCopy>().eq(WfCopy::getDeploymentId, deployId));
        }
    }

    /**
     * 强制删除部署
     *
     * @param deployIds
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void forcedDeletion(List<String> deployIds) {
        for (String deployId : deployIds) {
            List<ProcessDefinition> definitions = repositoryService.createProcessDefinitionQuery()
                    .deploymentId(deployId)
                    .list();
            definitions.forEach(definition -> {
                // 终止所有实例
                List<ProcessInstance> instances = runtimeService.createProcessInstanceQuery()
                        .processDefinitionId(definition.getId())
                        .list();
                instances.forEach(instance ->
                        runtimeService.deleteProcessInstance(instance.getId(), "强制删除部署终止"));
            });
            repositoryService.deleteDeployment(deployId, true);
            deployFormMapper.delete(new LambdaQueryWrapper<WfDeployForm>().eq(WfDeployForm::getDeployId, deployId));
            baseMapper.delete(new LambdaQueryWrapper<WfCopy>().eq(WfCopy::getDeploymentId, deployId));
        }
    }

    /**
     * 根据部署ID和状态进行线上或线下操作
     *
     * @param deployIds 部署ID列表
     * @param status    状态（上线："online"，下线："offline"）
     */
    @Override
    public void makeItOnline(List<String> deployIds, String status, String category) {
        if ("1".equals(status)) {
            ProcessDefinitionQuery processDefinitionQuery = repositoryService.createProcessDefinitionQuery()
                    .latestVersion()
                    .orderByProcessDefinitionKey()
                    .asc();
            // 构建搜索条件
            processDefinitionQuery.processDefinitionCategory(category);
            ProcessUtils.buildProcessSearch(processDefinitionQuery, new ProcessQuery());
            List<ProcessDefinition> list = processDefinitionQuery.list();
            if (CollectionUtils.isNotEmpty(list)) {
                List<ProcessDefinition> result = list.stream().filter(item -> item.getCategory().equals(category) && "1".equals(item.getDescription())).toList();
                if (CollectionUtils.isNotEmpty(result)) {
                    throw new FlowableException("存在已上线的同类型流程，不可重复上线");
                }
            }
        }
        //上线操作，修改TENANT_ID_为1;下线操作，修改TENANT_ID_为0;
        deployFormMapper.updateReProcdefOnLineStatusById(deployIds, status);
    }
}
