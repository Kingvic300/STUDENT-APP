package com.izabi.exception;

public class InvalidEmailRegexException extends RuntimeException {
    public InvalidEmailRegexException(String message) {
        super(message);
    }
}
