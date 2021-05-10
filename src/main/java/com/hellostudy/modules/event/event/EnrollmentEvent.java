package com.hellostudy.modules.event.event;

import lombok.Getter;

@Getter
public abstract class EnrollmentEvent {

    private final Long enrollmentId;

    private final String message;

    public EnrollmentEvent(Long enrollmentId, String message) {
        this.enrollmentId = enrollmentId;
        this.message = message;
    }
}
