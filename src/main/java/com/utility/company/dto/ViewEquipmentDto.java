package com.utility.company.dto;

import com.utility.company.model.Equipment;
import com.utility.company.model.Type;
import com.utility.company.model.enums.Status;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ViewEquipmentDto {
    private UUID id;

    private String name;

    private LocalDate date;


    private Type equipmentType;

    private Status status;

    public ViewEquipmentDto(Equipment equipment) {
        this.id = equipment.getId();
        this.name = equipment.getName();
        this.date = equipment.getDate();
        this.equipmentType = equipment.getEquipmentType();
        this.status = equipment.getStatus();
    }
}
