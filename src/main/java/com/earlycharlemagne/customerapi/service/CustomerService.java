package com.earlycharlemagne.customerapi.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.earlycharlemagne.customerapi.dto.CustomerDto;
import com.earlycharlemagne.customerapi.entity.Customer;
import com.earlycharlemagne.customerapi.exception.CustomerCreationException;
import com.earlycharlemagne.customerapi.exception.CustomerNotFoundException;
import com.earlycharlemagne.customerapi.repository.CustomerRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomerService {
    private final CustomerRepository repository;
    
    public List<CustomerDto> getAllCustomers() {
        return repository.findAll()
                         .stream()
                         .map(this::mapToCustomerDto)
                         .toList();
    }

    public List<CustomerDto> getCustomerByLastName(String lastName) {
        return repository.findByLastNameIgnoreCase(lastName)
                         .stream()
                         .map(this::mapToCustomerDto)
                         .toList();
    }

    public List<CustomerDto> getCustomerByFirstName(String firstName) {
        return repository.findByFirstNameIgnoreCase(firstName)
                         .stream()
                         .map(this::mapToCustomerDto)
                         .toList();
    }

    public List<CustomerDto> getCustomerByFirstAndLastName(String firstName, String lastName) {
        return repository.findByFirstNameIgnoreCaseAndLastNameIgnoreCase(firstName, lastName)
                         .stream()
                         .map(this::mapToCustomerDto)
                         .toList();
    }

    public CustomerDto getCustomerByGlobalId(String globalId) {
        return repository.findByGlobalId(globalId)
                         .map(this::mapToCustomerDto)
                         .orElseThrow(() -> new CustomerNotFoundException("Customer with globalId [%s] does not exist".formatted(globalId)));
    }

    public CustomerDto mapToCustomerDto(Customer customer) {
        return CustomerDto.builder()
                          .id(customer.getGlobalId())
                          .firstName(customer.getFirstName())
                          .lastName(customer.getLastName())
                          .age(customer.getAge())
                          .email(customer.getEmail())
                          .address(customer.getAddress())
                          .build();
    }

    public String createNewCustomer(CustomerDto customerDto) {
        if (customerExists(customerDto)) {
            throw new CustomerCreationException("Failed to create customer. Email already exists");
        }

        Customer customer = mapToCustomer(customerDto);
        customer.setGlobalId(UUID.randomUUID().toString());

        return repository.save(customer)
                         .getGlobalId();
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

    public void updateExistingCustomerAddress(String globalId, String address) {
        Customer customer = repository.findByGlobalId(globalId)
                                      .orElseThrow(() -> new CustomerNotFoundException("Failed to update address. Customer with globalId [%s] does not exist".formatted(globalId)));

        customer.setAddress(address);

        repository.save(customer);
    }
}