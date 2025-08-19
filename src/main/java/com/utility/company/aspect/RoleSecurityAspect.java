package com.utility.company.aspect;
import com.utility.company.model.User;
import com.utility.company.service.UserService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@RequiredArgsConstructor
public class RoleSecurityAspect {
    private final UserService userService;

    @Around("@annotation(customSecured)")
    public Object checkActiveUser(ProceedingJoinPoint joinPoint,
                                  CustomSecured customSecured) throws Throwable {
        String[] requiredRoles = customSecured.role();
        User user = null;
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof DefaultOAuth2User userDetails) {
            user = userService.findByEmail(userDetails.getAttribute("email"));
        } else if (authentication != null && authentication.getPrincipal() instanceof UserDetails userDetails) {
            user = userService.findByEmail(userDetails.getUsername());
        }
        // Проверяем, что пользователь имеет требуемую роль
        if (user != null && Arrays.asList(requiredRoles).contains(user.getRole().toString())) {
            return joinPoint.proceed(); // Продолжаем выполнение метода, если условие выполнено
        } else {
            throw new AccessDeniedException("Доступ запрещен");
        }
    }
}