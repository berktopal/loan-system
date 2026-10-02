# CrediGuard: Loan Origination and Decision Support System

CrediGuard is a financial services application designed for loan processing and risk assessment. It features a robust Java (Spring Boot) backend and a modern React (Material-UI) frontend, emphasizing security, scalability, and performance.

## Project Overview

The system streamlines the loan application workflow by providing a secure interface for financial personnel to evaluate customer loan requests. It implements strict authentication protocols to ensure data integrity and operational security.

## Technical Stack

### Backend
- **Java 21**
- **Spring Boot 4.1.0**
- **Spring Security & JWT**: For stateless authentication and authorization.
- **Spring Data JPA**: For reliable database persistence.
- **PostgreSQL**: Relational database management.

### Frontend
- **React (Vite)**
- **Material-UI (MUI)**: For a professional and responsive dashboard.
- **Axios**: For structured API communication.

## Security

- **No hardcoded credentials:** the loan officer account is configured via environment variables; a random password is generated when none is provided.
- **Stateless JWT authentication:** every endpoint except login requires a valid token; missing, expired or tampered tokens return `401`.
- **Input validation:** T.C. identity number format, positive amount (1,000 – 10,000,000 ₺, max 2 decimals) and term (3 – 120 months) are validated before reaching business logic.
- **Data minimization:** decision responses never include the customer's identity number, income or credit score.
- **Audit trail:** each application records which officer evaluated it (`createdBy`).
- **Restricted CORS:** only configured frontend origins can call the API from a browser.
- **Tested:** end-to-end API tests run against PostgreSQL in CI on every push and pull request.

## Setup and Execution

### Prerequisites
- JDK 21
- Node.js (v18 or higher)
- PostgreSQL

**Running the Backend:**
   Create a PostgreSQL database named `loan_db`, set the environment variables below, then run the application from the `loan-system` directory (IDE runner or `./mvnw spring-boot:run`).

   | Variable | Required | Description |
   |---|---|---|
   | `DB_PASSWORD` | yes | PostgreSQL password |
   | `ADMIN_USERNAME` | no | Loan officer username (default `admin`) |
   | `ADMIN_PASSWORD` | no | Loan officer password (min. 12 chars). If omitted, a random password is generated and printed in the startup log. |
   | `CORS_ALLOWED_ORIGINS` | no | Allowed frontend origin(s), comma-separated (default `http://localhost:5173`) |

 **Running the Frontend:**
   Navigate to the `frontend` directory and execute:
```bash
   npm install
   npm run dev