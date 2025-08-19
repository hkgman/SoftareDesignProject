package com.utility.company.dto;

import com.utility.company.model.Type;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class TypeDto {
    private UUID id;

    @NotBlank(message = "Ожидалось описание типа")
    @Size(min = 8, max = 64)
    private String text;

    public TypeDto(Type type) {
        this.id = type.getId();
        this.text = type.getText();
    }
}
