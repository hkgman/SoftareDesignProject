package com.utility.company.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserSignInDto {
    @NotBlank(message = "Ожидалась почта")
    @Size(min = 7, max = 64)
    @Pattern(regexp = "^(.+)@(\\S+)$", message = "Неправильная почта")
    private String username;

    @NotBlank(message = "Ожидался пароль")
    @Size(min = 8, max = 64)
    private String password;

    @NotNull(message = "Ожидался код верификации")
    private Integer otp;
}

