package com.teaching.job.task;

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.teaching.common.core.constant.Constants;
import com.teaching.common.core.constant.HttpStatus;
import com.teaching.common.core.constant.SecurityConstants;
import com.teaching.common.core.domain.R;
import com.teaching.common.log.aspect.LogAspect;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.system.api.CompetitionService;
import com.teaching.system.api.domain.CompetitionSeriesInfo;
import com.teaching.system.api.domain.SysUser;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("competitionTask")
public class CompetitionTask {

    private static final Logger log = LoggerFactory.getLogger(CompetitionTask.class);

    @Autowired
    private CompetitionService competitionService;

    public void updateCompetitionInfoStatus() {
        try{
            // 获取未开始的赛事信息
            R<List<CompetitionSeriesInfo>> noStartCompetitionInfo =
                    competitionService.getNoStartCompetitionInfo(Constants.COMPETITION_PUBLISH, SecurityConstants.INNER);
            if (noStartCompetitionInfo.getCode() == HttpStatus.SUCCESS) {
                List<CompetitionSeriesInfo> competitionSeriesInfoList = noStartCompetitionInfo.getData();
                if(CollectionUtils.isNotEmpty(competitionSeriesInfoList)){
                    for (CompetitionSeriesInfo competitionSeriesInfo : competitionSeriesInfoList) {
                        if(competitionSeriesInfo.getCompetitionStartTime().getTime() <= System.currentTimeMillis()){
                            // 修改赛事状态
                            CompetitionSeriesInfo seriesInfo = new CompetitionSeriesInfo();
                            seriesInfo.setCompetitionId(competitionSeriesInfo.getCompetitionId());
                            seriesInfo.setCompetitionSeriesId(competitionSeriesInfo.getCompetitionSeriesId());
                            seriesInfo.setCheckStatus(Constants.COMPETITION_RUNNING);
                            R<Integer> updateCompetitionInfoStatus =
                                    competitionService.updateTaskCompetitionInfoStatus(seriesInfo, SecurityConstants.INNER);
                            if (updateCompetitionInfoStatus.getCode() == HttpStatus.SUCCESS) {
                                log.info("赛事启动成功");
                            } else {
                                log.info("赛事启动失败:"+updateCompetitionInfoStatus.getMsg());
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("赛事任务启动失败:"+e);
        }
    }

    public void updateCompetitionEndStatus() {
        try{
            // 获取进行中的赛事信息,是否赛事结束
            R<List<CompetitionSeriesInfo>> runCompetitionInfo =
                    competitionService.getNoStartCompetitionInfo(Constants.COMPETITION_RUNNING, SecurityConstants.INNER);
            if (runCompetitionInfo.getCode() == HttpStatus.SUCCESS) {
                List<CompetitionSeriesInfo> competitionSeriesInfoList = runCompetitionInfo.getData();
                if(CollectionUtils.isNotEmpty(competitionSeriesInfoList)){
                    for (CompetitionSeriesInfo competitionSeriesInfo : competitionSeriesInfoList) {
                        if(competitionSeriesInfo.getCompetitionEndTime().getTime() <= System.currentTimeMillis()){
                            // 修改赛事状态
                            CompetitionSeriesInfo seriesInfo = new CompetitionSeriesInfo();
                            seriesInfo.setCompetitionId(competitionSeriesInfo.getCompetitionId());
                            seriesInfo.setCompetitionSeriesId(competitionSeriesInfo.getCompetitionSeriesId());
                            seriesInfo.setCheckStatus(Constants.COMPETITION_END);
                            R<Integer> updateCompetitionInfoStatus =
                                    competitionService.updateTaskCompetitionInfoStatus(seriesInfo, SecurityConstants.INNER);
                            if (updateCompetitionInfoStatus.getCode() == HttpStatus.SUCCESS) {
                                log.info("赛事结束成功");
                            } else {
                                log.info("赛事启结束失败:"+updateCompetitionInfoStatus.getMsg());
                            }
                        }
                    }
                }
            }
        }catch (Exception e){
            log.error("赛事任务结束失败:"+e);
        }
    }
}
