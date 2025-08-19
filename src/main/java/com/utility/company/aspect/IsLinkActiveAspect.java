package com.utility.company.aspect;

import com.utility.company.model.User;
import com.utility.company.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class IsLinkActiveAspect {
    private final UserService userService;

    @Before("execution(* *(..)) && @within(org.springframework.stereotype.Controller)  && args(model,..) ")
    public void isActive(Model model) {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof DefaultOAuth2User userDetails) {
            User user = userService.findByEmail(userDetails.getAttribute("email"));
            if (user != null) {
                model.addAttribute("userRole", userService.findByPrincipal().getRole().toString());
            }
        } else if (authentication != null && authentication.getPrincipal() instanceof UserDetails user) {
            User users = userService.findByEmail(user.getUsername());
            model.addAttribute("userRole", users.getRole().toString());
        }

    }
}
