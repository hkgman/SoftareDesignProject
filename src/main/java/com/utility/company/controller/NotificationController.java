package com.utility.company.controller;

import com.utility.company.model.UserNotification;
import com.utility.company.model.enums.UserRole;
import com.utility.company.service.NotificationService;
import com.utility.company.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@Secured({UserRole.AsString.USER, UserRole.AsString.ADMIN})
@RequestMapping("/notifications")
@Slf4j
public class NotificationController {
    private final UserService userService;
    private final NotificationService notificationService;

    @GetMapping
    public String userNotifications(Model model, Pageable pageable) {
        Page<UserNotification> notifications = userService.findNotificationsOrderByIsChecked(PageRequest.of(pageable.getPageNumber(), 8));

        model.addAttribute("userDetails", userService.findByPrincipal().getFullName());
        model.addAttribute("notifications", notifications.stream().toList());

        model.addAttribute("currentPage", pageable.getPageNumber());
        model.addAttribute("totalPages", notifications.getTotalPages());
        return "notifications";
    }

    @PostMapping("/all-checked")
    public String allChecked(Model model) {
        notificationService.checkedNotificationsWithoutPage(userService.findByPrincipal());
        model.addAttribute("hasUnreadNotifications", false);
        return "redirect:/notifications";
    }
}
