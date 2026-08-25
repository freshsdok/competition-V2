package com.teaching.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.teaching.system.domain.WechatIntegration;
import com.teaching.system.mapper.WechatIntegrationMapper;
import com.teaching.system.service.IWechatIntegrationService;

/**
 * 微信集成服务层实现
 * 
 * @author teaching
 */
@Service
public class WechatIntegrationServiceImpl implements IWechatIntegrationService
{
    @Autowired
    private WechatIntegrationMapper wechatIntegrationMapper;

    /**
     * 查询微信集成信息
     * 
     * @param id 微信集成ID
     * @return 微信集成信息
     */
    @Override
    public WechatIntegration selectWechatIntegrationById(Long id)
    {
        return wechatIntegrationMapper.selectWechatIntegrationById(id);
    }

    /**
     * 查询微信集成列表
     * 
     * @param wechatIntegration 微信集成信息
     * @return 微信集成集合
     */
    @Override
    public List<WechatIntegration> selectWechatIntegrationList(WechatIntegration wechatIntegration)
    {
        return wechatIntegrationMapper.selectWechatIntegrationList(wechatIntegration);
    }

    /**
     * 新增微信集成
     * 
     * @param wechatIntegration 微信集成信息
     * @return 结果
     */
    @Override
    public int insertWechatIntegration(WechatIntegration wechatIntegration)
    {
        return wechatIntegrationMapper.insertWechatIntegration(wechatIntegration);
    }

    /**
     * 修改微信集成
     * 
     * @param wechatIntegration 微信集成信息
     * @return 结果
     */
    @Override
    public int updateWechatIntegration(WechatIntegration wechatIntegration)
    {
        return wechatIntegrationMapper.updateWechatIntegration(wechatIntegration);
    }

    /**
     * 删除微信集成信息
     * 
     * @param id 微信集成ID
     * @return 结果
     */
    @Override
    public int deleteWechatIntegrationById(Long id)
    {
        return wechatIntegrationMapper.deleteWechatIntegrationById(id);
    }

    /**
     * 批量删除微信集成信息
     * 
     * @param ids 需要删除的微信集成ID
     * @return 结果
     */
    @Override
    public int deleteWechatIntegrationByIds(Long[] ids)
    {
        return wechatIntegrationMapper.deleteWechatIntegrationByIds(ids);
    }


    /**
     * 重置查询条件
     * 
     * @return 结果
     */
    @Override
    public int resetWechatIntegrationQuery()
    {
        return wechatIntegrationMapper.resetWechatIntegrationQuery();
    }
}
