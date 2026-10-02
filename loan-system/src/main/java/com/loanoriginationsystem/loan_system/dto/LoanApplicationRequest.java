package com.loanoriginationsystem.loan_system.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

/**
 * Kredi başvuru isteği. Geçersiz girdi servis katmanına hiç ulaşmaz:
 * negatif/sıfır tutar, sıfır vade (sıfıra bölme) ve hatalı TC burada reddedilir.
 */
public record LoanApplicationRequest(

        @NotBlank(message = "T.C. kimlik numarası zorunludur.")
        @Pattern(regexp = "\\d{11}", message = "T.C. kimlik numarası 11 haneli olmalıdır.")
        String identityNumber,

        @NotNull(message = "Kredi tutarı zorunludur.")
        @DecimalMin(value = "1000.00", message = "Kredi tutarı en az 1.000 ₺ olmalıdır.")
        @DecimalMax(value = "10000000.00", message = "Kredi tutarı en fazla 10.000.000 ₺ olabilir.")
        @Digits(integer = 8, fraction = 2, message = "Kredi tutarı en fazla 2 ondalık basamak içerebilir.")
        BigDecimal requestedAmount,

        @NotNull(message = "Vade zorunludur.")
        @Min(value = 3, message = "Vade en az 3 ay olmalıdır.")
        @Max(value = 120, message = "Vade en fazla 120 ay olabilir.")
        Integer termMonths
) {
}
