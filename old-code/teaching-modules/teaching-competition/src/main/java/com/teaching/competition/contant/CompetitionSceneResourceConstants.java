package com.teaching.competition.contant;

/**
 * 大赛现场设备资源管理与预约常量。
 */
public class CompetitionSceneResourceConstants {

    public static final String RESOURCE_TYPE_ROOM = "ROOM";
    public static final String RESOURCE_TYPE_LAB = "LAB";
    public static final String RESOURCE_TYPE_DEVICE = "DEVICE";
    public static final String RESOURCE_TYPE_WORKSTATION = "WORKSTATION";
    public static final String RESOURCE_TYPE_SERVER = "SERVER";
    public static final String RESOURCE_TYPE_SOFTWARE = "SOFTWARE";
    public static final String RESOURCE_TYPE_OTHER = "OTHER";

    public static final String RESOURCE_STATUS_ENABLED = "ENABLED";
    public static final String RESOURCE_STATUS_DISABLED = "DISABLED";
    public static final String RESOURCE_STATUS_MAINTENANCE = "MAINTENANCE";

    public static final String BOOKING_STATUS_DRAFT = "DRAFT";
    public static final String BOOKING_STATUS_READY = "READY";
    public static final String BOOKING_STATUS_OPEN = "OPEN";
    public static final String BOOKING_STATUS_PAUSED = "PAUSED";
    public static final String BOOKING_STATUS_CLOSED = "CLOSED";

    public static final String SLOT_STATUS_PENDING = "PENDING";
    public static final String SLOT_STATUS_OPEN = "OPEN";
    public static final String SLOT_STATUS_FULL = "FULL";
    public static final String SLOT_STATUS_CLOSED = "CLOSED";
    public static final String SLOT_STATUS_EXPIRED = "EXPIRED";

    public static final String SUBJECT_TYPE_TEAM = "TEAM";
    public static final String SUBJECT_TYPE_USER = "USER";

    public static final String RESERVATION_STATUS_RESERVED = "RESERVED";
    public static final String RESERVATION_STATUS_CANCELLED = "CANCELLED";
    public static final String RESERVATION_STATUS_CHECKED = "CHECKED";

    public static final String CHECK_STATUS_UNCHECKED = "UNCHECKED";
    public static final String CHECK_STATUS_CHECKED = "CHECKED";

    public static final String ERROR_NO_VALID_CREDENTIAL = "NO_VALID_CREDENTIAL";
    public static final String ERROR_NOT_SCHEDULE_TARGET = "NOT_SCHEDULE_TARGET";
    public static final String ERROR_SUBJECT_NOT_RESOLVED = "SUBJECT_NOT_RESOLVED";
    public static final String ERROR_SUBJECT_MEMBER_INVALID = "SUBJECT_MEMBER_INVALID";
    public static final String ERROR_ALREADY_RESERVED = "ALREADY_RESERVED";
    public static final String ERROR_ALREADY_RESERVED_BY_SUBJECT = "ALREADY_RESERVED_BY_SUBJECT";
    public static final String ERROR_RESOURCE_NOT_OPEN = "RESOURCE_NOT_OPEN";
    public static final String ERROR_RESOURCE_SCOPE_DENIED = "RESOURCE_SCOPE_DENIED";
    public static final String ERROR_SLOT_NOT_OPEN = "SLOT_NOT_OPEN";
    public static final String ERROR_SLOT_GROUP_DENIED = "SLOT_GROUP_DENIED";
    public static final String ERROR_CAPACITY_NOT_ENOUGH = "CAPACITY_NOT_ENOUGH";
    public static final String ERROR_EXCLUSIVE_SLOT_OCCUPIED = "EXCLUSIVE_SLOT_OCCUPIED";
    public static final String ERROR_DUPLICATE_RESERVATION = "DUPLICATE_RESERVATION";
    public static final String ERROR_RESERVATION_NOT_CANCELABLE = "RESERVATION_NOT_CANCELABLE";
    public static final String ERROR_IDEMPOTENCY_KEY_REQUIRED = "IDEMPOTENCY_KEY_REQUIRED";
    public static final String ERROR_RESERVATION_CONFLICT_RETRY_LATER = "RESERVATION_CONFLICT_RETRY_LATER";
    public static final String ERROR_IDEMPOTENCY_CONFLICT_RETRY_LATER = "IDEMPOTENCY_CONFLICT_RETRY_LATER";

    public static final Integer DELETED_NO = 0;
    public static final Integer DELETED_YES = 1;

    private CompetitionSceneResourceConstants() {
    }
}
