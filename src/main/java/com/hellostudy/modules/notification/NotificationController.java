package com.hellostudy.modules.notification;

import com.hellostudy.modules.account.Account;
import com.hellostudy.modules.account.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/notifications")
    public String newNotificationsView(@CurrentUser Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute("type", "new");

        List<Notification> notifications = notificationService.findAllByAccountId(account.getId());
        List<Notification> unCheckedNotifications = new ArrayList<>();

        notifications.forEach(notification -> {
            if (!notification.isChecked()) {
                unCheckedNotifications.add(notification);
            }
        });

        modelAddClassifiedNotifications(model, unCheckedNotifications);
        model.addAttribute("checkedNotificationsCount", notifications.size() - unCheckedNotifications.size());
        model.addAttribute("unCheckedNotificationsCount", unCheckedNotifications.size());
        notificationService.changeAsRead(unCheckedNotifications);
        return "notifications/view";
    }

    @GetMapping("/notifications/old")
    public String oldNotificationsView(@CurrentUser Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute("type", "old");

        List<Notification> notifications = notificationService.findAllByAccountId(account.getId());
        List<Notification> checkedNotifications = new ArrayList<>();

        notifications.forEach(notification -> {
            if (notification.isChecked()) {
                checkedNotifications.add(notification);
            }
        });

        modelAddClassifiedNotifications(model, checkedNotifications);
        model.addAttribute("checkedNotificationsCount", checkedNotifications.size());
        model.addAttribute("unCheckedNotificationsCount", notifications.size() - checkedNotifications.size());
        return "notifications/view";
    }

    @PostMapping("/notifications/delete")
    public String deleteNotifications(@CurrentUser Account account) {
        notificationService.deleteNotificationsByAccountId(account.getId());
        return "redirect:/notifications";
    }

    private void modelAddClassifiedNotifications(Model model, List<Notification> notifications) {
        List<Notification> createdStudyNotification = new ArrayList<>();
        List<Notification> updatedStudyNotification = new ArrayList<>();
        List<Notification> eventEnrollmentNotification = new ArrayList<>();

        for (Notification notification : notifications) {
            switch (notification.getNotificationType()) {
                case STUDY_CREATED:
                    createdStudyNotification.add(notification);
                    break;
                case STUDY_UPDATED:
                    updatedStudyNotification.add(notification);
                    break;
                case EVENT_ENROLLMENT:
                    eventEnrollmentNotification.add(notification);
                    break;
            }
        }

        model.addAttribute("totalCount", notifications.size());
        model.addAttribute("createdStudyNotification", createdStudyNotification);
        model.addAttribute("updatedStudyNotification", updatedStudyNotification);
        model.addAttribute("eventEnrollmentNotification", eventEnrollmentNotification);
    }
}
