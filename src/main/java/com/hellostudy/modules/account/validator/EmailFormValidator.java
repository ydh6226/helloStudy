package com.hellostudy.modules.account.validator;

import com.hellostudy.modules.account.repository.AccountRepository;
import com.hellostudy.modules.account.form.EmailForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class EmailFormValidator implements Validator {

    private final AccountRepository accountRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return EmailForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        EmailForm form = (EmailForm) target;

        if (!accountRepository.existsByEmail(form.getEmail())) {
            errors.rejectValue("email", "invalid.email", "존재하지 않는 이메일 입니다.");
        }
    }
}
