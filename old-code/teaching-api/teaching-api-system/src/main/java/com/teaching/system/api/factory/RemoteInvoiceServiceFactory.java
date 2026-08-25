package com.teaching.system.api.factory;

import com.teaching.common.core.domain.R;
import com.teaching.system.api.OrderService;
import com.teaching.system.api.RemoteInvoiceService;
import com.teaching.system.api.domain.OrderInfo;
import com.teaching.system.api.domain.SelectOrderStatusReq;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 赛事服务降级处理
 *
 * @author teaching
 */
@Component
public class RemoteInvoiceServiceFactory implements FallbackFactory<RemoteInvoiceService> {

    private static final Logger log = LoggerFactory.getLogger(RemoteInvoiceServiceFactory.class);

    @Override
    public RemoteInvoiceService create(Throwable cause) {
        return new RemoteInvoiceService(){
            @Override
            public R<Void> invoiceTask(String source) {
                return R.fail("发票状态同步失败:" + cause.getMessage());
            }
        };
    }
}
