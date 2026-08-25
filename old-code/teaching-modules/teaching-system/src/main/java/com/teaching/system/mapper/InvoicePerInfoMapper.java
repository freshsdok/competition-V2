package com.teaching.system.mapper;

import java.util.List;
import java.util.Map;

import com.teaching.system.domain.InvoicePerInfo;

/**
 * 开票信息记录Mapper接口
 * 
 * @author teaching
 * @date 2025-12-10
 */
public interface InvoicePerInfoMapper 
{
    /**
     * 查询开票信息记录
     * 
     * @param id 开票信息记录主键
     * @return 开票信息记录
     */
    public InvoicePerInfo selectInvoicePerInfoById(Long id);

    /**
     * 查询开票信息记录列表
     * 
     * @param invoicePerInfo 开票信息记录
     * @return 开票信息记录集合
     */
    public List<InvoicePerInfo> selectInvoicePerInfoList(InvoicePerInfo invoicePerInfo);

    public List<Map<String,Object>> selectInvoicePerInfo(InvoicePerInfo invoicePerInfo);

    /**
     * 新增开票信息记录
     * 
     * @param invoicePerInfo 开票信息记录
     * @return 结果
     */
    public int insertInvoicePerInfo(InvoicePerInfo invoicePerInfo);

    /**
     * 修改开票信息记录
     * 
     * @param invoicePerInfo 开票信息记录
     * @return 结果
     */
    public int updateInvoicePerInfo(InvoicePerInfo invoicePerInfo);

    /**
     * 删除开票信息记录
     * 
     * @param id 开票信息记录主键
     * @return 结果
     */
    public int deleteInvoicePerInfoById(Long id);

    /**
     * 批量删除开票信息记录
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteInvoicePerInfoByIds(Long[] ids);
}
