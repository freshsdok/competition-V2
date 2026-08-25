package com.teaching.system.service;

import java.util.List;
import java.util.Map;

import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.system.api.domain.OrderInfo;
import com.teaching.system.domain.InvoiceInfo;
import com.teaching.system.domain.vo.invoice.InvoiceAmountReq;
import com.teaching.system.domain.vo.invoice.InvoiceApplyReq;
import com.teaching.system.domain.vo.invoice.InvoiceQueryReq;

/**
 * 发票信息Service接口
 * 
 * @author teaching
 * @date 2025-10-28
 */
public interface IInvoiceInfoService 
{
    /**
     * 查询发票信息
     * 
     * @param id 发票信息主键
     * @return 发票信息
     */
    public InvoiceInfo selectInvoiceInfoById(Long id);

    public InvoiceInfo selectPersonalInvoiceById(Long id);

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
     * 批量删除发票信息
     * 
     * @param ids 需要删除的发票信息主键集合
     * @return 结果
     */
    public int deleteInvoiceInfoByIds(Long[] ids);

    /**
     * 删除发票信息信息
     * 
     * @param id 发票信息主键
     * @return 结果
     */
    public int deleteInvoiceInfoById(Long id);

    AjaxResult invoiceApply(InvoiceApplyReq applyReq);

    AjaxResult invoiceApplyNew(List<InvoiceApplyReq> applyReqList);

    Map<String,String> applyCallback(Map<String, String> notifyMap);

    AjaxResult deliveryInvoice(Long id);

    AjaxResult queryInvoiceResult(InvoiceQueryReq invoiceQueryReq);

    AjaxResult queryPersonalInvoiceResult(InvoiceQueryReq invoiceQueryReq);

    void syncInvoiceResult();

    AjaxResult reInvoice(Long id);

    AjaxResult fastInvoiceRed(Long id);

    AjaxResult queryTeamAndUserByOrderId(OrderInfo orderInfo);

    AjaxResult queryInvoiceAmount(List<InvoiceAmountReq> req);

    void pushUserInvoiceStatusAndUpdateOrderInvoiceStatus(String orderId,String status);

    // 查询赛证互通订单未开票信息
    List<Map<String,Object>> queryCompetitionCertExchangeRuleByOrderId(Long userId,String commodityType);
}
