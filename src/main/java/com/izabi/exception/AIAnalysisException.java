package com.izabi.exception;

public class AIAnalysisException extends RuntimeException {
    public AIAnalysisException(String message) {
        super(message);
    }
    public AIAnalysisException(String message, Throwable cause) {
        super(message, cause);
    }
}
