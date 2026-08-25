package com.teaching.flowable.mapper;

import com.teaching.flowable.core.mapper.BaseMapperPlus;
import com.teaching.flowable.domain.WfForm;
import com.teaching.flowable.domain.WfReport;
import com.teaching.flowable.domain.vo.WfFormVo;

import java.util.List;
import java.util.Map;

/**
 * 表单Mapper接口
 */
public interface WfReportMapper extends BaseMapperPlus<WfReportMapper, WfForm, WfFormVo> {

    /**
     * 不合并
     *
     * @param report
     * @return
     */
    List<WfReport> selectReportList(WfReport report);

    /**
     * 合并各个节点
     *
     * @param report
     * @return
     */
    List<WfReport> selectReportDetail(WfReport report);

    /**
     * 查询流程信息
     *
     * @return
     */
    List<Map<String, String>> selectFlowModel();

    /**
     * 统计本月发起的数量
     * @return
     */
    Long countThisMonthStarted();

}
