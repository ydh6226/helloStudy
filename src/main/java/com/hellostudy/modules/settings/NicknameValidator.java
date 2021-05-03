package com.hellostudy.modules.settings;

import com.hellostudy.modules.account.repository.AccountRepository;
import com.hellostudy.modules.settings.form.NicknameForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class NicknameValidator implements Validator {

    private final AccountRepository accountRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return NicknameForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        NicknameForm form = (NicknameForm) target;

        if (accountRepository.existsByNickname(form.getNickname())) {
            errors.rejectValue("nickname", "invalid.nickname" ,"이미 존재하는 닉네임입니다.");
        }
    }
}
