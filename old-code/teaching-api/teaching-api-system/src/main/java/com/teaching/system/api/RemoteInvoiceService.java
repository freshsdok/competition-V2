package com.teaching.system.api;


import com.teaching.common.core.constant.SecurityConstants;
import com.teaching.common.core.constant.ServiceNameConstants;
import com.teaching.common.core.domain.R;
import com.teaching.system.api.domain.OrderInfo;
import com.teaching.system.api.domain.SelectOrderStatusReq;
import com.teaching.system.api.factory.OrderServiceFactory;
import com.teaching.system.api.factory.RemoteInvoiceServiceFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(contextId = "invoiceInfoService", value = ServiceNameConstants.SYSTEM_SERVICE, fallbackFactory = RemoteInvoiceServiceFactory.class)
public interface RemoteInvoiceService {

    /**
     * 新增订单信息
     */
    @GetMapping("/invoice/syncInvoiceResult")
    R<Void> invoiceTask(@RequestHeader(SecurityConstants.FROM_SOURCE) String source);


}
