package com.teaching.job.task;

import com.teaching.common.core.constant.SecurityConstants;
import com.teaching.system.api.CompetitionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("studentInfoTask")
public class StudentInfoTask {

    private static final Logger logger = LoggerFactory.getLogger(StudentInfoTask.class);

    @Autowired
    private CompetitionService competitionService;

    public void saveStudentInfo(String updateSize) {
        logger.info("开始同步学生信息");
        try {
            competitionService.syncCompetitionApplyInfo(updateSize,SecurityConstants.INNER);
        } catch (Exception e) {
            logger.error("同步学生信息失败:"+e);
        }
    }
}
