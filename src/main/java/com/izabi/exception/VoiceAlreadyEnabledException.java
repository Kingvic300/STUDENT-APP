package com.izabi.exception;

public class VoiceAlreadyEnabledException extends RuntimeException {
    public VoiceAlreadyEnabledException(String message) {
        super(message);
    }
}
