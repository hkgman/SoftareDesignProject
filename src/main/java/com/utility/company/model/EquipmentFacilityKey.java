package com.utility.company.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class EquipmentFacilityKey implements Serializable {
    @Column(name = "equipment_id")
    private UUID equipmentId;
    @Column(name = "facility_id")
    private UUID facilityId;
}
