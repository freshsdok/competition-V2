package com.teaching.flowable.listener;

import cn.hutool.core.map.MapUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSONObject;
import com.teaching.common.core.JsonUtils;
import com.teaching.common.core.constant.SecurityConstants;
import com.teaching.common.core.domain.R;
import com.teaching.common.core.utils.SpringUtils;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.common.core.utils.bean.BeanUtils;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.system.api.CompetitionService;
import com.teaching.system.api.OrderService;
import com.teaching.system.api.domain.*;
import io.jsonwebtoken.lang.Collections;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 需要在流程中配置任务监听才能被调用
 * 创建退费订单监听器
 *
 * @author Administrator
 */
@Slf4j
@Component(value = "createRefundOrderListener")
public class createRefundOrderListener implements TaskListener {


    @Override
    public void notify(DelegateTask delegateTask) {
        System.out.println("进入新增退款单监听器...");
        // 获取任务的变量
        OrderService orderService = SpringUtils.getBean(OrderService.class);
        CompetitionService competitionService = SpringUtils.getBean(CompetitionService.class);
        Map<String, Object> variables = delegateTask.getVariables();
        //操作类型（change人员变更，repayment退费重缴费,retired退赛）
        TeamChangeDto teamChangeDto = new TeamChangeDto();
        teamChangeDto.setTaskId(delegateTask.getId());
        teamChangeDto.setProcessInstanceId(delegateTask.getProcessInstanceId());

        teamChangeDto.setChangeType(MapUtil.getStr(variables, "operationType"));
        teamChangeDto.setCommodityType(MapUtil.getStr(variables, "commodityType"));
        teamChangeDto.setTeamCode(MapUtil.getStr(variables, "teamCode"));
        String userIds = MapUtil.getStr(variables, "userIds");
        teamChangeDto.setUserIds(userIds);
        //原订单id-只有退费重缴才有值
        String payOrderId = MapUtil.getStr(variables, "payOrderId");
        teamChangeDto.setPayOrderId(payOrderId);
        String delUserIds = MapUtil.getStr(variables, "delUserIds");
        String addUserInfos = MapUtil.getStr(variables, "addUserInfos");
        // 增减人逻辑
        if(StringUtils.isNotEmpty(addUserInfos) || StringUtils.isNotEmpty(delUserIds)){
            //调用远端接口.获取商品详情
            Map<String, Object> reqMap = new HashMap<>();
            List<String> commodityIds = Arrays.asList(teamChangeDto.getTeamCode());
            reqMap.put("teamCodeList",commodityIds);
            R<List<CompetitionApplyInfoVO>> detailForOrder = competitionService.getDetailForOrder(reqMap, SecurityConstants.INNER);
            if (R.isSuccess(detailForOrder) && CollectionUtils.isNotEmpty(detailForOrder.getData())) {
                List<CompetitionApplyInfo> addUserInfoList;
                if(StringUtils.isNotEmpty(addUserInfos)){
                    addUserInfoList = JSONUtil.toList(addUserInfos, CompetitionApplyInfo.class);
                } else {
                    addUserInfoList = new ArrayList<>();
                }
                List<CompetitionApplyInfo> delUserInfoList = new ArrayList<>();
                if(StringUtils.isNotEmpty(delUserIds)){
                    List<Long> delUserIdList = Arrays.stream(delUserIds.split(",")).map(Long::parseLong).toList();
                    delUserIdList.stream().forEach(delUserId -> {
                        R<CompetitionApplyInfo> innerApplyDetailInfo = competitionService.getInnerApplyDetailInfo(delUserId, SecurityConstants.INNER);
                        if(R.isSuccess(innerApplyDetailInfo) && Objects.nonNull(innerApplyDetailInfo.getData())){
                            innerApplyDetailInfo.getData().setDelFlag("1");
                            delUserInfoList.add(innerApplyDetailInfo.getData());
                        }
                    });
                }
                List<CompetitionApplyInfoVO> data = detailForOrder.getData();
                List<RegistrationInfo> playersList = new ArrayList<>();
                Map<String, CompetitionApplyInfoVO> teamMap = data.stream().
                        collect(Collectors.toMap(CompetitionApplyInfoVO::getTeamCode, competitionApplyInfoVO -> competitionApplyInfoVO));
                CompetitionApplyInfoVO competitionApplyInfoVO = teamMap.get(teamChangeDto.getTeamCode());
                int teamMemberNum = 0;
                if(Objects.nonNull(competitionApplyInfoVO)){
                    if(CollectionUtils.isNotEmpty(delUserInfoList)){
                        delUserInfoList.stream().forEach(delUserInfo -> {
                            RegistrationInfo registrationInfo = new RegistrationInfo();
                            BeanUtils.copyProperties(delUserInfo, registrationInfo);
                            registrationInfo.setMemberId(delUserInfo.getMemberId()==null?null:delUserInfo.getMemberId().toString());
                            playersList.add(registrationInfo);
                        });
                        teamMemberNum = teamMemberNum - delUserInfoList.size();
                    }
                    if(CollectionUtils.isNotEmpty(addUserInfoList)){
                        addUserInfoList.stream().forEach(addUserInfo -> {
                            RegistrationInfo registrationInfo = new RegistrationInfo();
                            BeanUtils.copyProperties(addUserInfo, registrationInfo);
                            playersList.add(registrationInfo);
                        });
                        teamMemberNum = teamMemberNum + addUserInfoList.size();
                    }
                    competitionApplyInfoVO.setPlayersList(playersList);
//                    competitionApplyInfoVO.setInstructorList(null);
                    // 修改团队人数及费用
                    if(StringUtils.isNotEmpty(competitionApplyInfoVO.getFee())){
                        BigDecimal subtotal = new BigDecimal(competitionApplyInfoVO.getFee()).multiply(BigDecimal.valueOf(Math.abs(teamMemberNum)));
                        competitionApplyInfoVO.setSubtotal(subtotal.toString());
                    }
                    competitionApplyInfoVO.setTeamSize(Math.abs(teamMemberNum));
                }
                teamChangeDto.setTeamNewInfo(JSONObject.toJSONString(data));
            }
        } else {
            // 退赛及退费重缴逻辑
            //调用远端接口.获取商品详情
            Map<String, Object> reqMap = new HashMap<>();
            List<String> commodityIds = Arrays.asList(teamChangeDto.getTeamCode());
            reqMap.put("teamCodeList",commodityIds);
            R<List<CompetitionApplyInfoVO>> detailForOrder = competitionService.getDetailForOrder(reqMap, SecurityConstants.INNER);
            if(R.isSuccess(detailForOrder) && CollectionUtils.isNotEmpty(detailForOrder.getData())){
                teamChangeDto.setTeamNewInfo(JSONObject.toJSONString(detailForOrder.getData()));
            }
            //退费重缴可以直接获取接取原订单金额，有金额就不需要重新计算
            String amount = MapUtil.getStr(variables, "amount");
            String userNum = MapUtil.getStr(variables, "userNum");
            if (StringUtils.isNotEmpty(amount)) {
                teamChangeDto.setAmount(amount != null ?  new BigDecimal(amount) : BigDecimal.ZERO);
            }else {
                if(R.isSuccess(detailForOrder) && CollectionUtils.isNotEmpty(detailForOrder.getData())){
                    // 单团队直接取值
                    CompetitionApplyInfoVO data = detailForOrder.getData().get(0);
                    teamChangeDto.setAmount(data.getSubtotal() != null ? new BigDecimal(data.getSubtotal()) : BigDecimal.ZERO);
                    teamChangeDto.setUserNum(data.getTeamSize());
                } else {
                    List<String> list = Arrays.stream(userIds.split(",")).toList();
                    if (StringUtils.isNotBlank(userNum) || !Collections.isEmpty(list)) {
                        //没有金额的根据组别code获取单价重新计算
                        String price = competitionService.getCompetitionFee(MapUtil.getStr(variables, "secondLevelCode"), SecurityConstants.INNER).getData();
                        teamChangeDto.setAmount(new BigDecimal(price).multiply(new BigDecimal(String.valueOf(StringUtils.isNotBlank(userNum) ? userNum : list.size()))));
                    } else {
                        teamChangeDto.setAmount(BigDecimal.ZERO);
                    }
                }
            }
        }
        R<OrderInfo> refundOrderByTeamChange = orderService.createRefundOrderByTeamChange(teamChangeDto, SecurityConstants.INNER);
        if (refundOrderByTeamChange.getCode() == R.SUCCESS) {
            OrderInfo data = refundOrderByTeamChange.getData();
            //将退费订单id设置到流程变量中
            variables.put("refundOrderId", data.getId().toString());
            delegateTask.setVariables(variables);
            System.out.println("退费订单创建成功，订单ID：" + data.getId());
        }else{
            log.error("退费订单创建失败：" + refundOrderByTeamChange.getMsg());
        }
    }
}
