package com.loanoriginationsystem.loan_system.controller;

import com.loanoriginationsystem.loan_system.model.LoanApplication;
import com.loanoriginationsystem.loan_system.service.LoanService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LoanController {

    private final LoanService loanService;

    @PostMapping("/apply")
    public ResponseEntity<LoanApplication> applyForLoan(@RequestBody LoanRequest request) {
        LoanApplication result = loanService.processLoanApplication(
                request.getIdentityNumber(),
                request.getRequestedAmount(),
                request.getTermMonths()
        );
        return ResponseEntity.ok(result);
    }
}

// Frontend'den gelen istek gövdesini (JSON) karşılayan Data Transfer Object (DTO)
@Data
class LoanRequest {
    private String identityNumber;
    private BigDecimal requestedAmount;
    private Integer termMonths;
}