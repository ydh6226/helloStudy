package com.hellostudy.modules.settings;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hellostudy.modules.account.AccountService;
import com.hellostudy.modules.account.CurrentUser;
import com.hellostudy.modules.account.Account;
import com.hellostudy.modules.tag.Tag;
import com.hellostudy.modules.zone.Zone;
import com.hellostudy.modules.settings.form.*;
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
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class SettingController {

    static final String SETTINGS_PROFILE_URL = "/settings/profile";
    static final String SETTINGS_PROFILE_VIEW_NAME = "settings/profile";

    static final String SETTINGS_PASSWORD_URL = "/settings/password";
    static final String SETTINGS_PASSWORD_VIEW_NAME = "settings/password";

    static final String SETTINGS_NOTIFICATIONS_URL = "/settings/notifications";
    static final String SETTINGS_NOTIFICATIONS_VIEW_NAME = "settings/notifications";

    static final String SETTINGS_ACCOUNT_URL = "/settings/account";
    static final String SETTINGS_ACCOUNT_VIEW_NAME = "settings/account";

    static final String SETTINGS_TAGS_URL = "/settings/tags";
    static final String SETTINGS_TAGS_VIEW_NAME = "settings/tags";

    static final String SETTINGS_ZONES_URL = "/settings/zones";
    static final String SETTINGS_ZONES_VIEW_NAME = "settings/zones";


    private final AccountService accountService;

    private final TagRepository tagRepository;

    private final TagService tagService;

    private final ZoneRepository zoneRepository;

    private final ModelMapper modelMapper;

    private final NicknameValidator nicknameValidator;

    private final ObjectMapper objectMapper;

    @InitBinder("nicknameForm")
    public void nicknameValidation(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(nicknameValidator);
    }

    @GetMapping(SETTINGS_PROFILE_URL)
    public String profileUpdateForm(@CurrentUser Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(modelMapper.map(account, ProfileForm.class));
        return SETTINGS_PROFILE_VIEW_NAME;
    }

    @PostMapping(SETTINGS_PROFILE_URL)
    public String updateProfile(@CurrentUser Account account, @Valid ProfileForm form, BindingResult result,
                                Model model, RedirectAttributes attributes) {
        if (result.hasErrors()) {
            model.addAttribute(account);
            return SETTINGS_PROFILE_VIEW_NAME;
        }
        accountService.updateProfile(account, form);
        attributes.addFlashAttribute("message", "프로필을 수정했습니다.");
        return "redirect:/settings/profile";
    }

    @GetMapping(SETTINGS_PASSWORD_URL)
    public String updatePasswordForm(Model model) {
        model.addAttribute(new PasswordForm());
        return SETTINGS_PASSWORD_VIEW_NAME;
    }

    @PostMapping(SETTINGS_PASSWORD_URL)
    public String updatePassword(@CurrentUser Account account, @Valid PasswordForm passwordForm, BindingResult result,
                                 Model model, RedirectAttributes attributes) {
        if (result.hasErrors()) {
            return SETTINGS_PASSWORD_VIEW_NAME;
        }

        if (!passwordForm.getPassword().equals(passwordForm.getConfirmPassword())) {
            model.addAttribute("error", "비밀번호 확인값이 일치하지 않습니다.");
            return SETTINGS_PASSWORD_VIEW_NAME;
        }

        accountService.updatePassword(account, passwordForm.getPassword());
        attributes.addFlashAttribute("message", "비밀번호를 변경했습니다.");

        return "redirect:" + SETTINGS_PASSWORD_URL;
    }

    @GetMapping(SETTINGS_NOTIFICATIONS_URL)
    public String updateNotificationsForm(@CurrentUser Account account, Model model) {
        model.addAttribute(modelMapper.map(account, NotificationsForm.class));
        return SETTINGS_NOTIFICATIONS_VIEW_NAME;
    }

    @PostMapping(SETTINGS_NOTIFICATIONS_URL)
    public String updateNotifications(@CurrentUser Account account, Model model, NotificationsForm form,
                                      BindingResult result, RedirectAttributes attributes) {
        if (result.hasErrors()) {
            return SETTINGS_NOTIFICATIONS_VIEW_NAME;
        }

        accountService.updateNotifications(account, form);
        attributes.addFlashAttribute("message", "알림 설정을 변경했습니다.");
        return "redirect:" + SETTINGS_NOTIFICATIONS_URL;
    }

    @GetMapping(SETTINGS_ACCOUNT_URL)
    public String updateAccountForm(@CurrentUser Account account, Model model) {
        model.addAttribute(modelMapper.map(account, NicknameForm.class));
        return SETTINGS_ACCOUNT_VIEW_NAME;
    }

    @PostMapping(SETTINGS_ACCOUNT_URL)
    public String updateAccount(@CurrentUser Account account, Model model, @Valid NicknameForm nicknameForm, BindingResult result,
                                RedirectAttributes attributes) {
        if (result.hasErrors()) {
            return SETTINGS_ACCOUNT_VIEW_NAME;
        }

        accountService.updateNickname(account, nicknameForm.getNickname());
        attributes.addFlashAttribute("message", "닉네임을 변경했습니다.");
        return "redirect:" + SETTINGS_ACCOUNT_URL;
    }

    @GetMapping(SETTINGS_TAGS_URL)
    public String updateTagsForm(@CurrentUser Account account, Model model) throws JsonProcessingException {
        model.addAttribute(account);

        Set<Tag> tags = accountService.getTags(account);
        model.addAttribute("tags",
                tags.stream().map(Tag::getTitle).collect(Collectors.toList()));

        List<String> AllTagNames = tagRepository.findAll().stream().map(Tag::getTitle).collect(Collectors.toList());
        model.addAttribute("whiteList", objectMapper.writeValueAsString(AllTagNames));

        return SETTINGS_TAGS_VIEW_NAME;
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping(SETTINGS_TAGS_URL + "/add")
    public void addTag(@CurrentUser Account account, @RequestBody TagForm tagForm) {
        String title = tagForm.getTagTitle();
        Tag tag = tagService.findOrCreate(title);

        accountService.addTag(account, tag);
    }

    @PostMapping(SETTINGS_TAGS_URL + "/remove")
    public ResponseEntity<String> removeTag(@CurrentUser Account account, @RequestBody TagForm tagForm) {
        String title = tagForm.getTagTitle();

        Optional<Tag> findTag = tagRepository.findByTitle(title);
        if (findTag.isEmpty()) {
            return new ResponseEntity<>("bad-request", HttpStatus.BAD_REQUEST);
        }

        accountService.removeTag(account, findTag.get());
        return new ResponseEntity<>("ok", HttpStatus.OK);
    }

    @GetMapping(SETTINGS_ZONES_URL)
    public String updateZonesForm(@CurrentUser Account account, Model model) throws JsonProcessingException {
        model.addAttribute(account);

        List<String> zonesOfAccount = accountService.getZones(account).stream()
                .map(Zone::getFullName)
                .collect(Collectors.toList());
        model.addAttribute("zones", zonesOfAccount);

        List<String> allZonesName = zoneRepository.findAll().stream()
                .map(Zone::getFullName)
                .collect(Collectors.toList());
        model.addAttribute("whiteList", objectMapper.writeValueAsString(allZonesName));

        return SETTINGS_ZONES_VIEW_NAME;
    }

    @PostMapping(SETTINGS_ZONES_URL + "/add")
    public ResponseEntity<String> addZone(@CurrentUser Account account, @Valid @RequestBody ZoneForm zoneForm, BindingResult result) {
        if (result.hasErrors()) {
            return new ResponseEntity<>("wrong zone name", HttpStatus.BAD_REQUEST);
        }
        return processZone(account, zoneForm, Process.ADD);
    }

    @PostMapping(SETTINGS_ZONES_URL + "/remove")
    public ResponseEntity<String> removeZone(@CurrentUser Account account, @Valid @RequestBody ZoneForm zoneForm, BindingResult result) {
        if (result.hasErrors()) {
            return new ResponseEntity<>("wrong zone name", HttpStatus.BAD_REQUEST);
        }
        return processZone(account, zoneForm, Process.REMOVE);
    }

    private ResponseEntity<String> processZone(Account account, ZoneForm zoneForm, Process process) {
        Optional<Zone> zone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvinceName());
        if (zone.isEmpty()) {
            return new ResponseEntity<>("That zone doesn't exist", HttpStatus.BAD_REQUEST);
        }

        if (process == Process.ADD) {
            accountService.addZone(account, zone.get());
        } else if (process == Process.REMOVE){
            accountService.removeZone(account, zone.get());
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    private enum Process {
        ADD, REMOVE
    }
}
