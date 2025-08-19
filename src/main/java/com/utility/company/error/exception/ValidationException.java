package com.utility.company.error.exception;

import java.util.Set;

public class ValidationException extends RuntimeException {
    public ValidationException(Set<String> errors) {
        super(String.join("\n", errors));
    }
}
