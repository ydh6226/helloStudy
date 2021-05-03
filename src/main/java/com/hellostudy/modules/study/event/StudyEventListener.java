package com.hellostudy.modules.study.event;

import com.hellostudy.modules.study.Study;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Async
@Slf4j
@Component
@Transactional(readOnly = true)
public class StudyEventListener {

    @EventListener
    public void StudyCreatedEventHandler(StudyCreateEvent studyCreateEvent) {
        Study study = studyCreateEvent.getStudy();
        log.info("{} is created.", study.getTitle());
        // TODO: 2021-05-03 이메일 보내거나 , DB에 정보 저장
//        throw new RuntimeException();
    }
}

