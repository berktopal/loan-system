package com.loanoriginationsystem.loan_system.repository;

import com.loanoriginationsystem.loan_system.model.LoanApplication;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanApplicationRepository extends JpaRepository<LoanApplication, Long> {
}