# Seller & Identity Service

Manages all user identities (customers, sellers, admins), authentication/authorization, and the complete seller lifecycle (onboarding, KYC, compliance).

## Tech Stack

- Java 21
- Spring Boot 3.3.x
- Maven
- PostgreSQL (identity_db)
- Flyway (database migrations)
- SpringDoc OpenAPI (Swagger UI)
- Testcontainers (integration tests)

## Getting Started

```bash
# Build
mvn clean package

# Run (requires PostgreSQL on localhost:5432)
mvn spring-boot:run

# Run tests
mvn test
```

## API Endpoints

| Prefix | Domain |
|--------|--------|
| /api/v1/auth/* | Authentication (register, login, verify, refresh, password reset) |
| /api/v1/users/* | User profile and address management |
| /api/v1/sellers/* | Seller onboarding, KYC, approval |
| /swagger-ui.html | API documentation (SpringDoc OpenAPI) |

## Database

- **Database name:** identity_db
- **Migrations:** `src/main/resources/db/migration/`
- **Naming convention:** `V{version}__{description}.sql`

---

## Package Structure

```
com.marketplace.selleridentity
├── SellerIdentityServiceApplication.java   # Spring Boot entry point
│
├── auth/                          # Authentication & Authorization module
│   ├── controller/                # Auth REST endpoints (login, register, verify, token refresh)
│   ├── dto/                       # Auth request/response objects (LoginRequest, TokenResponse)
│   ├── service/                   # Auth business logic (token issuance, password hashing, OTP)
│   └── security/                  # Security config, JWT filter, token provider, auth entry point
│
├── identity/                      # User Identity module (core user management)
│   ├── controller/                # User profile and address REST endpoints
│   ├── dto/                       # User request/response objects (ProfileDTO, AddressDTO)
│   ├── entity/                    # JPA entities (AppUser, UserProfile, UserAddress, AuthSession)
│   ├── repository/                # Spring Data JPA repositories for user tables
│   └── service/                   # User business logic (CRUD, GDPR erasure, consent)
│
├── seller/                        # Seller Lifecycle module (onboarding, KYC, compliance)
│   ├── controller/                # Seller REST endpoints (register, KYC upload, approve, suspend)
│   ├── dto/                       # Seller request/response objects (SellerRegistrationRequest, etc.)
│   ├── entity/                    # JPA entities (Seller, KYCDocument, SellerBankAccount, SellerWarehouse)
│   ├── repository/                # Spring Data JPA repositories for seller tables
│   └── service/                   # Seller business logic (onboarding flow, KYC verification, tier mgmt)
│
├── outbox/                        # Transactional Outbox module (guaranteed event delivery)
│   ├── entity/                    # OutboxEvent JPA entity
│   ├── repository/                # Outbox table access
│   └── relay/                     # Background poller that publishes outbox events to message broker
│
└── common/                        # Shared cross-cutting concerns
    ├── audit/                     # Audit log entity, repository, and interceptor
    ├── config/                    # Application-wide configuration (Jackson, CORS, async)
    ├── event/                     # Event envelope models, event publisher abstraction
    ├── exception/                 # Global exception handler, custom exception classes, error responses
    └── util/                      # Utility classes (ID generation, date helpers, encryption utils)
```

---

## Package Purpose Breakdown

### `auth/` — Authentication & Authorization

Handles the security boundary of the service. Responsible for:
- User login (password, OTP, social login)
- Token issuance (JWT access + refresh tokens)
- Token validation endpoint (used by API Gateway and other services)
- Password reset flow
- MFA enrollment and verification
- Security filter chain configuration

Separated from `identity/` because auth is a cross-cutting concern with different change velocity (security patches, token format changes) versus user profile management.

### `identity/` — User Identity Management

The core user data model. Responsible for:
- User registration (all roles: customer, seller, admin)
- Profile management (name, avatar, DOB)
- Address CRUD (delivery addresses for customers)
- Session management
- RBAC role and permission definitions
- GDPR consent tracking and right-to-erasure

This is the "source of truth" for who a user is across the entire platform.

### `seller/` — Seller Lifecycle

Manages everything specific to the seller persona beyond their base user identity:
- Seller registration and onboarding flow
- KYC document upload and verification workflow
- Seller account state machine (pending → active → suspended → terminated)
- Bank account management and verification
- Warehouse/pickup address management
- Policy violation tracking
- Seller tier classification (bronze, silver, gold, platinum)

### `outbox/` — Transactional Outbox

Implements the outbox pattern for reliable event publishing:
- Stores events in the same transaction as business state changes
- Background relay polls for unpublished events and publishes to message broker
- Guarantees at-least-once delivery without dual-write problems
- Provides event replay capability for debugging

### `common/` — Shared Infrastructure

Cross-cutting concerns used by all modules:
- **audit/**: Append-only audit log for compliance (who did what, when)
- **config/**: Global configuration beans (ObjectMapper, async executor, CORS)
- **event/**: Event envelope model (standard event structure), publisher interface
- **exception/**: Global `@RestControllerAdvice`, custom business exceptions, error response format
- **util/**: ID generators, date/time helpers, encryption/hashing utilities

---

## Events Published

| Event | Trigger | Partition Key |
|-------|---------|---------------|
| user.registered | New user account created | user_id |
| user.verified | Email/phone verification completed | user_id |
| seller.registered | Seller application submitted | seller_id |
| seller.approved | Admin approves seller KYC | seller_id |
| seller.suspended | Seller account suspended | seller_id |
| seller.terminated | Seller permanently removed | seller_id |
| seller.tier.changed | Tier upgraded or downgraded | seller_id |

## Events Consumed (Phase 2 — Performance Module)

| Event | Source | Action |
|-------|--------|--------|
| order.confirmed | Commerce | Update seller confirmation metrics |
| order.shipped | Commerce | Update seller shipping metrics |
| order.cancelled | Commerce | Update seller cancellation rate |
| review.published | Reviews | Update seller average rating |

> These events will be consumed when the `performance/` module is introduced in Phase 2.
