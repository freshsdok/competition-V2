package com.teaching.system.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.CharUtil;
import cn.hutool.core.util.ObjectUtil;
import com.teaching.common.core.exception.ServiceException;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.security.utils.DictUtils;
import com.teaching.system.domain.MerchantWorkScope;
import com.teaching.system.mapper.MerchantWorkScopeMapper;
import com.teaching.system.service.ISysDictDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.teaching.system.mapper.MerchantParamConfigMapper;
import com.teaching.system.domain.MerchantParamConfig;
import com.teaching.system.service.IMerchantParamConfigService;
import org.springframework.transaction.annotation.Transactional;

import static com.teaching.common.core.constant.DictConstant.FEE_TYPE;
import static com.teaching.common.core.constant.DictConstant.INVOICE_GOODS_CODE;

/**
 * 商户参数配置（支付和发票）Service业务层处理
 * 
 * @author teaching
 * @date 2025-12-23
 */
@Service
public class MerchantParamConfigServiceImpl implements IMerchantParamConfigService 
{
    @Autowired
    private MerchantParamConfigMapper merchantParamConfigMapper;

    @Autowired
    private MerchantWorkScopeMapper merchantWorkScopeMapper;

    @Autowired
    private ISysDictDataService sysDictDataService;

    /**
     * 查询商户参数配置（支付和发票）
     * 
     * @param id 商户参数配置（支付和发票）主键
     * @return 商户参数配置（支付和发票）
     */
    @Override
    public MerchantParamConfig selectMerchantParamConfigById(Long id)
    {
        MerchantParamConfig merchantParamConfig = merchantParamConfigMapper.selectMerchantParamConfigById(id);
        if (ObjectUtil.isEmpty(merchantParamConfig)) {
            return null;
        }
        //作用范围
        List<MerchantWorkScope> workScopeList = merchantWorkScopeMapper.selectListByConfigId(id);
        merchantParamConfig.setWorkScopeList(workScopeList);
        //发票内容
        String invoiceContent = merchantParamConfig.getInvoiceContent();
        if (ObjectUtil.isNotEmpty(invoiceContent)) {
            List<Map<String,String>> contentMapList = new ArrayList<>();
            String[] split = invoiceContent.split(",");
            for (String content : split) {
                if (content.contains("&")) {
                    String[] split1 = content.split("&");
                    String goodsLabel = sysDictDataService.selectDictLabel(INVOICE_GOODS_CODE, split1[0]);
                    String feeTypeLabel = sysDictDataService.selectDictLabel(FEE_TYPE, split1[1]);
                    Map<String, String> map = new HashMap<>();
                    map.put(content,goodsLabel+feeTypeLabel);
                    contentMapList.add(map);
                }
            }
            merchantParamConfig.setContentMapList(contentMapList);
        }
        return merchantParamConfig;
    }

    /**
     * 查询商户参数配置（支付和发票）列表
     * 
     * @param merchantParamConfig 商户参数配置（支付和发票）
     * @return 商户参数配置（支付和发票）
     */
    @Override
    public List<MerchantParamConfig> selectMerchantParamConfigList(MerchantParamConfig merchantParamConfig)
    {
        return merchantParamConfigMapper.selectMerchantParamConfigList(merchantParamConfig);
    }

    /**
     * 新增商户参数配置（支付和发票）
     * 
     * @param merchantParamConfig 商户参数配置（支付和发票）
     * @return 结果
     */
    @Override
    @Transactional
    public int insertMerchantParamConfig(MerchantParamConfig merchantParamConfig)
    {
        MerchantParamConfig selectMerConfig = merchantParamConfigMapper.selectMerchantParamConfigByMerId(merchantParamConfig.getMerId());
        if (selectMerConfig != null) {
            throw new ServiceException("当前商户号已配置，不能再次配置");
        }
        MerchantParamConfig selectTaxNumConfig = merchantParamConfigMapper.selectMerchantParamConfigByTaxNum(merchantParamConfig.getTaxNum());
        if (selectTaxNumConfig != null) {
            throw new ServiceException("当前税号已配置，不能再次配置");
        }
        merchantParamConfig.setCreateTime(DateUtils.getNowDate());
        int i = merchantParamConfigMapper.insertMerchantParamConfig(merchantParamConfig);
        //作用范围不为空，更新对应数据
        List<MerchantWorkScope> workScopeList = merchantParamConfig.getWorkScopeList();
        addNewWorkScope(merchantParamConfig, workScopeList);
        //跟新发票内容的值
        updateInvoiceContent(merchantParamConfig);
        return i;
    }

    /**
     * 更新发票内容
     * @param merchantParamConfig
     */
    private static void updateInvoiceContent(MerchantParamConfig merchantParamConfig) {
        //发票内容处理
        if (CollUtil.isNotEmpty(merchantParamConfig.getContentList())) {
            StringBuilder builder = new StringBuilder();
            merchantParamConfig.getContentList().forEach(e -> {
                builder.append(e).append(",");
            });
            String substring = builder.substring(0, builder.length() - 1);
            merchantParamConfig.setInvoiceContent(substring);
        }
    }

    /**
     * 新增作用范围
     * @param merchantParamConfig
     * @param workScopeList
     */
    private void addNewWorkScope(MerchantParamConfig merchantParamConfig, List<MerchantWorkScope> workScopeList) {
        if (CollUtil.isNotEmpty(merchantParamConfig.getWorkScopeList())) {
            workScopeList.forEach(e->{
                e.setConfigId(merchantParamConfig.getId());
                e.setOrgId(merchantParamConfig.getOrgId());
                e.setUserId(merchantParamConfig.getUserId());
                e.setDelFlag("0");
                e.setCreateTime(DateUtils.getNowDate());
            });
            merchantWorkScopeMapper.insertMerchantWorkScopeBatch(workScopeList);
        }
    }

    /**
     * 修改商户参数配置（支付和发票）
     * 
     * @param merchantParamConfig 商户参数配置（支付和发票）
     * @return 结果
     */
    @Override
    @Transactional
    public int updateMerchantParamConfig(MerchantParamConfig merchantParamConfig)
    {
        MerchantParamConfig selectMerConfig = merchantParamConfigMapper.selectMerchantParamConfigByMerId(merchantParamConfig.getMerId());
        if (selectMerConfig != null && selectMerConfig.getId().longValue() != merchantParamConfig.getId().longValue()) {
            throw new ServiceException("当前商户号已配置，不能再次配置");
        }
        MerchantParamConfig selectTaxNumConfig = merchantParamConfigMapper.selectMerchantParamConfigByTaxNum(merchantParamConfig.getTaxNum());
        if (selectTaxNumConfig != null  && selectTaxNumConfig.getId().longValue() != merchantParamConfig.getId().longValue()) {
            throw new ServiceException("当前税号已配置，不能再次配置");
        }
        //跟新关联作用范围
        List<MerchantWorkScope> workScopeList = merchantParamConfig.getWorkScopeList();
        if(CollUtil.isNotEmpty(workScopeList)){
            //更新关联信息，先删除原有的，再新增新的
            merchantWorkScopeMapper.deleteByConfigId(merchantParamConfig.getId());
            addNewWorkScope(merchantParamConfig, workScopeList);
        }
        //更新开票内容
        updateInvoiceContent(merchantParamConfig);
        //更新配置信息
        merchantParamConfig.setUpdateTime(DateUtils.getNowDate());
        return merchantParamConfigMapper.updateMerchantParamConfig(merchantParamConfig);
    }

    /**
     * 批量删除商户参数配置（支付和发票）
     * 
     * @param ids 需要删除的商户参数配置（支付和发票）主键
     * @return 结果
     */
    @Override
    public int deleteMerchantParamConfigByIds(Long[] ids)
    {
        return merchantParamConfigMapper.deleteMerchantParamConfigByIds(ids);
    }

    /**
     * 删除商户参数配置（支付和发票）信息
     * 
     * @param id 商户参数配置（支付和发票）主键
     * @return 结果
     */
    @Override
    public int deleteMerchantParamConfigById(Long id)
    {
        return merchantParamConfigMapper.deleteMerchantParamConfigById(id);
    }

    @Override
    public int changeStatus(Long id, int status) {
        return merchantParamConfigMapper.changeStatus(id, status);
    }

    @Override
    public MerchantParamConfig getConfig(String categoryCode, Long eventId) {
        //获取开启状态的配置列表-按创建时间倒序排列
        List<MerchantParamConfig> merchantParamConfigs = merchantParamConfigMapper.selectWorkingConfigList();
        if(CollUtil.isEmpty(merchantParamConfigs)){
            throw new ServiceException("没有查询到该订单的商户配置信息，请联系管理员配置");
        }
        for (MerchantParamConfig merchantParamConfig : merchantParamConfigs) {
            //根据配置id和类型读取作用范围列表
            List<MerchantWorkScope> merchantWorkScopes = merchantWorkScopeMapper.selectListByConfigIdAndCategory(merchantParamConfig.getId(),categoryCode);
            if(CollUtil.isEmpty(merchantWorkScopes)){
                continue;
            }
            //如果只有一个，可能是配置的一级作用范围或者只配置了一个二级事项
            if (merchantWorkScopes.size() == 1) {
                MerchantWorkScope merchantWorkScope = merchantWorkScopes.get(0);
                //如果没有具体的二级事项id，就是配置的的一级作用范围，直接使用该配置
                if (ObjectUtil.isEmpty(merchantWorkScope.getEventId())) {
                    return merchantParamConfig;
                    //有具体的二级事项id，判断是否匹配,匹配则使用该配置
                }else if(eventId.intValue() == merchantWorkScope.getEventId().intValue()){
                    return merchantParamConfig;
                }else {
                    continue;
                }
            }
            //如果作用范围有多个，说明配置的是二级事项，获取所有二级事项的id集合，判断当前id是否在配置范围内
            if (merchantWorkScopes.size() > 1) {
                List<Long> list = merchantWorkScopes.stream().map(MerchantWorkScope::getEventId).toList();
                if (list.contains(eventId)) {
                    return merchantParamConfig;
                }
            }
        }
        throw new ServiceException("没有查询到该订单的商户配置信息，请联系管理员配置！");
    }

    @Override
    public MerchantParamConfig selectMerchantParamConfigByMerId(String merId) {
        MerchantParamConfig merchantParamConfig = merchantParamConfigMapper.selectMerchantParamConfigByMerId(merId);
        if (ObjectUtil.isEmpty(merchantParamConfig)) {
            throw new ServiceException("没有查询到该商户的配置信息，请联系管理员配置！");
        }
        return merchantParamConfig;
    }

    @Override
    public MerchantParamConfig selectMerchantParamConfigByTaxNum(String taxNum) {
        MerchantParamConfig merchantParamConfig = merchantParamConfigMapper.selectMerchantParamConfigByTaxNum(taxNum);
        if (ObjectUtil.isEmpty(merchantParamConfig)) {
            throw new ServiceException("没有查询到该商户的配置信息，请联系管理员配置！");
        }
        return merchantParamConfig;
    }

    @Override
    public List<Map<String, String>> merSelect() {
        return merchantParamConfigMapper.merSelect();
    }

}
