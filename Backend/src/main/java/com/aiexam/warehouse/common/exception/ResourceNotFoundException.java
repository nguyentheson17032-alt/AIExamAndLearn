package com.aiexam.warehouse.common.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends DomainException {

    public ResourceNotFoundException(String errorCode, String message) {
        super(errorCode, HttpStatus.NOT_FOUND, message);
    }

    public static ResourceNotFoundException question(java.util.UUID id) {
        return new ResourceNotFoundException("QUESTION_NOT_FOUND", "Question not found: " + id);
    }

    public static ResourceNotFoundException exam(java.util.UUID id) {
        return new ResourceNotFoundException("EXAM_NOT_FOUND", "Exam not found: " + id);
    }

    public static ResourceNotFoundException attempt(java.util.UUID id) {
        return new ResourceNotFoundException("ATTEMPT_NOT_FOUND", "Attempt not found: " + id);
    }

    public static ResourceNotFoundException user(String email) {
        return new ResourceNotFoundException("USER_NOT_FOUND", "User not found: " + email);
    }
}
