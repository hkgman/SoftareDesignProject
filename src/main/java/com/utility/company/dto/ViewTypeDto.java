package com.utility.company.dto;

import com.utility.company.model.Type;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ViewTypeDto {
    private UUID id;

    private String text;

    public ViewTypeDto(Type type) {
        this.id = type.getId();
        this.text = type.getText();
    }
}
