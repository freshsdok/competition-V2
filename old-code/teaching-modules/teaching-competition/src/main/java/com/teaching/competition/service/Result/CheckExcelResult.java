package com.teaching.competition.service.Result;

import java.io.Serializable;

/**
 * 校验结果
 */
public class CheckExcelResult implements Serializable {

    private Integer code;

    private String message;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
