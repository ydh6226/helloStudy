package com.hellostudy.modules.settings.form;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@Data
public class NicknameForm {

    @NotEmpty(message = "닉네임을 입력하세요.")
    @Length(min = 3, max = 20)
    @Pattern(regexp = "^[ㄱ-ㅎ가-힣a-z0-9_-]{3,20}$", message = "옳바르지 않은 닉네임 형식입니다.")
    private String nickname;
}
