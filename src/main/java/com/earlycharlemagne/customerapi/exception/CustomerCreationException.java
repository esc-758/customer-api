package com.earlycharlemagne.customerapi.exception;

public class CustomerCreationException extends RuntimeException {
    public CustomerCreationException(String message) {
        super(message);
    }
}