package com.hellostudy.modules.notification;

import com.hellostudy.modules.account.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    long countByAccountAndChecked(Account account, boolean b);

    List<Notification> findAllByAccountId(Long accountId);

    @Modifying(clearAutomatically = true)
    @Query("delete from Notification noti where noti.account.id = :accountId and noti.checked = :checked")
    void deleteNotificationsByAccountIdQuery(@Param("accountId") Long accountId, @Param("checked") boolean checked);
}
