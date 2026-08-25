package com.teaching.competition.domain;

import lombok.Data;

import java.util.List;

/**
 * 赛场安排按姓名排序入参。
 */
@Data
public class CompetitionSceneScheduleNameSequenceDTO {
    /**
     * 姓名文本，支持逗号、顿号、分号、空白和换行分隔。
     */
    private String namesText;

    /**
     * 已拆分的姓名列表，前端可直接传入。
     */
    private List<String> targetNames;
}
