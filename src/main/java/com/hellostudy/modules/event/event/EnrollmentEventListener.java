package com.hellostudy.modules.event.event;

import com.hellostudy.infra.config.AppProperties;
import com.hellostudy.infra.mail.EmailMessage;
import com.hellostudy.infra.mail.EmailService;
import com.hellostudy.modules.account.Account;
import com.hellostudy.modules.event.Enrollment;
import com.hellostudy.modules.event.EnrollmentRepository;
import com.hellostudy.modules.event.Event;
import com.hellostudy.modules.notification.Notification;
import com.hellostudy.modules.notification.NotificationDto;
import com.hellostudy.modules.notification.NotificationRepository;
import com.hellostudy.modules.notification.NotificationType;
import com.hellostudy.modules.study.Study;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;

@Async
@Slf4j
@Component
@Transactional
@RequiredArgsConstructor
public class EnrollmentEventListener {

    private final EnrollmentRepository enrollmentRepository;
    private final NotificationRepository notificationRepository;

    private final EmailService emailService;
    private final TemplateEngine templateEngine;
    private final AppProperties appProperties;



    @EventListener
    public void EnrollmentAcceptEventHandler(EnrollmentEvent enrollmentEvent) {
        Enrollment enrollment = enrollmentRepository
                .findEnrollmentWithAccountByIdQuery(enrollmentEvent.getEnrollmentId());

        Event event = enrollment.getEvent();
        Study study = event.getStudy();

        Account account = enrollment.getAccount();

        if (account.isStudyEnrollmentResultByEmail()) {
            sendStudyNotificationEmail(study, event, account, enrollmentEvent.getMessage());
        }

        if (account.isStudyEnrollmentResultByWeb()) {
            createEnrollmentNotification(study, account, event, enrollmentEvent.getMessage());
        }
    }

    private void createEnrollmentNotification(Study study, Account account, Event event, String message) {
        NotificationDto notificationDto = new NotificationDto();
        notificationDto.setTitle(study.getTitle() + "/" + event.getTitle());
        notificationDto.setLink("/study/" + study.getEncodePath() + "/events/" + event.getId());
        notificationDto.setCreatedTime(LocalDateTime.now());
        notificationDto.setMessage(message);
        notificationDto.setAccount(account);
        notificationDto.setNotificationType(NotificationType.EVENT_ENROLLMENT);

        notificationRepository.save(new Notification(notificationDto));
    }

    private void sendStudyNotificationEmail(Study study, Event event , Account account, String message) {
        Context context = new Context();
        context.setVariable("nickname", account.getNickname());
        context.setVariable("link", "/study/" + study.getEncodePath() + "/events/" + event.getId());
        context.setVariable("linkName", study.getTitle());
        context.setVariable("message", message);
        context.setVariable("host", appProperties.getHost());

        EmailMessage emailMessage = EmailMessage.builder()
                .subject("스터디올래, " + event.getTitle() + "모임 신청 결과입니다.")
                .to(account.getEmail())
                .message(templateEngine.process("mail/simple-link", context))
                .build();

        emailService.send(emailMessage);
    }

}
