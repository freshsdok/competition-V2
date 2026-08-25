package com.teaching.competition.domain;

import java.util.List;

public class CompetitionChildren {

    private String id;

    private String label;

    /**
     * 赛事级别
     */
    private Integer sort;

    private List<CompetitionChildren> children;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<CompetitionChildren> getChildren() {
        return children;
    }

    public void setChildren(List<CompetitionChildren> children) {
        this.children = children;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }
}
