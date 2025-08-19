package com.utility.company.error.exception;

import java.util.UUID;

public class UNotificationNotFoundException extends RuntimeException {
    public UNotificationNotFoundException(UUID id) {
        super(String.format("User Notification with id [%s] is not found", id));
    }
}
