package com.hellostudy.event;

import com.hellostudy.account.CurrentUser;
import com.hellostudy.domain.Account;
import com.hellostudy.study.StudyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/study/{path}")
@RequiredArgsConstructor
public class EventController {

    private final StudyService studyService;

    @GetMapping("/new-event")
    public String newEventForm(@CurrentUser Account account, @PathVariable String path, Model model) {
        model.addAttribute(studyService.getStudyWithManagers(account, path));
        model.addAttribute(account);
        model.addAttribute(new EventForm());
        return "event/form";
    }
}
