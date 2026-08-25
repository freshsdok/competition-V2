package com.teaching.system.api.domain;


import java.util.List;

public class SelectOrderStatusReq {

    private List<Long> commodityIds;

    private String commodityType;

    private String payStatus;


    public List<Long> getCommodityIds() {
        return commodityIds;
    }

    public void setCommodityIds(List<Long> commodityIds) {
        this.commodityIds = commodityIds;
    }

    public String getCommodityType() {
        return commodityType;
    }

    public void setCommodityType(String commodityType) {
        this.commodityType = commodityType;
    }

    public String getPayStatus() {
        return payStatus;
    }

    public void setPayStatus(String payStatus) {
        this.payStatus = payStatus;
    }
}
