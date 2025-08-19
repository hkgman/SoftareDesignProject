package com.utility.company;

import com.utility.company.error.exception.NotificationNotFoundException;
import com.utility.company.model.Notification;
import com.utility.company.model.User;
import com.utility.company.model.UserNotification;
import com.utility.company.model.UserNotificationKey;
import com.utility.company.model.enums.UserRole;
import com.utility.company.repository.NotificationRepository;
import com.utility.company.repository.UserNotificationRepository;
import com.utility.company.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)  // Добавляем аннотацию для использования Mockito с JUnit 5
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserNotificationRepository userNotificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    private User user;
    private Notification notification;
    private UserNotification userNotification;

    @BeforeEach
    void setUp() {
        user = new User(UUID.randomUUID(), "test@example.com", "password123", UserRole.USER, "Test User", "1234567890123456", LocalDate.now(), null, null);
        notification = new Notification(UUID.randomUUID(), "Test Notification", null);
        userNotification = new UserNotification(new UserNotificationKey(user.getId(), notification.getId()), user, notification, false);
    }

    @Test
    void testFindByUUID_ShouldReturnNotification() {
        // Given
        when(notificationRepository.findById(notification.getId())).thenReturn(Optional.of(notification));

        // When
        Notification foundNotification = notificationService.findByUUID(notification.getId());

        // Then
        assertEquals(notification.getId(), foundNotification.getId());
        assertEquals(notification.getText(), foundNotification.getText());
    }

    @Test
    void testFindByUUID_ShouldThrowExceptionWhenNotFound() {
        // Given
        when(notificationRepository.findById(notification.getId())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NotificationNotFoundException.class, () -> notificationService.findByUUID(notification.getId()));
    }

    @Test
    void testHasUnreadNotifications_ShouldReturnCount() {
        // Given
        when(userNotificationRepository.countByUserAndIsCheckedFalse(user)).thenReturn(5L);

        // When
        Long unreadCount = notificationService.hasUnreadNotifications(user);

        // Then
        assertEquals(5L, unreadCount);
    }

    @Test
    void testCheckedNotificationsWithoutPage_ShouldSetNotificationsAsChecked() {
        // Given
        when(userNotificationRepository.findByUser(user)).thenReturn(Arrays.asList(userNotification));

        // When
        notificationService.checkedNotificationsWithoutPage(user);

        // Then
        assertTrue(userNotification.isChecked());
        verify(userNotificationRepository, times(1)).save(userNotification);
    }

    @Test
    void testFindByUserOrderByIsChecked_ShouldReturnPageOfUserNotifications() {
        // Given
        Pageable pageable = mock(Pageable.class);
        Page<UserNotification> userNotificationPage = mock(Page.class);
        when(userNotificationRepository.findByUserOrderByIsChecked(user, pageable)).thenReturn(userNotificationPage);

        // When
        Page<UserNotification> result = notificationService.findByUserOrderByIsChecked(user, pageable);

        // Then
        assertNotNull(result);
        verify(userNotificationRepository, times(1)).findByUserOrderByIsChecked(user, pageable);
    }

    @Test
    void testSaveNotification_ShouldReturnSavedNotification() {
        // Given
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        // When
        Notification savedNotification = notificationService.save(notification);

        // Then
        assertNotNull(savedNotification);
        assertEquals(notification.getId(), savedNotification.getId());
    }

    @Test
    void testSaveUserNotification_ShouldReturnSavedUserNotification() {
        // Given
        when(userNotificationRepository.save(any(UserNotification.class))).thenReturn(userNotification);

        // When
        UserNotification savedUserNotification = notificationService.save(userNotification);

        // Then
        assertNotNull(savedUserNotification);
        assertEquals(userNotification.getId(), savedUserNotification.getId());
    }
}