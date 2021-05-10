package com.hellostudy.modules.study.event;

import com.hellostudy.infra.config.AppProperties;
import com.hellostudy.infra.mail.EmailMessage;
import com.hellostudy.infra.mail.EmailService;
import com.hellostudy.modules.account.Account;
import com.hellostudy.modules.account.repository.AccountRepository;
import com.hellostudy.modules.notification.Notification;
import com.hellostudy.modules.notification.NotificationDto;
import com.hellostudy.modules.notification.NotificationRepository;
import com.hellostudy.modules.notification.NotificationType;
import com.hellostudy.modules.study.Study;
import com.hellostudy.modules.study.repository.StudyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Async
@Slf4j
@Component
@Transactional
@RequiredArgsConstructor
public class StudyEventListener {

    private final StudyRepository studyRepository;
    private final AccountRepository accountRepository;
    private final NotificationRepository notificationRepository;

    private final EmailService emailService;

    private final TemplateEngine templateEngine;
    private final AppProperties appProperties;

    @EventListener
    public void StudyCreatedEventHandler(StudyCreateEvent studyCreateEvent) {
        Study study = studyRepository.findStudyWithTagsAndZonesById(studyCreateEvent.getStudyId());
        List<Account> accounts = accountRepository.findByTagsAndZone(study.getTags(), study.getZones());

        accounts.forEach(account -> {
            if (account.isStudyCreatedByEmail()) {
                sendStudyNotificationEmail(study, account,"새로운 스터디가 생겼습니다.",
                        String.format("\"%s\" 스터디가 생겼습니다.", study.getTitle()));
            }

            if (account.isStudyCreatedByWeb()) {
                Notification notification =
                        createNotification(study, account, study.getShortDescription(), NotificationType.STUDY_CREATED);
                notificationRepository.save(notification);
            }
        });
    }

    @EventListener
    public void studyUpdatedEventHandler(StudyUpdateEvent studyUpdateEvent) {
        Study study = studyRepository.findStudyWithManagerAndMemberById(studyUpdateEvent.getStudyId());
        Set<Account> accounts = new HashSet<>();
        accounts.addAll(study.getManagers());
        accounts.addAll(study.getMembers());

        accounts.forEach(account -> {
            if (account.isStudyUpdatedByEmail()) {
                sendStudyNotificationEmail(study, account, "스터디 설명이 변경되었습니다.",
                        String.format("\"%s\" 의 설명이 변경되었습니다.", study.getTitle()));
            }

            if (account.isStudyUpdatedByWeb()) {
                Notification notification =
                        createNotification(study, account, studyUpdateEvent.getMessage(), NotificationType.STUDY_UPDATED);
                notificationRepository.save(notification);
            }
        });
    }

    private Notification createNotification(Study study, Account account, String message, NotificationType notificationType) {
        NotificationDto notificationDto = new NotificationDto();
        notificationDto.setTitle(study.getTitle());
        notificationDto.setLink("/study/" + study.getEncodePath());
        notificationDto.setCreatedTime(LocalDateTime.now());
        notificationDto.setMessage(message);
        notificationDto.setAccount(account);
        notificationDto.setNotificationType(notificationType);

        return new Notification(notificationDto);
    }

    private void sendStudyNotificationEmail(Study study, Account account, String message, String subject) {
        Context context = new Context();
        context.setVariable("nickname", account.getNickname());
        context.setVariable("link", "/study/" + study.getEncodePath());
        context.setVariable("linkName", study.getTitle());
        context.setVariable("message", message);
        context.setVariable("host", appProperties.getHost());

        EmailMessage emailMessage = EmailMessage.builder()
                .subject(subject)
                .to(account.getEmail())
                .message(templateEngine.process("mail/simple-link", context))
                .build();

        emailService.send(emailMessage);
    }
}

