package com.hellostudy.domain;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter @Setter(value = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = "id")
public class Enrollment {

    @Id @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    private Account account;

    private LocalDateTime enrolledAt;

    private boolean accepted = false;

    private boolean attended = false;

    public static Enrollment createEnrollment(Event event, Account account) {
        Enrollment enrollment = new Enrollment();
        enrollment.setEvent(event);
        enrollment.setAccount(account);
        enrollment.setEnrolledAt(LocalDateTime.now());
        return enrollment;
    }

    public void acceptAccount(Event event) {
        accepted = true;
        event.increaseCurrentAcceptedCount();
    }
}
