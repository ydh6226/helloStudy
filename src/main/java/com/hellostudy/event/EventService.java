package com.hellostudy.event;

import com.hellostudy.domain.*;
import com.hellostudy.event.form.EventEditForm;
import com.hellostudy.study.StudyRepository;
import com.hellostudy.study.StudyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
                .orElseThrow(this::noSuchEventException);
    }

    public Event findEventWithoutFetchById(Long id) {
        return eventRepository.findEventWithoutFetchById(id)
                .orElseThrow(this::noSuchEventException);
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

    private IllegalArgumentException noSuchEventException() {
        return new IllegalArgumentException("존재 하지 않는 모임입니다.");
    }

    @Transactional
    public void eventCancel(Long id) {
        Event event = eventRepository.findEventWithEnrollmentsById(id)
                .orElseThrow(this::noSuchEventException);
        eventRepository.delete(event);
    }

    @Transactional
    public void joinEvent(Long eventId, Account account) {
        Event event = eventRepository.findEventForJoinByIdQuery(eventId)
                .orElseThrow(this::noSuchEventException);

        if (event.isJoined(account)) {
            throw new IllegalStateException("이미 참가 신청한 모임입니다.");
        }

        Enrollment enrollment = Enrollment.createEnrollment(event, account);
        event.join(enrollment);
    }

    @Transactional
    public void leaveEvent(Long eventId, Account account) {
        Event event = eventRepository.findEventForJoinByIdQuery(eventId)
                .orElseThrow(this::noSuchEventException);

        if (!event.isJoined(account)) {
            throw new IllegalStateException("참가 신청 하지 않은 모임입니다.");
        }

        event.remove(account);

        //승인 대기 중인 유저가 있는 경우
        if (canAcceptAnotherAccount(event)) {
           acceptAnotherUser(event);
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
                .sorted(Comparator.comparing(Enrollment::getEnrolledAt))
                .limit(count)
                .collect(Collectors.toList());

        enrollmentsForAccept.forEach(e -> e.acceptAccount(event));
    }
}
