# CrediGuard — Loan Origination & Decision Support System

A loan decision support system for bank staff. A loan officer signs in, enters a customer's T.C. identity number, the requested amount and term, and the rule engine returns an instant decision — **approved**, **rejected** (with the reason) or **manual review** — together with the monthly installment. Every decision is stored with the officer who made it.

![CI](https://github.com/berktopal/loan-system/actions/workflows/ci.yml/badge.svg)

## Decision Rules

```
monthly installment = requested amount / term

credit score < 500                                → REJECTED  ("Low credit score")
installment > 40% of the customer's monthly income → REJECTED  ("Installment exceeds income limit")
500 ≤ credit score ≤ 1000                         → MANUAL_REVIEW
credit score > 1000                               → APPROVED
```

Rules are evaluated in this order, so affordability is checked before any approval or review.

## Features

- **Instant credit decisions** with rejection reasons and monthly installment
- **Audit trail:** each application records the evaluating officer (`createdBy`) and timestamp
- **Officer portal** (React + Material UI) with field-level validation messages from the server
- **Stateless JWT authentication** for all API calls

## Security

- **No hardcoded credentials:** the officer account comes from environment variables; a random password is generated and logged when none is provided
- **Correct status codes:** missing, expired or tampered tokens return `401`; validation errors `400`; unknown customers `404` (the identity number is not echoed back)
- **Input validation:** 11-digit identity number, amount 1,000 – 10,000,000 ₺ (max 2 decimals), term 3 – 120 months — checked before business logic runs
- **Data minimization:** decision responses never include the customer's identity number, income or credit score
- **Restricted CORS:** only configured frontend origins can call the API from a browser

## API

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/auth/login` | – | `{ "username", "password" }` → `{ "token" }` |
| POST | `/api/loans/apply` | Bearer token | `{ "identityNumber", "requestedAmount", "termMonths" }` → decision |

Example decision response:

```json
{
  "id": 42,
  "status": "APPROVED",
  "rejectionReason": null,
  "requestedAmount": 120000.00,
  "termMonths": 12,
  "monthlyInstallment": 10000.00,
  "applicationDate": "2026-10-02T19:30:00"
}
```

## Tech Stack

| Layer | Technologies |
|---|---|
| Backend | Java 21, Spring Boot 4.1, Spring Security, JWT (jjwt), Spring Data JPA, Bean Validation, Lombok |
| Database | PostgreSQL |
| Frontend | React (Vite), Material UI, Axios |
| Testing / CI | JUnit 5, end-to-end API tests over HTTP, GitHub Actions with PostgreSQL |

## Project Structure

```
loan-system/
├── loan-system/                    # Spring Boot backend
│   └── src/main/java/com/loanoriginationsystem/loan_system/
│       ├── controller/             # AuthController, LoanController
│       ├── service/                # LoanService (decision rules)
│       ├── dto/                    # request / response models with validation
│       ├── model/                  # Customer, LoanApplication, ApplicationStatus
│       ├── repository/             # Spring Data JPA
│       ├── security/               # JWT filter & utility, security and CORS config
│       └── exception/              # consistent JSON error responses
└── frontend/                       # React officer portal
```

## Getting Started

### Prerequisites
- JDK 21, Node.js 18+, PostgreSQL

### Backend
```bash
createdb loan_db                        # or create it from your PostgreSQL client
cd loan-system
export DB_PASSWORD=your_postgres_password
export ADMIN_PASSWORD=choose-a-strong-password   # optional, min. 12 characters
./mvnw spring-boot:run                  # http://localhost:8080
```

| Variable | Required | Description |
|---|---|---|
| `DB_PASSWORD` | yes | PostgreSQL password |
| `ADMIN_USERNAME` | no | Loan officer username (default `admin`) |
| `ADMIN_PASSWORD` | no | Loan officer password (min. 12 characters). If omitted, a random password is generated and printed in the startup log |
| `CORS_ALLOWED_ORIGINS` | no | Allowed frontend origin(s), comma-separated (default `http://localhost:5173`) |

Customers are read from the `customers` table (`identity_number`, `full_name`, `monthly_income`, `credit_score`); insert a few rows to try the system.

### Frontend
```bash
cd frontend
npm install
npm run dev                             # http://localhost:5173
```

### Tests
```bash
cd loan-system
./mvnw verify      # needs a running PostgreSQL with the loan_db database
```

`LoanApiIntegrationTests` runs 13 end-to-end scenarios over HTTP: authentication failures, malformed tokens, input validation, every decision rule, absence of sensitive data in responses and the audit trail.
