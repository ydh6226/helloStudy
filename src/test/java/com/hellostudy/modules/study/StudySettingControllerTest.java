package com.hellostudy.modules.study;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hellostudy.modules.account.repository.AccountRepository;
import com.hellostudy.modules.account.AccountService;
import com.hellostudy.modules.account.form.SignUpForm;
import com.hellostudy.modules.account.Account;
import com.hellostudy.modules.tag.Tag;
import com.hellostudy.modules.settings.form.TagForm;
import com.hellostudy.modules.tag.TagRepository;
import com.hellostudy.modules.tag.TagService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("스터디 설정 컨트롤러 테스트")
class StudySettingControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    AccountService accountService;

    @Autowired
    StudyService studyService;

    @Autowired
    TagService tagService;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    TagRepository tagRepository;

    @Autowired
    StudyRepository studyRepository;

    @Autowired
    AccountRepository accountRepository;

    private static final String EMAIL = "hello@email";
    private static final String PASSWORD = "12341234";
    private static final String NICKNAME = "hello";

    private static final String STUDY_PATH = "hello-path";
    private static final String STUDY_SETTING_URL = "/study/{path}/settings";

    @BeforeEach
    private void beforeEach() {
        Account account = createAccount();
        createStudy(account);
    }

    private Account createAccount() {
        SignUpForm form = new SignUpForm();
        form.setEmail(EMAIL);
        form.setNickname(NICKNAME);
        form.setPassword(PASSWORD);

        return accountService.processNewAccount(form);
    }

    private void createStudy(Account account) {
        Study study = new Study(STUDY_PATH, "hello", "안녕", "안녕");
        studyService.createNewStudy(study, account);
    }


    @Test
    @WithUserDetails(value = EMAIL, setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("태그 설정 폼")
    void updateTagsForm() throws Exception {
        mockMvc.perform(get(STUDY_SETTING_URL + "/tags", STUDY_PATH))
                .andExpect(status().isOk())
                .andExpect(view().name("study/settings/tags"))
                .andExpect(model()
                        .attributeExists("account", "study", "tags", "whiteList"));
    }

    @Test
    @WithUserDetails(value = EMAIL, setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("태그 추가")
    void addTag() throws Exception {
        String tagTitle = "어서와 스프링";

        TagForm tagForm = new TagForm();
        tagForm.setTagTitle(tagTitle);

        mockMvc.perform(post(STUDY_SETTING_URL + "/tags/add", STUDY_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tagForm))
                .with(csrf()))
                .andExpect(status().isOk());

        Optional<Tag> findTag = tagRepository.findByTitle(tagTitle);
        assertThat(findTag.isPresent()).isTrue();

        Study findStudy = studyRepository.findStudyWithTagsByPath(STUDY_PATH);
        assertThat(findStudy.getTags().contains(findTag.get())).isTrue();
    }

    @Test
    @WithUserDetails(value = EMAIL, setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("태그 추가 - 존재하지 않는 스터디 경로")
    void addTagWithWrongStudyPath() {
        String tagTitle = "어서와 스프링";

        TagForm tagForm = new TagForm();
        tagForm.setTagTitle(tagTitle);

        assertThatThrownBy(() ->
                mockMvc.perform(post("/study/wrong-path/settings" + "/tags/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tagForm))
                .with(csrf())))
                .hasCause(new IllegalArgumentException("wrong-path에 해당하는 스터디가 없습니다."));
    }

    @Test
    @WithUserDetails(value = EMAIL, setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("태그 삭제")
    void removeTag() throws Exception {
        TagForm tagForm = new TagForm();
        String tagTitle = "어서와 스프링";
        tagForm.setTagTitle(tagTitle);

        Tag tag = tagService.findOrCreate(tagTitle);

        studyService.addTag(accountRepository.findByEmail(EMAIL), STUDY_PATH, tag);

        mockMvc.perform(post(STUDY_SETTING_URL + "/tags/remove", STUDY_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tagForm))
                .with(csrf()))
                .andExpect(status().isOk());

        Study findStudy = studyRepository.findByPath(STUDY_PATH);
        assertThat(findStudy.getTags().contains(tag)).isFalse();
    }

    @Test
    @WithUserDetails(value = EMAIL, setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("태그 삭제 - 존재하지 않는 태그 삭제")
    void removeTagWithWrongTag() throws Exception {
        TagForm tagForm = new TagForm();
        String tagTitle = "어서와 스프링";
        tagForm.setTagTitle(tagTitle);

        mockMvc.perform(post(STUDY_SETTING_URL + "/tags/remove", STUDY_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tagForm))
                .with(csrf()))
                .andExpect(status().isBadRequest());
    }
}