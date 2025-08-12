package com.izabi.exception;

public class PDFNotFoundException extends RuntimeException {
    public PDFNotFoundException(String message) {
        super(message);
    }
}
