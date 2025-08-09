package com.izabi.exception;

public class EmailCannotBeBlankException extends RuntimeException {
    public EmailCannotBeBlankException(String message) {
        super(message);
    }
}
