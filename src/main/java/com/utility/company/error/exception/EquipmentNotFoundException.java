package com.utility.company.error.exception;

import java.util.UUID;

public class EquipmentNotFoundException extends RuntimeException {
    public EquipmentNotFoundException(UUID id) {
        super(String.format("Equipment with id [%s] is not found", id));
    }
}
