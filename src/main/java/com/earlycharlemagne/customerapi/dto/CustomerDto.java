package com.earlycharlemagne.customerapi.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CustomerDto {
    String firstName;
    String lastName;
    String email;
    Integer age;
    AddressDto address;
}
