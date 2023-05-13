package com.earlycharlemagne.customerapi.customer.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(String errorCode, List<ValidationError> errors) {
    public ErrorResponse(String errorCode) {
        this(errorCode, null);
    }
}