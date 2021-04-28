package com.hellostudy.event;

import com.hellostudy.domain.Account;
import com.hellostudy.domain.Event;
import com.hellostudy.domain.Study;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;


    @Transactional
    public Long createEvent(Event event, Account account, Study study) {
        event.initEvent(account, study);
        return eventRepository.save(event).getId();
    }

    public Event findEventWithAllInfoById(Long id) {
        return eventRepository.findEventWithAllInfoById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재 하지 않는 모임입니다."));
    }
}
