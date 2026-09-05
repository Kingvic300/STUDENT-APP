package com.izabi.exception;

public class VoiceDoesNotMatchException extends RuntimeException {
    public VoiceDoesNotMatchException(String message) {
        super(message);
    }
}
