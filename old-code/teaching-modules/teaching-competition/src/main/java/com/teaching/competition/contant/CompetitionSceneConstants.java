package com.teaching.competition.contant;

/**
 * 赛事现场证件、赛场安排和扫码核验常量。
 */
public class CompetitionSceneConstants {

    public static final String CREDENTIAL_TYPE_PARTICIPANT = "PARTICIPANT";
    public static final String CREDENTIAL_TYPE_TEACHER = "TEACHER";
    public static final String CREDENTIAL_TYPE_EXPERT = "EXPERT";
    public static final String CREDENTIAL_TYPE_STAFF = "STAFF";
    public static final String CREDENTIAL_TYPE_VIP = "VIP";
    public static final String CREDENTIAL_TYPE_TEMP = "TEMP";

    /**
     * 历史参赛证枚举值，仅用于旧数据兼容。
     */
    public static final String CREDENTIAL_TYPE_COMPETITOR = "COMPETITOR";

    public static final String DIMENSION_TEAM = "TEAM";
    public static final String DIMENSION_PERSON = "PERSON";

    public static final String SUBJECT_TYPE_TEAM = "TEAM";
    public static final String SUBJECT_TYPE_PERSON = "PERSON";
    public static final String SUBJECT_TYPE_USER = "USER";
    public static final String SUBJECT_TYPE_EXPERT = "EXPERT";
    public static final String SUBJECT_TYPE_STAFF = "STAFF";
    public static final String SUBJECT_TYPE_VIP = "VIP";
    public static final String SUBJECT_TYPE_TEMP = "TEMP";

    public static final String ISSUE_CHANNEL_SCHEDULE_MATCH = "SCHEDULE_MATCH";
    public static final String ISSUE_CHANNEL_COMPETITION_DIRECT = "COMPETITION_DIRECT";
    public static final String ISSUE_CHANNEL_MANUAL = "MANUAL";
    public static final String ISSUE_CHANNEL_IMPORT = "IMPORT";

    public static final String SCOPE_TYPE_COMPETITION = "COMPETITION";
    public static final String SCOPE_TYPE_SCHEDULE = "SCHEDULE";
    public static final String SCOPE_TYPE_VIP = "VIP";
    public static final String SCOPE_TYPE_EXPERT = "EXPERT";
    public static final String SCOPE_TYPE_STAFF = "STAFF";
    public static final String SCOPE_TYPE_TEMP = "TEMP";

    public static final String TARGET_SOURCE_APPLY = "APPLY";
    public static final String TARGET_SOURCE_IMPORT = "IMPORT";
    public static final String TARGET_SOURCE_MANUAL = "MANUAL";
    public static final String TARGET_SOURCE_REVIEW = "REVIEW";

    public static final String TARGET_TYPE_REVIEW_OBJECT = "REVIEW_OBJECT";
    public static final String TARGET_TYPE_TEAM = "TEAM";
    public static final String TARGET_TYPE_PERSON = "PERSON";
    public static final String TARGET_TYPE_USER = "USER";
    public static final String TARGET_TYPE_CREDENTIAL = "CREDENTIAL";
    public static final String TARGET_TYPE_MANUAL = "MANUAL";

    public static final String TARGET_ROLE_TEACHER = "TEACHER";
    public static final String TARGET_ROLE_MEMBER = "MEMBER";
    public static final String TARGET_ROLE_EXPERT = "EXPERT";
    public static final String TARGET_ROLE_CAPTAIN = "CAPTAIN";
    public static final String TARGET_ROLE_MATERIAL_STAFF = "MATERIAL_STAFF";
    public static final String TARGET_ROLE_CHECKIN_STAFF = "CHECKIN_STAFF";
    public static final String TARGET_ROLE_STAFF = "STAFF";
    public static final String TARGET_ROLE_VOLUNTEER = "VOLUNTEER";
    public static final String TARGET_ROLE_UNKNOWN = "UNKNOWN";

    public static final String MATCH_STATUS_MATCHED = "MATCHED";
    public static final String MATCH_STATUS_INVALID = "INVALID";

    public static final String STATUS_NORMAL = "0";
    public static final String STATUS_DISABLED = "1";

    public static final String DEL_FLAG_NORMAL = "0";
    public static final String DEL_FLAG_DELETED = "1";

    public static final String CREDENTIAL_STATUS_EFFECTIVE = "EFFECTIVE";
    public static final String CREDENTIAL_STATUS_REVOKED = "REVOKED";
    public static final String CREDENTIAL_STATUS_EXPIRED = "EXPIRED";

    public static final String OPERATION_VERIFY = "VERIFY";
    public static final String OPERATION_REPORT_SIGN = "REPORT_SIGN";
    public static final String OPERATION_MATERIAL_RECEIVE = "MATERIAL_RECEIVE";
    public static final String OPERATION_WAITING_CHECK_IN = "WAITING_CHECK_IN";
    public static final String OPERATION_CANCEL_REPORT_SIGN = "CANCEL_REPORT_SIGN";
    public static final String OPERATION_CANCEL_MATERIAL_RECEIVE = "CANCEL_MATERIAL_RECEIVE";
    public static final String OPERATION_CANCEL_WAITING_CHECK_IN = "CANCEL_WAITING_CHECK_IN";
    public static final String OPERATION_EXPERT_REVIEW_ENTRY = "EXPERT_REVIEW_ENTRY";

    public static final String STATE_OPERATION_REPORT = "REPORT";
    public static final String STATE_OPERATION_MATERIAL = "MATERIAL";
    public static final String STATE_OPERATION_WAITING = "WAITING";
    public static final String STATE_STATUS_DONE = "DONE";
    public static final String STATE_STATUS_CANCELLED = "CANCELLED";
    public static final String STATE_STATUS_INVALID = "INVALID";
    public static final Integer STATE_DELETED_NO = 0;
    public static final Integer STATE_DELETED_YES = 1;

    public static final String DELEGATE_RELATION_SELF = "SELF";
    public static final String DELEGATE_RELATION_TEAM_MEMBER = "TEAM_MEMBER";

    public static final String OPERATION_STAGE_SCAN = "SCAN";
    public static final String OPERATION_STAGE_CONFIRM = "CONFIRM";

    public static final String OPERATION_RESULT_PASS = "PASS";
    public static final String OPERATION_RESULT_FAIL = "FAIL";
    public static final String OPERATION_RESULT_DUPLICATE = "DUPLICATE";
    public static final String OPERATION_RESULT_EXCEPTION = "EXCEPTION";

    public static final String ERROR_CREDENTIAL_SCOPE_NOT_SUPPORT_WAITING = "CREDENTIAL_SCOPE_NOT_SUPPORT_WAITING";

    public static final String CHECK_RESULT_PASS = "PASS";
    public static final String CHECK_RESULT_FAIL = "FAIL";
    public static final String CHECK_RESULT_SKIP = "SKIP";

    public static final String DONE_NO = "0";
    public static final String DONE_YES = "1";

    public static final String ACTION_KIND_CONFIRM = "CONFIRM";
    public static final String ACTION_KIND_PROMPT = "PROMPT";
    public static final String ACTION_STATUS_PENDING = "PENDING";
    public static final String ACTION_STATUS_DONE = "DONE";
    public static final String ACTION_STATUS_DISABLED = "DISABLED";

    public static final String QR_CONTENT_PREFIX = "csc_";

    private CompetitionSceneConstants() {
    }
}
