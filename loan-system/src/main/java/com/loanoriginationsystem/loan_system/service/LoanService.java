package com.loanoriginationsystem.loan_system.service;

import com.loanoriginationsystem.loan_system.model.ApplicationStatus;
import com.loanoriginationsystem.loan_system.model.Customer;
import com.loanoriginationsystem.loan_system.model.LoanApplication;
import com.loanoriginationsystem.loan_system.repository.CustomerRepository;
import com.loanoriginationsystem.loan_system.repository.LoanApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class LoanService {

    static final int MIN_CREDIT_SCORE = 500;
    static final int MANUAL_REVIEW_MAX_SCORE = 1000;
    static final BigDecimal MAX_INSTALLMENT_TO_INCOME = new BigDecimal("0.40");

    private final CustomerRepository customerRepository;
    private final LoanApplicationRepository loanApplicationRepository;

    @Transactional
    public LoanApplication processLoanApplication(String identityNumber, BigDecimal requestedAmount,
                                                  Integer termMonths, String evaluatedBy) {
        // Controller'daki doğrulamaya ek savunma: servis başka yerden çağrılsa da
        // negatif tutar onaylanamaz, sıfır vade sıfıra bölme hatasına yol açamaz.
        if (requestedAmount == null || requestedAmount.signum() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Kredi tutarı sıfırdan büyük olmalıdır.");
        }
        if (termMonths == null || termMonths <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vade sıfırdan büyük olmalıdır.");
        }

        // 1. Müşteriyi T.C. kimlik numarasına göre bul (hata mesajında TC tekrar edilmez)
        Customer customer = customerRepository.findByIdentityNumber(identityNumber)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Bu T.C. kimlik numarasına kayıtlı müşteri bulunamadı."));

        // 2. Başvuruyu oluştur
        LoanApplication application = new LoanApplication();
        application.setCustomer(customer);
        application.setRequestedAmount(requestedAmount);
        application.setTermMonths(termMonths);
        application.setMonthlyInstallment(requestedAmount.divide(BigDecimal.valueOf(termMonths), 2, RoundingMode.HALF_UP));
        application.setCreatedBy(evaluatedBy);

        // 3. Kural motorunu çalıştır
        evaluateApplication(application, customer);

        // 4. Veritabanına kaydet ve sonucu döndür
        return loanApplicationRepository.save(application);
    }

    void evaluateApplication(LoanApplication app, Customer customer) {
        // Kural 1: Kredi skoru eşik altındaysa direkt ret
        if (customer.getCreditScore() < MIN_CREDIT_SCORE) {
            app.setStatus(ApplicationStatus.REJECTED);
            app.setRejectionReason("Düşük kredi skoru.");
            return;
        }

        // Kural 2: Aylık taksit, gelirin %40'ını geçemez
        BigDecimal maxAllowedInstallment = customer.getMonthlyIncome().multiply(MAX_INSTALLMENT_TO_INCOME);
        if (app.getMonthlyInstallment().compareTo(maxAllowedInstallment) > 0) {
            app.setStatus(ApplicationStatus.REJECTED);
            app.setRejectionReason("Aylık taksit tutarı, gelir limitini aşıyor.");
            return;
        }

        // Kural 3: Kredi skoru orta bantta ise manuel inceleme
        if (customer.getCreditScore() <= MANUAL_REVIEW_MAX_SCORE) {
            app.setStatus(ApplicationStatus.MANUAL_REVIEW);
            return;
        }

        // Diğer durumlarda otomatik onay
        app.setStatus(ApplicationStatus.APPROVED);
    }
}
