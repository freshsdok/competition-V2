package com.teaching.system.service;

import java.util.List;
import com.teaching.system.domain.MerchantWorkScope;

/**
 * 商户作用范围Service接口
 * 
 * @author teaching
 * @date 2025-12-23
 */
public interface IMerchantWorkScopeService 
{
    /**
     * 查询商户作用范围
     * 
     * @param id 商户作用范围主键
     * @return 商户作用范围
     */
    public MerchantWorkScope selectMerchantWorkScopeById(Long id);

    /**
     * 查询商户作用范围列表
     * 
     * @param merchantWorkScope 商户作用范围
     * @return 商户作用范围集合
     */
    public List<MerchantWorkScope> selectMerchantWorkScopeList(MerchantWorkScope merchantWorkScope);

    /**
     * 新增商户作用范围
     * 
     * @param merchantWorkScope 商户作用范围
     * @return 结果
     */
    public int insertMerchantWorkScope(MerchantWorkScope merchantWorkScope);

    /**
     * 修改商户作用范围
     * 
     * @param merchantWorkScope 商户作用范围
     * @return 结果
     */
    public int updateMerchantWorkScope(MerchantWorkScope merchantWorkScope);

    /**
     * 批量删除商户作用范围
     * 
     * @param ids 需要删除的商户作用范围主键集合
     * @return 结果
     */
    public int deleteMerchantWorkScopeByIds(Long[] ids);

    /**
     * 删除商户作用范围信息
     * 
     * @param id 商户作用范围主键
     * @return 结果
     */
    public int deleteMerchantWorkScopeById(Long id);
}
