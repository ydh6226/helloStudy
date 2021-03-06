package com.hellostudy.modules.event;

import com.hellostudy.modules.account.AccountService;
import com.hellostudy.modules.account.UserAccount;
import com.hellostudy.modules.account.form.SignUpForm;
import com.hellostudy.modules.account.Account;
import com.hellostudy.modules.event.repository.EventRepository;
import com.hellostudy.modules.study.Study;
import com.hellostudy.modules.event.form.EventForm;
import com.hellostudy.modules.study.repository.StudyRepository;
import com.hellostudy.modules.study.StudyService;
import com.hellostudy.modules.study.form.StudyForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.context.support.TestExecutionEvent.TEST_EXECUTION;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EventControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    AccountService accountService;

    @Autowired
    EventService eventService;

    @Autowired
    StudyService studyService;

    @Autowired
    StudyRepository studyRepository;

    @Autowired
    EventRepository eventRepository;

    private static final String EMAIL = "hello@email";
    private static final String PASSWORD = "12341234";
    private static final String NICKNAME = "hello";
    private static final String STUDY_PATH = "test-path";
    private static final String BASE_URL = "/study/" + STUDY_PATH;


    @BeforeEach
    private void initDb() {
        Account account = createAccount();
        createStudy(account);
    }

    @Test
    @WithUserDetails(value = EMAIL, setupBefore = TEST_EXECUTION)
    @DisplayName("?????? ?????? ???")
    void eventForm() throws Exception {
        mockMvc.perform(get(BASE_URL + "/new-event"))
                .andExpect(status().isOk())
                .andExpect(view().name("event/form"))
                .andExpect(model().attributeExists("account", "study", "eventForm"));
    }

    @Test
    @WithUserDetails(value = EMAIL, setupBefore = TEST_EXECUTION)
    @DisplayName("?????? ??????")
    void eventFormSubmit() throws Exception {
        MultiValueMap<String, String> params = getEventFormParams();

        mockMvc.perform(post(BASE_URL + "/new-event")
                .params(params)
                .with(csrf()))
                .andExpect(status().is3xxRedirection());

        assertThat(eventRepository.findAll().size()).isEqualTo(1);
    }

    @Test
    @WithUserDetails(value = EMAIL, setupBefore = TEST_EXECUTION)
    @DisplayName("?????? ?????? - ??????")
    void eventFormSubmitWithError() throws Exception {
        MultiValueMap<String, String> params = getEventFormParams();

        params.set("limitOfEnrollments", "1");

        mockMvc.perform(post(BASE_URL + "/new-event")
                .params(params)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("event/form"))
                .andExpect(model().attributeExists("account", "study", "eventForm"))
                .andExpect(model().hasErrors());

        assertThat(eventRepository.findAll().size()).isEqualTo(0);
    }

    @Test
    @WithUserDetails(value = EMAIL, setupBefore = TEST_EXECUTION)
    @DisplayName("?????? ??????")
    void eventView() throws Exception {
        Long eventId = createEvent(getCurrentUser(), getStudy(STUDY_PATH));

        mockMvc.perform(get(BASE_URL + "/events/" + eventId))
                .andExpect(model().attributeExists("account", "study", "event"))
                .andExpect(status().isOk())
                .andExpect(view().name("event/view"));
    }

    @Test
    @WithUserDetails(value = EMAIL, setupBefore = TEST_EXECUTION)
    @DisplayName("?????? ??????")
    void eventCancel() throws Exception {
        Long eventId = createEvent(getCurrentUser(), getStudy(STUDY_PATH));

        mockMvc.perform(post(BASE_URL + "/events/" + eventId + "/delete")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(BASE_URL));

        assertThat(eventRepository.findAll().size()).isEqualTo(0);
        // TODO: 2021-04-30 enrollments?????? ?????? ?????? ????????? ?????? 
    }

    private Account createAccount() {
        SignUpForm form = new SignUpForm();
        form.setEmail(EMAIL);
        form.setNickname(NICKNAME);
        form.setPassword(PASSWORD);
        return accountService.processNewAccount(form);
    }

    private void createStudy(Account account) {
        StudyForm studyForm = new StudyForm();
        studyForm.setPath(STUDY_PATH);
        studyForm.setTitle("hello-title");
        studyForm.setShortDescription("???????????????");
        studyForm.setFullDescription("???????????????");

        studyService.createNewStudy(new Study(studyForm.getPath(), studyForm.getTitle(),
                studyForm.getShortDescription(), studyForm.getFullDescription()), account);
    }

    private Long createEvent(Account account, Study study) {
        EventForm eventForm = new EventForm();
        eventForm.setTitle("event-title");
        eventForm.setDescription("hello");
        eventForm.setEventType(EventType.FCFS);
        eventForm.setEndEnrollmentDateTime(LocalDateTime.now().plusDays(1));
        eventForm.setStartDateTime(LocalDateTime.now().plusDays(1).plusHours(5));
        eventForm.setEndDateTime(LocalDateTime.now().plusDays(1).plusHours(7));
        eventForm.setLimitOfEnrollments(3);
        return eventService.createEvent(Event.createEvent(eventForm), account, study);
    }

    private Account getCurrentUser() {
        UserAccount principal = (UserAccount) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return principal.getAccount();
    }

    private Study getStudy(String path) {
        return studyRepository.findByPath(path);
    }

    private MultiValueMap<String, String> getEventFormParams() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("title", "???????????????");
        params.add("description", "????????????");
        params.add("eventType", "CONFIRMATIVE");
        params.add("endEnrollmentDateTime", LocalDateTime.now().plusHours(1L).format(DateTimeFormatter.ISO_DATE_TIME));
        params.add("startDateTime", LocalDateTime.now().plusHours(2L).format(DateTimeFormatter.ISO_DATE_TIME));
        params.add("endDateTime", LocalDateTime.now().plusHours(3L).format(DateTimeFormatter.ISO_DATE_TIME));
        params.add("limitOfEnrollments", "3");
        return params;
    }
}