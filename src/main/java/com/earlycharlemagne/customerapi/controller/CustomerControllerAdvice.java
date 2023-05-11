package com.earlycharlemagne.customerapi.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.earlycharlemagne.customerapi.exception.CustomerCreationException;
import com.earlycharlemagne.customerapi.exception.CustomerNotFoundException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ControllerAdvice
public class CustomerControllerAdvice extends ResponseEntityExceptionHandler {
    @ExceptionHandler(CustomerNotFoundException.class)
    ResponseEntity<String> handleCustomerNotFoundException(CustomerNotFoundException e) {
        log.error("handleCustomerNotFoundException [{}]", e.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                             .body("CUSTOMER_NOT_FOUND");
    }

    @ExceptionHandler(CustomerCreationException.class)
    ResponseEntity<String> handleCustomerCreationException(CustomerCreationException e) {
        log.error("handleCustomerCreationException [{}]", e.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                             .body("EMAIL_EXISTS");
    }

}
