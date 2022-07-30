package com.example.hhvolgograd.exception;

public class IncorrectJsonException extends RuntimeException {
    public IncorrectJsonException(String message, Exception e) {
        super(message, e);
    }
}
