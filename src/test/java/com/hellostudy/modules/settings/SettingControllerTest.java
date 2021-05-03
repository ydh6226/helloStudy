package com.hellostudy.modules.settings;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hellostudy.modules.account.repository.AccountRepository;
import com.hellostudy.modules.account.AccountService;
import com.hellostudy.modules.account.form.SignUpForm;
import com.hellostudy.modules.account.Account;
import com.hellostudy.modules.tag.Tag;
import com.hellostudy.modules.settings.form.TagForm;
import com.hellostudy.modules.tag.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static com.hellostudy.modules.settings.SettingController.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.context.support.TestExecutionEvent.TEST_EXECUTION;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SettingControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    AccountService accountService;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    TagRepository tagRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    ObjectMapper objectMapper;

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
    @WithUserDetails(value = nickname, setupBefore = TEST_EXECUTION)
    @DisplayName("프로필 수정 폼")
    void updateProfileForm() throws Exception {
        mockMvc.perform(get(SETTINGS_PROFILE_URL))
                .andExpect(status().isOk())
                .andExpect(view().name(SETTINGS_PROFILE_VIEW_NAME))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profileForm"));
    }

    @Test
    @WithUserDetails(value = nickname, setupBefore = TEST_EXECUTION)
    @DisplayName("프로필 수정하기 - 입력값 정상")
    void updateProfile() throws Exception {

        String bio = "짧은 소개";
        mockMvc.perform(post(SETTINGS_PROFILE_URL)
                .param("bio", bio)
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(SETTINGS_PROFILE_URL))
                .andExpect(flash().attributeExists("message"));

        Account memberA = accountRepository.findByNickname(nickname);
        assertThat(memberA.getBio()).isEqualTo(bio);
    }

    @Test
    @WithUserDetails(value = nickname, setupBefore = TEST_EXECUTION)
    @DisplayName("프로필 수정하기 - 입력값 에러")
    void updateProfileWithError() throws Exception {

        String bio = "긴 소개 긴 소개 긴 소개 긴 소개 긴 소개 긴 소개 긴 소개 긴 소개 긴 소개 긴 소개 긴 소개 " +
                "긴 소개 긴 소개 긴 소개 긴 소개 긴 소개 긴 소개 긴 소개 긴 소개 긴 소개 긴 소개 긴 소개 긴 소개 "
                + "긴 소개 긴 소개 긴 소개 긴 소개 긴 소개 긴 소개 긴 소개 긴 소개 긴 소개 긴 소개 긴 소개 긴 소개 ";
        mockMvc.perform(post(SETTINGS_PROFILE_URL)
                .param("bio", bio)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SETTINGS_PROFILE_VIEW_NAME))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profileForm"));

        Account memberA = accountRepository.findByNickname(nickname);
        assertThat(memberA.getBio()).isNull();
    }

    @Test
    @WithUserDetails(value = nickname, setupBefore = TEST_EXECUTION)
    @DisplayName("패스워드 수정 폼")
    void updatePasswordForm() throws Exception {
        mockMvc.perform(get(SETTINGS_PASSWORD_URL))
                .andExpect(model().attributeExists("passwordForm"))
                .andExpect(status().isOk())
                .andExpect(view().name(SETTINGS_PASSWORD_VIEW_NAME));
    }

    @Test
    @WithUserDetails(value = nickname, setupBefore = TEST_EXECUTION)
    @DisplayName("패스워드 수정 - 입력값 정상")
    void updatePassword() throws Exception {
        String newPassword = "qweasdzxc";

        mockMvc.perform(post(SETTINGS_PASSWORD_URL)
                .param("password", newPassword)
                .param("confirmPassword", newPassword)
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(SETTINGS_PASSWORD_URL))
                .andExpect(flash().attributeExists("message"));

        Account findAccount = accountRepository.findByNickname(nickname);
        assertThat(passwordEncoder.matches(newPassword, findAccount.getPassword()));
    }

    @Test
    @WithUserDetails(value = nickname, setupBefore = TEST_EXECUTION)
    @DisplayName("패스워드 수정 - 입력값 오류")
    void updatePasswordWithError() throws Exception {
        String newPassword = "qweasdzxc";
        String confirmPassword = "zxcasdqwe";

        mockMvc.perform(post(SETTINGS_PASSWORD_URL)
                .param("password", newPassword)
                .param("confirmPassword", confirmPassword)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SETTINGS_PASSWORD_VIEW_NAME))
                .andExpect(model().attributeExists("error"));
    }

    @Test
    @WithUserDetails(value = nickname, setupBefore = TEST_EXECUTION)
    @DisplayName("닉네임 수정 폼")
    void updateNicknameForm() throws Exception {
        mockMvc.perform(get(SETTINGS_ACCOUNT_URL))
                .andExpect(status().isOk())
                .andExpect(view().name(SETTINGS_ACCOUNT_VIEW_NAME))
                .andExpect(model().attributeExists("nicknameForm"));
    }

    @Test
    @WithUserDetails(value = nickname, setupBefore = TEST_EXECUTION)
    @DisplayName("닉네임 수정 - 입력값 정상")
    void updateNickname() throws Exception {
        String newNickname = "hello2";

        mockMvc.perform(post(SETTINGS_ACCOUNT_URL)
                .param("nickname", newNickname)
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(SETTINGS_ACCOUNT_URL))
                .andExpect(flash().attributeExists("message"));

        Account findAccount = accountRepository.findByEmail(email);
        assertThat(findAccount.getNickname()).isEqualTo(newNickname);
    }

    @Test
    @WithUserDetails(value = nickname, setupBefore = TEST_EXECUTION)
    @DisplayName("닉네임 수정 - 입력값 오류")
    void updateNicknameWithWrongNickname() throws Exception {

        //닉네임이 너무 짧은 경우
        String newNickname = "he";
        mockMvc.perform(post(SETTINGS_ACCOUNT_URL)
                .param("nickname", newNickname)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SETTINGS_ACCOUNT_VIEW_NAME))
                .andExpect(model().hasErrors());

        //이미 존재하는 닉네임 인경우
        mockMvc.perform(post(SETTINGS_ACCOUNT_URL)
                .param("nickname", nickname)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SETTINGS_ACCOUNT_VIEW_NAME))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("nicknameForm"));
    }

    @Test
    @WithUserDetails(value = nickname, setupBefore = TEST_EXECUTION)
    @DisplayName("태그 수정 폼")
    void updateTagsForm() throws Exception {
        mockMvc.perform(get(SETTINGS_TAGS_URL))
                .andExpect(status().isOk())
                .andExpect(view().name(SETTINGS_TAGS_VIEW_NAME))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("tags"))
                .andExpect(model().attributeExists("whiteList"));
    }

    @Test
    @WithUserDetails(value = nickname, setupBefore = TEST_EXECUTION)
    @DisplayName("태그 추가")
    void addTags() throws Exception {
        TagForm tag = new TagForm();
        tag.setTagTitle("hello");

        mockMvc.perform(post(SETTINGS_TAGS_URL + "/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tag))
                .with(csrf()))
                .andExpect(status().isOk());

        Account findAccount = accountRepository.findByNickname(nickname);
        assertThat(findAccount.getTags().size()).isEqualTo(1);
        assertThat(tagRepository.existsByTitle(tag.getTagTitle())).isTrue();
    }

    @Test
    @WithUserDetails(value = nickname, setupBefore = TEST_EXECUTION)
    @DisplayName("태그 삭제")
    void removeTags() throws Exception {
        Account account = accountRepository.findByNickname(nickname);
        Tag newTag = new Tag("hello");
        tagRepository.save(newTag);
        accountService.addTag(account, newTag);


        TagForm tag = new TagForm();
        tag.setTagTitle("hello");

        mockMvc.perform(post(SETTINGS_TAGS_URL + "/remove")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tag))
                .with(csrf()))
                .andExpect(status().isOk());

        Account findAccount = accountRepository.findByNickname(nickname);
        assertThat(findAccount.getTags().size()).isEqualTo(0);
        assertThat(tagRepository.existsByTitle(tag.getTagTitle())).isTrue();
    }

}