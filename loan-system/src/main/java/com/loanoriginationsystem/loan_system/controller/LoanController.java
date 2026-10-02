package com.loanoriginationsystem.loan_system.controller;

import com.loanoriginationsystem.loan_system.dto.LoanApplicationRequest;
import com.loanoriginationsystem.loan_system.dto.LoanDecisionResponse;
import com.loanoriginationsystem.loan_system.service.LoanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    /**
     * Kredi uzmanı, müşterinin T.C. kimlik numarasıyla başvuru değerlendirir.
     * Kararı veren personel (denetim izi için) JWT'deki kullanıcıdan alınır.
     */
    @PostMapping("/apply")
    public ResponseEntity<LoanDecisionResponse> applyForLoan(@Valid @RequestBody LoanApplicationRequest request,
                                                             Authentication authentication) {
        var result = loanService.processLoanApplication(
                request.identityNumber(),
                request.requestedAmount(),
                request.termMonths(),
                authentication.getName()
        );
        return ResponseEntity.ok(LoanDecisionResponse.of(result));
    }
}
