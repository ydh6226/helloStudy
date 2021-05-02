package com.hellostudy.event;

import com.hellostudy.domain.*;
import com.hellostudy.event.form.EventEditForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    private final EnrollmentRepository enrollmentRepository;

    @Transactional
    public Long createEvent(Event event, Account account, Study study) {
        event.initEvent(account, study);
        return eventRepository.save(event).getId();
    }

    public Event findEventWithAllInfoById(Long id) {
        return eventRepository.findEventWithAllInfoById(id)
                .orElseThrow(EventException::noSuchEventException);
    }

    public Event findEventWithoutFetchById(Long id) {
        return eventRepository.findEventWithoutFetchById(id)
                .orElseThrow(EventException::noSuchEventException);
    }

    public List<Event> findEventWithEnrollmentsByStudyId(Long studyId) {
        return eventRepository.findEventWithEnrollmentsByStudyId(studyId);
    }

    @Transactional
    public void editEvent(Event event, EventEditForm eventEditForm) {
        event.editEvent(eventEditForm);

        //승인 대기 중인 유저가 있는 경우
        if (canAcceptAnotherAccount(event)) {
            acceptAnotherUser(event);
        }
    }

    @Transactional
    public void eventCancel(Long eventId) {
        Event event = findEventWithEnrollmentsByEventId(eventId);
        eventRepository.delete(event);
    }

    @Transactional
    public void joinEvent(Long eventId, Account account) {
        Event event = eventRepository.findEventForJoinByIdQuery(eventId)
                .orElseThrow(EventException::noSuchEventException);

        if (event.isJoined(account)) {
            throw new IllegalStateException("이미 참가 신청한 모임입니다.");
        }

        Enrollment enrollment = Enrollment.createEnrollment(event, account);
        event.join(enrollment);
    }

    @Transactional
    public void leaveEvent(Long eventId, Account account) {
        Event event = eventRepository.findEventForJoinByIdQuery(eventId)
                .orElseThrow(EventException::noSuchEventException);

        if (!event.isJoined(account)) {
            throw new IllegalStateException("참가 신청 하지 않은 모임입니다.");
        }

        event.remove(account);

        //승인 대기 중인 유저가 있는 경우
        if (canAcceptAnotherAccount(event)) {
           acceptAnotherUser(event);
        }
    }

    @Transactional
    public void checkIn(Long eventId, Long enrollmentId) {
        Event event = findEventWithEnrollmentsByEventId(eventId);
        if (!event.checkIn(enrollmentId)) {
            throw EventException.unAcceptedAccountException();
        }
    }

    @Transactional
    public void checkOut(Long eventId, Long enrollmentId) {
        Event event = findEventWithEnrollmentsByEventId(eventId);
        if (!event.checkOut(enrollmentId)) {
            throw EventException.unAcceptedAccountException();
        }
    }

    @Transactional
    public void accept(Long eventId, Long enrollmentId) {
        Event event = findEventWithEnrollmentsByEventId(eventId);
        if (!event.accept(enrollmentId)) {
            throw EventException.unRegisterAccountException();
        }
    }

    @Transactional
    public void disAccept(Long eventId, Long enrollmentId) {
        Event event = findEventWithEnrollmentsByEventId(eventId);
        if (!event.disAccept(enrollmentId)) {
            throw EventException.unRegisterAccountException();
        }
    }

    private boolean canAcceptAnotherAccount (Event event) {
        return event.getEventType() == EventType.FCFS && event.CanAcceptOtherAccountAfterRemove();
    }

    private void acceptAnotherUser(Event event) {
        int count = event.getCountCanAccept();

        List<Enrollment> enrollments = event.getEnrollments();
        List<Enrollment> enrollmentsForAccept = enrollments.stream()
                .filter(e -> !e.isAccepted())
                .limit(count)
                .collect(Collectors.toList());

        enrollmentsForAccept.forEach(e -> e.acceptForFcfs(event));
    }

    private Event findEventWithEnrollmentsByEventId(Long eventId) {
        return eventRepository.findEventWithEnrollmentsById(eventId)
                .orElseThrow(EventException::noSuchEventException);
    }


    private static class EventException {
        private static IllegalArgumentException noSuchEventException() {
            return new IllegalArgumentException("존재 하지 않는 모임입니다.");
        }

        private static IllegalArgumentException unRegisterAccountException() {
            return new IllegalArgumentException("등록 하지 않은 회원입니다.");
        }

        private static IllegalArgumentException unAcceptedAccountException() {
            return new IllegalArgumentException("참가 신청 하지 않은 회원입니다.");
        }
    }
}
