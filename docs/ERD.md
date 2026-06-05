# Entity Relationship Diagram (ERD)

## WASAC/REG Utility Billing System

Database: PostgreSQL

```mermaid
erDiagram
    ROLES ||--o{ USER_ROLES : has
    USERS ||--o{ USER_ROLES : has
    USERS ||--o| CUSTOMERS : "linked to"
    CUSTOMERS ||--o{ METERS : owns
    METERS ||--o{ METER_READINGS : records
    USERS ||--o{ METER_READINGS : captures
    CUSTOMERS ||--o{ BILLS : receives
    BILLS ||--o{ BILL_LINE_ITEMS : contains
    METERS ||--o{ BILL_LINE_ITEMS : references
    BILLS ||--o{ PAYMENTS : paid_by
    USERS ||--o{ PAYMENTS : records
    USERS ||--o{ BILLS : approves
    CUSTOMERS ||--o{ NOTIFICATIONS : receives
    BILLS ||--o{ NOTIFICATIONS : triggers
    TARIFFS ||--o{ TARIFF_TIERS : contains

    ROLES {
        bigint id PK
        varchar name UK "ROLE_ADMIN|OPERATOR|FINANCE|CUSTOMER"
    }

    USERS {
        bigint id PK
        varchar full_names
        varchar email UK
        varchar phone_number
        varchar password
        varchar status "ACTIVE|INACTIVE"
        timestamp created_at
    }

    CUSTOMERS {
        bigint id PK
        varchar full_names
        varchar national_id UK
        varchar email
        varchar phone_number
        varchar address
        varchar status "ACTIVE|INACTIVE"
        bigint user_id FK
    }

    METERS {
        bigint id PK
        varchar meter_number UK
        varchar meter_type "WATER|ELECTRICITY"
        date installation_date
        varchar status "ACTIVE|INACTIVE"
        bigint customer_id FK
    }

    METER_READINGS {
        bigint id PK
        bigint meter_id FK
        decimal previous_reading
        decimal current_reading
        date reading_date
        int billing_month
        int billing_year
        bigint captured_by FK
        unique meter_id_billing_month_year
    }

    TARIFFS {
        bigint id PK
        varchar name
        varchar meter_type
        varchar tariff_type "FLAT|TIER_BASED"
        decimal flat_rate
        decimal service_charge
        int version
        date effective_from
        date effective_to
        boolean active
    }

    TARIFF_TIERS {
        bigint id PK
        bigint tariff_id FK
        int tier_order
        decimal min_units
        decimal max_units
        decimal rate_per_unit
    }

    TAX_CONFIGS {
        bigint id PK
        varchar name
        decimal rate
        int version
        date effective_from
        date effective_to
        boolean active
    }

    PENALTY_CONFIGS {
        bigint id PK
        varchar name
        decimal rate
        int grace_days
        int version
        date effective_from
        date effective_to
        boolean active
    }

    BILLS {
        bigint id PK
        varchar bill_reference UK
        bigint customer_id FK
        int billing_month
        int billing_year
        decimal consumption_charge
        decimal service_charge
        decimal tax_amount
        decimal penalty_amount
        decimal total_amount
        decimal amount_paid
        decimal outstanding_balance
        varchar status "PENDING|APPROVED|PAID|OVERDUE|CANCELLED"
        date due_date
        bigint approved_by FK
    }

    BILL_LINE_ITEMS {
        bigint id PK
        bigint bill_id FK
        bigint meter_id FK
        varchar meter_type
        decimal consumption
        decimal amount
        varchar description
    }

    PAYMENTS {
        bigint id PK
        bigint bill_id FK
        decimal amount_paid
        varchar payment_method
        date payment_date
        varchar transaction_reference
        bigint recorded_by FK
    }

    NOTIFICATIONS {
        bigint id PK
        bigint customer_id FK
        bigint bill_id FK
        varchar type
        text message
        boolean sent
        timestamp created_at
    }
```

## Key Relationships

| Relationship | Cardinality | Description |
|---|---|---|
| User ↔ Role | M:N | Users can have multiple roles via `user_roles` |
| User ↔ Customer | 1:1 | Optional link for customer portal access |
| Customer → Meter | 1:N | Each customer may have multiple meters |
| Meter → Reading | 1:N | One reading per meter per month/year |
| Tariff → Tier | 1:N | Tier-based tariffs have multiple bands |
| Customer → Bill | 1:N | Monthly bills per customer |
| Bill → Payment | 1:N | Supports partial payments |
| Bill/Customer → Notification | 1:N | Auto-generated via triggers |

## Business Rule Constraints

- `customers.national_id` — UNIQUE (no duplicate registration)
- `meters.meter_number` — UNIQUE
- `meter_readings(meter_id, billing_month, billing_year)` — UNIQUE
- `bills(customer_id, billing_month, billing_year)` — UNIQUE
- Inactive customers/meters excluded from billing
- Tariff/tax/penalty configs are versioned with `effective_from` / `effective_to`
