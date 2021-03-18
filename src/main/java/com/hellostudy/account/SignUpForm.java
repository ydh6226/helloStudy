package com.hellostudy.account;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@Data
public class SignUpForm {

    @NotEmpty(message = "닉네임을 입력하세요.")
    @Length(min = 3, max = 20)
    @Pattern(regexp = "^[ㄱ-ㅎ가-힣a-z0-9_-]{3,20}$", message = "옳바르지 닉네임 않은 형식입니다.")
    private String nickname;

    @NotEmpty(message = "이메일을 입력하세요.")
    @Email(message = "옳바른 이메일 형식을 입력하세요.")
    private String email;

    @NotEmpty
    @Length(min = 8, max = 50)
    private String password;

}
