package com.teaching.system.service;

import java.util.List;
import java.util.Map;

import com.teaching.system.domain.MerchantParamConfig;

/**
 * 商户参数配置（支付和发票）Service接口
 * 
 * @author teaching
 * @date 2025-12-23
 */
public interface IMerchantParamConfigService 
{
    /**
     * 查询商户参数配置（支付和发票）
     * 
     * @param id 商户参数配置（支付和发票）主键
     * @return 商户参数配置（支付和发票）
     */
    public MerchantParamConfig selectMerchantParamConfigById(Long id);

    /**
     * 查询商户参数配置（支付和发票）列表
     * 
     * @param merchantParamConfig 商户参数配置（支付和发票）
     * @return 商户参数配置（支付和发票）集合
     */
    public List<MerchantParamConfig> selectMerchantParamConfigList(MerchantParamConfig merchantParamConfig);

    /**
     * 新增商户参数配置（支付和发票）
     * 
     * @param merchantParamConfig 商户参数配置（支付和发票）
     * @return 结果
     */
    public int insertMerchantParamConfig(MerchantParamConfig merchantParamConfig);

    /**
     * 修改商户参数配置（支付和发票）
     * 
     * @param merchantParamConfig 商户参数配置（支付和发票）
     * @return 结果
     */
    public int updateMerchantParamConfig(MerchantParamConfig merchantParamConfig);

    /**
     * 批量删除商户参数配置（支付和发票）
     * 
     * @param ids 需要删除的商户参数配置（支付和发票）主键集合
     * @return 结果
     */
    public int deleteMerchantParamConfigByIds(Long[] ids);

    /**
     * 删除商户参数配置（支付和发票）信息
     * 
     * @param id 商户参数配置（支付和发票）主键
     * @return 结果
     */
    public int deleteMerchantParamConfigById(Long id);

    int changeStatus(Long id, int status);

    MerchantParamConfig getConfig(String categoryCode, Long eventId);

    MerchantParamConfig selectMerchantParamConfigByMerId(String merId);

    MerchantParamConfig selectMerchantParamConfigByTaxNum(String taxNum);

    List<Map<String,String>> merSelect();
}
