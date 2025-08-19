package com.utility.company.error.exception;

import java.util.UUID;

public class NotificationNotFoundException extends RuntimeException {
    public NotificationNotFoundException(UUID id) {
        super(String.format("Notification with id [%s] is not found", id));
    }
}
