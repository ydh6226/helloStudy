package com.hellostudy.study;

import com.hellostudy.account.CurrentUser;
import com.hellostudy.config.AppProperties;
import com.hellostudy.domain.Account;
import com.hellostudy.domain.Study;
import com.hellostudy.study.form.StudyForm;
import com.hellostudy.study.validator.StudyFormValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Slf4j
@Controller
@RequiredArgsConstructor
public class StudyController {

    private final StudyService studyService;

    private final StudyFormValidator studyFormValidator;

    private final AppProperties appProperties;

    @InitBinder("studyForm")
    private void studyFormInitBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(studyFormValidator);
    }


    @GetMapping("/new-study")
    public String newStudyForm(@CurrentUser Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(new StudyForm());
        return "study/form";
    }

    @PostMapping("/new-study")
    public String createStudy(@CurrentUser Account account, @Valid StudyForm studyForm, BindingResult result) {
        if (result.hasErrors()) {
            return "study/form";
        }

        Study newStudy = studyService.createNewStudy(new Study(studyForm.getPath(), studyForm.getTitle(),
                studyForm.getShortDescription(), studyForm.getFullDescription()), account);

        return "redirect:/study/" + URLEncoder.encode(newStudy.getPath(), StandardCharsets.UTF_8);
    }

    @ResponseBody
    @PostMapping(value = "/uploadSummernoteImageFile")
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile multipartFile) {
        String originalName = multipartFile.getOriginalFilename();
        String extension = originalName.substring(multipartFile.getOriginalFilename().lastIndexOf("."));
        String savedName = UUID.randomUUID() + extension;

        File file = new File(appProperties.getImageDirectory() + savedName);
        try {
            multipartFile.transferTo(file);
            return new ResponseEntity<>(appProperties.getHost() + "/localImage/" + savedName, HttpStatus.OK);
        } catch (IOException e) {
//            FileUtils.deleteDirectory(file);
            log.error(e.getMessage());
            return new ResponseEntity<>("error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ResponseBody
    @GetMapping(value = "/localImage/{name}")
    public ResponseEntity<byte[]> getImage(@PathVariable("name") String name) {
        try {
            FileInputStream fileInputStream = new FileInputStream(appProperties.getImageDirectory() + name);
            return new ResponseEntity<>(fileInputStream.readAllBytes(), HttpStatus.OK);
        } catch (IOException e) {
            log.error(e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/study/{path}")
    public String viewStudy(@CurrentUser Account account, @PathVariable("path") String path, Model model) {
        Study study = studyService.getStudy(path);

        model.addAttribute(account);
        model.addAttribute(study);
        return "study/view";
    }

    @GetMapping("/study/{path}/members")
    public String viewMembers(@CurrentUser Account account, @PathVariable("path") String path, Model model) {
        Study study = studyService.getStudy(path);

        model.addAttribute(account);
        model.addAttribute(study);
        return "study/members";
    }
}