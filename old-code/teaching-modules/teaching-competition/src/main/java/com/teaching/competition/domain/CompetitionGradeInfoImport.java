package com.teaching.competition.domain;

import com.teaching.common.core.annotation.Excel;
import jakarta.validation.constraints.NotNull;

/**
 * 成绩导入实体类
 *
 * @author teaching
 */
public class CompetitionGradeInfoImport{

    /** 姓名 */
    @Excel(name = "参赛者姓名")
    @NotNull(message = "姓名不能为空")
    private String userName;

    /** 身份证号 */
    @Excel(name = "身份证号")
    @NotNull(message = "身份证号不能为空")
    private String idCard;

    /** 分数 */
    @Excel(name = "分数")
    @NotNull(message = "分数不能为空")
    private String score;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }
}
