package com.utility.company.error;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class AdviceController {
    @ExceptionHandler(Exception.class)
    public String handleUnknownException(Throwable e, Model model) {
        e.printStackTrace();
        model.addAttribute("error", e.getMessage());
        return "error";
    }
}