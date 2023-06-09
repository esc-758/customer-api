package com.earlycharlemagne.customerapi.customer.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.earlycharlemagne.customerapi.customer.dto.AddressRequest;
import com.earlycharlemagne.customerapi.customer.dto.CustomerDto;
import com.earlycharlemagne.customerapi.customer.dto.CustomerIdResponse;
import com.earlycharlemagne.customerapi.customer.service.CustomerService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {
    private final CustomerService customerService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<CustomerDto> getCustomers(@RequestParam(required = false) String firstName, @RequestParam(required = false) String lastName) {
        if (firstName != null && lastName != null) {
            return customerService.getCustomerByFirstAndLastName(firstName, lastName);
        }

        if (firstName != null) {
            return customerService.getCustomerByFirstName(firstName);
        }

        if (lastName != null) {
            return customerService.getCustomerByLastName(lastName);
        }

        return customerService.getAllCustomers();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public CustomerDto getCustomerById(@PathVariable("id") String globalId) {
        return customerService.getCustomerByGlobalId(globalId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerIdResponse createCustomer(@Valid @RequestBody CustomerDto customerDto) {
        String globalId = customerService.createNewCustomer(customerDto);

        return new CustomerIdResponse(globalId);
    }

    @PutMapping("/{id}/address")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateCustomerAddress(@PathVariable("id") String globalId, @Valid @RequestBody AddressRequest addressRequest) {
        customerService.updateExistingCustomerAddress(globalId, addressRequest.address());
    }
}
