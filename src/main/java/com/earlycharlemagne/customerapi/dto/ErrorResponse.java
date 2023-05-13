package com.earlycharlemagne.customerapi.dto;

import java.util.List;

public record ErrorResponse(String errorCode, List<ValidationError> errors) {
    public ErrorResponse(String errorCode) {
        this(errorCode, null);
    }
}