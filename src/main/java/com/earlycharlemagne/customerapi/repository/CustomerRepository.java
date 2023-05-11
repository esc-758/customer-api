package com.earlycharlemagne.customerapi.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.earlycharlemagne.customerapi.entity.Customer;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByGlobalId(String globalId);
    boolean existsByEmail(String email);
}
