package com.utility.company.dto;

import com.utility.company.model.Facility;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ViewFacilityDto {

    private UUID id;

    private String name;

    private Integer price;

    public ViewFacilityDto(Facility facility) {
        this.id = facility.getId();
        this.name = facility.getName();
        this.price = facility.getPrice();
    }
}
