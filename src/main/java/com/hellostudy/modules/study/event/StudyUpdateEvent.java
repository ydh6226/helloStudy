package com.hellostudy.modules.study.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEvent;

@Getter
@RequiredArgsConstructor
public class StudyUpdateEvent {

    private final Long studyId;

    private final String message;
}
