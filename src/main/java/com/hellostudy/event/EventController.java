package com.hellostudy.event;

import com.hellostudy.account.CurrentUser;
import com.hellostudy.domain.Account;
import com.hellostudy.domain.Event;
import com.hellostudy.domain.Study;
import com.hellostudy.event.form.EventEditForm;
import com.hellostudy.event.form.EventForm;
import com.hellostudy.event.form.validator.EventFormValidator;
import com.hellostudy.study.StudyService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Controller
@RequestMapping("/study/{path}")
@RequiredArgsConstructor
public class EventController {

    private final StudyService studyService;

    private final EventService eventService;

    private final EventFormValidator eventFormValidator;

    private final ModelMapper modelMapper;

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
                              @Valid EventForm eventForm, BindingResult result) {
        Study study = studyService.getStudyWithManagers(account, path);
        if (result.hasErrors()) {
            model.addAttribute(study);
            model.addAttribute(account);
            return "event/form";
        }

        Long eventId = eventService.createEvent(Event.createEvent(eventForm), account, study);
        return getRedirectEventViewUrl(study, eventId);
    }

    @GetMapping("/events/{id}")
    public String EventView(@CurrentUser Account account, @PathVariable String path,
                            @PathVariable("id") Long eventId, Model model) {
        model.addAttribute(account);
        model.addAttribute(studyService.getStudyForView(path));
        model.addAttribute("event", eventService.findEventWithAllInfoById(eventId));
        return "event/view";
    }

    @GetMapping("/events/{id}/edit")
    public String eventEditForm(@CurrentUser Account account, @PathVariable String path,
                            @PathVariable("id") Long eventId, Model model) {
        model.addAttribute(account);
        model.addAttribute(studyService.getStudyWithManagers(account, path));

        Event event = eventService.findEventWithoutFetchById(eventId);
        model.addAttribute("event", event);
        model.addAttribute(modelMapper.map(event, EventEditForm.class));
        return "event/edit";
    }

    @PostMapping("/events/{id}/edit")
    public String eventEdit(@CurrentUser Account account, @PathVariable String path,
                            @PathVariable("id") Long eventId, Model model,
                            EventEditForm eventEditForm, BindingResult result) {
        Event event = eventService.findEventWithoutFetchById(eventId);
        Study study = studyService.getStudyWithManagers(account, path);

        if (event.getLimitOfEnrollments() > eventEditForm.getLimitOfEnrollments()) {
            result.rejectValue("limitOfEnrollments", "wrong.limitOfEnrollments",
                    "모집인원은" + event.getLimitOfEnrollments() + "명 이상 이어야 합니다.");
        }

        if (result.hasErrors()) {
            model.addAttribute(account);
            model.addAttribute(study);
            model.addAttribute("event", event);
            return "event/edit";
        }

        eventService.editEvent(event, eventEditForm);
        return getRedirectEventViewUrl(study, eventId);
    }

    @PostMapping("/events/{id}/delete")
    public String eventCancel(@CurrentUser Account account, @PathVariable String path, @PathVariable("id") Long eventId) {
        Study study = studyService.getStudyWithManagers(account, path);
        eventService.eventCancel(eventId);
        return "redirect:/study/" + study.getEncodePath();
    }

    @PostMapping("/events/{id}/join")
    public String joinEvent(@CurrentUser Account account, @PathVariable String path, @PathVariable("id") Long eventId) {
        Study study = studyService.pathVerification(path);
        eventService.joinEvent(eventId, account);
        return getRedirectEventViewUrl(study, eventId);
    }

    @PostMapping("/events/{id}/leave")
    public String leaveEvent(@CurrentUser Account account, @PathVariable String path, @PathVariable("id") Long eventId) {
        Study study = studyService.pathVerification(path);
        eventService.leaveEvent(eventId, account);
        return getRedirectEventViewUrl(study, eventId);
    }

    @PostMapping("/events/{id}/checkIn/{enrollmentId}")
    public String checkIn(@CurrentUser Account account, @PathVariable String path, @PathVariable("id") Long eventId,
                          @PathVariable("enrollmentId") Long enrollmentId) {
        Study study = studyService.getStudyWithManagers(account, path);
        eventService.checkIn(eventId, enrollmentId);
        return getRedirectEventViewUrl(study, eventId);
    }

    @PostMapping("/events/{id}/checkOut/{enrollmentId}")
    public String checkOut(@CurrentUser Account account, @PathVariable String path, @PathVariable("id") Long eventId,
                          @PathVariable("enrollmentId") Long enrollmentId) {
        Study study = studyService.getStudyWithManagers(account, path);
        eventService.checkOut(eventId, enrollmentId);
        return getRedirectEventViewUrl(study, eventId);
    }

    @PostMapping("/events/{id}/accept/{enrollmentId}")
    public String accept(@CurrentUser Account account, @PathVariable String path, @PathVariable("id") Long eventId,
                           @PathVariable("enrollmentId") Long enrollmentId) {
        Study study = studyService.getStudyWithManagers(account, path);
        eventService.accept(eventId, enrollmentId);
        return getRedirectEventViewUrl(study, eventId);
    }

    @PostMapping("/events/{id}/disAccept/{enrollmentId}")
    public String disAccept(@CurrentUser Account account, @PathVariable String path, @PathVariable("id") Long eventId,
                         @PathVariable("enrollmentId") Long enrollmentId) {
        Study study = studyService.getStudyWithManagers(account, path);
        eventService.disAccept(eventId, enrollmentId);
        return getRedirectEventViewUrl(study, eventId);
    }

    private String getRedirectEventViewUrl(Study study, Long eventId) {
        return "redirect:/study/" + study.getEncodePath() + "/events/" + eventId;
    }

}
