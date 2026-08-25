package com.teaching.competition.service;

import com.teaching.competition.domain.AwardDetails;
import com.teaching.competition.domain.AwardPublicity;

import java.util.List;
import java.util.Map;

public interface IAwardDetailsUserService {

    // 查询pc端获奖公示列表
    public List<AwardDetails> selectAwardDetailsList(AwardDetails awardDetails);

    public List<AwardPublicity> selectAwardPublicityList(Long userId);

    // 批量修改获奖公示信息
    public int updateAwardDetails(List<AwardDetails> awardDetails);

    // 修改指导教师校验
    public String updateCheckAwardDetails(List<AwardDetails> awardDetails);
}
