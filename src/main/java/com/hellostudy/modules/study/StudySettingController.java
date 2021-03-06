package com.hellostudy.modules.study;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hellostudy.modules.account.CurrentUser;
import com.hellostudy.modules.account.Account;
import com.hellostudy.modules.tag.Tag;
import com.hellostudy.modules.zone.Zone;
import com.hellostudy.modules.settings.form.TagForm;
import com.hellostudy.modules.settings.form.ZoneForm;
import com.hellostudy.modules.study.form.StudyDescriptionForm;
import com.hellostudy.modules.study.form.StudyTitleForm;
import com.hellostudy.modules.tag.TagRepository;
import com.hellostudy.modules.tag.TagService;
import com.hellostudy.modules.zone.ZoneRepository;
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

        attributes.addFlashAttribute("message", "????????? ??????????????????.");

        // TODO: 2021-04-15 getRedirectUrl()????????????
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
            model.addAttribute("error", "?????? ????????? ??????????????????");
            return "study/settings/study";
        }

        studyService.publish(study);
        attributes.addFlashAttribute("message", "???????????? ??????????????????");
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
        attributes.addFlashAttribute("message", "???????????? ??????????????????.");
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
        attributes.addFlashAttribute("message", "?????? ????????? ???????????????.");
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
        attributes.addFlashAttribute("message", "?????? ????????? ???????????????.");
        return getRedirectUrl("/study", path);
    }

    @PostMapping("/study/updatePath")
    public String updatePath(@CurrentUser Account account, Model model,
                             @PathVariable("path") String path, RedirectAttributes attributes,
                             String newPath) {
        if (!studyService.isValidPath(newPath)) {
            model.addAttribute(account);
            model.addAttribute(studyService.getStudyToUpdate(account, path));
            model.addAttribute("studyPathError", "??? ????????? ????????? ????????? ??? ????????????.");
            return "study/settings/study";
        }

        Study study = studyService.getStudyWithoutFetch(account, path);
        studyService.updatePath(study, newPath);
        attributes.addFlashAttribute("message", "????????? ????????? ??????????????????.");
        return getRedirectUrl("/study", newPath);
    }

    @PostMapping("/study/updateTitle")
    public String updateTitle(@CurrentUser Account account, Model model,
                              @PathVariable("path") String path, RedirectAttributes attributes,
                              StudyTitleForm studyTitleForm, BindingResult result) {
        if (result.hasErrors()) {
            model.addAttribute(account);
            model.addAttribute(studyService.getStudyToUpdate(account, path));
            return "study/settings/study";
        }

        Study study = studyService.getStudyWithoutFetch(account, path);
        studyService.updateTitle(study, studyTitleForm.getTitle());
        attributes.addFlashAttribute("message", "????????? ????????? ??????????????????.");
        return getRedirectUrl("/study", path);
    }

    @PostMapping("/study/delete")
    public String deleteStudy(@CurrentUser Account account, @PathVariable("path") String path) {
        Study study = studyService.getStudyWithoutFetch(account, path);

        studyService.deleteStudy(study);
        return "redirect:/";
    }

    private boolean isStudyPublishing(Account account, Model model, String path, Study study) {
        if (!(study.isPublished() && !study.isClosed())) {
            model.addAttribute(account);
            model.addAttribute(studyService.getStudyToUpdate(account, path));
            model.addAttribute("error", "???????????? ?????? ???????????????.");
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
            model.addAttribute("message", "????????? ?????? ????????? 3????????? ????????? ???????????????.");
            return false;
        }
        return true;
    }

    /**
     *
     * @param url BASE_REDIRECT_URL????????? ????????? ?????? url ex) /study
     * @param path ????????? ??????
     */
    private String getRedirectUrl(String url, String path) {
        return String.format(BASE_REDIRECT_URL + url ,encodePath(path));
    }

    private String encodePath(String path) {
        return URLEncoder.encode(path, StandardCharsets.UTF_8);
    }
}
