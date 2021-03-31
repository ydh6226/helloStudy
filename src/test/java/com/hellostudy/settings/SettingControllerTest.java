package com.hellostudy.settings;

import com.hellostudy.account.AccountRepository;
import com.hellostudy.account.AccountService;
import com.hellostudy.account.SignUpForm;
import com.hellostudy.domain.Account;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

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
    PasswordEncoder passwordEncoder;

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
        mockMvc.perform(get(SettingController.SETTINGS_PROFILE_URL))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingController.SETTINGS_PROFILE_VIEW_NAME))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profileForm"));
    }

    @Test
    @WithUserDetails(value = nickname, setupBefore = TEST_EXECUTION)
    @DisplayName("프로필 수정하기 - 입력값 정상")
    void updateProfile() throws Exception {

        String bio = "짧은 소개";
        mockMvc.perform(post(SettingController.SETTINGS_PROFILE_URL)
                .param("bio", bio)
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(SettingController.SETTINGS_PROFILE_URL))
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
        mockMvc.perform(post(SettingController.SETTINGS_PROFILE_URL)
                .param("bio", bio)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingController.SETTINGS_PROFILE_VIEW_NAME))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profileForm"));

        Account memberA = accountRepository.findByNickname(nickname);
        assertThat(memberA.getBio()).isNull();
    }

    @Test
    @WithUserDetails(value = nickname, setupBefore = TEST_EXECUTION)
    @DisplayName("패스워드 수정 폼")
    void updatePasswordForm() throws Exception {
        mockMvc.perform(get(SettingController.SETTINGS_PASSWORD_URL))
                .andExpect(model().attributeExists("passwordForm"))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingController.SETTINGS_PASSWORD_VIEW_NAME));
    }

    @Test
    @WithUserDetails(value = nickname, setupBefore = TEST_EXECUTION)
    @DisplayName("패스워드 수정 - 입력값 정상")
    void updatePassword() throws Exception {
        String newPassword = "qweasdzxc";

        mockMvc.perform(post(SettingController.SETTINGS_PASSWORD_URL)
                .param("password", newPassword)
                .param("confirmPassword", newPassword)
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(SettingController.SETTINGS_PASSWORD_URL))
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

        mockMvc.perform(post(SettingController.SETTINGS_PASSWORD_URL)
                .param("password", newPassword)
                .param("confirmPassword", confirmPassword)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingController.SETTINGS_PASSWORD_VIEW_NAME))
                .andExpect(model().attributeExists("error"));
    }

}