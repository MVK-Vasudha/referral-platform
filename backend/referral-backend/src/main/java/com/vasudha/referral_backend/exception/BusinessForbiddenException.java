package com.vasudha.referral_backend.exception;

public class BusinessForbiddenException extends RuntimeException {
    public BusinessForbiddenException(String message) {
        super(message);
    }
}
