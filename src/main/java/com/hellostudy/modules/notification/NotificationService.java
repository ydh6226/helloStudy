package com.hellostudy.modules.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public List<Notification> findAllByAccountId(Long id) {
        return notificationRepository.findAllByAccountId(id);
    }

    public void changeAsRead(List<Notification> notifications) {
        notifications.forEach(Notification::updateAsRead);
    }

    public void deleteNotificationsByAccountId(Long accountId) {
        notificationRepository.deleteNotificationsByAccountIdQuery(accountId, true);
    }
}
