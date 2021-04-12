package com.hellostudy.study.form;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
@NoArgsConstructor
public class StudyDescriptionForm {

    @NotBlank(message = "공백일 수 없습니다.")
    @Length(max = 100, message = "100자 이하로 작성하세요.")
    private String shortDescription;

    @NotBlank(message = "공백일 수 없습니다.")
    private String fullDescription;
}
