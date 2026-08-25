package com.teaching.system.api.domain;

import java.math.BigDecimal;

public class NumDetail {

    private String name;

    private Integer num;

    private String price;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getNum() {
        return num;
    }

    public void setNum(Integer num) {
        this.num = num;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public NumDetail() {
    }

    public NumDetail(String name, Integer num, String price) {
        this.name = name;
        this.num = num;
        this.price = price;
    }
}
