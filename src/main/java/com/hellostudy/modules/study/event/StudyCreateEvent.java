package com.hellostudy.modules.study.event;

import com.hellostudy.modules.study.Study;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEvent;

@Getter
@RequiredArgsConstructor
public class StudyCreateEvent {

    private final Long studyId;
}
