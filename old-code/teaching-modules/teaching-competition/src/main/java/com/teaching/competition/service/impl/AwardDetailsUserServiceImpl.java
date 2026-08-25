package com.teaching.competition.service.impl;

import com.teaching.common.core.constant.SecurityConstants;
import com.teaching.common.core.domain.R;
import com.teaching.common.core.exception.GlobalException;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.competition.domain.AwardDetails;
import com.teaching.competition.domain.AwardPlayerInfo;
import com.teaching.competition.domain.AwardPublicity;
import com.teaching.competition.mapper.AwardDetailsMapper;
import com.teaching.competition.mapper.AwardPublicityMapper;
import com.teaching.competition.service.IAwardDetailsService;
import com.teaching.competition.service.IAwardDetailsUserService;
import com.teaching.system.api.OrderService;
import com.teaching.system.api.RemoteUserService;
import com.teaching.system.api.domain.NationwideCollegeInfo;
import com.teaching.system.api.domain.OrderInfo;
import com.teaching.system.api.domain.TeamManagerInfoAwardsUserInfo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
public class AwardDetailsUserServiceImpl implements IAwardDetailsUserService {

    private static final Logger log = LoggerFactory.getLogger(AwardDetailsUserServiceImpl.class);

    @Autowired
    private AwardDetailsMapper awardDetailsMapper;

    @Autowired
    private IAwardDetailsService awardDetailsService;

    @Autowired
    private AwardPublicityMapper awardPublicityMapper;

    @Autowired
    private OrderService orderService;

    @Autowired
    private RemoteUserService userService;

    @Override
    public List<AwardDetails> selectAwardDetailsList(AwardDetails awardDetails) {
        Long userId = SecurityUtils.getLoginUser().getSysUser().getUserId();
        awardDetails.setUserId(userId);
        List<AwardDetails> awardDetailsList = awardDetailsMapper.selectAwardDetailsCompetitionApplyInfoList(awardDetails);
        if(!CollectionUtils.isEmpty(awardDetailsList)){
            awardDetailsList.stream().forEach(awardDetailsRes -> {
//                R<OrderInfo> ordersResponse = orderService.getOrdersByCommodityId(awardDetailsRes.getTeamCode(), SecurityConstants.INNER);
//                if (R.isSuccess(ordersResponse) && Objects.nonNull(ordersResponse.getData())) {
//                    OrderInfo data = ordersResponse.getData();
//                    if (Objects.nonNull(data.getPayTime())) {
//                        awardDetailsRes.setPayTime(data.getPayTime());
//                    } else {
//                        log.warn("获奖公示团队编码 {} 的订单支付时间为空，订单ID: {}", awardDetailsRes.getTeamCode(), data.getOrderId());
//                    }
//                } else {
//                    log.warn("获奖公示根据团队编码 {} 查询订单失败或订单不存在，响应: {}", awardDetailsRes.getTeamCode(), ordersResponse);
//                }
                //获取学校名称
                // 根据school作为id查询省份
                if (StringUtils.isNotEmpty(awardDetailsRes.getSchoolId())) {
                    R<NationwideCollegeInfo> collegeInfoResponse = userService.getNationwideCollegeInfoInfo(awardDetailsRes.getSchoolId(), SecurityConstants.INNER);
                    if (R.isSuccess(collegeInfoResponse) && Objects.nonNull(collegeInfoResponse.getData())) {
                        NationwideCollegeInfo collegeInfo = collegeInfoResponse.getData();
                        awardDetailsRes.setSchoolName(collegeInfo.getSchoolName());
                    }
                }
            });
            // 排序
//            awardDetailsList = awardDetailsList.stream()
//                    .sorted(Comparator.comparing(AwardDetails::getPayTime, Comparator.nullsFirst(Date::compareTo))
//                            .reversed()).collect(Collectors.toList());
        }
        return awardDetailsList;
    }

    @Override
    public List<AwardPublicity> selectAwardPublicityList(Long userId) {
        List<AwardPublicity> awardPublicitieList = awardPublicityMapper.selectAwardPublicityByLeaderTeacherId(userId);
        if(!CollectionUtils.isEmpty(awardPublicitieList)){
            awardPublicitieList.stream().forEach(awardPublicity -> {
                // 判断当前公示时间是否已截至
                if(Objects.nonNull(awardPublicity.getExpirationTime()) &&
                        awardPublicity.getExpirationTime().getTime() > System.currentTimeMillis()){
                    awardPublicity.setExpired(true);
                } else {
                    awardPublicity.setExpired(false);
                }
            });
        }
        Date now = DateUtils.getNowDate();
        return awardPublicitieList.stream()
                .peek(awardPublicity1 -> {
                    Date expirationTime = awardPublicity1.getExpirationTime();
                    if (expirationTime == null) {
                        awardPublicity1.setStatus("未开始");
                    } else if (expirationTime.after(now)) {
                        awardPublicity1.setStatus("公示中");
                    } else {
                        awardPublicity1.setStatus("已结束");
                    }
                }).collect(Collectors.toList());
    }

    @Override
    public int updateAwardDetails(List<AwardDetails> awardDetails) {
//        updateCheckAwardDetails(awardDetails);
        AwardPublicity awardPublicity =
                awardPublicityMapper.selectAwardPublicityById(awardDetails.get(0).getAwardPublicityId());
        if(Objects.nonNull(awardPublicity) && Objects.nonNull(awardPublicity.getExpirationTime()) &&
                awardPublicity.getExpirationTime().getTime() < System.currentTimeMillis()){
            throw new GlobalException("当前公示已截止，信息已锁定");
        }
        return awardDetailsService.updateAwardDetails(awardDetails);
    }

    // 校验指导教师暂时不用
    @Override
    public String updateCheckAwardDetails(List<AwardDetails> awardDetails) {
        AtomicReference<String> checkMessage = new AtomicReference<>("");
        awardDetails.stream().forEach(awardDetail -> {
            List<AwardPlayerInfo> guideTeacherList = new ArrayList<>();
            awardDetail.getGuiderTeacherList().stream().forEach(guideTeacher -> {
                if(StringUtils.isNotBlank(guideTeacher.getUserName())){
                    guideTeacherList.add(guideTeacher);
                }
            });
            // 指教教师数量
            if (guideTeacherList.size() < 1) {
                checkMessage.set("指导教师人数不能少于1名");
                return;
            }
            if (guideTeacherList.size() > 2) {
                checkMessage.set("指导教师人数不能多于2名");
                return;
            }
        });
        return checkMessage.get();
    }
}
