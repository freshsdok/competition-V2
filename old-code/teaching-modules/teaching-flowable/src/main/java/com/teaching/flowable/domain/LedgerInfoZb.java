package com.teaching.flowable.domain;

import com.teaching.common.core.annotation.Excel;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.teaching.flowable.core.domain.model.PageQuery;
import lombok.Data;

/**
 * 台账信息
 *
 * @author Administrator
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LedgerInfoZb extends PageQuery {

    /**
     * 追溯码
     */
    @Excel(name = "追溯码", width = 30)
    private String traceabilityCode;
    /**
     * 填报人
     */
    @Excel(name = "提报人")
    private String startUserName;
    /**
     * 填报人id
     */
    private String startUserId;
    /**
     * 填报时间
     */
    @Excel(name = "提报时间", width = 20)
    private String startTime;
    /**
     * 问题内容
     */
    @Excel(name = "问题内容", width = 40)
    private String content;
    /**
     * 四级网格负责人
     */
    @Excel(name = "四级网格负责人", readConverterExp = "1=解决,4=上报,7=撤回")
    private String grid4;
    /**
     * 四级网格负责人解决方案
     */
    @Excel(name = "四级网格负责人解决方案")
    private String solution4;
    /**
     * 三级网格
     */
    @Excel(name = "三级网格负责人", readConverterExp = "1=解决,4=上报,7=撤回")
    private String grid3;
    /**
     * 三级网格负责人解决方案
     */
    @Excel(name = "三级网格负责人解决方案")
    private String solution3;
    /**
     * 二级网关负责人
     */
    @Excel(name = "二级网格负责人", readConverterExp = "1=解决,4=上报,7=撤回")
    private String grid2Header;
    /**
     * 二级网格负责人解决方案
     */
    @Excel(name = "二级网格负责人解决方案")
    private String solution2Header;
    /**
     * 二级网格
     */
    @Excel(name = "二级网格成员", readConverterExp = "1=解决,4=上报,7=撤回")
    private String grid2;
    /**
     * 二级网格人员解决方案
     */
    @Excel(name = "二级网格成员解决方案")
    private String solution2;
    /**
     * 一级网格
     */
    @Excel(name = "一级网格成员", readConverterExp = "1=解决,4=上报,7=撤回")
    private String grid1;
    /**
     * 一级网格负责人解决方案
     */
    @Excel(name = "一级网格成员解决方案")
    private String solution1;
    /**
     * 一级网格负责人
     */
    @Excel(name = "一级网格负责人", readConverterExp = "1=解决,4=上报,7=撤回")
    private String grid1Header;
    /**
     * 一级网格负责人解决方案
     * partyOpinions
     */
    @Excel(name = "一级网格负责人解决方案")
    private String solution1Header;

    /**
     * 是否是支部查询,默认不是
     * 支部查询只查询本支部下所有已解决的，非支部查整个系统的
     */
    private boolean isZb = false;

    public boolean getIsZb() {
        return isZb;
    }

    public void setIsZb(boolean zb) {
        isZb = zb;
    }
}
