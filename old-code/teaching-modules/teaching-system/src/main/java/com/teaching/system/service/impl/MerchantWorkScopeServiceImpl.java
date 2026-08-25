package com.teaching.system.service.impl;

import java.util.List;
import com.teaching.common.core.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.teaching.system.mapper.MerchantWorkScopeMapper;
import com.teaching.system.domain.MerchantWorkScope;
import com.teaching.system.service.IMerchantWorkScopeService;

/**
 * 商户作用范围Service业务层处理
 * 
 * @author teaching
 * @date 2025-12-23
 */
@Service
public class MerchantWorkScopeServiceImpl implements IMerchantWorkScopeService 
{
    @Autowired
    private MerchantWorkScopeMapper merchantWorkScopeMapper;

    /**
     * 查询商户作用范围
     * 
     * @param id 商户作用范围主键
     * @return 商户作用范围
     */
    @Override
    public MerchantWorkScope selectMerchantWorkScopeById(Long id)
    {
        return merchantWorkScopeMapper.selectMerchantWorkScopeById(id);
    }

    /**
     * 查询商户作用范围列表
     * 
     * @param merchantWorkScope 商户作用范围
     * @return 商户作用范围
     */
    @Override
    public List<MerchantWorkScope> selectMerchantWorkScopeList(MerchantWorkScope merchantWorkScope)
    {
        return merchantWorkScopeMapper.selectMerchantWorkScopeList(merchantWorkScope);
    }

    /**
     * 新增商户作用范围
     * 
     * @param merchantWorkScope 商户作用范围
     * @return 结果
     */
    @Override
    public int insertMerchantWorkScope(MerchantWorkScope merchantWorkScope)
    {
        merchantWorkScope.setCreateTime(DateUtils.getNowDate());
        return merchantWorkScopeMapper.insertMerchantWorkScope(merchantWorkScope);
    }

    /**
     * 修改商户作用范围
     * 
     * @param merchantWorkScope 商户作用范围
     * @return 结果
     */
    @Override
    public int updateMerchantWorkScope(MerchantWorkScope merchantWorkScope)
    {
        merchantWorkScope.setUpdateTime(DateUtils.getNowDate());
        return merchantWorkScopeMapper.updateMerchantWorkScope(merchantWorkScope);
    }

    /**
     * 批量删除商户作用范围
     * 
     * @param ids 需要删除的商户作用范围主键
     * @return 结果
     */
    @Override
    public int deleteMerchantWorkScopeByIds(Long[] ids)
    {
        return merchantWorkScopeMapper.deleteMerchantWorkScopeByIds(ids);
    }

    /**
     * 删除商户作用范围信息
     * 
     * @param id 商户作用范围主键
     * @return 结果
     */
    @Override
    public int deleteMerchantWorkScopeById(Long id)
    {
        return merchantWorkScopeMapper.deleteMerchantWorkScopeById(id);
    }
}
