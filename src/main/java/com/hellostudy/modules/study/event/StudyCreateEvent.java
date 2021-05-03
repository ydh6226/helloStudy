package com.hellostudy.modules.study.event;

import com.hellostudy.modules.study.Study;
import lombok.Data;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class StudyCreateEvent {

    private Study study;

    public StudyCreateEvent(Study study) {
        this.study = study;
    }
}
