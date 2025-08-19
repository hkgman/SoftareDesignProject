package com.utility.company.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;
@Getter
@Setter
@NoArgsConstructor
public class EquipmentReportEqDto {
    private UUID equipmentId;
    private String equipmentName;
    private String equipmentType;
    private String userName;

    public EquipmentReportEqDto(UUID equipmentId, String equipmentName, String equipmentType, String userName) {
        this.equipmentId = equipmentId;
        this.equipmentName = equipmentName;
        this.equipmentType = equipmentType;
        this.userName = userName;
    }

    // Getters и setters
}
