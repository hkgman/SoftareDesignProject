package com.utility.company;

import com.utility.company.dto.NotificationDto;
import com.utility.company.dto.TypeDto;
import com.utility.company.model.*;
import com.utility.company.model.enums.UserRole;
import com.utility.company.repository.NotificationRepository;
import com.utility.company.repository.UserNotificationRepository;
import com.utility.company.repository.UserRepository;
import com.utility.company.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class NotificationServiceH2Test {
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserNotificationRepository userNotificationRepository;

    @Autowired
    UserRepository userRepository;
    private User user;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();
        userNotificationRepository.deleteAll();
        userRepository.deleteAll();
        user = new User();
        user.setEmail("test.user@example.com");
        user.setPassword("securePassword");
        user.setRole(UserRole.USER);
        user.setFullName("Test User");
        user.setPhone("1234567812345678");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        userRepository.save(user);
    }

    @Test
    void testHasUnreadNotifications() {
        Notification notification = new Notification();
        notification.setText("Unread notification");
        Notification savedNotification = notificationService.save(notification);

        UserNotification userNotification = new UserNotification();
        userNotification.setId(new UserNotificationKey(user.getId(), savedNotification.getId()));
        userNotification.setUser(user);
        userNotification.setNotification(savedNotification);
        userNotification.setChecked(false);
        notificationService.save(userNotification);

        Long unreadCount = notificationService.hasUnreadNotifications(user);
        assertEquals(1, unreadCount);
    }

    @Test
    void testFindNotification() {

        Notification notification = new Notification();
        notification.setText("Findable not");
        Notification notificationCur = notificationService.save(notification);

        Notification find = notificationService.findByUUID(notificationCur.getId());

        assertNotNull(find);
        assertEquals("Findable not", find.getText());
    }

    @Test
    void testCheckedNotificationsWithoutPage() {
        Notification notification = new Notification();
        notification.setText("Notification to check");
        Notification savedNotification = notificationService.save(notification);

        UserNotification userNotification = new UserNotification();
        userNotification.setId(new UserNotificationKey(user.getId(), savedNotification.getId()));
        userNotification.setUser(user);
        userNotification.setNotification(savedNotification);
        userNotification.setChecked(false);
        notificationService.save(userNotification);

        notificationService.checkedNotificationsWithoutPage(user);

        UserNotification updatedNotification = userNotificationRepository
                .findById(userNotification.getId())
                .orElseThrow();
        assertTrue(updatedNotification.isChecked());
    }
    @Test
    void testFindByUserOrderByIsChecked() {
        Notification notification1 = new Notification();
        notification1.setText("Checked notification");
        Notification savedNotification1 = notificationService.save(notification1);

        Notification notification2 = new Notification();
        notification2.setText("Unchecked notification");
        Notification savedNotification2 = notificationService.save(notification2);

        UserNotification userNotification1 = new UserNotification();
        userNotification1.setId(new UserNotificationKey(user.getId(), savedNotification1.getId()));
        userNotification1.setUser(user);
        userNotification1.setNotification(savedNotification1);
        userNotification1.setChecked(true);
        notificationService.save(userNotification1);

        UserNotification userNotification2 = new UserNotification();
        userNotification2.setId(new UserNotificationKey(user.getId(), savedNotification2.getId()));
        userNotification2.setUser(user);
        userNotification2.setNotification(savedNotification2);
        userNotification2.setChecked(false); // Не прочитано
        notificationService.save(userNotification2);

        Pageable pageable = PageRequest.of(0, 10);
        Page<UserNotification> notificationsPage = notificationService.findByUserOrderByIsChecked(user, pageable);

        List<UserNotification> notifications = notificationsPage.getContent();
        assertEquals(2, notifications.size());
        assertFalse(notifications.get(0).isChecked()); // Непрочитанные уведомления идут первыми
        assertTrue(notifications.get(1).isChecked());
    }
}


