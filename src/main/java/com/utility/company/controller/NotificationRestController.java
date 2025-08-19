package com.utility.company.controller;

import com.utility.company.error.exception.UNotificationNotFoundException;
import com.utility.company.model.User;
import com.utility.company.model.UserNotification;
import com.utility.company.model.UserNotificationKey;
import com.utility.company.repository.UserNotificationRepository;
import com.utility.company.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
@Slf4j
public class NotificationRestController {
    private final UserNotificationRepository userNotificationRepository;
    private final UserService userService;

    @PostMapping("/{id}")
    public Boolean checkNotification(Model model, @PathVariable UUID id) {
        User user = userService.findByPrincipal();
        UserNotification un = userNotificationRepository.findById(new UserNotificationKey(user.getId(), id)).orElse(null);
        if (un == null) {
            throw new UNotificationNotFoundException(id);
        }
        un.setChecked(true);
        Boolean flag = userNotificationRepository.countByUserAndIsCheckedFalse(user) != 1;
        model.addAttribute("hasUnreadNotifications", flag);
        userNotificationRepository.save(un);
        return flag;
    }

    @PostMapping("/delete")
    public Boolean deleteNotifications(Model model, @RequestBody List<UUID> request) {
        Boolean hasUnreadNotifications = userService.deleteNotificationsInUser(userService.findByPrincipal().getEmail(), request);
        model.addAttribute("hasUnreadNotifications", hasUnreadNotifications);
        return hasUnreadNotifications;
    }
}
