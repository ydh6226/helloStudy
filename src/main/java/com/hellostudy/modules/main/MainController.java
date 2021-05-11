package com.hellostudy.modules.main;

import com.hellostudy.modules.account.CurrentUser;
import com.hellostudy.modules.account.Account;
import com.hellostudy.modules.study.Study;
import com.hellostudy.modules.tag.Tag;
import com.hellostudy.modules.zone.Zone;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequiredArgsConstructor
public class MainController {

    private final MainService mainService;

    @GetMapping("/")
    public String home(@CurrentUser Account account, Model model) {
        addAccountToModel(account, model);
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/search/study")
    public String studySearch(@CurrentUser Account account, Model model, String keyword) {
        addAccountToModel(account, model);

        List<StudyParam> studyParams = mainService.findByStudyByKeyword(keyword)
                .stream()
                .map(StudyParam::new)
                .collect(Collectors.toList());

        model.addAttribute("keyword", keyword);
        model.addAttribute("studyParams", new StudyParamWrapper<>(studyParams));

        return "search";
    }

    @Data
    private static class StudyParamWrapper<T> {

        private final int count;
        private final List<T> studies;
        public StudyParamWrapper(List<T> studies) {
            this.count = studies.size();
            this.studies = studies;
        }

    }
    @Data
    private static class StudyParam {

        private final String title;
        private final String path;
        private final String image;
        private final String shortDescription;
        private final int memberCount;
        private final LocalDateTime publishedDateTime;
        private final List<String> tagTitles;
        private final List<String> zoneLocalNames;
        public StudyParam(Study study) {
            this.title = study.getTitle();
            this.path = study.getEncodePath();
            this.image = study.getImage();
            this.shortDescription = study.getShortDescription();
            this.memberCount = study.getManagers().size() + study.getMembers().size();
            this.publishedDateTime = study.getPublishedDateTime();
            this.tagTitles = study.getTags()
                    .stream()
                    .map(Tag::getTitle)
                    .collect(Collectors.toList());
            this.zoneLocalNames = study.getZones()
                    .stream()
                    .map(Zone::getLocalNameOfCity)
                    .collect(Collectors.toList());
        }
    }

    private void addAccountToModel(Account account, Model model) {
        if (account != null) {
            model.addAttribute(account);
        }
    }
}
