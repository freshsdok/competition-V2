package com.teaching.system.mapper;

import java.util.List;
import java.util.Map;

import com.teaching.system.domain.MerchantParamConfig;
import com.teaching.system.domain.MerchantWorkScope;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商户参数配置（支付和发票）Mapper接口
 * 
 * @author teaching
 * @date 2025-12-23
 */
@Mapper
public interface MerchantParamConfigMapper 
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
     * 删除商户参数配置（支付和发票）
     * 
     * @param id 商户参数配置（支付和发票）主键
     * @return 结果
     */
    public int deleteMerchantParamConfigById(Long id);

    /**
     * 批量删除商户参数配置（支付和发票）
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteMerchantParamConfigByIds(Long[] ids);

    int changeStatus(Long id, int status);

    List<MerchantParamConfig> selectWorkingConfigList();

    MerchantParamConfig selectMerchantParamConfigByMerId(String merId);

    MerchantParamConfig selectMerchantParamConfigByTaxNum(String taxNum);

    List<Map<String, String>> merSelect();
}
