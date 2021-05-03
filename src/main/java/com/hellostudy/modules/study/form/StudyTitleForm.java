package com.hellostudy.modules.study.form;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

@Data
public class StudyTitleForm {

    @NotBlank
    @Length(max = 50)
    private String title;
}
