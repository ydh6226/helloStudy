package com.hellostudy.account;

import com.hellostudy.domain.Account;
import com.hellostudy.settings.SettingController;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@AutoConfigureMockMvc
@SpringBootTest
class AccountControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    AccountService accountService;

    @Autowired
    AccountRepository accountRepository;

    @MockBean
    JavaMailSender javaMailSender;

    @Test
    @DisplayName("회원가입 화면 보이는지 테스트")
    void signUpFOrm() throws Exception {
        mockMvc.perform(get("/sign-up"))
//                .andDo(print()) // http 출력
                .andExpect(status().isOk())
                .andExpect(view().name("account/sign-up"))
                .andExpect(model().attributeExists("signUpForm"));
    }

    @Test
    @DisplayName("회원가입 처리 - 입력값 정상")
    void signUpSubmit() throws Exception {
        //given
        String email = "email@naver.com";
        String password = "12345678";

        //when
        mockMvc.perform(post("/sign-up")
                .param("nickname", "ydh")
                .param("email", email)
                .param("password", password)
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/"))
                .andExpect(authenticated().withUsername("ydh"));

        Account findAccount = accountRepository.findByEmail(email);

        //then
        assertThat(accountRepository.existsByEmail(email)).isTrue();
        assertThat(findAccount.getEmailCheckToken()).isNotNull();
        then(javaMailSender).should().send(any(SimpleMailMessage.class));

        //비밀번호 암호화 확인
        assertThat(findAccount.getPassword()).isNotEqualTo(password);
    }

    @Test
    @DisplayName("회원가입 처리 - 입력값 오류")
    void signUpSubmit_with_wrong_input() throws Exception {
        mockMvc.perform(post("/sign-up")
                .param("nickname", "ydh")
                .param("email", "email@@@")
                .param("password", "12345")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("account/sign-up"));
    }

    @Test
    @DisplayName("회원가입 후 이메일 인증이 필요하다")
    void needToEmailVerificationAfterSignUp() throws Exception {
        //given
        String email = "member123@email.com";

        mockMvc.perform(post("/sign-up")
                .param("nickname", "회원123")
                .param("email", email)
                .param("password", "12341234")
                .with(csrf()));

        //when
        Account findAccount = accountRepository.findByEmail(email);

        //then
        assertThat(findAccount.isEmailVerified()).isFalse();
    }

    @Test
    @DisplayName("이메일 인증 - 입력값 정상")
    void verifyEmailToken() throws Exception {
        Account account = Account.builder()
                .email("hello@email.com")
                .password("aaaaaaaa")
                .nickname("hello")
                .emailVerified(true)
                .build();

        account.generateEmailCheckToken();
        accountRepository.save(account);

        mockMvc.perform(get("/check-email-token")
                .param("token", account.getEmailCheckToken())
                .param("email", account.getEmail())
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("account/checked-email"))
                .andExpect(model().attributeDoesNotExist("error"))
                .andExpect(model().attributeExists("numberOfUser"))
                .andExpect(model().attributeExists("nickname"))
                .andExpect(authenticated());

        assertThat(account.isEmailVerified()).isTrue();
    }

    @Test
    @DisplayName("이메일 인증 - 입력값 오류")
    void verifyEmailTokenWithWrongInput() throws Exception {
        Account account = Account.builder()
                .email("hello@email.com")
                .password("aaaaaaaa")
                .nickname("hello")
                .emailVerified(true)
                .build();

        account.generateEmailCheckToken();
        accountRepository.save(account);

        //이메일이 존재하지 않는 경우
        mockMvc.perform(get("/check-email-token")
                .param("token", "abcde")
                .param("email", "abcde@email.com"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("error"))
                .andExpect(unauthenticated());


        //토근이 잘못된 경우
        mockMvc.perform(get("/check-email-token")
                .param("token", "abcde")
                .param("email", account.getEmail()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("error"))
                .andExpect(unauthenticated());

    }

    @Test
    @DisplayName("이메일 재요청은 1시간에 한번만 가능하다.")
    void EmailReSendCanBeOnlyBeMadeOnceAnHour() throws Exception {
        //given
        Account account = Account.builder()
                .email("hello@email.com")
                .password("aaaaaaaa")
                .nickname("hello")
                .emailVerified(true)
                .build();

        accountRepository.save(account);
        accountService.login(account);

        //when
        /* 첫 번째 요청 */
        mockMvc.perform(get("/check-email/re-request-token"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/"))
                .andExpect(model().attributeDoesNotExist("error"));

        //then
        /* 두 번째 요청 */
        mockMvc.perform(get("/check-email/re-request-token"))
                .andExpect(status().isOk())
                .andExpect(view().name("account/re-request-email"))
                .andExpect(model().attributeExists("error"));
    }

}