package com.utility.company.dto;

import com.utility.company.model.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserDto {
    @NotBlank(message = "Ожидалась почта")
    @Size(min = 7, max = 64)
    @Pattern(regexp = "^(.+)@(\\S+)$", message = "Неправильная почта")
    private String email;

    @NotBlank(message = "Full name is required")
    @Size(min = 8, max = 64)
    private String fullName;

    @NotBlank(message = "Ожидался телефон")
    @Size(min = 16, max = 16)
    @Pattern(regexp = "^\\+7\\(\\d{3}\\)\\d{3}-\\d{2}-\\d{2}$", message = "Неправильный номер телефона")
    private String phone;

    @NotBlank(message = "Ожидался пароль")
    @Size(min = 8, max = 64)
    private String password;

    public UserDto(User user) {
        this.email = user.getEmail();
        this.fullName = user.getFullName();
        this.phone = user.getPhone();
        this.password = user.getPassword();
    }
}
