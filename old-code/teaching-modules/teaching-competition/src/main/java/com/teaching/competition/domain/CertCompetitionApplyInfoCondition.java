package com.teaching.competition.domain;

import java.io.Serializable;

public class CertCompetitionApplyInfoCondition implements Serializable {

    private static final long serialVersionUID = 1L;

    // 分数赛选条件
    private String conditions;

    // 分数
    private String userScore;

    // 分数区间开始
    private String userScoreStart;

    // 分数区间结束
    private String userScoreEnd;

    // 排名赛选条件
    private String ranking;

    // 获取前N名
    private String topN;

    // 获取后N名
    private String lowN;

    public String getConditions() {
        return conditions;
    }

    public void setConditions(String conditions) {
        this.conditions = conditions;
    }

    public String getUserScore() {
        return userScore;
    }

    public void setUserScore(String userScore) {
        this.userScore = userScore;
    }

    public String getUserScoreStart() {
        return userScoreStart;
    }

    public void setUserScoreStart(String userScoreStart) {
        this.userScoreStart = userScoreStart;
    }

    public String getUserScoreEnd() {
        return userScoreEnd;
    }

    public void setUserScoreEnd(String userScoreEnd) {
        this.userScoreEnd = userScoreEnd;
    }

    public String getRanking() {
        return ranking;
    }

    public void setRanking(String ranking) {
        this.ranking = ranking;
    }

    public String getTopN() {
        return topN;
    }

    public void setTopN(String topN) {
        this.topN = topN;
    }

    public String getLowN() {
        return lowN;
    }

    public void setLowN(String lowN) {
        this.lowN = lowN;
    }
}
