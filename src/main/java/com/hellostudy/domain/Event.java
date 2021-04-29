package com.hellostudy.domain;

import com.hellostudy.account.UserAccount;
import com.hellostudy.event.form.EventEditForm;
import com.hellostudy.event.form.EventForm;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @EqualsAndHashCode(of = "id")
@Setter(value = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Event {

    @Id @GeneratedValue
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Study study;

    @ManyToOne(fetch = FetchType.LAZY)
    private Account createBy;

    @Column(nullable = false)
    private String title;

    @Lob
    private String description;

    @Column(nullable = false)
    private LocalDateTime createDateTime;

    @Column(nullable = false)
    private LocalDateTime endEnrollmentDateTime;

    @Column(nullable = false)
    private LocalDateTime startDateTime;

    @Column(nullable = false)
    private LocalDateTime endDateTime;

    private Integer limitOfEnrollments;

    @OneToMany(mappedBy = "event")
    private List<Enrollment> enrollments = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private EventType eventType;

    public static Event createEvent(EventForm eventForm) {
        Event event = new Event();
        event.setTitle(eventForm.getTitle());
        event.setDescription(eventForm.getDescription());
        event.setEventType(eventForm.getEventType());
        event.setEndEnrollmentDateTime(eventForm.getEndEnrollmentDateTime());
        event.setStartDateTime(eventForm.getStartDateTime());
        event.setEndDateTime(eventForm.getEndDateTime());
        event.setLimitOfEnrollments(eventForm.getLimitOfEnrollments());

        return event;
    }

    public int numberOfRemainSpot() {
        return limitOfEnrollments - (int) enrollments.stream().filter(Enrollment::isAccepted).count();
    }

    public void initEvent(Account account, Study study) {
        createDateTime = LocalDateTime.now();
        createBy = account;
        this.study = study;
    }

    public boolean isEnrollableFor(UserAccount userAccount) {
        return isNotClosed() || !isEnrolled(userAccount.getAccount());
    }

    public boolean isDisEnrollableFor(UserAccount userAccount) {
        return isNotClosed() || isEnrolled(userAccount.getAccount());
    }

    public boolean isAttended(UserAccount userAccount) {
        Account account = userAccount.getAccount();
        for (Enrollment enrollment : enrollments) {
            if (enrollment.getAccount().equals(account) && enrollment.isAttended()) {
                return true;
            }
        }
        return false;
    }

    public boolean isNotClosed() {
        return LocalDateTime.now().isBefore(endEnrollmentDateTime);
    }

    public void editEvent(EventEditForm eventEditForm) {
        title = eventEditForm.getTitle();
        description = eventEditForm.getDescription();
        endEnrollmentDateTime  = eventEditForm.getEndEnrollmentDateTime();
        startDateTime = eventEditForm.getStartDateTime();
        endDateTime = eventEditForm.getEndDateTime();
        limitOfEnrollments = eventEditForm.getLimitOfEnrollments();
    }

    private boolean isEnrolled(Account account) {
        for (Enrollment enrollment : enrollments) {
            if (enrollment.getAccount().equals(account)) {
                return true;
            }
        }
        return false;
    }
}