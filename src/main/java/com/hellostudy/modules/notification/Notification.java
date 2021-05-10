package com.hellostudy.modules.notification;

import com.hellostudy.modules.account.Account;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
public class Notification {

    @Id @GeneratedValue
    Long id;

    private String title;

    private String link;

    private String message;

    private boolean checked = false;

    @ManyToOne(fetch = FetchType.LAZY)
    private Account account;

    private LocalDateTime createdTime;

    @Enumerated(EnumType.STRING)
    private NotificationType notificationType;

    public Notification(NotificationDto notificationDto) {
        this.title = notificationDto.getTitle();
        this.link = notificationDto.getLink();
        this.message = notificationDto.getMessage();
        this.account = notificationDto.getAccount();
        this.createdTime = notificationDto.getCreatedTime();
        this.notificationType = notificationDto.getNotificationType();
    }

    public void updateAsRead() {
        checked = true;
    }
}
