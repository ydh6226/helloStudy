package com.hellostudy.account;

import com.hellostudy.domain.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountService {

    private final AccountRepository accountRepository;

    private final JavaMailSender javaMailSender;

    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void processNewAccount(SignUpForm form) {
        Account newAccount = saveNewAccount(form);
        newAccount.generateEmailCheckToken();
        sendSignUpConfirmEmail(newAccount);
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

    private void sendSignUpConfirmEmail(Account newAccount) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(newAccount.getEmail());
        mailMessage.setSubject("스터디올래, 회원 가입 인증");
        mailMessage.setText("/check-email-token?token=" + newAccount.getEmailCheckToken()
                + "&email=" + newAccount.getEmail());
        javaMailSender.send(mailMessage);
    }

    public Account completeSignUp(String email) {
        Account account = accountRepository.findByEmail(email);
        account.completeSignUp();
        return account;
    }
}
