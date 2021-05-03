package com.hellostudy.modules.event;

import com.hellostudy.modules.account.Account;
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

    public void acceptForFcfs(Event event) {
        accepted = true;
        event.increaseCurrentAcceptedCount();
    }

    public void acceptForConfirm() {
        accepted = true;
    }

    public void disAcceptForConfirm() {
        accepted = false;
    }

    public void attend() {
        attended = true;
    }

    public void notAttend() {
        attended = false;
    }
}
