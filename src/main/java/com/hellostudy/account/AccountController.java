package com.hellostudy.account;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AccountController {

    @GetMapping("/sign-up")
    public String signUp(Model model) {
        model.addAttribute("hello", new SignUpForm()); //attributeName 생락하면 클래스명을 camel case로 attributeName 등록됨
        return "account/sign-up";
    }
}
