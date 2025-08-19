package com.utility.company.controller;

import com.utility.company.dto.UserDto;
import com.utility.company.error.exception.UserNotFoundException;
import com.utility.company.model.User;
import com.utility.company.model.enums.UserRole;
import com.utility.company.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@Secured({UserRole.AsString.USER, UserRole.AsString.ADMIN})
@RequestMapping("/user")
@Slf4j
public class UserController {
    private final UserService userService;

    @GetMapping("/edit")
    public String edit(Model model) {
        User user = getCurrentUser();
        if (user != null) {
            UUID id = user.getId();
            UserDto userDto = new UserDto(userService.find(id));
            userDto.setPassword("");
            model.addAttribute("userDto", userDto);
        }
        return "user-edit";
    }

    @PostMapping("/edit")
    public String update(Model model,
                         @ModelAttribute("userDto") @Valid UserDto userDto,
                         BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> fieldErrors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error -> fieldErrors.put(error.getField(), error.getDefaultMessage()));
            model.addAttribute("errors", fieldErrors);
            return "user-edit";
        }
        User user = getCurrentUser();
        if (user == null) {
            throw new UserNotFoundException(null);
        }
        userService.update(user.getId(), userDto);
        UserDetails updatedUserDetails = userService.loadUserByUsername(userDto.getEmail());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(updatedUserDetails, updatedUserDetails.getPassword(), updatedUserDetails.getAuthorities())
        );
        return "user-edit";
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof DefaultOAuth2User userDetails) {
                String email = userDetails.getAttribute("email");
                return userService.findByEmail(email);
            } else if (principal instanceof UserDetails userDetails) {
                return userService.findByEmail(userDetails.getUsername());
            }
        }
        return null;
    }
}
