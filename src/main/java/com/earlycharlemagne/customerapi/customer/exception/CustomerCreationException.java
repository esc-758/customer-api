package com.earlycharlemagne.customerapi.customer.exception;

public class CustomerCreationException extends RuntimeException {
    public CustomerCreationException(String message) {
        super(message);
    }
}