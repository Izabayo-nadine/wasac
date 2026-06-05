# Spring Boot Flow Diagram

## WASAC/REG Utility Billing System Architecture

```mermaid
flowchart TB
    subgraph Client["Client Layer"]
        POSTMAN[Postman / Swagger UI]
    end

    subgraph Security["Security Layer"]
        JWT_FILTER[JwtAuthenticationFilter]
        SEC_CONFIG[SecurityConfig]
        AUTH_CTRL[AuthController]
    end

    subgraph API["REST Controllers"]
        USER_CTRL[UserController]
        CUST_CTRL[CustomerController]
        METER_CTRL[MeterController]
        READ_CTRL[MeterReadingController]
        TARIFF_CTRL[TariffController]
        TAX_CTRL[TaxPenaltyController]
        BILL_CTRL[BillController]
        PAY_CTRL[PaymentController]
        NOTIF_CTRL[NotificationController]
    end

    subgraph Service["Service Layer"]
        AUTH_SVC[AuthService]
        CUST_SVC[CustomerService]
        METER_SVC[MeterService]
        READ_SVC[MeterReadingService]
        TARIFF_SVC[TariffService]
        TAX_SVC[TaxPenaltyService]
        BILL_SVC[BillService]
        PAY_SVC[PaymentService]
        NOTIF_SVC[NotificationService]
    end

    subgraph Data["Data Layer"]
        JPA[Spring Data JPA Repositories]
        DB[(PostgreSQL)]
        TRIGGERS[DB Triggers]
    end

    POSTMAN -->|HTTP Request| JWT_FILTER
    JWT_FILTER -->|/api/auth/** public| AUTH_CTRL
    JWT_FILTER -->|Authenticated| API

    AUTH_CTRL --> AUTH_SVC
    USER_CTRL --> AUTH_SVC
    CUST_CTRL --> CUST_SVC
    METER_CTRL --> METER_SVC
    READ_CTRL --> READ_SVC
    TARIFF_CTRL --> TARIFF_SVC
    TAX_CTRL --> TAX_SVC
    BILL_CTRL --> BILL_SVC
    PAY_CTRL --> PAY_SVC
    NOTIF_CTRL --> NOTIF_SVC

    AUTH_SVC --> JPA
    CUST_SVC --> JPA
    METER_SVC --> JPA
    READ_SVC --> JPA
    TARIFF_SVC --> JPA
    TAX_SVC --> JPA
    BILL_SVC --> JPA
    BILL_SVC --> NOTIF_SVC
    PAY_SVC --> JPA
    PAY_SVC --> NOTIF_SVC

    JPA --> DB
    DB --> TRIGGERS
    TRIGGERS -->|INSERT notification| DB
```

## Request Flow by Task

### Task 1 — Authentication
```
POST /api/auth/register → AuthService → ROLE_CUSTOMER (public) or staff roles (admin)
POST /api/auth/login  → AuthenticationManager → JwtTokenProvider → JWT token
All other endpoints   → JwtAuthenticationFilter validates Bearer token
```

### Task 2 — Customer & Meter
```
POST /api/customers → validate unique nationalId → save
POST /api/meters    → validate unique meterNumber → link to customer
```

### Task 3 — Meter Reading
```
POST /api/meter-readings → validate:
  ✓ meter is ACTIVE
  ✓ current > previous
  ✓ one reading per meter/month/year
```

### Task 4 — Tariff Configuration
```
POST /api/tariffs → version++ → close previous tariff → save new version
POST /api/config/tax|penalty → versioned config
```

### Task 5 — Billing & Payment
```
POST /api/bills/generate → readings + tariffs + tax → create bill → notification
PATCH /api/bills/{id}/approve → status APPROVED
POST /api/payments → partial/full → update balance → PAID if zero → notification
```

### Task 6 — Database Triggers (PostgreSQL)
```
AFTER INSERT ON bills     → INSERT notification (BILL_GENERATED)
AFTER INSERT ON payments  → IF fully paid → UPDATE status PAID + notification
```

## Role-Based Access

| Endpoint | ADMIN | OPERATOR | FINANCE | CUSTOMER |
|---|:---:|:---:|:---:|:---:|
| /api/auth/** | ✓ | ✓ | ✓ | ✓ |
| /api/users/** | ✓ | | | |
| /api/customers | ✓ | ✓ | ✓ | |
| /api/meters | ✓ | ✓ | | |
| /api/meter-readings | | ✓ | | |
| /api/tariffs, /api/config | ✓ | | ✓ | |
| /api/bills | ✓ | | ✓ | view own |
| /api/payments | ✓ | | ✓ | view own |
| /api/notifications | ✓ | | ✓ | view own |
