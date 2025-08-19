package com.utility.company.service;

import com.utility.company.error.exception.NotificationNotFoundException;
import com.utility.company.model.*;
import com.utility.company.repository.NotificationRepository;
import com.utility.company.repository.UserNotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserNotificationRepository userNotificationRepository;


    @Transactional(readOnly = true)
    public Notification findByUUID(UUID id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public Long hasUnreadNotifications(User user) {
        return userNotificationRepository.countByUserAndIsCheckedFalse(user);
    }

    @Transactional
    public void checkedNotificationsWithoutPage(User user) {
        List<UserNotification> notifications = userNotificationRepository.findByUser(user);
        for (UserNotification notification : notifications) {
            notification.setChecked(true);
            userNotificationRepository.save(notification);
        }
    }

    @Transactional(readOnly = true)
    public Page<UserNotification> findByUserOrderByIsChecked(User user, Pageable pageable) {
        return userNotificationRepository
                .findByUserOrderByIsChecked(user, pageable);
    }

    public Notification save(Notification notification) {
        // Perform any additional logic or validation here if needed
        return notificationRepository.save(notification);
    }

    public UserNotification save(UserNotification notification) {
        // Perform any additional logic or validation here if needed
        return userNotificationRepository.save(notification);
    }
}
