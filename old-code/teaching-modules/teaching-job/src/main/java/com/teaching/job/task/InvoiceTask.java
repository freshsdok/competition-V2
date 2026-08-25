package com.teaching.job.task;

import com.teaching.common.core.constant.SecurityConstants;
import com.teaching.system.api.OrderService;
import com.teaching.system.api.RemoteInvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 定时任务调度测试
 * 
 * @author teaching
 */
@Component("invoiceTask")
public class InvoiceTask
{

    @Autowired
    private RemoteInvoiceService invoiceService;


    public void ryOrderPayTask(){
        invoiceService.invoiceTask(SecurityConstants.INNER);
    }

}
