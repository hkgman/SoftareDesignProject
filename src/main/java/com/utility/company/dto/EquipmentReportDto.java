package com.utility.company.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
@NoArgsConstructor
public class EquipmentReportDto {
    private String equipmentName;
    private String equipmentType;
    private String userName;
    private List<FacilityInfo> facilities; // создадим новый класс FacilityInfo для хранения информации о facility

    public EquipmentReportDto(String equipmentName, String equipmentType, String userName, List<FacilityInfo> facilities) {
        this.equipmentName = equipmentName;
        this.equipmentType = equipmentType;
        this.userName = userName;
        this.facilities = facilities;
    }

    @Getter
    @Setter
    public static class FacilityInfo {
        private String name;
        private Integer price;

        public FacilityInfo(String name, Integer price) {
            this.name = name;
            this.price = price;
        }
    }
}