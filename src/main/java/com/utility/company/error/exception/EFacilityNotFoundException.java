package com.utility.company.error.exception;

import com.utility.company.model.EquipmentFacilityKey;

import java.util.UUID;

public class EFacilityNotFoundException extends RuntimeException{
    public EFacilityNotFoundException(EquipmentFacilityKey key) {
        super(String.format("Equipment Facility with id [%s] is not found", key.getFacilityId() +" "+ key.getEquipmentId()));
    }
}
