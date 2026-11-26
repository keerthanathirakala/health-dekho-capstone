CREATE TABLE customer (
    customer_id BIGSERIAL PRIMARY KEY,
    email VARCHAR(254) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    full_name VARCHAR(160) NOT NULL,
    date_of_birth DATE NOT NULL CHECK (date_of_birth <= CURRENT_DATE),
    phone VARCHAR(30),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'SUSPENDED')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE insurance_product (
    product_id BIGSERIAL PRIMARY KEY,
    product_code VARCHAR(40) NOT NULL UNIQUE,
    policy_name VARCHAR(160) NOT NULL,
    policy_type VARCHAR(30) NOT NULL CHECK (policy_type IN ('TERM', 'HEALTH', 'SAVINGS')),
    term_months INTEGER NOT NULL CHECK (term_months > 0),
    premium_frequency VARCHAR(20) NOT NULL CHECK (premium_frequency IN ('MONTHLY', 'QUARTERLY', 'HALF_YEARLY', 'YEARLY')),
    premium_amount NUMERIC(14,2) NOT NULL CHECK (premium_amount > 0),
    sum_insured NUMERIC(16,2) NOT NULL CHECK (sum_insured > 0),
    min_entry_age INTEGER NOT NULL CHECK (min_entry_age >= 0),
    max_entry_age INTEGER NOT NULL CHECK (max_entry_age >= min_entry_age),
    active BOOLEAN NOT NULL DEFAULT true
);

CREATE TABLE customer_policy (
    policy_id BIGSERIAL PRIMARY KEY,
    policy_number VARCHAR(40) NOT NULL UNIQUE,
    customer_id BIGINT NOT NULL REFERENCES customer(customer_id),
    product_id BIGINT NOT NULL REFERENCES insurance_product(product_id),
    start_date DATE NOT NULL,
    maturity_date DATE NOT NULL,
    premium_frequency VARCHAR(20) NOT NULL,
    premium_amount NUMERIC(14,2) NOT NULL CHECK (premium_amount > 0),
    sum_insured NUMERIC(16,2) NOT NULL CHECK (sum_insured > 0),
    total_premium_payable NUMERIC(16,2) NOT NULL CHECK (total_premium_payable > 0),
    status VARCHAR(20) NOT NULL CHECK (status IN ('ACTIVE', 'MATURED', 'CANCELLED')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_customer_product UNIQUE (customer_id, product_id)
);

CREATE TABLE premium_installment (
    installment_id BIGSERIAL PRIMARY KEY,
    policy_id BIGINT NOT NULL REFERENCES customer_policy(policy_id),
    installment_number INTEGER NOT NULL CHECK (installment_number > 0),
    due_date DATE NOT NULL,
    grace_period_end_date DATE NOT NULL,
    scheduled_amount NUMERIC(14,2) NOT NULL CHECK (scheduled_amount > 0),
    paid_amount NUMERIC(14,2) NOT NULL DEFAULT 0 CHECK (paid_amount >= 0),
    payment_status VARCHAR(20) NOT NULL CHECK (payment_status IN ('PENDING', 'PAID', 'OVERDUE')),
    CONSTRAINT uq_policy_installment UNIQUE (policy_id, installment_number)
);

CREATE TABLE premium_payment (
    payment_id BIGSERIAL PRIMARY KEY,
    installment_id BIGINT NOT NULL REFERENCES premium_installment(installment_id),
    transaction_reference VARCHAR(100) NOT NULL UNIQUE,
    payment_date TIMESTAMPTZ NOT NULL,
    amount_paid NUMERIC(14,2) NOT NULL CHECK (amount_paid > 0),
    penalty_amount NUMERIC(14,2) NOT NULL DEFAULT 0 CHECK (penalty_amount >= 0),
    payment_method VARCHAR(30) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE ai_recommendation_request (
    recommendation_request_id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES customer(customer_id),
    insurance_goals TEXT NOT NULL,
    preferred_term INTEGER NOT NULL CHECK (preferred_term > 0),
    preferred_policy_type VARCHAR(30),
    calculated_age INTEGER NOT NULL CHECK (calculated_age >= 0),
    model_name VARCHAR(100) NOT NULL,
    model_version VARCHAR(100) NOT NULL,
    prompt_version VARCHAR(30) NOT NULL,
    requested_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE ai_recommendation_item (
    recommendation_item_id BIGSERIAL PRIMARY KEY,
    recommendation_request_id BIGINT NOT NULL REFERENCES ai_recommendation_request(recommendation_request_id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES insurance_product(product_id),
    rank_order INTEGER NOT NULL CHECK (rank_order > 0),
    recommendation_reason TEXT NOT NULL,
    suitability VARCHAR(30) NOT NULL,
    selected_for_registration BOOLEAN NOT NULL DEFAULT false,
    CONSTRAINT uq_request_product UNIQUE (recommendation_request_id, product_id),
    CONSTRAINT uq_request_rank UNIQUE (recommendation_request_id, rank_order)
);

CREATE INDEX idx_customer_policy_customer ON customer_policy(customer_id);
CREATE INDEX idx_installment_policy_due_date ON premium_installment(policy_id, due_date);
CREATE INDEX idx_payment_installment ON premium_payment(installment_id);
CREATE INDEX idx_recommendation_customer_requested ON ai_recommendation_request(customer_id, requested_at DESC);
CREATE INDEX idx_product_active_type ON insurance_product(active, policy_type);

CREATE SEQUENCE policy_number_seq START 1;

INSERT INTO insurance_product
(product_code, policy_name, policy_type, term_months, premium_frequency, premium_amount, sum_insured, min_entry_age, max_entry_age, active)
VALUES
('HD-TERM-001', 'HealthDekho Secure Term', 'TERM', 240, 'MONTHLY', 1250, 5000000, 18, 60, true),
('HD-HEALTH-001', 'HealthDekho Family Health', 'HEALTH', 12, 'YEARLY', 18000, 1000000, 18, 65, true),
('HD-SAV-001', 'HealthDekho Smart Savings', 'SAVINGS', 120, 'QUARTERLY', 7500, 1500000, 21, 55, true);
