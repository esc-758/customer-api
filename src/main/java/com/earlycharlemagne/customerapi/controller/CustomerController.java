package com.earlycharlemagne.customerapi.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.earlycharlemagne.customerapi.dto.CustomerDto;
import com.earlycharlemagne.customerapi.entity.Customer;
import com.earlycharlemagne.customerapi.exception.CustomerCreationException;
import com.earlycharlemagne.customerapi.exception.CustomerNotFoundException;
import com.earlycharlemagne.customerapi.repository.CustomerRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {
    private final CustomerRepository repository;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<CustomerDto> getCustomers() {
        return repository.findAll()
                         .stream()
                         .map(this::mapToCustomerDto)
                         .toList();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public CustomerDto getCustomerById(@PathVariable String id) {
        return repository.findByGlobalId(id)
                         .map(this::mapToCustomerDto)
                         .orElseThrow(() -> new CustomerNotFoundException("Customer with id [%s] does not exist".formatted(id)));
    }

    private CustomerDto mapToCustomerDto(Customer customer) {
        return CustomerDto.builder()
                          .firstName(customer.getFirstName())
                          .lastName(customer.getLastName())
                          .age(customer.getAge())
                          .email(customer.getEmail())
                          .address(customer.getAddress())
                          .build();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public String createCustomer(@RequestBody CustomerDto customerDto) {
        if (customerExists(customerDto)) {
            throw new CustomerCreationException("Failed to create customer. Email already exists");
        }

        Customer customer = mapToCustomer(customerDto);
        customer.setGlobalId(UUID.randomUUID().toString());

        return repository.save(customer).getGlobalId();
    }

    private boolean customerExists(CustomerDto customerDto) {
        return repository.existsByEmail(customerDto.getEmail());
    }

    private Customer mapToCustomer(CustomerDto customerDto) {
        Customer customer = new Customer();

        customer.setFirstName(customerDto.getFirstName());
        customer.setLastName(customerDto.getLastName());
        customer.setAge(customerDto.getAge());
        customer.setEmail(customerDto.getEmail());
        customer.setAddress(customerDto.getAddress());

        return customer;
    }
}
