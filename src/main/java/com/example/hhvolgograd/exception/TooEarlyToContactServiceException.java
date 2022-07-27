package com.example.hhvolgograd.exception;

public class TooEarlyToContactServiceException extends RuntimeException {
    public TooEarlyToContactServiceException(String message) {
        super(message);
    }
}
