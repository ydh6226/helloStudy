package com.hellostudy.modules.main;

import com.hellostudy.modules.account.Account;
import com.hellostudy.modules.account.CurrentUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@ControllerAdvice
public class ExceptionAdvice {

    @ExceptionHandler(RuntimeException.class)
    public String handleRuntimeException(@CurrentUser Account account, HttpServletRequest request,
                                         RuntimeException error, Model model) {
        if (account != null) {
            log.info("{} request {}", account.getNickname(), request.getRequestURI());
        } else {
            log.info("anonymousUser request {}", request.getRequestURI());
        }
        log.error("bad request", error);
        model.addAttribute("message", error.getMessage());
        return "error/customError";
    }
}
