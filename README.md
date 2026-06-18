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

## Setup and Execution

### Prerequisites
- JDK 21
- Node.js (v18 or higher)
- PostgreSQL

**Running the Backend:**
   Navigate to the `loan-system` directory and execute the application via your IDE's runner.

 **Running the Frontend:**
   Navigate to the `frontend` directory and execute:
```bash
   npm install
   npm run dev