package com.hellostudy.study;

import com.hellostudy.account.CurrentUser;
import com.hellostudy.domain.Account;
import com.hellostudy.study.form.StudyForm;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StudyController {

    @GetMapping("/new-study")
    public String newStudyForm(@CurrentUser Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(new StudyForm());
        return "study/form";
    }
}
