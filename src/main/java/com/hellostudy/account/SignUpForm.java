package com.hellostudy.account;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class SignUpForm {

    @NotNull(message = "닉네임은 필수잆니다.")

    private String nickname;

    private String email;

    private String password;

}
