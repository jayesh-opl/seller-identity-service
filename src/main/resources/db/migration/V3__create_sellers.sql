-- =============================================================================
-- V3: Create Sellers Table
-- =============================================================================
-- Seller profiles linked to authenticated users (one-to-one).
-- A CUSTOMER becomes a SELLER by creating a seller profile.
-- =============================================================================

CREATE TABLE sellers (
    id              UUID            NOT NULL DEFAULT gen_random_uuid(),
    user_id         UUID            NOT NULL,
    display_name    VARCHAR(200)    NOT NULL,
    status          VARCHAR(30)     NOT NULL,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT now(),
    version         BIGINT          NOT NULL DEFAULT 0,

    -- Primary key
    CONSTRAINT pk_sellers PRIMARY KEY (id),

    -- One-to-one: a user can have at most one seller profile
    CONSTRAINT uq_seller_user_id UNIQUE (user_id),

    -- Referential integrity: seller must belong to an existing user
    -- RESTRICT prevents deleting a user who has an active seller profile
    CONSTRAINT fk_seller_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE RESTRICT,

    -- Data integrity: status must be a valid lifecycle state
    CONSTRAINT chk_seller_status CHECK (status IN (
        'PENDING_APPROVAL', 'ACTIVE', 'SUSPENDED', 'REJECTED'
    ))
);

-- Index: admin filtering by status (find pending sellers for review)
CREATE INDEX idx_seller_status ON sellers (status);

COMMENT ON TABLE sellers IS 'Seller profiles. One-to-one with users. Created during seller onboarding.';
COMMENT ON COLUMN sellers.user_id IS 'FK to users.id. RESTRICT on delete: cannot remove user with seller profile.';
COMMENT ON COLUMN sellers.display_name IS 'Public-facing seller/shop name shown to customers.';
COMMENT ON COLUMN sellers.status IS 'Lifecycle state: PENDING_APPROVAL, ACTIVE, SUSPENDED, REJECTED.';
