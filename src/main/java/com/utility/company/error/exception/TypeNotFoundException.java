package com.utility.company.error.exception;

import java.util.UUID;

public class TypeNotFoundException extends RuntimeException {
    public TypeNotFoundException(UUID id) {
        super(String.format("Type with id [%s] is not found", id));
    }
}
