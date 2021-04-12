package com.hellostudy.study;

import com.hellostudy.account.CurrentUser;
import com.hellostudy.domain.Account;
import com.hellostudy.domain.Study;
import com.hellostudy.study.form.StudyDescriptionForm;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@Controller
@RequestMapping("/study/{path}/settings/")
@RequiredArgsConstructor
public class StudySettingController {

    private final StudyService studyService;

    private final StudyRepository studyRepository;

    private final ModelMapper modelMapper;

    @GetMapping("/description")
    public String updateDescriptionForm(@CurrentUser Account account, Model model, @PathVariable("path") String path) {
        Study study = studyService.getStudyToUpdate(account, path);

        model.addAttribute(account);
        model.addAttribute(study);
        model.addAttribute("studyForm", modelMapper.map(study, StudyDescriptionForm.class));
        return "study/settings/description";
    }

    @PostMapping("/description")
    public String updateDescription(@CurrentUser Account account, Model model, @PathVariable("path") String path,
                                    @Valid StudyDescriptionForm studyDescriptionForm, BindingResult result,
                                    RedirectAttributes attributes) {
        Study study = studyService.getStudyToUpdate(account, path);

        if (result.hasErrors()) {
            model.addAttribute(account);
            model.addAttribute(study);
            return "study/settings/description";
        }

        studyService.updateDescription(path, studyDescriptionForm.getShortDescription(),
                studyDescriptionForm.getFullDescription());

        attributes.addFlashAttribute("message", "변경을 완료했습니다.");
        return "redirect:/study/" + path + "/settings/description";
    }
}
