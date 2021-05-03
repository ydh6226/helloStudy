package com.hellostudy.modules.notification;

import com.hellostudy.modules.account.Account;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class Notification {

    @Id @GeneratedValue
    Long id;

    private String title;

    private String link;

    private String message;

    private boolean checked;

    @ManyToOne
    private Account account;

    private LocalDateTime createdTime;

    @Enumerated(EnumType.STRING)
    private NotificationType notificationType;
}
