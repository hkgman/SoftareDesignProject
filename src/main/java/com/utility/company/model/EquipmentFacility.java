package com.utility.company.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "equipment_facility")
public class EquipmentFacility {
    @EmbeddedId
    private EquipmentFacilityKey id;

    @ManyToOne
    @MapsId("equipmentId")
    @JoinColumn(name = "equipment_id")
    private Equipment equipment;

    @ManyToOne
    @MapsId("facilityId")
    @JoinColumn(name = "facility_id")
    private Facility facility;
}
