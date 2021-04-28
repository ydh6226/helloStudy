package com.hellostudy.event;

import com.hellostudy.account.CurrentUser;
import com.hellostudy.domain.Account;
import com.hellostudy.domain.Event;
import com.hellostudy.domain.Study;
import com.hellostudy.event.form.EventForm;
import com.hellostudy.event.form.validator.EventFormValidator;
import com.hellostudy.study.StudyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/study/{path}")
@RequiredArgsConstructor
public class EventController {

    private final StudyService studyService;

    private final EventService eventService;

    private final EventFormValidator eventFormValidator;

    private final EventRepository eventRepository;

    @InitBinder("eventForm")
    public void eventFormValidator(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(eventFormValidator);
    }

    @GetMapping("/new-event")
    public String newEventForm(@CurrentUser Account account, @PathVariable String path, Model model) {
        model.addAttribute(studyService.getStudyWithManagers(account, path));
        model.addAttribute(account);
        model.addAttribute(new EventForm());
        return "event/form";
    }

    @PostMapping("/new-event")
    public String createEvent(@CurrentUser Account account, @PathVariable String path, Model model,
                              EventForm eventForm, BindingResult result) {
        Study study = studyService.getStudyWithManagers(account, path);
        if (result.hasErrors()) {
            model.addAttribute(study);
            model.addAttribute(account);
            return "event/form";
        }

        Long eventId = eventService.createEvent(Event.createEvent(eventForm), account, study);
        return "redirect:/study/" + study.getEncodePath() + "/events/" + eventId;
    }

    @GetMapping("/events/{id}")
    public String EventView(@CurrentUser Account account, @PathVariable String path, @PathVariable("id") Long eventId,
                            Model model) {
        model.addAttribute(account);
        model.addAttribute(studyService.getStudy(path));
        model.addAttribute("event", eventService.findEventWithAllInfoById(eventId));
        return "event/view";
    }
}
