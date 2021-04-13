package com.hellostudy.study;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hellostudy.account.AccountService;
import com.hellostudy.account.CurrentUser;
import com.hellostudy.domain.Account;
import com.hellostudy.domain.Study;
import com.hellostudy.domain.Tag;
import com.hellostudy.settings.form.TagForm;
import com.hellostudy.study.form.StudyDescriptionForm;
import com.hellostudy.tag.TagRepository;
import com.hellostudy.tag.TagService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.*;

@Controller
@RequestMapping("/study/{path}/settings")
@RequiredArgsConstructor
public class StudySettingController {

    private final StudyService studyService;

    private final ModelMapper modelMapper;

    private final TagService tagService;

    private final TagRepository tagRepository;

    private final ObjectMapper objectMapper;

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
        Study study = studyService.getStudyToUpdateDescription(account, path);

        if (result.hasErrors()) {
            model.addAttribute(account);
            model.addAttribute(study);
            return "study/settings/description";
        }

        studyService.updateDescription(path, studyDescriptionForm.getShortDescription(),
                studyDescriptionForm.getFullDescription());

        attributes.addFlashAttribute("message", "변경을 완료했습니다.");
        return "redirect:/study/" + encodePath(path) + "/settings/description";
    }

    @GetMapping("/banner")
    public String updateBannerForm(@CurrentUser Account account, Model model, @PathVariable("path") String path) {
        Study study = studyService.getStudyToUpdate(account, path);

        model.addAttribute(account);
        model.addAttribute(study);
        model.addAttribute("studyForm", modelMapper.map(study, StudyDescriptionForm.class));
        return "study/settings/banner";
    }

    @PostMapping("/banner")
    public String updateBanner(@CurrentUser Account account, @PathVariable("path") String path, String image) {
        Study study = studyService.getStudyToUpdate(account, path);
        studyService.updateBanner(study, image);
        return "redirect:/study/" + encodePath(path) + "/settings/banner";
    }

    @PostMapping("/banner/enable")
    public String EnableStudyBanner(@CurrentUser Account account, @PathVariable("path") String path) {
        Study study = studyService.getStudyToUpdate(account, path);
        studyService.EnableStudyBanner(study);
        return "redirect:/study/" + encodePath(path) + "/settings/banner";
    }

    @PostMapping("/banner/disable")
    public String DisableStudyBanner(@CurrentUser Account account, @PathVariable("path") String path) {
        Study study = studyService.getStudyToUpdate(account, path);
        studyService.DisableStudyBanner(study);
        return "redirect:/study/" + encodePath(path) + "/settings/banner";
    }

    private String encodePath(String path) {
        return URLEncoder.encode(path, StandardCharsets.UTF_8);
    }

    @GetMapping("/tags")
    public String updateZonesForm(@CurrentUser Account account, @PathVariable("path") String path, Model model) throws JsonProcessingException {
        Study study = studyService.getStudyToUpdate(account, path);

        model.addAttribute(account);
        model.addAttribute(study);
        model.addAttribute("tags",
                study.getTags()
                        .stream()
                        .map(Tag::getTitle)
                        .collect(Collectors.toList()));

        List<String> AllTagNames = tagRepository.findAll()
                .stream()
                .map(Tag::getTitle)
                .collect(Collectors.toList());
        model.addAttribute("whiteList", objectMapper.writeValueAsString(AllTagNames));
        return "study/settings/tags";
    }

    @PostMapping("/tags/add")
    @ResponseStatus(OK)
    public void addTag(@CurrentUser Account account, @PathVariable("path") String path, @RequestBody TagForm tagForm) {
        String title = tagForm.getTagTitle();
        Tag tag = tagService.findOrCreate(title);

        studyService.addTag(account, path, tag);
    }

    @PostMapping("/tags/remove")
    public ResponseEntity<String> removeTag(@CurrentUser Account account, @PathVariable("path") String path, @RequestBody TagForm tagForm) {
        String title = tagForm.getTagTitle();
        Optional<Tag> tag = tagRepository.findByTitle(title);
        if (tag.isEmpty()) {
            return new ResponseEntity<>(BAD_REQUEST);
        }
        studyService.removeTag(account, path, tag.get());
        return new  ResponseEntity<>(OK);
    }
}
