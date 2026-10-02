package com.loanoriginationsystem.loan_system;

import com.loanoriginationsystem.loan_system.model.ApplicationStatus;
import com.loanoriginationsystem.loan_system.model.Customer;
import com.loanoriginationsystem.loan_system.model.LoanApplication;
import com.loanoriginationsystem.loan_system.repository.CustomerRepository;
import com.loanoriginationsystem.loan_system.repository.LoanApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Comparator;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Gerçek HTTP sunucusu + PostgreSQL üzerinde uçtan uca API testleri.
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "app.security.admin.username=officer",
                "app.security.admin.password=test-password-123"
        })
class LoanApiIntegrationTests {

    private static final HttpClient HTTP = HttpClient.newHttpClient();

    @Value("${local.server.port}")
    int port;

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    LoanApplicationRepository loanApplicationRepository;

    String token;

    @BeforeEach
    void login() throws Exception {
        HttpResponse<String> res = post("/api/auth/login",
                "{\"username\":\"officer\",\"password\":\"test-password-123\"}", null);
        assertEquals(200, res.statusCode(), res.body());
        Matcher m = Pattern.compile("\"token\"\\s*:\\s*\"([^\"]+)\"").matcher(res.body());
        assertTrue(m.find(), res.body());
        token = m.group(1);
    }

    // ---------- Kimlik doğrulama ----------

    @Test
    void wrongPasswordReturns401() throws Exception {
        assertEquals(401, post("/api/auth/login", "{\"username\":\"officer\",\"password\":\"wrong\"}", null).statusCode());
    }

    @Test
    void hardcodedLegacyCredentialsNoLongerWork() throws Exception {
        assertEquals(401, post("/api/auth/login", "{\"username\":\"admin\",\"password\":\"12345\"}", null).statusCode());
    }

    @Test
    void missingTokenReturns401() throws Exception {
        assertEquals(401, apply("12345678901", "10000", 12, null).statusCode());
    }

    @Test
    void malformedTokenReturns401NotServerError() throws Exception {
        assertEquals(401, apply("12345678901", "10000", 12, "not-a-jwt").statusCode());
        assertEquals(401, apply("12345678901", "10000", 12,
                "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJvZmZpY2VyIn0.invalidsignature").statusCode());
    }

    // ---------- Girdi doğrulama ----------

    @Test
    void negativeAmountIsRejected() throws Exception {
        String tc = newCustomer(1500, "100000");
        HttpResponse<String> res = apply(tc, "-100000", 12, token);
        assertEquals(400, res.statusCode());
        assertTrue(res.body().contains("requestedAmount"), res.body());
    }

    @Test
    void zeroTermIsRejectedInsteadOfDivisionByZero() throws Exception {
        String tc = newCustomer(1500, "100000");
        HttpResponse<String> res = apply(tc, "10000", 0, token);
        assertEquals(400, res.statusCode());
        assertTrue(res.body().contains("termMonths"), res.body());
    }

    @Test
    void invalidIdentityNumberIsRejected() throws Exception {
        assertEquals(400, apply("12ab", "10000", 12, token).statusCode());
    }

    @Test
    void missingFieldsAreRejected() throws Exception {
        assertEquals(400, post("/api/loans/apply", "{}", token).statusCode());
        assertEquals(400, post("/api/loans/apply", "not json", token).statusCode());
    }

    @Test
    void unknownCustomerReturns404WithoutEchoingIdentityNumber() throws Exception {
        HttpResponse<String> res = apply("99999999998", "10000", 12, token);
        assertEquals(404, res.statusCode());
        assertFalse(res.body().contains("99999999998"), res.body());
    }

    // ---------- Karar kuralları ve yanıt içeriği ----------

    @Test
    void highScoreAffordableLoanIsApprovedAndResponseHasNoSensitiveData() throws Exception {
        String tc = newCustomer(1500, "50000");
        HttpResponse<String> res = apply(tc, "120000", 12, token);
        assertEquals(200, res.statusCode(), res.body());
        assertTrue(res.body().contains("\"status\":\"APPROVED\""), res.body());
        assertTrue(res.body().contains("\"monthlyInstallment\":10000"), res.body());
        for (String leaked : new String[]{tc, "creditScore", "monthlyIncome", "identityNumber", "customer"}) {
            assertFalse(res.body().contains(leaked), "Yanıtta olmamalı: " + leaked + " -> " + res.body());
        }

        LoanApplication saved = loanApplicationRepository.findAll().stream()
                .max(Comparator.comparing(LoanApplication::getId)).orElseThrow();
        assertEquals("officer", saved.getCreatedBy(), "Denetim izi kaydedilmeli");
    }

    @Test
    void lowScoreIsRejected() throws Exception {
        String tc = newCustomer(400, "50000");
        HttpResponse<String> res = apply(tc, "12000", 12, token);
        assertTrue(res.body().contains("\"status\":\"" + ApplicationStatus.REJECTED + "\""), res.body());
    }

    @Test
    void installmentAboveIncomeLimitIsRejected() throws Exception {
        String tc = newCustomer(1500, "10000");           // limit: 4.000/ay
        HttpResponse<String> res = apply(tc, "60000", 12, token); // taksit: 5.000/ay
        assertTrue(res.body().contains("\"status\":\"REJECTED\""), res.body());
    }

    @Test
    void midScoreGoesToManualReview() throws Exception {
        String tc = newCustomer(800, "50000");
        HttpResponse<String> res = apply(tc, "12000", 12, token);
        assertTrue(res.body().contains("\"status\":\"MANUAL_REVIEW\""), res.body());
    }

    // ---------- yardımcılar ----------

    private String newCustomer(int creditScore, String monthlyIncome) {
        String tc = Long.toString(ThreadLocalRandom.current().nextLong(10_000_000_000L, 99_999_999_990L));
        Customer c = new Customer();
        c.setIdentityNumber(tc);
        c.setFullName("Test Musteri");
        c.setCreditScore(creditScore);
        c.setMonthlyIncome(new BigDecimal(monthlyIncome));
        customerRepository.save(c);
        return tc;
    }

    private HttpResponse<String> apply(String tc, String amount, int term, String bearer) throws Exception {
        return post("/api/loans/apply",
                "{\"identityNumber\":\"" + tc + "\",\"requestedAmount\":" + amount + ",\"termMonths\":" + term + "}",
                bearer);
    }

    private HttpResponse<String> post(String path, String json, String bearer) throws Exception {
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json));
        if (bearer != null) {
            b.header("Authorization", "Bearer " + bearer);
        }
        return HTTP.send(b.build(), HttpResponse.BodyHandlers.ofString());
    }
}
