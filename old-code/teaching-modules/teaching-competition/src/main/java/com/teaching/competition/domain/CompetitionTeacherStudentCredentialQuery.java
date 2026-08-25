package com.teaching.competition.domain;

import lombok.Data;

import java.io.Serializable;

/**
 * 教师个人中心查看指导学生参赛证查询条件。
 */
@Data
public class CompetitionTeacherStudentCredentialQuery implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long competitionId;
    private Long competitionSeriesId;
    private String teamCode;
    private String keyword;
}
