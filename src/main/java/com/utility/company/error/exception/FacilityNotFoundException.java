package com.utility.company.error.exception;

import java.util.UUID;

public class FacilityNotFoundException extends RuntimeException {
    public FacilityNotFoundException(UUID id) {
        super(String.format("Facility with id [%s] is not found", id));
    }
}
