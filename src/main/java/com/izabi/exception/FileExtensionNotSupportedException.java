package com.izabi.exception;

public class FileExtensionNotSupportedException extends RuntimeException {
    public FileExtensionNotSupportedException(String message) {
        super(message);
    }
}
