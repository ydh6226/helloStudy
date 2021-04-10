package com.hellostudy.study.validator;

import com.hellostudy.domain.Study;
import com.hellostudy.study.StudyRepository;
import com.hellostudy.study.form.StudyForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class StudyFormValidator implements Validator {

    private final StudyRepository studyRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.isAssignableFrom(Study.class);
    }

    @Override
    public void validate(Object target, Errors errors) {
        StudyForm studyForm = (StudyForm) target;
        if (studyRepository.existsByPath(studyForm.getPath())) {
            errors.rejectValue("path", "wrong.path", "이미 존재하는 스터디 경로 입니다.");
        }
    }
}
