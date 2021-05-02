package com.hellostudy.event;

import com.hellostudy.account.AccountRepository;
import com.hellostudy.account.AccountService;
import com.hellostudy.account.form.SignUpForm;
import com.hellostudy.domain.*;
import com.hellostudy.event.form.EventEditForm;
import com.hellostudy.event.form.EventForm;
import com.hellostudy.study.StudyRepository;
import com.hellostudy.study.StudyService;
import com.hellostudy.study.form.StudyForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class EventServiceTest {

    @Autowired
    EventService eventService;

    @Autowired
    AccountService accountService;

    @Autowired
    StudyService studyService;

    @Autowired
    EventRepository eventRepository;

    @Autowired
    StudyRepository studyRepository;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    EnrollmentRepository enrollmentRepository;

    @Autowired
    EntityManager em;

    private static final String EMAIL = "hello@email";
    private static final String PASSWORD = "12341234";
    private static final String NICKNAME = "hello";
    private static final String STUDY_PATH = "test-path";

    @BeforeEach
    private void initDb() {
        Account account = createAccount(EMAIL, NICKNAME, PASSWORD);
        createStudy(account);
    }

    @Test
    @DisplayName("모임 참가 신청(FCFS) - 공석이 있을 경우 참가상태는 True이다.'")
    void joinEventWithFcfsOnVacancy() throws Exception {
        //given
        Account account = accountRepository.findByNickname(NICKNAME);
        Study study = studyRepository.findStudyWithoutFetchByPath(STUDY_PATH);

        Long eventId = createEvent(account, study);
        Event event = eventRepository.findEventForJoinByIdQuery(eventId).get();

        //when
        Enrollment enrollment = Enrollment.createEnrollment(event, account);
        event.join(enrollment);

        //then
        assertThat(event.getCurrentAcceptedCount()).isEqualTo(1);
        assertThat(event.getEnrollments().size()).isEqualTo(1);
        assertThat(enrollment.isAccepted()).isTrue();
    }

    /**
     * 첫번째, 두번째 회원은 참가 상태가 true이지만
     * 세번째 회원 부터는 참가 상태가 false여야함.
     */
    @Test
    @DisplayName("모임 참가 신청(FCFS) - 공석이 없을 경우 참가상태는 False이다.'")
    void joinEventWithFcfsOnFull() throws Exception {
        //given
        Account account1 = accountRepository.findByNickname(NICKNAME);
        Account account2 = createAccount("second", "second", "second");
        Account account3 = createAccount("third", "third", "third");

        Study study = studyRepository.findStudyWithoutFetchByPath(STUDY_PATH);

        Long eventId = createEvent(account1, study);
        Event event = eventRepository.findEventForJoinByIdQuery(eventId).get();

        //when
        Enrollment enrollment1 = Enrollment.createEnrollment(event, account1);
        Enrollment enrollment2 = Enrollment.createEnrollment(event, account2);
        Enrollment enrollment3 = Enrollment.createEnrollment(event, account3);

        event.join(enrollment1);
        event.join(enrollment2);
        event.join(enrollment3);

        //then
        assertThat(event.getCurrentAcceptedCount()).isEqualTo(2);
        assertThat(event.getEnrollments().size()).isEqualTo(3);

        assertThat(enrollment1.isAccepted()).isTrue();
        assertThat(enrollment2.isAccepted()).isTrue();
        assertThat(enrollment3.isAccepted()).isFalse();
    }

    @Test
    @DisplayName("모임 참가 신청 취소(FCFS)")
    void leaveEventWithFcfsOnFull() throws Exception {
        //given
        Account account = accountRepository.findByNickname(NICKNAME);
        Study study = studyRepository.findStudyWithoutFetchByPath(STUDY_PATH);

        Long eventId = createEvent(account, study);
        Event event = eventRepository.findEventForJoinByIdQuery(eventId).get();

        Enrollment enrollment = Enrollment.createEnrollment(event, account);
        event.join(enrollment);

        //when
        eventService.leaveEvent(eventId, account);

        //then
        assertThat(event.getCurrentAcceptedCount()).isEqualTo(0);
        assertThat(event.getEnrollments().size()).isEqualTo(0);
        assertThat(enrollmentRepository.findAll().isEmpty()).isTrue();
    }

    @Test
    @DisplayName("모임 참가 신청(FCFS) - 공석이 있을 경우 다음 순서로 신청한 사람이 승인된다.'")
    void leaveEventWithAcceptingOtherAccount() throws Exception {
        //given
        Account account1 = accountRepository.findByNickname(NICKNAME);
        Account account2 = createAccount("second", "second", "second");
        Account account3 = createAccount("third", "third", "third");

        Study study = studyRepository.findStudyWithoutFetchByPath(STUDY_PATH);

        Long eventId = createEvent(account1, study);
        Event event = eventRepository.findEventForJoinByIdQuery(eventId).get();

        Enrollment enrollment1 = Enrollment.createEnrollment(event, account1);
        Enrollment enrollment2 = Enrollment.createEnrollment(event, account2);
        Enrollment enrollment3 = Enrollment.createEnrollment(event, account3);

        event.join(enrollment1);
        event.join(enrollment2);
        event.join(enrollment3);

        //when
        eventService.leaveEvent(eventId ,account1);

        //then
        assertThat(event.getCurrentAcceptedCount()).isEqualTo(2);
        assertThat(event.getEnrollments().size()).isEqualTo(2);

        assertThat(enrollment2.isAccepted()).isTrue();
        assertThat(enrollment3.isAccepted()).isTrue();

        assertThat(enrollmentRepository.findAll().size()).isEqualTo(2);
    }

    @Test
    @DisplayName("모집인원이 늘어났을 때 대기 중인 유저가 있으면 승인된다.")
    void editEventWhenIncreaseLimitCountAndWaitingAccount() throws Exception {
        //given
        //given
        Account account1 = accountRepository.findByNickname(NICKNAME);
        Account account2 = createAccount("second", "second", "second");
        Account account3 = createAccount("third", "third", "third");
        Account account4 = createAccount("4th", "4th", "4th");

        Study study = studyRepository.findStudyWithoutFetchByPath(STUDY_PATH);

        Long eventId = createEvent(account1, study);
        Event event = eventRepository.findEventForJoinByIdQuery(eventId).get();

        Enrollment enrollment1 = Enrollment.createEnrollment(event, account1);
        Enrollment enrollment2 = Enrollment.createEnrollment(event, account2);
        Enrollment enrollment3 = Enrollment.createEnrollment(event, account3);
        Enrollment enrollment4 = Enrollment.createEnrollment(event, account4);

        event.join(enrollment1);
        event.join(enrollment2);
        event.join(enrollment3);
        event.join(enrollment4);

        //when
        EventEditForm form = new EventEditForm();
        form.setLimitOfEnrollments(event.getLimitOfEnrollments() + 1);
        eventService.editEvent(event, form);

        //then
        assertThat(event.getCurrentAcceptedCount()).isEqualTo(3);
        assertThat(event.getEnrollments().size()).isEqualTo(4);

        assertThat(enrollment1.isAccepted()).isTrue();
        assertThat(enrollment2.isAccepted()).isTrue();
        assertThat(enrollment3.isAccepted()).isTrue();
        assertThat(enrollment4.isAccepted()).isFalse();
    }


    private Account createAccount(String email, String nickname, String password) {
        SignUpForm form = new SignUpForm();
        form.setEmail(email);
        form.setNickname(nickname);
        form.setPassword(password);
        return accountService.processNewAccount(form);
    }

    private Study createStudy(Account account) {
        StudyForm studyForm = new StudyForm();
        studyForm.setPath(STUDY_PATH);
        studyForm.setTitle("hello-title");
        studyForm.setShortDescription("안녕하세요");
        studyForm.setFullDescription("안녕하세요");

        return studyService.createNewStudy(new Study(studyForm.getPath(), studyForm.getTitle(),
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
        eventForm.setLimitOfEnrollments(2);
        return eventService.createEvent(Event.createEvent(eventForm), account, study);
    }

}