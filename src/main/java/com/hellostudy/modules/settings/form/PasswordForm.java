package com.hellostudy.modules.settings.form;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;

@Data
public class PasswordForm {

    @NotEmpty
    @Length(min = 8, max = 50, message = "입력형식에 맞지 않습니다.")
    String password;

    @NotEmpty
    @Length(min = 8, max = 50, message = "입력형식에 맞지 않습니다.")
    String confirmPassword;
}
