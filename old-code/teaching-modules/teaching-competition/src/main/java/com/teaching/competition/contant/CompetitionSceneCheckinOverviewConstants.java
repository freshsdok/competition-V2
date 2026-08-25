package com.teaching.competition.contant;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 赛事现场签到概览常量。
 */
public final class CompetitionSceneCheckinOverviewConstants {

    public static final String SCOPE_TYPE_SCHEDULE = "SCHEDULE";
    public static final String OPERATION_TYPE_REPORT = "REPORT";
    public static final String OPERATION_STATUS_DONE = "DONE";

    public static final String STATUS_ALL = "ALL";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_PARTIAL = "PARTIAL";
    public static final String STATUS_NOT_STARTED = "NOT_STARTED";
    public static final String STATUS_WARNING = "WARNING";

    public static final String PERSON_SIGNED = "SIGNED";
    public static final String PERSON_UNSIGNED = "UNSIGNED";

    public static final String TEAM_COMPLETED = "COMPLETED";
    public static final String TEAM_PARTIAL = "PARTIAL";
    public static final String TEAM_NOT_STARTED = "NOT_STARTED";

    public static final String WARNING_NORMAL = "NORMAL";
    public static final String WARNING_YELLOW = "YELLOW";
    public static final String WARNING_ORANGE = "ORANGE";
    public static final String WARNING_RED = "RED";

    public static final int YELLOW_THRESHOLD_MINUTES = 30;
    public static final int ORANGE_THRESHOLD_MINUTES = 10;
    public static final BigDecimal YELLOW_RATE = new BigDecimal("80");

    public static final Set<String> MEMBER_ROLE_NAMES = new HashSet<>(
            Arrays.asList("队员", "MEMBER", "选手", "PLAYER")
    );

    private CompetitionSceneCheckinOverviewConstants() {
    }
}
