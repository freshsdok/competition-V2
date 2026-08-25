package com.teaching.system.domain.vo.invoice;

import lombok.Data;

@Data
public class InvoiceAmountReq {

    //订单主键id
    private Long orderId;

    //学员id
    private Long userId;

}
