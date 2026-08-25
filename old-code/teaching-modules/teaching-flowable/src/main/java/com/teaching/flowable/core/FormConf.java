package com.teaching.flowable.core;

import lombok.Data;

import java.util.Map;

/**
 * 表单属性类
 *
 * @author KonBAI
 * @createTime 2022/8/6 18:54
 */
@Data
public class FormConf {

    /**
     * 标题
     */
    private String title;

    /**
     * 禁用表单
     */
    private Boolean disabled = false;
    /**
     * 表单按钮
     */
    private Boolean formBtns = true;

    /**
     * 表单模型-pc
     */
    private Map<String, Object> formModel;
    /**
     * 表单模型 --移动端
     */
    private Map<String, Object> formModelMobile;

    /**
     * 表单数据
     */
    private Map<String, Object> formData;
    /**
     * 流程名称
     */
    private String processName;
}
