package com.utility.company.error.exception;

import java.util.UUID;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(UUID id) {
        super(String.format("User with id [%s] is not found", id));
    }

}
