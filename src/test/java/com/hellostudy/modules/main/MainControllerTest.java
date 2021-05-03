package com.hellostudy.modules.main;

import com.hellostudy.modules.account.repository.AccountRepository;
import com.hellostudy.modules.account.AccountService;
import com.hellostudy.modules.account.form.SignUpForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class MainControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    AccountService accountService;

    @Autowired
    AccountRepository accountRepository;

    private final String email = "hello@email";
    private final String password = "12341234";
    private final String nickname = "hello";

    @BeforeEach
    private void createAccount() {
        SignUpForm form = new SignUpForm();
        form.setEmail(email);
        form.setNickname(nickname);
        form.setPassword(password);

        accountService.processNewAccount(form);
    }


    @ParameterizedTest
    @ValueSource(strings = {"hello@email", "hello"})
    @DisplayName("로그인(이메일, 닉네임) - 성공")
    void login_success_with_email(String emailAndNickname) throws Exception {
        mockMvc.perform(post("/login")
                .param("username", emailAndNickname)
                .param("password", password)
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(authenticated().withUsername(nickname));
    }

    @Test
    @WithMockUser
    @DisplayName("로그인 - 실패")
    void login_fail() throws Exception {
        mockMvc.perform(post("/login")
                .param("username", "wrong_email")
                .param("password", "wrong_password")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"))
                .andExpect(unauthenticated());
    }

    @Test
    @DisplayName("로그아웃")
    void logout() throws Exception {
        mockMvc.perform(post("/logout")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(unauthenticated());
    }
}