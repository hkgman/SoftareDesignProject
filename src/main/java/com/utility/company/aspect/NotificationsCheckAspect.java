package com.utility.company.aspect;

import com.utility.company.error.exception.UserNotFoundException;
import com.utility.company.model.User;
import com.utility.company.service.NotificationService;
import com.utility.company.service.UserService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

@Aspect
@Component
@RequiredArgsConstructor
public class NotificationsCheckAspect {
    private final NotificationService notificationService;
    private final UserService userService;

    @Before("execution(* com.utility.company.controller.*.*(..)) && args(model,..) ")
    public void beforeControllerMethodExecution(Model model) {
        try{
            User user = userService.findByPrincipal();
            Long hasUnreadNotifications = 0L;
            if (user != null) {
                hasUnreadNotifications = notificationService.hasUnreadNotifications(user);
            }
            model.addAttribute("hasUnreadNotifications", hasUnreadNotifications > 0);
        }catch (UserNotFoundException ex)
        {

        }

    }
}
