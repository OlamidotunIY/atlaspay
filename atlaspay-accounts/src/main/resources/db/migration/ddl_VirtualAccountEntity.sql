CREATE TABLE virtual_accounts
(
    id              VARCHAR(50)  NOT NULL,
    owner_id        VARCHAR(50)  NOT NULL,
    owner_type      VARCHAR(20)  NOT NULL,
    account_name    VARCHAR(100) NOT NULL,
    bank_name       VARCHAR(50)  NOT NULL,
    nuban           VARCHAR(10)  NULL,
    status          VARCHAR(30)  NOT NULL,
    idempotency_key VARCHAR(100) NULL,
    version         INT          NOT NULL,
    created_at      datetime     NOT NULL,
    updated_at      datetime     NOT NULL,
    CONSTRAINT pk_virtual_accounts PRIMARY KEY (id)
);

ALTER TABLE virtual_accounts
    ADD CONSTRAINT uk_va_idempotency UNIQUE (idempotency_key);

ALTER TABLE virtual_accounts
    ADD CONSTRAINT uk_va_nuban UNIQUE (nuban);

CREATE INDEX idx_va_nuban ON virtual_accounts (nuban);

CREATE INDEX idx_va_owner ON virtual_accounts (owner_id);