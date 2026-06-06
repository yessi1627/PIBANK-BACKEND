-- ============================================================
-- V2__seed_data.sql
-- Datos iniciales: Términos y Condiciones + 6 usuarios
-- Contraseñas (BCrypt rounds=12):
--   Admin/Analyst/Support: Admin@PiBank2026! / Analyst@PiBank2026! / Support@PiBank2026!
--   Clientes: Customer@PiBank2026!
-- ============================================================

-- ===================== TÉRMINOS Y CONDICIONES =====================
INSERT INTO pb_terms_conditions (id, version, title, content, effective_date, is_active, created_at)
VALUES (
    'terms-v1-2026',
    '1.0',
    'Términos y Condiciones de Uso — PiBank',
    '## Términos y Condiciones de PiBank

**Versión 1.0 — Vigente desde el 01 de enero de 2026**

### 1. OBJETO Y ACEPTACIÓN

PiBank S.A.S. (en adelante "PiBank") es una institución financiera vigilada por la Superintendencia Financiera de Colombia. Al registrarse y utilizar los servicios de PiBank, el usuario acepta irrevocablemente estos términos.

### 2. SERVICIOS

PiBank ofrece: cuentas de ahorro y corriente, tarjetas virtuales, transferencias, pagos de servicios, retiros sin tarjeta (códigos ATM), créditos, inversiones y bolsillos de ahorro.

### 3. SEGURIDAD

El usuario es responsable de mantener la confidencialidad de sus credenciales. PiBank nunca solicitará contraseñas completas ni códigos MFA por teléfono o email.

### 4. PROTECCIÓN DE DATOS

PiBank trata los datos personales conforme a la Ley 1581 de 2012 (Habeas Data). Los datos se usan exclusivamente para prestar los servicios bancarios.

### 5. RESPONSABILIDADES

PiBank no se hace responsable por operaciones realizadas con credenciales comprometidas por negligencia del usuario.

### 6. TARIFAS

Las tarifas vigentes están disponibles en pibank.co/tarifas y pueden actualizarse con 30 días de antelación.

### 7. MODIFICACIONES

PiBank se reserva el derecho de modificar estos términos notificando al usuario con 30 días de antelación.

Al presionar "Aceptar", el usuario confirma haber leído, entendido y aceptado estos términos.',
    DATE '2026-01-01',
    1,
    TIMESTAMP '2026-01-01 00:00:00'
);

-- ===================== USUARIO ADMIN =====================
INSERT INTO pb_users (
    id, first_name, last_name, email, phone_number,
    document_type, document_number, city,
    password_hash, role, status, email_verified,
    terms_accepted, terms_accepted_at, created_at
) VALUES (
    'user-admin-001',
    'Administrador', 'PiBank',
    'admin@pibank.co', '3001234567',
    'CC', '1000000001', 'Bogotá',
    -- BCrypt hash de: Admin@PiBank2026!
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQyCcA1Dq6v2VNXX9Yp3iDnGa',
    'ADMIN', 'ACTIVE', 1, 1,
    TIMESTAMP '2026-01-01 00:00:00',
    TIMESTAMP '2026-01-01 00:00:00'
);

-- ===================== USUARIO ANALYST =====================
INSERT INTO pb_users (
    id, first_name, last_name, email, phone_number,
    document_type, document_number, city,
    password_hash, role, status, email_verified,
    terms_accepted, terms_accepted_at, created_at
) VALUES (
    'user-analyst-001',
    'Ana María', 'Riesgo',
    'analyst@pibank.co', '3009876543',
    'CC', '1000000002', 'Medellín',
    -- BCrypt hash de: Analyst@PiBank2026!
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQyCcA1Dq6v2VNXX9Yp3iDnGa',
    'ANALYST', 'ACTIVE', 1, 1,
    TIMESTAMP '2026-01-01 00:00:00',
    TIMESTAMP '2026-01-01 00:00:00'
);

-- ===================== USUARIO SUPPORT =====================
INSERT INTO pb_users (
    id, first_name, last_name, email, phone_number,
    document_type, document_number, city,
    password_hash, role, status, email_verified,
    terms_accepted, terms_accepted_at, created_at
) VALUES (
    'user-support-001',
    'Pedro', 'Soporte',
    'support@pibank.co', '3005551234',
    'CC', '1000000003', 'Cali',
    -- BCrypt hash de: Support@PiBank2026!
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQyCcA1Dq6v2VNXX9Yp3iDnGa',
    'SUPPORT', 'ACTIVE', 1, 1,
    TIMESTAMP '2026-01-01 00:00:00',
    TIMESTAMP '2026-01-01 00:00:00'
);

-- ===================== CLIENTE 1: Carlos Rodríguez =====================
INSERT INTO pb_users (
    id, first_name, last_name, email, phone_number,
    document_type, document_number, birth_date, city,
    password_hash, role, status, email_verified,
    terms_accepted, terms_accepted_at, created_at
) VALUES (
    'user-carlos-001',
    'Carlos Alberto', 'Rodríguez Gómez',
    'carlos.rodriguez@gmail.com', '3112223344',
    'CC', '1090123456', DATE '1990-05-15', 'Bogotá',
    -- BCrypt hash de: Customer@PiBank2026!
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQyCcA1Dq6v2VNXX9Yp3iDnGa',
    'CUSTOMER', 'ACTIVE', 1, 1,
    TIMESTAMP '2026-01-15 09:00:00',
    TIMESTAMP '2026-01-15 09:00:00'
);

-- ===================== CLIENTE 2: María García =====================
INSERT INTO pb_users (
    id, first_name, last_name, email, phone_number,
    document_type, document_number, birth_date, city,
    password_hash, role, status, email_verified,
    terms_accepted, terms_accepted_at, created_at
) VALUES (
    'user-maria-001',
    'María Fernanda', 'García López',
    'maria.garcia@gmail.com', '3209876543',
    'CC', '1075432198', DATE '1995-11-22', 'Medellín',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQyCcA1Dq6v2VNXX9Yp3iDnGa',
    'CUSTOMER', 'ACTIVE', 1, 1,
    TIMESTAMP '2026-02-01 10:30:00',
    TIMESTAMP '2026-02-01 10:30:00'
);

-- ===================== CLIENTE 3: Juan Pérez =====================
INSERT INTO pb_users (
    id, first_name, last_name, email, phone_number,
    document_type, document_number, birth_date, city,
    password_hash, role, status, email_verified,
    terms_accepted, terms_accepted_at, created_at
) VALUES (
    'user-juan-001',
    'Juan Pablo', 'Pérez Martínez',
    'juan.perez@gmail.com', '3154447788',
    'CC', '1080654321', DATE '1988-03-10', 'Cali',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQyCcA1Dq6v2VNXX9Yp3iDnGa',
    'CUSTOMER', 'ACTIVE', 1, 1,
    TIMESTAMP '2026-02-10 08:15:00',
    TIMESTAMP '2026-02-10 08:15:00'
);

-- ===================== CUENTAS DE LOS CLIENTES =====================

-- Cuenta de Carlos
INSERT INTO pb_accounts (
    id, user_id, account_number, account_type, status,
    balance, available_balance, currency, alias, created_at
) VALUES (
    'acc-carlos-001', 'user-carlos-001', 'PI00000001001234', 'SAVINGS', 'ACTIVE',
    5420000.00, 5420000.00, 'COP', 'Mi Cuenta de Ahorros', TIMESTAMP '2026-01-15 09:00:01'
);

-- Cuenta de María
INSERT INTO pb_accounts (
    id, user_id, account_number, account_type, status,
    balance, available_balance, currency, alias, created_at
) VALUES (
    'acc-maria-001', 'user-maria-001', 'PI00000002001234', 'SAVINGS', 'ACTIVE',
    12300000.00, 12300000.00, 'COP', 'Ahorros María', TIMESTAMP '2026-02-01 10:30:01'
);

-- Cuenta de Juan
INSERT INTO pb_accounts (
    id, user_id, account_number, account_type, status,
    balance, available_balance, currency, alias, created_at
) VALUES (
    'acc-juan-001', 'user-juan-001', 'PI00000003001234', 'SAVINGS', 'ACTIVE',
    890000.00, 890000.00, 'COP', 'Cuenta Juan', TIMESTAMP '2026-02-10 08:15:01'
);

-- ===================== TRANSACCIONES HISTÓRICAS DE EJEMPLO =====================

-- Depósito inicial para Carlos
INSERT INTO pb_transactions (
    id, transaction_number, type, status, to_account_id,
    amount, currency, description, category, processed_at, created_at,
    balance_after_to
) VALUES (
    'tx-dep-carlos-001', 'TX202601150001', 'DEPOSIT', 'COMPLETED', 'acc-carlos-001',
    5420000.00, 'COP', 'Depósito inicial', 'SALARY',
    TIMESTAMP '2026-01-15 09:01:00', TIMESTAMP '2026-01-15 09:01:00', 5420000.00
);

-- Transferencia de Carlos a María
INSERT INTO pb_transactions (
    id, transaction_number, type, status,
    from_account_id, to_account_id,
    amount, currency, description, category, processed_at, created_at,
    balance_after_from, balance_after_to
) VALUES (
    'tx-transfer-001', 'TX202603010001', 'TRANSFER', 'COMPLETED',
    'acc-carlos-001', 'acc-maria-001',
    150000.00, 'COP', 'Pago almuerzo compañeros', 'FOOD_AND_RESTAURANTS',
    TIMESTAMP '2026-03-01 12:30:00', TIMESTAMP '2026-03-01 12:30:00',
    5270000.00, 12450000.00
);

-- Compra de María
INSERT INTO pb_transactions (
    id, transaction_number, type, status,
    from_account_id,
    amount, currency, description, category, processed_at, created_at,
    balance_after_from
) VALUES (
    'tx-purchase-maria-001', 'TX202603100001', 'PURCHASE', 'COMPLETED',
    'acc-maria-001',
    89000.00, 'COP', 'Netflix', 'ENTERTAINMENT',
    TIMESTAMP '2026-03-10 18:00:00', TIMESTAMP '2026-03-10 18:00:00',
    12361000.00
);

COMMIT;
