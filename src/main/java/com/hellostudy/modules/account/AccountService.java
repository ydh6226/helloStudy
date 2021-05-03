package com.hellostudy.modules.account;

import com.hellostudy.modules.account.form.SignUpForm;
import com.hellostudy.infra.config.AppProperties;
import com.hellostudy.modules.account.repository.AccountRepository;
import com.hellostudy.modules.tag.Tag;
import com.hellostudy.modules.zone.Zone;
import com.hellostudy.infra.mail.EmailMessage;
import com.hellostudy.infra.mail.EmailService;
import com.hellostudy.modules.settings.form.NotificationsForm;
import com.hellostudy.modules.settings.form.ProfileForm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountService implements UserDetailsService {

    private final AccountRepository accountRepository;

    private final EmailService emailService;

    private final PasswordEncoder passwordEncoder;

    private final TemplateEngine templateEngine;

    private final AppProperties appProperties;

    @Transactional
    public Account processNewAccount(SignUpForm form) {
        Account newAccount = saveNewAccount(form);
        sendSignUpConfirmEmail(newAccount);
        return newAccount;
    }

    @Transactional
    public void ResendSignUpConfirmEmail(String email) {
        Account account = accountRepository.findByEmail(email);
        account.generateEmailCheckToken();
        account.initEmailTokenTime();
        sendSignUpConfirmEmail(account);

        // 계정정보의 수정이 생겼으므로 Security가 관리 하는 계정업데이트
        login(account);
    }

    private Account saveNewAccount(SignUpForm form) {
        Account newAccount = Account.builder()
                .nickname(form.getNickname())
                .email(form.getEmail())
                .password(passwordEncoder.encode(form.getPassword()))
                .studyCreatedByWeb(true)
                .studyEnrollmentResultByWeb(true)
                .studyUpdatedByWeb(true)
                .tags(new HashSet<>())
                .zones(new HashSet<>())
                .build();
        newAccount.generateEmailCheckToken();

        accountRepository.save(newAccount);
        return newAccount;
    }

    private void sendSignUpConfirmEmail(Account account) {
        Context context = new Context();
        context.setVariable("link", "/check-email-token?token=" + account.getEmailCheckToken()
                + "&email=" + account.getEmail());
        context.setVariable("nickname", account.getNickname());
        context.setVariable("linkName", "이메일 인증하기");
        context.setVariable("message", "스터디올래 서비스를 사용하려면 링크를 클릭하세요.");
        context.setVariable("host", appProperties.getHost());
        String message = templateEngine.process("mail/simple-link", context);

        EmailMessage emailMessage = EmailMessage.builder()
                .to(account.getEmail())
                .subject("스터디올래, 회원 가입 인증")
                .message(message)
                .build();

        emailService.send(emailMessage);
    }

    @Transactional
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
                throw new UsernameNotFoundException(emailOrNickName);
            }
        }

        return new UserAccount(account);
    }

    @Transactional
    public void updateProfile(Account account, ProfileForm form) {
        account.updateProfile(form.getBio(), form.getUrl(), form.getOccupation(), form.getLocation(), form.getProfileImage());
        accountRepository.save(account);
    }

    @Transactional
    public void updatePassword(Account account, String password) {
        Account findAccount = accountRepository.findByEmail(account.getEmail());
        findAccount.updatePassword(passwordEncoder.encode(password));
    }

    @Transactional
    public void updateNotifications(Account account, NotificationsForm form) {
        account.updateNotifications(form);
        accountRepository.save(account);
    }

    @Transactional
    public void updateNickname(Account account, String nickname) {
        account.updateNickname(nickname);
        accountRepository.save(account);
        login(account);
    }

    @Transactional
    public void sendLoginLink(String email) {
        Account account = accountRepository.findByEmail(email);
        account.generateEmailLoginToken();

        Context context = new Context();
        context.setVariable("link", "/login-by-Email?token=" + account.getEmailLoginToken() + "&email=" + email);
        context.setVariable("nickname", account.getNickname());
        context.setVariable("linkName", "이메일로 로그인하기");
        context.setVariable("message", "로그인 하려면 아래 링크를 클릭하세요.");
        context.setVariable("host", appProperties.getHost());
        String message = templateEngine.process("mail/simple-link", context);


        EmailMessage emailMessage = EmailMessage.builder()
                .to(email)
                .subject("스터디 올래, 로그인 링크")
                .message(message)
                .build();

        emailService.send(emailMessage);
    }

    public Set<Tag> getTags(Account account) {
        Optional<Account> findAccount = accountRepository.findById(account.getId());
        return findAccount.orElseThrow().getTags();
    }

    @Transactional
    public void addTag(Account account, Tag tag) {
        Optional<Account> findAccount = accountRepository.findById(account.getId());
        findAccount.ifPresent(a -> a.getTags().add(tag));
    }

    @Transactional
    public void removeTag(Account account, Tag title) {
        Optional<Account> findAccount = accountRepository.findById(account.getId());
        findAccount.ifPresent(a -> a.getTags().remove(title));
    }

    public Set<Zone> getZones(Account account) {
        Optional<Account> findAccount = accountRepository.findById(account.getId());
        return findAccount.orElseThrow().getZones();
    }

    @Transactional
    public void addZone(Account account, Zone zone) {
        Optional<Account> findAccount = accountRepository.findById(account.getId());
        findAccount.orElseThrow().getZones().add(zone);
    }

    @Transactional
    public void removeZone(Account account, Zone zone) {
        Optional<Account> findAccount = accountRepository.findById(account.getId());
        findAccount.orElseThrow().getZones().remove(zone);
    }

    public Account getAccount(String nickname) {
        Account findAccount = accountRepository.findByNickname(nickname);
        if (findAccount == null) {
            throw new IllegalArgumentException(nickname + "에 해당하는 사용자가 없습니다.");
        }
        return findAccount;
    }
}
