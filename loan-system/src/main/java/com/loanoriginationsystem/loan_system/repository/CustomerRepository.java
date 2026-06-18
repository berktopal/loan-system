package com.loanoriginationsystem.loan_system.repository;

import com.loanoriginationsystem.loan_system.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByIdentityNumber(String identityNumber);
}