package com.teaching.system.mapper;

import java.util.List;
import com.teaching.system.domain.InvoiceInfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 发票信息Mapper接口
 * 
 * @author teaching
 * @date 2025-10-28
 */
@Mapper
public interface InvoiceInfoMapper 
{
    /**
     * 查询发票信息
     * 
     * @param id 发票信息主键
     * @return 发票信息
     */
    public InvoiceInfo selectInvoiceInfoById(Long id);

    /**
     * 查询发票信息列表
     * 
     * @param invoiceInfo 发票信息
     * @return 发票信息集合
     */
    public List<InvoiceInfo> selectInvoiceInfoList(InvoiceInfo invoiceInfo);

    /**
     * 新增发票信息
     * 
     * @param invoiceInfo 发票信息
     * @return 结果
     */
    public int insertInvoiceInfo(InvoiceInfo invoiceInfo);

    /**
     * 修改发票信息
     * 
     * @param invoiceInfo 发票信息
     * @return 结果
     */
    public int updateInvoiceInfo(InvoiceInfo invoiceInfo);

    /**
     * 删除发票信息
     * 
     * @param id 发票信息主键
     * @return 结果
     */
    public int deleteInvoiceInfoById(Long id);

    /**
     * 批量删除发票信息
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteInvoiceInfoByIds(Long[] ids);

    InvoiceInfo selectInvoiceInfoByOrderId(String orderId);

    InvoiceInfo selectInvoiceInfoBySerialNo(String serialNo);

    List<InvoiceInfo> syncInvoiceResult();
}
