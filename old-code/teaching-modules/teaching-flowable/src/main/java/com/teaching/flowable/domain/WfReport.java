package com.teaching.flowable.domain;


import com.teaching.common.core.annotation.Excel;

import java.io.Serializable;
import java.util.List;

/**
 * @author Administrator
 */
public class WfReport implements Serializable {
    private String lcKey;
    @Excel(name = "企业名称", width = 30)
    private String deptName;
    @Excel(name = "流程名称", width = 17)
    private String lcName;
    @Excel(name = "流程版本", width = 10)
    private String lcVersion;
    private String defId;
    @Excel(name = "节点名称", width = 17)
    private String nodeName;
    @Excel(name = "节点审批人", width = 10)
    private String assigneeName;
    private String startTime;
    private String endTime;
    @Excel(name = "审批耗时(分钟)", width = 17)
    private String durationMinute;
    private String duration;
    @Excel(name = "审批耗时", width = 17)
    private String durationFormat;
    @Excel(name = "实例编号", width = 35)
    private String instId;
    private List<WfReportNode> nodeList;

    public String getDurationMinute() {
        return durationMinute;
    }

    public void setDurationMinute(String durationMinute) {
        this.durationMinute = durationMinute;
    }

    public String getDurationFormat() {
        return durationFormat;
    }

    public void setDurationFormat(String durationFormat) {
        this.durationFormat = durationFormat;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getAssigneeName() {
        return assigneeName;
    }

    public void setAssigneeName(String assigneeName) {
        this.assigneeName = assigneeName;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public String getLcKey() {
        return lcKey;
    }

    public void setLcKey(String lcKey) {
        this.lcKey = lcKey;
    }

    public String getLcName() {
        return lcName;
    }

    public void setLcName(String lcName) {
        this.lcName = lcName;
    }

    public String getLcVersion() {
        return lcVersion;
    }

    public void setLcVersion(String lcVersion) {
        this.lcVersion = lcVersion;
    }

    public String getDefId() {
        return defId;
    }

    public void setDefId(String defId) {
        this.defId = defId;
    }

    public String getInstId() {
        return instId;
    }

    public void setInstId(String instId) {
        this.instId = instId;
    }

    public List<WfReportNode> getNodeList() {
        return nodeList;
    }

    public void setNodeList(List<WfReportNode> nodeList) {
        this.nodeList = nodeList;
    }
}
