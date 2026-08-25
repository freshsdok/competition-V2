package com.teaching.flowable.service;


import com.teaching.flowable.core.domain.ProcessQuery;
import com.teaching.flowable.core.domain.model.PageQuery;
import com.teaching.flowable.core.page.TableDataInfo;
import com.teaching.flowable.domain.vo.WfDeployVo;

import java.util.List;

/**
 * @author KonBAI
 * @createTime 2022/6/30 9:03
 */
public interface IWfDeployService {

    TableDataInfo<WfDeployVo> queryPageList(ProcessQuery processQuery, PageQuery pageQuery);

    TableDataInfo<WfDeployVo> queryPublishList(String processKey, PageQuery pageQuery);

    void updateState(String definitionId, String stateCode);

    String queryBpmnXmlById(String definitionId);

    void deleteByIds(List<String> deployIds);

    /**
     * 强制删除
     *
     * @param deployIds
     */
    void forcedDeletion(List<String> deployIds);

    /**
     * 上线
     *
     * @param deployIds
     * @param status
     */
    void makeItOnline(List<String> deployIds, String status,String category);
}
