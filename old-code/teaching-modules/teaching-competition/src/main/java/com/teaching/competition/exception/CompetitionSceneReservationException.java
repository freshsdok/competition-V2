package com.teaching.competition.exception;

import com.teaching.competition.domain.CompetitionSceneResourceReservationVO;
import lombok.Getter;

/**
 * 大赛现场资源预约业务异常。
 */
@Getter
public class CompetitionSceneReservationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final String errorCode;
    private final CompetitionSceneResourceReservationVO existingReservation;

    public CompetitionSceneReservationException(String errorCode, String message) {
        this(errorCode, message, null);
    }

    public CompetitionSceneReservationException(String errorCode, String message,
                                                CompetitionSceneResourceReservationVO existingReservation) {
        super(message);
        this.errorCode = errorCode;
        this.existingReservation = existingReservation;
    }
}
