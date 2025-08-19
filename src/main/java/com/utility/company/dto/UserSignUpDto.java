package com.utility.company.dto;

import com.utility.company.model.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class UserSignUpDto {
    @NotBlank(message = "Ожидалась почта")
    @Size(min = 7, max = 64)
    @Pattern(regexp = "^(.+)@(\\S+)$", message = "Неправильная почта")
    private String email;

    @NotBlank(message = "Ожидался пароль")
    @Size(min = 8, max = 64)
    private String password;

    @NotBlank(message = "Ожидался пароль для подтверждения")
    @Size(min = 8, max = 64)
    private String passwordConfirm;

    @NotBlank(message = "Ожидалось ФИО")
    @Size(min = 8, max = 64)
    private String fullName;

    @NotBlank(message = "Ожидался телефон")
    @Size(min = 16, max = 16)
    @Pattern(regexp = "^\\+7\\(\\d{3}\\)\\d{3}-\\d{2}-\\d{2}$", message = "Неверный телефон")
    private String phone;

    @NotNull(message = "Ожидалась дата рождения")
    private LocalDate birthday;


    public UserSignUpDto(User user) {
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.passwordConfirm = user.getPassword();
        this.fullName = user.getFullName();
        this.phone = user.getPhone();
        this.birthday = user.getBirthday();
    }
}
