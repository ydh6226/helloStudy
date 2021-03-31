package com.hellostudy.settings;

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

    public ProfileForm(Account account) {
        this.bio = account.getBio();
        this.url = account.getUrl();
        this.occupation = account.getOccupation();
        this.location = account.getLocation();
        this.profileImage = account.getProfileImage();
    }
}
