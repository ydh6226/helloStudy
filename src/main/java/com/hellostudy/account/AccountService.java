package com.hellostudy.account;

import com.hellostudy.domain.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountService implements UserDetailsService {

    private final AccountRepository accountRepository;

    private final JavaMailSender javaMailSender;

    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Account processNewAccount(SignUpForm form) {
        Account newAccount = saveNewAccount(form);
        newAccount.generateEmailCheckToken();
        sendSignUpConfirmEmail(newAccount);
        return newAccount;
    }

    @Transactional
    public void ResendSignUpConfirmEmail(String email) {
        Account account = accountRepository.findByEmail(email);
        account.generateEmailCheckToken();
        account.initEmailTokenTime();
        sendSignUpConfirmEmail(account);

        login(account); //TODO 이 사용 방법 말고 다르게 login 된 계정 정보 변경 하는 법 찾기
    }

    private Account saveNewAccount(SignUpForm form) {
        Account newAccount = Account.builder()
                .nickname(form.getNickname())
                .email(form.getEmail())
                .password(passwordEncoder.encode(form.getPassword()))
                .studyCreatedByWeb(true)
                .studyEnrollmentResultByWeb(true)
                .studyUpdatedByWeb(true)
                .build();

        accountRepository.save(newAccount);
        return newAccount;
    }

    private void sendSignUpConfirmEmail(Account account) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(account.getEmail());
        mailMessage.setSubject("스터디올래, 회원 가입 인증");
        mailMessage.setText("/check-email-token?token=" + account.getEmailCheckToken()
                + "&email=" + account.getEmail());
        javaMailSender.send(mailMessage);
    }

    public Account completeSignUp(String email) {
        Account account = accountRepository.findByEmail(email);
        account.completeSignUp();
        return account;
    }

    public void login(Account account) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                new UserAccount(account),
                account.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_USER")));

        SecurityContextHolder.getContext().setAuthentication(token);
    }

    @Override
    public UserDetails loadUserByUsername(String emailOrNickName) throws UsernameNotFoundException {
        Account account = accountRepository.findByEmail(emailOrNickName);

        if (account == null) {
            account = accountRepository.findByNickname(emailOrNickName);
            if (account == null) {
                throw  new UsernameNotFoundException(emailOrNickName);
            }
        }

        return new UserAccount(account);
    }
}
