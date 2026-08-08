CREATE TABLE users (
    id UUID PRIMARY KEY,
    subject VARCHAR(128) NOT NULL UNIQUE,
    time_zone VARCHAR(64) NOT NULL,
    status VARCHAR(16) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT users_status_check CHECK (status IN ('ACTIVE', 'DISABLED'))
);

CREATE TABLE commitments (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users (id),
    display_name VARCHAR(120) NOT NULL,
    category VARCHAR(32) NOT NULL,
    expected_amount_minor BIGINT NOT NULL,
    currency CHAR(3) NOT NULL,
    recurrence_type VARCHAR(16) NOT NULL,
    due_day SMALLINT NOT NULL,
    payment_mode VARCHAR(16) NOT NULL,
    status VARCHAR(16) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT commitments_display_name_check CHECK (char_length(trim(display_name)) > 0),
    CONSTRAINT commitments_expected_amount_check CHECK (expected_amount_minor >= 0),
    CONSTRAINT commitments_currency_check CHECK (currency = 'INR'),
    CONSTRAINT commitments_recurrence_check CHECK (recurrence_type = 'MONTHLY'),
    CONSTRAINT commitments_due_day_check CHECK (due_day BETWEEN 1 AND 31),
    CONSTRAINT commitments_payment_mode_check CHECK (payment_mode IN ('MANUAL', 'AUTO_DEBIT')),
    CONSTRAINT commitments_status_check CHECK (status IN ('ACTIVE', 'PAUSED', 'ENDED'))
);

CREATE INDEX commitments_user_status_idx ON commitments (user_id, status);

CREATE TABLE payment_occurrences (
    id UUID PRIMARY KEY,
    commitment_id UUID NOT NULL REFERENCES commitments (id),
    due_date DATE NOT NULL,
    original_due_date DATE NOT NULL,
    expected_amount_minor BIGINT NOT NULL,
    currency CHAR(3) NOT NULL,
    status VARCHAR(16) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT payment_occurrences_expected_amount_check CHECK (expected_amount_minor >= 0),
    CONSTRAINT payment_occurrences_currency_check CHECK (currency = 'INR'),
    CONSTRAINT payment_occurrences_status_check CHECK (status IN ('PLANNED', 'DUE', 'PAID', 'SKIPPED', 'POSTPONED', 'OVERDUE')),
    CONSTRAINT payment_occurrences_commitment_due_date_unique UNIQUE (commitment_id, due_date)
);

CREATE INDEX payment_occurrences_due_date_idx ON payment_occurrences (due_date);

CREATE TABLE outbox_events (
    id UUID PRIMARY KEY,
    aggregate_type VARCHAR(64) NOT NULL,
    aggregate_id UUID NOT NULL,
    event_type VARCHAR(128) NOT NULL,
    payload JSONB NOT NULL,
    occurred_at TIMESTAMPTZ NOT NULL,
    available_at TIMESTAMPTZ NOT NULL,
    attempt_count INTEGER NOT NULL DEFAULT 0,
    processed_at TIMESTAMPTZ,
    last_error TEXT,
    CONSTRAINT outbox_events_attempt_count_check CHECK (attempt_count >= 0)
);

CREATE INDEX outbox_events_available_idx ON outbox_events (available_at)
    WHERE processed_at IS NULL;

CREATE TABLE audit_events (
    id UUID PRIMARY KEY,
    actor_user_id UUID REFERENCES users (id),
    aggregate_type VARCHAR(64) NOT NULL,
    aggregate_id UUID NOT NULL,
    event_type VARCHAR(128) NOT NULL,
    details JSONB NOT NULL,
    occurred_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX audit_events_aggregate_idx ON audit_events (aggregate_type, aggregate_id, occurred_at);

CREATE TABLE idempotency_keys (
    user_id UUID NOT NULL REFERENCES users (id),
    idempotency_key VARCHAR(255) NOT NULL,
    request_fingerprint CHAR(64) NOT NULL,
    response_status SMALLINT NOT NULL,
    resource_id UUID NOT NULL REFERENCES commitments (id),
    occurrence_id UUID NOT NULL REFERENCES payment_occurrences (id),
    created_at TIMESTAMPTZ NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    PRIMARY KEY (user_id, idempotency_key),
    CONSTRAINT idempotency_keys_response_status_check CHECK (response_status BETWEEN 100 AND 599),
    CONSTRAINT idempotency_keys_expiry_check CHECK (expires_at > created_at)
);
