package com.hellostudy.study;

import com.hellostudy.account.CurrentUser;
import com.hellostudy.domain.Account;
import com.hellostudy.domain.Study;
import com.hellostudy.study.form.StudyForm;
import com.hellostudy.study.validator.StudyFormValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequiredArgsConstructor
public class StudyController {

    private final StudyService studyService;

    private final StudyFormValidator studyFormValidator;

    @InitBinder("studyForm")
    private void studyFormInitBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(studyFormValidator);
    }


    @GetMapping("/new-study")
    public String newStudyForm(@CurrentUser Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(new StudyForm());
        return "study/form";
    }

    @PostMapping("/new-study")
    public String newStudyForm(@CurrentUser Account account, @Valid StudyForm studyForm, BindingResult result) {
        if (result.hasErrors()) {
            return "study/form";
        }

        Study newStudy = studyService.createNewStudy(new Study(studyForm.getPath(), studyForm.getTitle(),
                studyForm.getShortDescription(), studyForm.getFullDescription()), account);

        return "rediect:/study/" + URLEncoder.encode(newStudy.getPath(), StandardCharsets.UTF_8);
    }
}
