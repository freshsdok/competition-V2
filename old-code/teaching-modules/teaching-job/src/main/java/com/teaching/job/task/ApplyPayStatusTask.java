package com.teaching.job.task;

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.alibaba.nacos.common.utils.StringUtils;
import com.teaching.common.core.constant.SecurityConstants;
import com.teaching.common.core.domain.R;
import com.teaching.system.api.CompetitionService;
import com.teaching.system.api.OrderService;
import com.teaching.system.api.domain.CompetitionApplyInfo;
import com.teaching.system.api.domain.OrderInfo;
import com.teaching.system.api.domain.SelectOrderStatusReq;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.compress.utils.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

// 支付状态数据同步
@Component("applyPayStatusTask")
public class ApplyPayStatusTask {

    private static final Logger logger = LoggerFactory.getLogger(ApplyPayStatusTask.class);

    @Autowired
    private CompetitionService competitionService;

    @Autowired
    private OrderService orderService;

    public void updateApplyPayStatusTask() {
        try {
            // 获取未支付成功的报名信息
            R<List<CompetitionApplyInfo>> CompetitionApplyInfoListR =
                    competitionService.selectCompetitionApplyInfoByPayStatus(SecurityConstants.INNER);
            if (R.isSuccess(CompetitionApplyInfoListR) && CollectionUtils.isNotEmpty(CompetitionApplyInfoListR.getData())) {
                List<CompetitionApplyInfo> CompetitionApplyInfoList = CompetitionApplyInfoListR.getData();
                List<Long> competitionSeriesIdList = CompetitionApplyInfoList.stream().distinct().
                        map(CompetitionApplyInfo::getCompetitionSeriesId).collect(Collectors.toList());
                List<List<Long>> partitionList = ListUtils.partition(competitionSeriesIdList, 100);
                for (List<Long> competitionSeriesIdListP : partitionList){
                    // 获取订单信息
                    SelectOrderStatusReq req = new SelectOrderStatusReq();
                    req.setCommodityIds(competitionSeriesIdListP);
                    R<List<OrderInfo>> OrderInfoListR = orderService.selectOrderStatus(req, SecurityConstants.INNER);
                    if (R.isSuccess(OrderInfoListR) && CollectionUtils.isNotEmpty(OrderInfoListR.getData())) {
                        List<OrderInfo> orderInfoList = OrderInfoListR.getData();
                        CompetitionApplyInfoList.stream().forEach(competitionApplyInfo -> {
                            orderInfoList.stream().forEach(orderInfo -> {
                                if(Long.parseLong(orderInfo.getCommodityId()) == competitionApplyInfo.getCompetitionSeriesId() &&
                                        Objects.equals(orderInfo.getUserId(), competitionApplyInfo.getUserId())){
                                    competitionApplyInfo.setPayStatus(orderInfo.getPayStatus());
                                }
                            });
                        });
                    }
                    // 每次更新一万条
                    List<CompetitionApplyInfo> competitionApplyInfoListRes = CompetitionApplyInfoList.stream().filter(competitionApplyInfo -> StringUtils.isNotEmpty(competitionApplyInfo.getPayStatus()))
                            .collect(Collectors.toList());
                    List<List<CompetitionApplyInfo>> updateCompetitionApplyPayStatusList =
                            ListUtils.partition(competitionApplyInfoListRes, 10000);
                    for (List<CompetitionApplyInfo> updateCompetitionApplyInfoList : updateCompetitionApplyPayStatusList){
                        competitionService.updatePayStatus(updateCompetitionApplyInfoList, SecurityConstants.INNER);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("报名支付状态同步失败:"+e);
        }
    }
}
