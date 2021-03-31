package com.hellostudy.settings.form;

import com.hellostudy.domain.Account;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
public class ProfileForm {

    @Size(max = 35, message = "35글자 이하로 입력하세요.")
    String bio;

    @Size(max = 35, message = "35글자 이하로 입력하세요.")
    String url;

    @Size(max = 35, message = "35글자 이하로 입력하세요.")
    String occupation;

    @Size(max = 35, message = "35글자 이하로 입력하세요.")
    String location;

    private String profileImage;
}
