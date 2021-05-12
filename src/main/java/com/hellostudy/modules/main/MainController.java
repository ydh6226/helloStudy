package com.hellostudy.modules.main;

import com.hellostudy.modules.account.CurrentUser;
import com.hellostudy.modules.account.Account;
import com.hellostudy.modules.account.repository.AccountRepository;
import com.hellostudy.modules.event.Enrollment;
import com.hellostudy.modules.event.Event;
import com.hellostudy.modules.event.repository.EnrollmentRepository;
import com.hellostudy.modules.study.Study;
import com.hellostudy.modules.study.repository.StudyRepository;
import com.hellostudy.modules.tag.Tag;
import com.hellostudy.modules.zone.Zone;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.data.domain.Sort.Direction.DESC;

@Slf4j
@Controller
@RequiredArgsConstructor
public class MainController {

    private final MainService mainService;

    private final StudyRepository studyRepository;

    private final AccountRepository accountRepository;

    private final EnrollmentRepository enrollmentRepository;

    @GetMapping("/")
    public String home(@CurrentUser Account account, Model model) {
        if (account == null) {
            List<StudyParam> studyParams = studyRepository.findTop9ByOrderByMemberCountDesc()
                    .stream()
                    .map(StudyParam::new)
                    .collect(Collectors.toList());
            model.addAttribute("studies", studyParams);
        } else {
            Account findAccount = accountRepository.findAccountWithTagsAndZonesById(account.getId());
            model.addAttribute("account", findAccount);
            model.addAttribute("tags", findAccount.getTags());
            model.addAttribute("zones", findAccount.getZones());
            model.addAttribute("managingStudies", studyRepository.findAllByManagersId(account.getId()));
            model.addAttribute("joinedStudies", studyRepository.findAllByMembersId(account.getId()));

            List<StudyParam> recommendedStudies =
                    studyRepository.findStudyByTagsAndZones(findAccount.getTags(), findAccount.getZones())
                            .stream()
                            .map(StudyParam::new)
                            .collect(Collectors.toList());
            model.addAttribute("recommendedStudies", recommendedStudies);

            List<Event> eventsForParticipate =
                    enrollmentRepository.findAllNeedToParticipateByAccountId(account.getId())
                            .stream()
                            .map(Enrollment::getEvent)
                            .collect(Collectors.toList());
            model.addAttribute("eventsForParticipate", eventsForParticipate);
        }
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/search/study")
    public String studySearch(@CurrentUser Account account, Model model, String keyword,
                              @PageableDefault(size = 9, sort = "publishedDateTime", direction = DESC) Pageable pageable) {
        addAccountToModel(account, model);

        PageImpl<Study> studies = mainService.findPagedStudyByKeyword(keyword, pageable);
        List<StudyParam> studyParams = studies
                .stream()
                .map(StudyParam::new)
                .collect(Collectors.toList());

        model.addAttribute("keyword", keyword);
        model.addAttribute("studyParams", new StudyParamWrapper<>(studyParams));
        model.addAttribute("pageParam", new PageParam(studies));
        model.addAttribute("sort",
                pageable.getSort().toString().contains("publishedDateTime") ? "publishedDateTime" : "memberCount");
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

    @Data
    private static class PageParam {
        private final boolean hasPrevious;
        private final boolean hasNext;
        private final int currentPage;
        private final int totalPage;
        private final long totalElements;

        public PageParam(PageImpl<?> pageResult) {
            hasPrevious = pageResult.hasPrevious();
            hasNext = pageResult.hasNext();
            currentPage = pageResult.getNumber();
            totalPage = pageResult.getTotalPages();
            totalElements = pageResult.getTotalElements();
        }
    }

    private void addAccountToModel(Account account, Model model) {
        if (account != null) {
            model.addAttribute(account);
        }
    }
}