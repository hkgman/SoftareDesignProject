package com.utility.company.dto;

import com.utility.company.model.Equipment;
import com.utility.company.model.enums.Status;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class EquipmentDto {
    private UUID id;

    @NotBlank(message = "Ожидалось имя")
    @Size(min = 8, max = 64)
    private String name;

    @NotNull(message = "Ожидался тип оборудования")
    private UUID equipmentType;

    public EquipmentDto(Equipment equipment) {
        this.id = equipment.getId();
        this.name = equipment.getName();
        this.equipmentType = equipment.getEquipmentType().getId();
    }
}
