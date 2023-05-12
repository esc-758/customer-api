package com.earlycharlemagne.customerapi.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.earlycharlemagne.customerapi.entity.Customer;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    List<Customer> findByFirstName(String firstName);
    List<Customer> findByLastName(String lastName);
    Optional<Customer> findByGlobalId(String globalId);
    boolean existsByEmail(String email);
}
