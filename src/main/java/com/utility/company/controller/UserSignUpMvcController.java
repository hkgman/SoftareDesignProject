package com.utility.company.controller;

import com.utility.company.dto.UserSignUpDto;
import com.utility.company.model.User;
import com.utility.company.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping(UserSignUpMvcController.SIGNUP_URL)
@RequiredArgsConstructor
@Slf4j
public class UserSignUpMvcController {
    public static final String SIGNUP_URL = "/signup";
    private final UserService userService;

    @GetMapping
    public String showSignupForm(Model model) {
        model.addAttribute("userDto", new UserSignUpDto());
        return "signup";
    }

    @PostMapping
    public String signup(Model model,
                         @ModelAttribute("userDto") @Valid UserSignUpDto userSignupDto,
                         BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> fieldErrors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error -> fieldErrors.put(error.getField(), error.getDefaultMessage()));
            model.addAttribute("errors", fieldErrors);
            return "signup";
        }
        try {
            final User user = userService.create(userSignupDto);
            return "redirect:/email?created=" + user.getEmail();
        } catch (ValidationException e) {
            model.addAttribute("errors", e.getMessage());
            return "signup";
        }
    }
}