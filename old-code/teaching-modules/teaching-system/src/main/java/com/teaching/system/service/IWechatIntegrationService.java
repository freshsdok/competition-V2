package com.teaching.system.service;

import java.util.List;
import com.teaching.system.domain.WechatIntegration;

/**
 * 微信集成服务层
 * 
 * @author teaching
 */
public interface IWechatIntegrationService
{
    /**
     * 查询微信集成信息
     * 
     * @param id 微信集成ID
     * @return 微信集成信息
     */
    public WechatIntegration selectWechatIntegrationById(Long id);

    /**
     * 查询微信集成列表
     * 
     * @param wechatIntegration 微信集成信息
     * @return 微信集成集合
     */
    public List<WechatIntegration> selectWechatIntegrationList(WechatIntegration wechatIntegration);

    /**
     * 新增微信集成
     * 
     * @param wechatIntegration 微信集成信息
     * @return 结果
     */
    public int insertWechatIntegration(WechatIntegration wechatIntegration);

    /**
     * 修改微信集成
     * 
     * @param wechatIntegration 微信集成信息
     * @return 结果
     */
    public int updateWechatIntegration(WechatIntegration wechatIntegration);

    /**
     * 删除微信集成信息
     * 
     * @param id 微信集成ID
     * @return 结果
     */
    public int deleteWechatIntegrationById(Long id);
    
    /**
     * 批量删除微信集成信息
     * 
     * @param ids 需要删除的微信集成ID
     * @return 结果
     */
    public int deleteWechatIntegrationByIds(Long[] ids);


    /**
     * 重置查询条件
     * 
     * @return 结果
     */
    public int resetWechatIntegrationQuery();
}
