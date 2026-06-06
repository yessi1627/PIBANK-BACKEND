-- ============================================================
-- V1__init_schema.sql
-- Esquema completo de PiBank
-- Norma ISO 9075 — SQL estándar
-- ============================================================

-- ===================== SECUENCIAS =====================
CREATE SEQUENCE pb_audit_seq
    START WITH 1
    INCREMENT BY 50
    NOCACHE
    NOCYCLE;

-- ===================== USUARIOS =====================
CREATE TABLE pb_users (
    id                          VARCHAR2(36)    NOT NULL,
    first_name                  VARCHAR2(100)   NOT NULL,
    last_name                   VARCHAR2(100)   NOT NULL,
    email                       VARCHAR2(150)   NOT NULL,
    phone_number                VARCHAR2(20)    NOT NULL,
    document_type               VARCHAR2(20)    NOT NULL,
    document_number             VARCHAR2(20)    NOT NULL,
    birth_date                  DATE,
    address                     VARCHAR2(255),
    city                        VARCHAR2(100),
    password_hash               VARCHAR2(255)   NOT NULL,
    role                        VARCHAR2(20)    NOT NULL DEFAULT 'CUSTOMER',
    status                      VARCHAR2(30)    NOT NULL DEFAULT 'PENDING_VERIFICATION',
    mfa_enabled                 NUMBER(1)       NOT NULL DEFAULT 0,
    mfa_secret                  VARCHAR2(255),
    failed_login_attempts       NUMBER(3)       NOT NULL DEFAULT 0,
    locked_until                TIMESTAMP,
    last_login_at               TIMESTAMP,
    last_login_ip               VARCHAR2(45),
    email_verified              NUMBER(1)       NOT NULL DEFAULT 0,
    email_verification_token    VARCHAR2(255),
    password_reset_token        VARCHAR2(255),
    password_reset_expires      TIMESTAMP,
    terms_accepted              NUMBER(1)       NOT NULL DEFAULT 0,
    terms_accepted_at           TIMESTAMP,
    profile_picture_url         VARCHAR2(500),
    created_at                  TIMESTAMP       NOT NULL,
    updated_at                  TIMESTAMP,
    created_by                  VARCHAR2(100),
    updated_by                  VARCHAR2(100),
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT uq_users_document UNIQUE (document_number),
    CONSTRAINT chk_users_role CHECK (role IN ('CUSTOMER','ADMIN','ANALYST','SUPPORT')),
    CONSTRAINT chk_users_status CHECK (status IN ('ACTIVE','INACTIVE','BLOCKED','PENDING_VERIFICATION','SUSPENDED'))
);

CREATE INDEX idx_users_email ON pb_users (email);
CREATE INDEX idx_users_document ON pb_users (document_number);
CREATE INDEX idx_users_phone ON pb_users (phone_number);
CREATE INDEX idx_users_status ON pb_users (status);

-- ===================== CUENTAS =====================
CREATE TABLE pb_accounts (
    id                  VARCHAR2(36)        NOT NULL,
    user_id             VARCHAR2(36)        NOT NULL,
    account_number      VARCHAR2(20)        NOT NULL,
    account_type        VARCHAR2(20)        NOT NULL,
    status              VARCHAR2(20)        NOT NULL DEFAULT 'ACTIVE',
    balance             NUMBER(18,2)        NOT NULL DEFAULT 0,
    available_balance   NUMBER(18,2)        NOT NULL DEFAULT 0,
    currency            VARCHAR2(3)         NOT NULL DEFAULT 'COP',
    alias               VARCHAR2(50),
    credit_limit        NUMBER(18,2),
    interest_rate       NUMBER(6,4),
    cutoff_day          NUMBER(2),
    payment_day         NUMBER(2),
    last_movement_at    TIMESTAMP,
    rounding_enabled    NUMBER(1)           NOT NULL DEFAULT 0,
    created_at          TIMESTAMP           NOT NULL,
    updated_at          TIMESTAMP,
    created_by          VARCHAR2(100),
    updated_by          VARCHAR2(100),
    CONSTRAINT pk_accounts PRIMARY KEY (id),
    CONSTRAINT uq_accounts_number UNIQUE (account_number),
    CONSTRAINT fk_accounts_user FOREIGN KEY (user_id) REFERENCES pb_users(id),
    CONSTRAINT chk_accounts_type CHECK (account_type IN ('SAVINGS','CHECKING','CREDIT')),
    CONSTRAINT chk_accounts_status CHECK (status IN ('ACTIVE','INACTIVE','FROZEN','CLOSED')),
    CONSTRAINT chk_accounts_balance CHECK (balance >= 0)
);

CREATE INDEX idx_accounts_user ON pb_accounts (user_id);
CREATE INDEX idx_accounts_number ON pb_accounts (account_number);

-- ===================== TRANSACCIONES =====================
CREATE TABLE pb_transactions (
    id                      VARCHAR2(36)    NOT NULL,
    transaction_number      VARCHAR2(30)    NOT NULL,
    type                    VARCHAR2(30)    NOT NULL,
    status                  VARCHAR2(20)    NOT NULL DEFAULT 'PENDING',
    from_account_id         VARCHAR2(36),
    to_account_id           VARCHAR2(36),
    amount                  NUMBER(18,2)    NOT NULL,
    currency                VARCHAR2(3)     NOT NULL DEFAULT 'COP',
    description             VARCHAR2(500),
    category                VARCHAR2(40),
    reference               VARCHAR2(50),
    ip_address              VARCHAR2(45),
    device_info             VARCHAR2(255),
    location_info           VARCHAR2(255),
    fraud_score             NUMBER(5,4),
    failure_reason          VARCHAR2(255),
    processed_at            TIMESTAMP,
    original_transaction_id VARCHAR2(36),
    balance_after_from      NUMBER(18,2),
    balance_after_to        NUMBER(18,2),
    created_at              TIMESTAMP       NOT NULL,
    updated_at              TIMESTAMP,
    created_by              VARCHAR2(100),
    updated_by              VARCHAR2(100),
    CONSTRAINT pk_transactions PRIMARY KEY (id),
    CONSTRAINT uq_transactions_number UNIQUE (transaction_number),
    CONSTRAINT fk_trans_from FOREIGN KEY (from_account_id) REFERENCES pb_accounts(id),
    CONSTRAINT fk_trans_to FOREIGN KEY (to_account_id) REFERENCES pb_accounts(id),
    CONSTRAINT chk_trans_amount CHECK (amount > 0)
);

CREATE INDEX idx_transactions_from_account ON pb_transactions (from_account_id);
CREATE INDEX idx_transactions_to_account ON pb_transactions (to_account_id);
CREATE INDEX idx_transactions_created_at ON pb_transactions (created_at);
CREATE INDEX idx_transactions_status ON pb_transactions (status);

-- ===================== TARJETAS VIRTUALES =====================
CREATE TABLE pb_virtual_cards (
    id                      VARCHAR2(36)    NOT NULL,
    account_id              VARCHAR2(36)    NOT NULL,
    user_id                 VARCHAR2(36)    NOT NULL,
    card_number_encrypted   VARCHAR2(500)   NOT NULL,
    last_four_digits        VARCHAR2(4)     NOT NULL,
    card_type               VARCHAR2(20)    NOT NULL,
    status                  VARCHAR2(20)    NOT NULL DEFAULT 'ACTIVE',
    expiry_year_month       VARCHAR2(7)     NOT NULL,
    cvv_encrypted           VARCHAR2(255)   NOT NULL,
    cvv_last_rotated_at     TIMESTAMP       NOT NULL,
    alias                   VARCHAR2(50),
    card_holder_name        VARCHAR2(100)   NOT NULL,
    blocked_reason          VARCHAR2(255),
    created_at              TIMESTAMP       NOT NULL,
    updated_at              TIMESTAMP,
    created_by              VARCHAR2(100),
    updated_by              VARCHAR2(100),
    CONSTRAINT pk_virtual_cards PRIMARY KEY (id),
    CONSTRAINT fk_cards_account FOREIGN KEY (account_id) REFERENCES pb_accounts(id),
    CONSTRAINT fk_cards_user FOREIGN KEY (user_id) REFERENCES pb_users(id)
);

CREATE INDEX idx_cards_account ON pb_virtual_cards (account_id);
CREATE INDEX idx_cards_user ON pb_virtual_cards (user_id);

-- ===================== CÓDIGOS ATM =====================
CREATE TABLE pb_atm_codes (
    id              VARCHAR2(36)    NOT NULL,
    user_id         VARCHAR2(36)    NOT NULL,
    account_id      VARCHAR2(36)    NOT NULL,
    code_hash       VARCHAR2(255)   NOT NULL,
    amount          NUMBER(18,2)    NOT NULL,
    status          VARCHAR2(20)    NOT NULL DEFAULT 'PENDING',
    expires_at      TIMESTAMP       NOT NULL,
    used_at         TIMESTAMP,
    used_at_ip      VARCHAR2(45),
    atm_identifier  VARCHAR2(100),
    created_at      TIMESTAMP       NOT NULL,
    updated_at      TIMESTAMP,
    created_by      VARCHAR2(100),
    updated_by      VARCHAR2(100),
    CONSTRAINT pk_atm_codes PRIMARY KEY (id),
    CONSTRAINT fk_atm_user FOREIGN KEY (user_id) REFERENCES pb_users(id),
    CONSTRAINT fk_atm_account FOREIGN KEY (account_id) REFERENCES pb_accounts(id),
    CONSTRAINT chk_atm_status CHECK (status IN ('PENDING','USED','EXPIRED','CANCELLED'))
);

CREATE INDEX idx_atm_user ON pb_atm_codes (user_id);
CREATE INDEX idx_atm_status ON pb_atm_codes (status);
CREATE INDEX idx_atm_expires ON pb_atm_codes (expires_at);

-- ===================== CRÉDITOS =====================
CREATE TABLE pb_loans (
    id                          VARCHAR2(36)    NOT NULL,
    user_id                     VARCHAR2(36)    NOT NULL,
    disbursement_account_id     VARCHAR2(36),
    loan_number                 VARCHAR2(20)    NOT NULL,
    requested_amount            NUMBER(18,2)    NOT NULL,
    approved_amount             NUMBER(18,2),
    outstanding_balance         NUMBER(18,2)    DEFAULT 0,
    interest_rate               NUMBER(6,4)     NOT NULL,
    term_months                 NUMBER(3)       NOT NULL,
    monthly_payment             NUMBER(18,2),
    purpose                     VARCHAR2(255)   NOT NULL,
    credit_score                NUMBER(4),
    status                      VARCHAR2(30)    NOT NULL DEFAULT 'PENDING_REVIEW',
    rejection_reason            VARCHAR2(500),
    approved_by                 VARCHAR2(36),
    approved_at                 TIMESTAMP,
    disbursed_at                TIMESTAMP,
    first_payment_date          DATE,
    next_payment_date           DATE,
    last_payment_date           DATE,
    payments_made               NUMBER(3)       DEFAULT 0,
    days_overdue                NUMBER(4)       DEFAULT 0,
    created_at                  TIMESTAMP       NOT NULL,
    updated_at                  TIMESTAMP,
    created_by                  VARCHAR2(100),
    updated_by                  VARCHAR2(100),
    CONSTRAINT pk_loans PRIMARY KEY (id),
    CONSTRAINT uq_loans_number UNIQUE (loan_number),
    CONSTRAINT fk_loans_user FOREIGN KEY (user_id) REFERENCES pb_users(id)
);

CREATE INDEX idx_loans_user ON pb_loans (user_id);
CREATE INDEX idx_loans_status ON pb_loans (status);

-- ===================== INVERSIONES =====================
CREATE TABLE pb_investments (
    id                  VARCHAR2(36)    NOT NULL,
    user_id             VARCHAR2(36)    NOT NULL,
    account_id          VARCHAR2(36)    NOT NULL,
    investment_number   VARCHAR2(20)    NOT NULL,
    investment_type     VARCHAR2(30)    NOT NULL,
    status              VARCHAR2(20)    NOT NULL DEFAULT 'ACTIVE',
    initial_amount      NUMBER(18,2)    NOT NULL,
    current_value       NUMBER(18,2)    NOT NULL,
    earned_returns      NUMBER(18,2)    DEFAULT 0,
    annual_interest_rate NUMBER(6,4)   NOT NULL,
    term_days           NUMBER(4),
    maturity_date       DATE,
    auto_reinvest       NUMBER(1)       DEFAULT 0,
    alias               VARCHAR2(100),
    created_at          TIMESTAMP       NOT NULL,
    updated_at          TIMESTAMP,
    created_by          VARCHAR2(100),
    updated_by          VARCHAR2(100),
    CONSTRAINT pk_investments PRIMARY KEY (id),
    CONSTRAINT uq_investments_number UNIQUE (investment_number),
    CONSTRAINT fk_investments_user FOREIGN KEY (user_id) REFERENCES pb_users(id),
    CONSTRAINT fk_investments_account FOREIGN KEY (account_id) REFERENCES pb_accounts(id)
);

CREATE INDEX idx_investments_user ON pb_investments (user_id);
CREATE INDEX idx_investments_status ON pb_investments (status);

-- ===================== BOLSILLOS DE AHORRO =====================
CREATE TABLE pb_saving_pockets (
    id                  VARCHAR2(36)    NOT NULL,
    user_id             VARCHAR2(36)    NOT NULL,
    account_id          VARCHAR2(36)    NOT NULL,
    name                VARCHAR2(100)   NOT NULL,
    emoji               VARCHAR2(10),
    target_amount       NUMBER(18,2),
    current_amount      NUMBER(18,2)    NOT NULL DEFAULT 0,
    target_date         DATE,
    rounding_enabled    NUMBER(1)       NOT NULL DEFAULT 0,
    rounding_multiplier NUMBER(5,2)     DEFAULT 1,
    is_active           NUMBER(1)       NOT NULL DEFAULT 1,
    is_completed        NUMBER(1)       NOT NULL DEFAULT 0,
    created_at          TIMESTAMP       NOT NULL,
    updated_at          TIMESTAMP,
    created_by          VARCHAR2(100),
    updated_by          VARCHAR2(100),
    CONSTRAINT pk_saving_pockets PRIMARY KEY (id),
    CONSTRAINT fk_pockets_user FOREIGN KEY (user_id) REFERENCES pb_users(id),
    CONSTRAINT fk_pockets_account FOREIGN KEY (account_id) REFERENCES pb_accounts(id)
);

CREATE INDEX idx_pockets_user ON pb_saving_pockets (user_id);
CREATE INDEX idx_pockets_account ON pb_saving_pockets (account_id);

-- ===================== ALERTAS DE FRAUDE =====================
CREATE TABLE pb_fraud_alerts (
    id              VARCHAR2(36)    NOT NULL,
    user_id         VARCHAR2(36)    NOT NULL,
    transaction_id  VARCHAR2(36),
    alert_type      VARCHAR2(30)    NOT NULL,
    severity        VARCHAR2(20)    NOT NULL,
    status          VARCHAR2(20)    NOT NULL DEFAULT 'OPEN',
    description     VARCHAR2(1000)  NOT NULL,
    ip_address      VARCHAR2(45),
    device_info     VARCHAR2(255),
    location_info   VARCHAR2(255),
    resolved_by     VARCHAR2(36),
    resolved_at     TIMESTAMP,
    resolution_notes VARCHAR2(1000),
    created_at      TIMESTAMP       NOT NULL,
    updated_at      TIMESTAMP,
    created_by      VARCHAR2(100),
    updated_by      VARCHAR2(100),
    CONSTRAINT pk_fraud_alerts PRIMARY KEY (id),
    CONSTRAINT fk_alerts_user FOREIGN KEY (user_id) REFERENCES pb_users(id),
    CONSTRAINT fk_alerts_trans FOREIGN KEY (transaction_id) REFERENCES pb_transactions(id),
    CONSTRAINT chk_alerts_status CHECK (status IN ('OPEN','INVESTIGATING','RESOLVED','FALSE_POSITIVE'))
);

CREATE INDEX idx_alerts_user ON pb_fraud_alerts (user_id);
CREATE INDEX idx_alerts_status ON pb_fraud_alerts (status);
CREATE INDEX idx_alerts_severity ON pb_fraud_alerts (severity);

-- ===================== TÉRMINOS Y CONDICIONES =====================
CREATE TABLE pb_terms_conditions (
    id                  VARCHAR2(36)    NOT NULL,
    version             VARCHAR2(20)    NOT NULL,
    title               VARCHAR2(255)   NOT NULL,
    content             CLOB            NOT NULL,
    effective_date      DATE            NOT NULL,
    is_active           NUMBER(1)       NOT NULL DEFAULT 0,
    privacy_policy_url  VARCHAR2(500),
    created_at          TIMESTAMP       NOT NULL,
    updated_at          TIMESTAMP,
    created_by          VARCHAR2(100),
    updated_by          VARCHAR2(100),
    CONSTRAINT pk_terms PRIMARY KEY (id),
    CONSTRAINT uq_terms_version UNIQUE (version)
);

-- ===================== ACEPTACIÓN DE TÉRMINOS =====================
CREATE TABLE pb_user_terms_acceptance (
    id          VARCHAR2(36)    NOT NULL,
    user_id     VARCHAR2(36)    NOT NULL,
    terms_id    VARCHAR2(36)    NOT NULL,
    accepted_at TIMESTAMP       NOT NULL,
    ip_address  VARCHAR2(45),
    user_agent  VARCHAR2(500),
    created_at  TIMESTAMP       NOT NULL,
    updated_at  TIMESTAMP,
    CONSTRAINT pk_terms_acceptance PRIMARY KEY (id),
    CONSTRAINT fk_acceptance_user FOREIGN KEY (user_id) REFERENCES pb_users(id),
    CONSTRAINT fk_acceptance_terms FOREIGN KEY (terms_id) REFERENCES pb_terms_conditions(id)
);

CREATE INDEX idx_terms_user ON pb_user_terms_acceptance (user_id);

-- ===================== REFRESH TOKENS =====================
CREATE TABLE pb_refresh_tokens (
    id          VARCHAR2(36)    NOT NULL,
    user_id     VARCHAR2(36)    NOT NULL,
    token_hash  VARCHAR2(255)   NOT NULL,
    expires_at  TIMESTAMP       NOT NULL,
    revoked     NUMBER(1)       NOT NULL DEFAULT 0,
    revoked_at  TIMESTAMP,
    ip_address  VARCHAR2(45),
    device_info VARCHAR2(255),
    created_at  TIMESTAMP       NOT NULL,
    updated_at  TIMESTAMP,
    created_by  VARCHAR2(100),
    updated_by  VARCHAR2(100),
    CONSTRAINT pk_refresh_tokens PRIMARY KEY (id),
    CONSTRAINT uq_refresh_token UNIQUE (token_hash),
    CONSTRAINT fk_refresh_user FOREIGN KEY (user_id) REFERENCES pb_users(id)
);

CREATE INDEX idx_refresh_user ON pb_refresh_tokens (user_id);
CREATE INDEX idx_refresh_token ON pb_refresh_tokens (token_hash);

-- ===================== AUDIT LOG =====================
CREATE TABLE pb_audit_logs (
    id          NUMBER          NOT NULL,
    user_id     VARCHAR2(36),
    action      VARCHAR2(100)   NOT NULL,
    entity_type VARCHAR2(50),
    entity_id   VARCHAR2(36),
    description VARCHAR2(1000),
    ip_address  VARCHAR2(45),
    user_agent  VARCHAR2(500),
    result      VARCHAR2(20),
    error_message VARCHAR2(500),
    created_at  TIMESTAMP       NOT NULL,
    CONSTRAINT pk_audit_logs PRIMARY KEY (id)
);

CREATE INDEX idx_audit_user ON pb_audit_logs (user_id);
CREATE INDEX idx_audit_action ON pb_audit_logs (action);
CREATE INDEX idx_audit_created ON pb_audit_logs (created_at);
