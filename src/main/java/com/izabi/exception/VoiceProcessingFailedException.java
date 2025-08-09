package com.izabi.exception;

public class VoiceProcessingFailedException extends RuntimeException {
    public VoiceProcessingFailedException(String message) {
        super(message);
    }
}
