package com.teaching.job.task;

import com.teaching.common.core.constant.SecurityConstants;
import com.teaching.system.api.RemoteUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("teacherTmpTask")
public class TeacherTmpTask {

    @Autowired
    private RemoteUserService remoteUserService;

    public void saveTeacher() {
        try {
            remoteUserService.insertTeacherTmpInfo(SecurityConstants.INNER);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
