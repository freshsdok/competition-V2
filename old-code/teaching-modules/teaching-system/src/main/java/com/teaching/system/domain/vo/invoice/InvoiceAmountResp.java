package com.teaching.system.domain.vo.invoice;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class InvoiceAmountResp {

    //商户号
    private String merId;

    //收款单位
    private String merName;

    //税号
    private String taxNum;

    //开票金额
    private BigDecimal invoiceAmount;

    //发票内容
    private List<Map<String,String>> invoiceContent;

    //关联人员id
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private List<Long> userIds;

    //关联订单id
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private List<Long> orderIds;

    /**
     * 团队编号
     */
    private String teamCodes;

    /**
     * 团队人员信息
     */
    private List<Map<String,Object>> members;

    /**
     * 证书申请规则id
     */
    private String certRuleId;

    /**
     * 证书申请信息
     */
    private List<Map<String,Object>> certApplyInfo;

    /**
     * 证书申请备注信息
     */
    private List<Map<String,Object>> certApplyRemarkInfo;

    /**
     * 商品类型
     */
    private String commodityType;
}
