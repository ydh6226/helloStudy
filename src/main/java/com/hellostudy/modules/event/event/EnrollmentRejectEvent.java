package com.hellostudy.modules.event.event;

import lombok.Getter;

@Getter
public class EnrollmentRejectEvent extends EnrollmentEvent {

    public EnrollmentRejectEvent(Long enrollmentId) {
        super(enrollmentId, "모임 참가 신청이 거절되었습니다.");
    }
}
