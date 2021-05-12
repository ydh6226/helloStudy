package com.hellostudy.modules.study;

import com.hellostudy.modules.account.repository.AccountRepository;
import com.hellostudy.modules.account.AccountService;
import com.hellostudy.modules.account.form.SignUpForm;
import com.hellostudy.modules.account.Account;
import com.hellostudy.modules.event.EventService;
import com.hellostudy.modules.study.repository.StudyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.context.support.TestExecutionEvent.TEST_EXECUTION;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class StudyControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    StudyService studyService;

    @Autowired
    AccountService accountService;

    @Autowired
    EventService eventService;

    @Autowired
    StudyRepository studyRepository;

    @Autowired
    AccountRepository accountRepository;

    private static final String email = "hello@email";
    private static final String password = "12341234";
    private static final String nickname = "hello";

    @BeforeEach
    private void beforeEach() {
        SignUpForm form = new SignUpForm();
        form.setEmail(email);
        form.setNickname(nickname);
        form.setPassword(password);

        accountService.processNewAccount(form);
    }

    @Test
    @WithUserDetails(value = email, setupBefore = TEST_EXECUTION)
    @DisplayName("스터디 생성 폼")
    void newStudyForm() throws Exception {
        mockMvc.perform(get("/new-study"))
                .andExpect(model().attributeExists("studyForm"))
                .andExpect(model().attributeExists("account"))
                .andExpect(view().name("study/form"));
    }

    @Test
    @WithUserDetails(value = email, setupBefore = TEST_EXECUTION)
    @DisplayName("스터디 생성 - 성공")
    void createNewStudy() throws Exception {
        String studyPath = "hello";
        mockMvc.perform(post("/new-study")
                .param("path", studyPath)
                .param("title", "스프링 공부")
                .param("shortDescription", "안녕하세요")
                .param("fullDescription", "긴 설명")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + studyPath));

        Study findStudy = studyRepository.findByPath(studyPath);
        assertThat(findStudy).isNotNull();

        Account findAccount = accountRepository.findByEmail(email);
        assertThat(findStudy.getManagers().contains(findAccount)).isTrue();
    }

    @Test
    @WithUserDetails(value = email, setupBefore = TEST_EXECUTION)
    @DisplayName("스터디 생성 - 실패")
    void createNewStudyWithError() throws Exception {
        String studyPath = "wrong path";
        mockMvc.perform(post("/new-study")
                .param("path", studyPath)
                .param("title", "스프링 공부")
                .param("shortDescription", "안녕하세요")
                .param("fullDescription", "긴 설명")
                .with(csrf()))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("studyForm"))
                .andExpect(status().isOk())
                .andExpect(view().name("study/form"));

        Study findStudy = studyRepository.findByPath(studyPath);
        assertThat(findStudy).isNull();
    }

    @Test
    @WithUserDetails(value = email, setupBefore = TEST_EXECUTION)
    @DisplayName("스터디 조회")
    void viewStudy() throws Exception {
        String studyPath = "hello-path";
        Study study = new Study(studyPath, "타이틀", "짧은설명", "긴설명");
        Account findAccount = accountRepository.findByEmail(email);
        studyService.createNewStudy(study, findAccount);

        mockMvc.perform(get("/study/" + studyPath))
                .andExpect(view().name("study/view"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("study"));
    }

    @Test
    @WithUserDetails(value = email, setupBefore = TEST_EXECUTION)
    @DisplayName("모임 목록 조회")
    void viewEventList() throws Exception {
        String studyPath = "hello-path";
        Study study = new Study(studyPath, "타이틀", "짧은설명", "긴설명");
        Account findAccount = accountRepository.findByEmail(email);

        studyService.createNewStudy(study, findAccount);

        mockMvc.perform(get("/study/" + studyPath + "/events"))
                .andExpect(status().isOk())
                .andExpect(view().name("study/events"))
                .andExpect(model().attributeExists("account", "study", "newEvents", "endEvents"));
    }
}