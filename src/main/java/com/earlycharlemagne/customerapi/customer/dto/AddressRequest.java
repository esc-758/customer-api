package com.earlycharlemagne.customerapi.customer.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public record AddressRequest(
    @NotBlank(message = "Address is required")
    @Size(min = 2, max = 255, message = "Length of address must be between 2 and 255 characters")
    String address
) {}
