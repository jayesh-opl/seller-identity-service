-- =============================================================================
-- V2: Create Verification Tokens Table
-- =============================================================================
-- One-time tokens used for email/phone verification during user registration.
-- Tokens are single-use (marked via 'used' flag) and time-bound (expires_at).
-- =============================================================================

CREATE TABLE verification_tokens (
    id              UUID            NOT NULL DEFAULT gen_random_uuid(),
    user_id         UUID            NOT NULL,
    token           VARCHAR(255)    NOT NULL,
    expires_at      TIMESTAMPTZ     NOT NULL,
    used            BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT now(),
    version         BIGINT          NOT NULL DEFAULT 0,

    -- Primary key
    CONSTRAINT pk_verification_tokens PRIMARY KEY (id),

    -- Each token value must be globally unique (prevents reuse/collision)
    CONSTRAINT uq_verification_token_token UNIQUE (token),

    -- Referential integrity: token belongs to a user; cascade on user deletion
    CONSTRAINT fk_verification_token_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE
);

-- Index: find all tokens for a specific user (e.g., invalidate previous tokens)
CREATE INDEX idx_verification_token_user_id ON verification_tokens (user_id);

-- Index: cleanup job to purge expired tokens
CREATE INDEX idx_verification_token_expires_at ON verification_tokens (expires_at);

COMMENT ON TABLE verification_tokens IS 'One-time tokens for user email/phone verification. Single-use and time-bound.';
COMMENT ON COLUMN verification_tokens.token IS 'Unique token string sent to the user. Looked up during verification.';
COMMENT ON COLUMN verification_tokens.expires_at IS 'Token expiry time. Tokens past this time are invalid regardless of used flag.';
COMMENT ON COLUMN verification_tokens.used IS 'TRUE once the token has been consumed. Prevents replay.';
