package com.aiexam.warehouse.common.exception;

import org.springframework.http.HttpStatus;

public class BusinessRuleViolationException extends DomainException {

    public BusinessRuleViolationException(String errorCode, String message) {
        super(errorCode, HttpStatus.UNPROCESSABLE_ENTITY, message);
    }
}
