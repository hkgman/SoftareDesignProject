package com.utility.company.configuration;

import com.utility.company.dto.UserSignUpDto;
import com.utility.company.model.User;
import com.utility.company.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Random;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomSuccessHandler implements AuthenticationSuccessHandler {
    private final UserService userService;
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        // Метод вызывается при успешной аутентификации пользователя.
        // В зависимости от типа аутентификации определяет, куда перенаправить пользователя.
        String redirectUrl;

        if (authentication.getPrincipal() instanceof DefaultOAuth2User) {
            // Обработка OAuth2 пользователя.
            redirectUrl = handleOAuth2User((DefaultOAuth2User) authentication.getPrincipal());
        } else {
            // Обработка стандартного пользователя.
            redirectUrl = handleUserDetails((UserDetails) authentication.getPrincipal());
        }
        new DefaultRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }

    private String handleOAuth2User(DefaultOAuth2User userDetails) {
        // Обработка пользователя, аутентифицированного через OAuth2.
        // Получаем email пользователя. Если email отсутствует, создаем его на основе логина.
        String email = userDetails.getAttribute("email") != null ?
                userDetails.getAttribute("email") : userDetails.getAttribute("login") + "@gmail.com";

        // Поиск пользователя в базе данных по email.
        User user = userService.findByEmail(email);
        if (user == null) {
            // Если пользователь не найден, создаем нового.
            UserSignUpDto userDto = new UserSignUpDto();
            userDto.setEmail(email); // Устанавливаем email.
            userDto.setFullName(userDetails.getAttribute("name") != null ? userDetails.getAttribute("name") : email); // Устанавливаем имя.
            userDto.setPhone(generateRandomPhoneNumber());
            userDto.setPassword("Dummy");
            userDto.setPasswordConfirm("Dummy");
            userDto.setBirthday(LocalDate.now());
            userService.create(userDto);
        } else {
            userService.callRepositorySave(user);
        }
        return "/";
    }


    public String handleUserDetails(UserDetails userDetails) {
        User user = userService.findByEmail(userDetails.getUsername());

        userService.callRepositorySave(user);

        return "/";
    }


    private String generateRandomPhoneNumber() {
        // Генерация случайного номера телефона в формате +7(XXX)XXX-XX-XX.
        Random random = new Random();
        String regionCode = String.format("%03d", random.nextInt(1000)); // Случайный код региона (XXX).
        String firstThreeDigits = String.format("%03d", random.nextInt(1000)); // Случайные первые три цифры (XXX).
        String lastFourDigits = String.format("%04d", random.nextInt(10000)); // Случайные последние четыре цифры (XXXX).
        // Форматируем номер в строку вида +7(XXX)XXX-XX-XX.
        return String.format("+7(%s)%s-%s-%s", regionCode, firstThreeDigits, lastFourDigits.substring(0, 2),
                lastFourDigits.substring(2));
    }
}
