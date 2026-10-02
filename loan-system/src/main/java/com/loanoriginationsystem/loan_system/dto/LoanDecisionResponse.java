package com.loanoriginationsystem.loan_system.dto;

import com.loanoriginationsystem.loan_system.model.ApplicationStatus;
import com.loanoriginationsystem.loan_system.model.LoanApplication;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Başvuru sonucu. Müşterinin geliri, kredi skoru ve T.C. kimlik numarası
 * yanıtta yer almaz (veri minimizasyonu); ekranın ihtiyaç duyduğu karar bilgisi döner.
 */
public record LoanDecisionResponse(
        Long id,
        ApplicationStatus status,
        String rejectionReason,
        BigDecimal requestedAmount,
        Integer termMonths,
        BigDecimal monthlyInstallment,
        LocalDateTime applicationDate
) {
    public static LoanDecisionResponse of(LoanApplication app) {
        return new LoanDecisionResponse(
                app.getId(),
                app.getStatus(),
                app.getRejectionReason(),
                app.getRequestedAmount(),
                app.getTermMonths(),
                app.getMonthlyInstallment(),
                app.getApplicationDate()
        );
    }
}
