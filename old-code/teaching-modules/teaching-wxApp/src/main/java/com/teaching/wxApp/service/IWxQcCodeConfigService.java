package com.teaching.wxApp.service;

import com.teaching.wxApp.domain.WxQcCodeConfig;

import java.util.List;

/**
 * 二维码配置Service接口
 *
 * @author teaching
 * @date 2026-04-08
 */
public interface IWxQcCodeConfigService {

    /**
     * 查询二维码配置
     *
     * @param codeConfigId 二维码配置主键
     * @return 二维码配置
     */
    public WxQcCodeConfig selectWxQcCodeConfigByCodeConfigId(Long codeConfigId);

    /**
     * 查询二维码配置列表
     *
     * @param wxQcCodeConfig 二维码配置
     * @return 二维码配置集合
     */
    public List<WxQcCodeConfig> selectWxQcCodeConfigList(WxQcCodeConfig wxQcCodeConfig);

    /**
     * 新增二维码配置
     *
     * @param wxQcCodeConfig 二维码配置
     * @return 结果
     */
    public int insertWxQcCodeConfig(WxQcCodeConfig wxQcCodeConfig);

    /**
     * 修改二维码配置
     *
     * @param wxQcCodeConfig 二维码配置
     * @return 结果
     */
    public int updateWxQcCodeConfig(WxQcCodeConfig wxQcCodeConfig);

    /**
     * 批量删除二维码配置
     *
     * @param codeConfigIds 需要删除的二维码配置主键集合
     * @return 结果
     */
    public int deleteWxQcCodeConfigByCodeConfigIds(Long[] codeConfigIds);

    /**
     * 删除二维码配置信息
     *
     * @param codeConfigId 二维码配置主键
     * @return 结果
     */
    public int deleteWxQcCodeConfigByCodeConfigId(Long codeConfigId);
}
