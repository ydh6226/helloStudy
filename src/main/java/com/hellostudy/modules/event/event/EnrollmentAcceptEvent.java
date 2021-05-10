package com.hellostudy.modules.event.event;

import lombok.Getter;

@Getter
public class EnrollmentAcceptEvent extends EnrollmentEvent {

    public EnrollmentAcceptEvent(Long enrollmentId) {
        super(enrollmentId, "모임 참가 신청이 승인되었습니다.");
    }
}
