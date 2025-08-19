package com.utility.company.dto;

import com.utility.company.model.Facility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class FacilityDto {
    private UUID id;

    @NotBlank(message = "Ожидалось имя")
    @Size(min = 8, max = 64)
    private String name;

    @NotNull(message = "Ожидалась цена")
    private Integer price;

    public FacilityDto(Facility facility) {
        this.id = facility.getId();
        this.name = facility.getName();
        this.price = facility.getPrice();
    }
}
