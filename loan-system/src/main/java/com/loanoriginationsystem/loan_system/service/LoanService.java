package com.loanoriginationsystem.loan_system.service;

import com.loanoriginationsystem.loan_system.model.ApplicationStatus;
import com.loanoriginationsystem.loan_system.model.Customer;
import com.loanoriginationsystem.loan_system.model.LoanApplication;
import com.loanoriginationsystem.loan_system.repository.CustomerRepository;
import com.loanoriginationsystem.loan_system.repository.LoanApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class LoanService {

    private final CustomerRepository customerRepository;
    private final LoanApplicationRepository loanApplicationRepository;

    @Transactional
    public LoanApplication processLoanApplication(String identityNumber, BigDecimal requestedAmount, Integer termMonths) {
        // 1. Müşteriyi TC/Kimlik numarasına göre bul
        Customer customer = customerRepository.findByIdentityNumber(identityNumber)
                .orElseThrow(() -> new RuntimeException("Müşteri bulunamadı: " + identityNumber));

        // 2. Başvuruyu oluştur
        LoanApplication application = new LoanApplication();
        application.setCustomer(customer);
        application.setRequestedAmount(requestedAmount);
        application.setTermMonths(termMonths);

        // 3. Kural motorunu çalıştır
        evaluateApplication(application, customer);

        // 4. Veritabanına kaydet ve sonucu döndür
        return loanApplicationRepository.save(application);
    }

    private void evaluateApplication(LoanApplication app, Customer customer) {
        // Kural 1: Kredi skoru 500'ün altındaysa direkt ret
        if (customer.getCreditScore() < 500) {
            app.setStatus(ApplicationStatus.REJECTED);
            app.setRejectionReason("Düşük kredi skoru.");
            return;
        }

        // Kural 2: Aylık taksit, gelirin %40'ını geçemez
        BigDecimal monthlyInstallment = app.getRequestedAmount().divide(BigDecimal.valueOf(app.getTermMonths()), 2, RoundingMode.HALF_UP);
        BigDecimal maxAllowedInstallment = customer.getMonthlyIncome().multiply(new BigDecimal("0.40"));

        if (monthlyInstallment.compareTo(maxAllowedInstallment) > 0) {
            app.setStatus(ApplicationStatus.REJECTED);
            app.setRejectionReason("Aylık taksit tutarı, gelir limitini aşıyor.");
            return;
        }

        // Kural 3: Kredi skoru 500-1000 arasıysa manuel inceleme
        if (customer.getCreditScore() <= 1000) {
            app.setStatus(ApplicationStatus.MANUAL_REVIEW);
            return;
        }

        // Diğer durumlarda otomatik onay
        app.setStatus(ApplicationStatus.APPROVED);
    }
}