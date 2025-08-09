package com.izabi.exception;

public class NoAuthenticatedUserException extends RuntimeException {
    public NoAuthenticatedUserException(String message) {
        super(message);
    }
}
