package com.training.app.exception;

public class UnsupportedFileType extends RuntimeException {
    public UnsupportedFileType(String message) {
        super(message);
    }
}
