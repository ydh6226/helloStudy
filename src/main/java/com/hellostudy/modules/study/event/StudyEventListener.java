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
import com.hellostudy.modules.study.StudyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.util.List;

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
                sendStudyCreatedEmail(study, account);

            }
            
            if (account.isStudyCreatedByWeb()) {
                Notification notification = createNotification(study, account);
                notificationRepository.save(notification);
            }
        });


    }

    private Notification createNotification(Study study, Account account) {
        NotificationDto notificationDto = new NotificationDto();
        notificationDto.setTitle(study.getTitle());
        notificationDto.setLink("/study/" + study.getEncodePath());
        notificationDto.setCreatedTime(LocalDateTime.now());
        notificationDto.setMessage(study.getShortDescription());
        notificationDto.setAccount(account);
        notificationDto.setNotificationType(NotificationType.STUDY_CREATED);

        return new Notification(notificationDto);
    }

    private void sendStudyCreatedEmail(Study study, Account account) {
        Context context = new Context();
        context.setVariable("nickname", account.getNickname());
        context.setVariable("link", "/study/" + study.getEncodePath());
        context.setVariable("linkName", study.getTitle());
        context.setVariable("message", "새로운 스터디가 생겼습니다.");
        context.setVariable("host", appProperties.getHost());

        String message = templateEngine.process("mail/simple-link", context);

        EmailMessage emailMessage = EmailMessage.builder()
                .subject(String.format("스터디 올래, \"%s\" 스터디가 생겼습니다.", study.getTitle()))
                .to(account.getEmail())
                .message(message)
                .build();

        emailService.send(emailMessage);
    }
}

