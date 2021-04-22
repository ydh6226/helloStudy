package com.hellostudy.study;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hellostudy.account.CurrentUser;
import com.hellostudy.domain.Account;
import com.hellostudy.domain.Study;
import com.hellostudy.domain.Tag;
import com.hellostudy.domain.Zone;
import com.hellostudy.settings.form.TagForm;
import com.hellostudy.settings.form.ZoneForm;
import com.hellostudy.study.form.StudyDescriptionForm;
import com.hellostudy.tag.TagRepository;
import com.hellostudy.tag.TagService;
import com.hellostudy.zone.ZoneRepository;
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

    private final ZoneRepository zoneRepository;

    private final ObjectMapper objectMapper;


    private static final String BASE_REDIRECT_URL = "redirect:/study/%s/settings";

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

        // TODO: 2021-04-15 getRedirectUrl()사용하기
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

    @GetMapping("zones")
    public String updateZoneForm(@CurrentUser Account account, @PathVariable("path") String path, Model model) throws JsonProcessingException {
        Study study = studyService.getStudyToUpdate(account, path);

        List<String> zonesOfStudy = study.getZones()
                .stream()
                .map(Zone::getFullName)
                .collect(Collectors.toList());

        List<String> allZonesName = zoneRepository.findAll()
                .stream()
                .map(Zone::getFullName)
                .collect(Collectors.toList());

        model.addAttribute(account);
        model.addAttribute(study);
        model.addAttribute("zones", zonesOfStudy);
        model.addAttribute("whiteList", objectMapper.writeValueAsString(allZonesName));

        return "study/settings/zones";
    }

    @PostMapping("/zones/add")
    public ResponseEntity<String> addZone(@CurrentUser Account account, @PathVariable("path") String path,
                        @Valid @RequestBody ZoneForm zoneForm, BindingResult result) {
        if (result.hasErrors()) {
            return new ResponseEntity<>("wrong zone name", BAD_REQUEST);
        }

        Optional<Zone> findZone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvinceName());
        if (findZone.isEmpty()) {
            return new ResponseEntity<>("That zone doesn't exist", HttpStatus.BAD_REQUEST);
        }

        studyService.addZone(account, path, findZone.get());
        return new ResponseEntity<>(OK);
    }

    @PostMapping("/zones/remove")
    public ResponseEntity<String> removeZone(@CurrentUser Account account, @PathVariable("path") String path,
                                          @Valid @RequestBody ZoneForm zoneForm, BindingResult result) {
        if (result.hasErrors()) {
            return new ResponseEntity<>("wrong zone name", BAD_REQUEST);
        }

        Optional<Zone> findZone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvinceName());
        if (findZone.isEmpty()) {
            return new ResponseEntity<>("That zone doesn't exist", HttpStatus.BAD_REQUEST);
        }

        studyService.removeZone(account, path, findZone.get());
        return new ResponseEntity<>(OK);
    }

    @GetMapping("/study")
    public String updateStudyForm(@CurrentUser Account account, Model model, @PathVariable("path") String path) {
        Study study = studyService.getStudyToUpdate(account, path);

        model.addAttribute(account);
        model.addAttribute(study);
        return "study/settings/study";
    }

    @PostMapping("/study/publish")
    public String publishStudy(@CurrentUser Account account, Model model,
                               @PathVariable("path") String path, RedirectAttributes attributes) {
        Study study = studyService.getStudyWithoutFetch(account, path);
        if (study.isPublished()) {
            model.addAttribute(account);
            model.addAttribute(studyService.getStudyToUpdate(account, path));
            model.addAttribute("error", "이미 공개된 스터디입니다");
            return "study/settings/study";
        }

        studyService.publish(study);
        attributes.addFlashAttribute("message", "스터디를 공개했습니다");
        return getRedirectUrl("/study", path);
    }

    @PostMapping("/study/close")
    public String closeStudy(@CurrentUser Account account, Model model,
                               @PathVariable("path") String path, RedirectAttributes attributes) {
        Study study = studyService.getStudyWithoutFetch(account, path);
        if (!isStudyPublishing(account, model, path, study)) {
            return "study/settings/study";
        }

        studyService.close(study);
        attributes.addFlashAttribute("message", "스터디를 종료했습니다.");
        return getRedirectUrl("/study", path);
    }

    @PostMapping("/study/startRecruiting")
    public String startRecruiting(@CurrentUser Account account, Model model,
                             @PathVariable("path") String path, RedirectAttributes attributes) {
        Study study = studyService.getStudyWithoutFetch(account, path);
        if (!canUpdateRecruitingStatus(account, model, path, study)) {
            return "study/settings/study";
        }

        studyService.startRecruit(study);
        attributes.addFlashAttribute("message", "팀원 모집을 시작합니다.");
        return getRedirectUrl("/study", path);
    }

    @PostMapping("/study/stopRecruiting")
    public String stopRecruiting(@CurrentUser Account account, Model model,
                                  @PathVariable("path") String path, RedirectAttributes attributes) {
        Study study = studyService.getStudyWithoutFetch(account, path);
        if (!canUpdateRecruitingStatus(account, model, path, study)) {
            return "study/settings/study";
        }

        studyService.stopRecruit(study);
        attributes.addFlashAttribute("message", "팀원 모집을 종료합니다.");
        return getRedirectUrl("/study", path);
    }


    private boolean isStudyPublishing(Account account, Model model, String path, Study study) {
        if (!(study.isPublished() && !study.isClosed())) {
            model.addAttribute(account);
            model.addAttribute(studyService.getStudyToUpdate(account, path));
            model.addAttribute("error", "올바르지 않은 요청입니다.");
            return false;
        }
        return true;
    }

    private boolean canUpdateRecruitingStatus(Account account, Model model, String path, Study study) {
        if (!isStudyPublishing(account, model, path, study)) {
            return false;
        }

        if (!study.canUpdateRecruitingStatus()) {
            model.addAttribute(account);
            model.addAttribute(studyService.getStudyToUpdate(account, path));
            model.addAttribute("message", "스터디 모집 변경은 3시간에 한번만 가능합니다.");
            return false;
        }
        return true;
    }

    private String encodePath(String path) {
        return URLEncoder.encode(path, StandardCharsets.UTF_8);
    }

    /**
     *
     * @param url BASE_REDIRECT_URL다음에 위치할 요청 url ex) /study
     * @param path 스터디 경로
     */
    private String getRedirectUrl(String url, String path) {
        return String.format(BASE_REDIRECT_URL + url ,encodePath(path));
    }

}
