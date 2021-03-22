package com.hellostudy.account;

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

    //클래스이름과 camel-case로 연결됨
    @InitBinder("signUpForm")
    public void initBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(signUpFormValidator);
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
        accountService.processNewAccount(form);
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

        if (!findAccount.getEmailCheckToken().equals(token)) {
            model.addAttribute("error", "wrong.token");
            return view;
        }

        Account account = accountService.completeSignUp(email);
        model.addAttribute("nickname", account.getNickname());
        model.addAttribute("numberOfUser", accountRepository.count());
        return view;
    }
}
