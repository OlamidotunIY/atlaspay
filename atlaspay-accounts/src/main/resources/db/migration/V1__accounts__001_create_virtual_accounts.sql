CREATE TABLE virtual_accounts (
    id VARCHAR(50) PRIMARY KEY,
    owner_id VARCHAR(50) NOT NULL,
    owner_type VARCHAR(20) NOT NULL,
    account_name VARCHAR(100) NOT NULL,
    bank_name VARCHAR(50) NOT NULL,
    nuban VARCHAR(10) NULL,
    status VARCHAR(30) NOT NULL,
    idempotency_key VARCHAR(100) UNIQUE NULL,
    version INT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    
    CONSTRAINT uq_nuban UNIQUE (nuban),
    CONSTRAINT uq_merchant_bank UNIQUE (owner_id, bank_name)
);

CREATE INDEX idx_owner ON virtual_accounts (owner_id, owner_type);
