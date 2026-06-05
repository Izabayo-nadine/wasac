# WASAC/REG Utility Billing System

Spring Boot backend for Water and Sanitation Corporation (WASAC) and Rwanda Energy Group (REG) utility billing.

## Tech Stack

- Java 17+ / Spring Boot 3.2
- Spring Data JPA / Spring Security / JWT
- **PostgreSQL**
- Swagger OpenAPI 3

## Quick Start

### Prerequisites

- JDK 17+
- Maven 3.8+
- PostgreSQL 14+ installed and running locally

### 1. Create the database

In pgAdmin or `psql`:

```sql
CREATE DATABASE utility_billing;
```

Default connection settings (edit `application.properties` if yours differ):

| Setting  | Value            |
|----------|------------------|
| Host     | localhost:5432   |
| Database | utility_billing  |
| Username | postgres         |
| Password | postgres         |

### 2. Run the application

```bash
cd utility-billing-system
mvn spring-boot:run
```

On startup the app will:

1. Create/update tables via Hibernate (`ddl-auto=update`)
2. Seed roles and default users
3. Install PostgreSQL notification triggers automatically

- **Swagger UI:** http://localhost:8080/swagger-ui.html

### Default Users

| Email | Password | Role |
|---|---|---|
| admin@wasac.com | admin123 | ADMIN |
| operator@wasac.rw | Password@123 | OPERATOR |
| finance@wasac.rw | Password@123 | FINANCE |
| customer@example.rw | Password@123 | CUSTOMER |

### Authentication

| Endpoint | Description |
|----------|-------------|
| `POST /api/auth/register` | Customer self-register (OTP sent) or admin creates staff |
| `POST /api/auth/verify-otp` | Verify email with 6-digit OTP |
| `POST /api/auth/resend-otp` | Resend OTP |
| `POST /api/auth/login` | Login → JWT token |
| `POST /api/auth/logout` | Invalidate JWT (requires Bearer token) |

**Customer flow:**
1. `POST /api/auth/register` → OTP sent (logged to console when `app.mail.enabled=false`)
2. `POST /api/auth/verify-otp` → account activated
3. `POST /api/auth/login` → JWT

**Admin login:** `admin@wasac.com` / `admin123`

**User management (ADMIN only):**

| Method | Endpoint |
|--------|----------|
| POST | `/api/users` |
| GET | `/api/users` |
| GET | `/api/users/{id}` |
| PUT | `/api/users/{id}` |
| DELETE | `/api/users/{id}` |

## Configuration

Edit `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/utility_billing
spring.datasource.username=postgres
spring.datasource.password=postgres
```

## API Workflow (Postman / Swagger)

1. `POST /api/auth/login` — get JWT token
2. Add header: `Authorization: Bearer <token>`
3. `POST /api/customers` — register customer
4. `POST /api/meters` — assign meters
5. `POST /api/meter-readings` — capture readings (as operator)
6. `POST /api/tariffs` — configure tariffs (as admin)
7. `POST /api/bills/generate` — generate monthly bills
8. `PATCH /api/bills/{id}/approve` — approve bill
9. `POST /api/payments` — record payment
10. `GET /api/notifications/customer/{id}` — view notifications

## Documentation

- [ERD](docs/ERD.md) — Entity Relationship Diagram
- [Spring Boot Flow](docs/SPRING_BOOT_FLOW.md) — Architecture and request flows
- [DB Triggers](src/main/resources/db/triggers-postgres.sql) — Bill/payment notification triggers

## Project Structure

```
src/main/java/com/wasac/billing/
├── config/          # OpenAPI, data seeding, PostgreSQL triggers
├── controller/      # REST endpoints
├── domain/entity/   # JPA entities
├── domain/enums/    # Status and type enums
├── dto/             # Request/response DTOs
├── exception/       # Global error handling
├── repository/      # Spring Data JPA
├── security/        # JWT + Spring Security
└── service/         # Business logic
```
