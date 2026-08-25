package com.teaching.flowable.listener;

import cn.hutool.core.map.MapUtil;
import com.teaching.common.core.JsonUtils;
import com.teaching.common.core.constant.DictConstant;
import com.teaching.common.core.constant.SecurityConstants;
import com.teaching.common.core.utils.SpringUtils;
import com.teaching.system.api.CompetitionService;
import com.teaching.system.api.domain.CompetitionApplyInfo;
import io.jsonwebtoken.lang.Collections;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

// 团队人员减员
@Slf4j
@Component(value = "changeApplyInfoListener")
public class ChangeApplyInfoListener implements TaskListener {


    @Override
    public void notify(DelegateTask delegateTask) {
        log.info("进入团队人员减员监听器...");
        CompetitionService competitionService = SpringUtils.getBean(CompetitionService.class);
        Map<String, Object> variables = delegateTask.getVariables();
        String operationType = MapUtil.getStr(variables, "operationType");
        if(DictConstant.RETIRED.equals(operationType)){
            log.info("退赛删除团队code:"+MapUtil.getStr(variables, "teamCode"));
            competitionService.removeTeam(MapUtil.getStr(variables, "teamCode"), SecurityConstants.INNER);
        } else {
            String delUserIds = MapUtil.getStr(variables, "delUserIds");
            if(StringUtils.isNotBlank(delUserIds)){
                log.info("删除报名人员id集合:"+delUserIds);
                competitionService.deleteCompetitionApplyInfoByMemberId(delUserIds, SecurityConstants.INNER);
            }
            // 增加人员集合
            String addUserInfos = MapUtil.getStr(variables, "addUserInfos");
            if(StringUtils.isNotBlank(addUserInfos)){
                log.info("新增报名人员集合:"+addUserInfos);
                List<CompetitionApplyInfo> addUserInfoList = JsonUtils.parseArray(addUserInfos, CompetitionApplyInfo.class);
                if(!Collections.isEmpty(addUserInfoList)){
                    addUserInfoList.stream().forEach(addUserInfo -> {
                        addUserInfo.setPayStatus(DictConstant.PAID);
                    });
                }
                competitionService.saveBatchCompetitionApplyInfo(addUserInfoList, SecurityConstants.INNER);
            }
        }
    }
}
