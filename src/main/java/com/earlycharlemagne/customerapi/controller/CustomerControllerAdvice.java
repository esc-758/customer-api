package com.earlycharlemagne.customerapi.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.earlycharlemagne.customerapi.dto.ErrorResponse;
import com.earlycharlemagne.customerapi.dto.ValidationError;
import com.earlycharlemagne.customerapi.exception.CustomerCreationException;
import com.earlycharlemagne.customerapi.exception.CustomerNotFoundException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class CustomerControllerAdvice {
    @ExceptionHandler(CustomerNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    String handleCustomerNotFoundException(CustomerNotFoundException e) {
        log.error("handleCustomerNotFoundException [{}]", e.getMessage());

        return "CUSTOMER_NOT_FOUND";
    }

    @ExceptionHandler(CustomerCreationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    String handleCustomerCreationException(CustomerCreationException e) {
        log.error("handleCustomerCreationException [{}]", e.getMessage());

        return "EMAIL_EXISTS";
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ErrorResponse handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error("handleMethodArgumentNotValidException [{}]", e.getMessage());
        List<ValidationError> errors = e.getBindingResult()
                                        .getFieldErrors()
                                        .stream()
                                        .map(fieldError -> new ValidationError(fieldError.getField(), fieldError.getDefaultMessage()))
                                        .toList();

        return new ErrorResponse("VALIDATION_ERROR", errors);
    }
}
