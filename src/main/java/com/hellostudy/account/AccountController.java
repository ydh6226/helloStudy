package com.hellostudy.account;

import com.hellostudy.account.form.EmailForm;
import com.hellostudy.account.form.SignUpForm;
import com.hellostudy.account.validator.EmailFormValidator;
import com.hellostudy.account.validator.SignUpFormValidator;
import com.hellostudy.domain.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Controller
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    private final AccountRepository accountRepository;

    private final SignUpFormValidator signUpFormValidator;

    private final EmailFormValidator emailFormValidator;

    //클래스이름과 camel-case로 연결됨
    @InitBinder("signUpForm")
    public void signUpFormValidation(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(signUpFormValidator);
    }

    @InitBinder("emailForm")
    public void emailFormValidation(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(emailFormValidator);
    }

    @GetMapping("/sign-up")
    public String signUpForm(Model model) {
        model.addAttribute(new SignUpForm()); //attributeName 생락하면 클래스명을 camel-case로 attributeName 등록됨
        return "account/sign-up";
    }

    @PostMapping("/sign-up")
    public String signUp(@Valid SignUpForm form, BindingResult result) {
        if (result.hasErrors()) {
            return "account/sign-up";
        }

        Account account = accountService.processNewAccount(form);
        accountService.login(account);
        return "redirect:/";
    }

    @GetMapping("/check-email-token")
    public String confirmEmailToken(String email, String token, Model model) {
        String view = "account/checked-email";

        Account findAccount = accountRepository.findByEmail(email);
        if (findAccount == null) {
            model.addAttribute("error", "wrong.email");
            return view;
        }

        if (!findAccount.isValidToken(token)) {
            model.addAttribute("error", "wrong.token");
            return view;
        }

        Account account = accountService.completeSignUp(email);

        accountService.login(account);

        model.addAttribute("nickname", account.getNickname());
        model.addAttribute("numberOfUser", accountRepository.count());
        return view;
    }

    @GetMapping("/check-email")
    public String ReRequestEmailTokenForm(@CurrentUser Account account, Model model) {
        model.addAttribute("email", account.getEmail());
        return "account/re-request-email";
    }

    @GetMapping("/check-email/re-request-token")
    public String ReRequestEmailToken(@CurrentUser Account account, Model model) {
        if (!account.canSendEmailToken()) {
            model.addAttribute("error", "인증 이메일은 1시간에 한번만 전송할 수 있습니다.");
            model.addAttribute("email", account.getEmail());
            return "account/re-request-email";
        }

        accountService.ResendSignUpConfirmEmail(account.getEmail());
        return "redirect:/";
    }

    @GetMapping("/profile/{nickname}")
    public String viewProfile(@PathVariable String nickname, Model model, @CurrentUser Account account) {
        Account findAccount = accountRepository.findByNickname(nickname);
        if (findAccount == null) {
            throw new IllegalArgumentException(nickname + "에 해당하는 사용자가 없습니다.");
        }

        model.addAttribute("account", findAccount);
        model.addAttribute("isOwner", findAccount.equals(account));
        return "account/profile";
    }

    @GetMapping("/email-login")
    public String sendEmailLoginLinkForm(Model model) {
        model.addAttribute(new EmailForm());
        return "account/emailLogin";
    }

    @PostMapping("/email-login")
    public String sendEmailLoginLink(@Valid EmailForm emailForm, BindingResult result, Model model) {
        String view = "account/emailLogin";
        if (result.hasErrors()) {
            return view;
        }

        Account account = accountRepository.findByEmail(emailForm.getEmail());
        if (!account.canSendEmailLoginToken()) {
            model.addAttribute("error", "로그인링크는 한시간에 한번만 발송가능합니다.");
            return view;
        }

        accountService.sendLoginLink(emailForm.getEmail());
        model.addAttribute("message", "이메일 인증 메일을 발송했습니다.");
        return view;
    }

    @GetMapping("login-by-Email")
    public String loginByEmail(String token, String email, Model model) {
        String view = "account/login-by-email";

        Account account = accountRepository.findByEmail(email);
        if (!accountRepository.existsByEmail(email)) {
            model.addAttribute("error", "wrong.email");
            return view;
        }

        if (!account.getEmailLoginToken().equals(token)) {
            model.addAttribute("error", "wrong.token");
            return view;
        }

        accountService.login(account);
        return view;
    }


}
