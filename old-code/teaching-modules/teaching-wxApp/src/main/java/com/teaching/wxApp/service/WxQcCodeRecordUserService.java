package com.teaching.wxApp.service;

import com.teaching.wxApp.domain.WxQcCodeConfig;

import java.util.List;

/**
 * 二维码生成记录用户服务
 */
public interface WxQcCodeRecordUserService {

    /**
     * 查询二维码生成记录用户列表
     *
     * @return 二维码生成记录用户列表
     */
    public List<WxQcCodeConfig> selectWxQcCodeConfigList(WxQcCodeConfig wxQcCodeConfig);

    /**
     * 按用户组查询二维码生成记录用户列表
     *
     * @param wxQcCodeConfig
     * @return
     */
    public List<WxQcCodeConfig> selectWxQcCodeConfigPcList(WxQcCodeConfig wxQcCodeConfig);
}
