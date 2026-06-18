package com.loanoriginationsystem.loan_system.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name = "customers")
@Data
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 11)
    private String identityNumber;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private BigDecimal monthlyIncome;

    @Column(nullable = false)
    private Integer creditScore;
}