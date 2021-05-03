package com.hellostudy.modules.study.event;

import com.hellostudy.modules.study.Study;
import lombok.Data;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class StudyCreateEvent {

    private final Long studyId;

    public StudyCreateEvent(Long studyId) {
        this.studyId = studyId;
    }
}
