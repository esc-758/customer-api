package com.earlycharlemagne.customerapi.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CustomerDto {
    String id;
    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "Length of first name must be between 2 and 50 characters")
    String firstName;
    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Length of last name must be between 2 and 50 characters")
    String lastName;
    @NotBlank(message = "Email is required")
    @Email(message = "Email is not valid")
    String email;
    @NotNull(message = "Age is required")
    @Min(value = 18, message = "Age must be 18 and above")
    Integer age;
    @NotBlank(message = "Address is required")
    @Size(min = 2, max = 255, message = "Length of address must be between 2 and 255 characters")
    String address;
}
