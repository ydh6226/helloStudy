package com.hellostudy.modules.notification;

import com.hellostudy.modules.account.Account;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationDto {

    private String title;

    private String link;

    private String message;

    private Account account;

    private LocalDateTime createdTime;

    private NotificationType notificationType;
}
