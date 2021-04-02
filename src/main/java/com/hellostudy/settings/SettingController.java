package com.hellostudy.settings;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hellostudy.account.AccountService;
import com.hellostudy.account.CurrentUser;
import com.hellostudy.domain.Account;
import com.hellostudy.domain.Tag;
import com.hellostudy.settings.form.*;
import com.hellostudy.tag.TagRepository;
import lombok.Getter;
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
import java.awt.desktop.OpenFilesEvent;
import java.util.ArrayList;
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


    private final AccountService accountService;

    private final ModelMapper modelMapper;

    private final NicknameValidator nicknameValidator;

    private final TagRepository tagRepository;

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
    public void addTag(@CurrentUser Account account, Model model, @RequestBody TagForm tagForm) {
        String title = tagForm.getTagTitle();

        Tag tag = tagRepository.findByTitle(title)
                .orElseGet(() -> tagRepository.save(new Tag(title)));

        accountService.addTag(account, tag);
    }

    @PostMapping(SETTINGS_TAGS_URL + "/remove")
    public ResponseEntity<String> removeTag(@CurrentUser Account account, Model model, @RequestBody TagForm tagForm) {
        String title = tagForm.getTagTitle();

        Optional<Tag> findTag = tagRepository.findByTitle(title);
        if (findTag.isEmpty()) {
            return new ResponseEntity<>("bad-request", HttpStatus.BAD_REQUEST);
        }

        accountService.removeTag(account, findTag.get());
        return new ResponseEntity<>("ok", HttpStatus.OK);
    }
}
