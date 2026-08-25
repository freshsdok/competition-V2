package com.teaching.job.task;

import com.teaching.common.core.constant.SecurityConstants;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.system.api.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 定时任务调度测试
 * 
 * @author teaching
 */
@Component("orderTask")
public class OrderStatementTask
{

    @Autowired
    private OrderService orderService;

    public void ryOrderStatement(){
        String billDate = LocalDate.now().minusDays(1).toString();

        //查询前一天的对账单对执行对账任务
        orderService.statementTask(billDate, SecurityConstants.INNER);
    }

    public void ryOrderPayTask(){
        orderService.payTask(SecurityConstants.INNER);
    }

    public void ryOrderRefundTask(){
        orderService.refundTask(SecurityConstants.INNER);
    }
}
