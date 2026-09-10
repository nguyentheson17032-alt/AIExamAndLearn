package com.aiexam.warehouse.common.exception;

import org.springframework.http.HttpStatus;

public class DuplicateResourceException extends DomainException {

    public DuplicateResourceException(String errorCode, String message) {
        super(errorCode, HttpStatus.CONFLICT, message);
    }
}
