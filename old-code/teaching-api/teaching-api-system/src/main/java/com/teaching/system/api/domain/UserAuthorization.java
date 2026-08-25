package com.teaching.system.api.domain;

import com.teaching.common.core.web.domain.BaseEntity;

public class UserAuthorization extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private Long userId;

    /** 已认证学生标识 */
    private boolean studentFlag;

    /** 已认证教师标识 */
    private boolean teacherFlag;

    /** 比赛用户标识 */
    private boolean competitionFlag;

    /** 比赛队长标识 */
    private boolean captainFlag;


    /** 已实名认证标识 */
    private boolean authFlag;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public boolean isCompetitionFlag() {
        return competitionFlag;
    }

    public void setCompetitionFlag(boolean competitionFlag) {
        this.competitionFlag = competitionFlag;
    }

    public boolean isCaptainFlag() {
        return captainFlag;
    }

    public void setCaptainFlag(boolean captainFlag) {
        this.captainFlag = captainFlag;
    }

    public boolean isStudentFlag() {
        return studentFlag;
    }

    public void setStudentFlag(boolean studentFlag) {
        this.studentFlag = studentFlag;
    }

    public boolean isTeacherFlag() {
        return teacherFlag;
    }

    public void setTeacherFlag(boolean teacherFlag) {
        this.teacherFlag = teacherFlag;
    }

    public boolean isAuthFlag() {
        return authFlag;
    }

    public void setAuthFlag(boolean authFlag) {
        this.authFlag = authFlag;
    }
}
