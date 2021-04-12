package com.hellostudy.study;

import com.hellostudy.account.CurrentUser;
import com.hellostudy.domain.Account;
import com.hellostudy.domain.Study;
import com.hellostudy.study.form.StudyForm;
import com.hellostudy.study.validator.StudyFormValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Slf4j
@Controller
@RequiredArgsConstructor
public class StudyController {

    private final StudyService studyService;

    private final StudyRepository studyRepository;

    private final StudyFormValidator studyFormValidator;

    @Value("${app.imageDirectory}")
    private String imagePath;

    @Value("${app.host}")
    private String host;

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

        File file = new File(imagePath + savedName);
        try {
            multipartFile.transferTo(file);
            return new ResponseEntity<>(host + "/localImage/" + savedName, HttpStatus.OK);
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
            FileInputStream fileInputStream = new FileInputStream(imagePath + name);
            return new ResponseEntity<>(fileInputStream.readAllBytes(), HttpStatus.OK);
        } catch (IOException e) {
            log.error(e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/study/{path}")
    public String viewStudy(@CurrentUser Account account, @PathVariable("path") String path, Model model) {
        model.addAttribute(account);
        model.addAttribute(studyRepository.findByPath(path));
        return "study/view";
    }
}
