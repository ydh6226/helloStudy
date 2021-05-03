package com.hellostudy.modules.settings.form;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Pattern;

@Data
@NoArgsConstructor
public class ZoneForm {

    @Pattern(regexp = "^[a-zA-Z]+\\([가-힣]+\\)/[a-zA-Z\\s]+$")
    String fullName;

    public String getCityName() {
        return fullName.substring(0, fullName.indexOf('('));
    }

    public String getProvinceName() {
        return fullName.substring(fullName.indexOf('/') + 1);
    }
}
