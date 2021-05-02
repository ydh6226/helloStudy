package com.hellostudy.modules.account.form;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

@Data
public class EmailForm {

    @NotEmpty(message = "이메일을 입력하세요.")
    @Email(message = "옳바른 이메일 형식을 입력하세요.")
    private String email;
}
