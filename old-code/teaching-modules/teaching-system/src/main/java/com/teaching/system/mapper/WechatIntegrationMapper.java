package com.teaching.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import com.teaching.system.domain.WechatIntegration;

/**
 * 微信集成Mapper接口
 * 
 * @author teaching
 */
@Mapper
public interface WechatIntegrationMapper
{
    /**
     * 查询微信集成
     * 
     * @param id 微信集成主键
     * @return 微信集成
     */
    public WechatIntegration selectWechatIntegrationById(Long id);

    /**
     * 查询微信集成列表
     * 
     * @param wechatIntegration 微信集成
     * @return 微信集成集合
     */
    public List<WechatIntegration> selectWechatIntegrationList(WechatIntegration wechatIntegration);

    /**
     * 新增微信集成
     * 
     * @param wechatIntegration 微信集成
     * @return 结果
     */
    public int insertWechatIntegration(WechatIntegration wechatIntegration);

    /**
     * 修改微信集成
     * 
     * @param wechatIntegration 微信集成
     * @return 结果
     */
    public int updateWechatIntegration(WechatIntegration wechatIntegration);

    /**
     * 删除微信集成
     * 
     * @param id 微信集成主键
     * @return 结果
     */
    public int deleteWechatIntegrationById(Long id);

    /**
     * 批量删除微信集成
     * 
     * @param ids 需要删除的数据主键集合
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
