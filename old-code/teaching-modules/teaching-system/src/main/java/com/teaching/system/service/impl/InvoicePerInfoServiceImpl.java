package com.teaching.system.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.teaching.system.mapper.InvoicePerInfoMapper;
import com.teaching.system.domain.InvoicePerInfo;
import com.teaching.system.service.IInvoicePerInfoService;

/**
 * 开票信息记录Service业务层处理
 * 
 * @author teaching
 * @date 2025-12-10
 */
@Service
public class InvoicePerInfoServiceImpl implements IInvoicePerInfoService 
{
    @Autowired
    private InvoicePerInfoMapper invoicePerInfoMapper;

    /**
     * 查询开票信息记录
     * 
     * @param id 开票信息记录主键
     * @return 开票信息记录
     */
    @Override
    public InvoicePerInfo selectInvoicePerInfoById(Long id)
    {
        return invoicePerInfoMapper.selectInvoicePerInfoById(id);
    }

    /**
     * 查询开票信息记录列表
     * 
     * @param invoicePerInfo 开票信息记录
     * @return 开票信息记录
     */
    @Override
    public List<InvoicePerInfo> selectInvoicePerInfoList(InvoicePerInfo invoicePerInfo) {
        return invoicePerInfoMapper.selectInvoicePerInfoList(invoicePerInfo);
    }

    @Override
    public List<Map<String, Object>> selectInvoicePerInfo(InvoicePerInfo invoicePerInfo) {
        return invoicePerInfoMapper.selectInvoicePerInfo(invoicePerInfo);
    }

    /**
     * 新增开票信息记录
     * 
     * @param invoicePerInfo 开票信息记录
     * @return 结果
     */
    @Override
    public int insertInvoicePerInfo(InvoicePerInfo invoicePerInfo)
    {
        return invoicePerInfoMapper.insertInvoicePerInfo(invoicePerInfo);
    }

    /**
     * 修改开票信息记录
     * 
     * @param invoicePerInfo 开票信息记录
     * @return 结果
     */
    @Override
    public int updateInvoicePerInfo(InvoicePerInfo invoicePerInfo)
    {
        return invoicePerInfoMapper.updateInvoicePerInfo(invoicePerInfo);
    }

    /**
     * 批量删除开票信息记录
     * 
     * @param ids 需要删除的开票信息记录主键
     * @return 结果
     */
    @Override
    public int deleteInvoicePerInfoByIds(Long[] ids)
    {
        return invoicePerInfoMapper.deleteInvoicePerInfoByIds(ids);
    }

    /**
     * 删除开票信息记录信息
     * 
     * @param id 开票信息记录主键
     * @return 结果
     */
    @Override
    public int deleteInvoicePerInfoById(Long id)
    {
        return invoicePerInfoMapper.deleteInvoicePerInfoById(id);
    }
}
