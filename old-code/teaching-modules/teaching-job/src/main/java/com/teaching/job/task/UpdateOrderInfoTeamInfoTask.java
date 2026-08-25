package com.teaching.job.task;

import com.teaching.common.core.constant.SecurityConstants;
import com.teaching.system.api.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("updateOrderInfoTeamInfoTask")
public class UpdateOrderInfoTeamInfoTask {

    private static final Logger logger = LoggerFactory.getLogger(UpdateOrderInfoTeamInfoTask.class);

    @Autowired
    private OrderService orderService;

    public void updateTeamInfo(String updateSize) {
        logger.info("开始同步团队信息");
        try {
            orderService.updateTeamInfo(updateSize, SecurityConstants.INNER);
        } catch (Exception e) {
            logger.error("同步团队失败:"+e);
        }
    }
}
