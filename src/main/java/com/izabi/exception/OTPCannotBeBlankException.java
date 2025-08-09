package com.izabi.exception;

public class OTPCannotBeBlankException extends RuntimeException {
    public OTPCannotBeBlankException(String message) {
        super(message);
    }
}
