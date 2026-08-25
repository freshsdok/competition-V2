package com.teaching.flowable.core.domain;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 流程查询实体对象
 *
 * @author KonBAI
 * @createTime 2022/6/11 01:15
 */
@Data
public class ProcessQuery {

    /**
     * 流程标识
     */
    private String processKey;

    /**
     * 流程名称
     */
    private String processName;

    /**
     * 流程分类
     */
    private String category;

    /**
     * 状态
     */
    private String state;

    /**
     * 请求参数
     */
    private Map<String, Object> params = new HashMap<>();

    /**
     * 银行账号
     */
    private String accountCode;
    /**
     * 开户银行
     */
    private String accountName;
    /**
     * 企业名称
     */
    private String deptName;
    /**
     * 流程实例id
     */
    private String procInsId;
    /**
     * 问题紧急程度
     */
    private String urgentLevel;
    /**
     * 问题类型
     */
    private String issueType;
    /**
     * 追溯码
     */
    private String traceabilityCode;
    /**
     * 发起人id
     */
    private String startUserId;
    /**
     * 发起人部门id
     */
    private Long startDeptId;
    /**
     * 团队编号
     */
    private String teamCode;

    /**
     * 操作类型
     * change人员变更，repayment退费重缴费,retired退赛
     */
    private String operationType;
}
