package com.hellostudy.event;

import com.hellostudy.domain.Account;
import com.hellostudy.domain.Event;
import com.hellostudy.domain.Study;
import com.hellostudy.event.form.EventEditForm;
import com.hellostudy.study.StudyRepository;
import com.hellostudy.study.StudyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class EventService {

    private final StudyService studyService;

    private final EventRepository eventRepository;

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
    }

    private IllegalArgumentException noSuchEventException() {
        return new IllegalArgumentException();
    }

    @Transactional
    public void eventCancel(Long id) {
        Event event = eventRepository.findEventWithEnrollmentsById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재 하지 않는 모임입니다."));
        eventRepository.delete(event);
    }
}
