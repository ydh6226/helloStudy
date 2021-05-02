package com.hellostudy.modules.event;

import com.hellostudy.modules.account.Account;
import com.hellostudy.modules.account.UserAccount;
import com.hellostudy.modules.study.Study;
import com.hellostudy.modules.event.form.EventEditForm;
import com.hellostudy.modules.event.form.EventForm;
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

    private int currentAcceptedCount = 0;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy(value = "enrolledAt")
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
        return isNotClosed() && !isEnrolled(userAccount.getAccount());
    }

    public boolean isDisEnrollableFor(UserAccount userAccount) {
        return isNotClosed() && isEnrolled(userAccount.getAccount());
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

    public boolean isJoined(Account account) {
        for (Enrollment enrollment : enrollments) {
            if (enrollment.getAccount().equals(account)) {
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

    public void join(Enrollment enrollment) {
        this.enrollments.add(enrollment);

        if (eventType == EventType.FCFS && !isEnrollmentsFull()) {
            enrollment.acceptForFcfs(this);
        }
    }

    public void remove(Account account) {
        for (Enrollment enrollment : enrollments) {
            if (enrollment.getAccount().equals(account)) {
                if (enrollment.isAttended()) {
                    throw new IllegalArgumentException("출석한 모임은 취소할 수 없습니다.");
                }

                enrollments.remove(enrollment);

                if (enrollment.isAccepted()) {
                    currentAcceptedCount--;
                }
                return;
            }
        }

        throw new IllegalArgumentException("참가 신청 하지 않은 회원입니다.");
    }

    /**
     * remove() 이후에 대기중인 유저가 있는지 확인 하는 함수
     */
    public boolean CanAcceptOtherAccountAfterRemove() {
        return limitOfEnrollments > currentAcceptedCount && isExistsWaitingAccount();
    }

    public boolean isCreatedBy(UserAccount userAccount) {
        return createBy.equals(userAccount.getAccount());
    }

    public int getCountCanAccept() {
        return Math.min(limitOfEnrollments - currentAcceptedCount, enrollments.size() - currentAcceptedCount);
    }

    public void increaseCurrentAcceptedCount() {
        currentAcceptedCount++;
    }

    public boolean checkIn(Long enrollmentId) {
        for (Enrollment enrollment : enrollments) {
            if (enrollment.getId().equals(enrollmentId)) {
                enrollment.attend();
                return true;
            }
        }
        return false;
    }

    public boolean checkOut(Long enrollmentId) {
        for (Enrollment enrollment : enrollments) {
            if (enrollment.getId().equals(enrollmentId)) {
                enrollment.notAttend();
                return true;
            }
        }
        return false;
    }

    public boolean accept(Long enrollmentId) {
        for (Enrollment enrollment : enrollments) {
            if (enrollment.getId().equals(enrollmentId)) {
                enrollment.acceptForConfirm();
                return true;
            }
        }
        return false;
    }

    public boolean disAccept(Long enrollmentId) {
        for (Enrollment enrollment : enrollments) {
            if (enrollment.getId().equals(enrollmentId)) {
                enrollment.disAcceptForConfirm();
                return true;
            }
        }
        return false;
    }

    private boolean isEnrolled(Account account) {
        for (Enrollment enrollment : enrollments) {
            if (enrollment.getAccount().equals(account)) {
                return true;
            }
        }
        return false;
    }

    private boolean isEnrollmentsFull() {
        return limitOfEnrollments <= currentAcceptedCount;
    }

    private boolean isExistsWaitingAccount() {
        return (enrollments.size() - currentAcceptedCount) > 0;
    }
}