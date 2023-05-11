package com.earlycharlemagne.customerapi.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AddressDto {
    String street;
    String postCode;
    String city;
    String country;
}
