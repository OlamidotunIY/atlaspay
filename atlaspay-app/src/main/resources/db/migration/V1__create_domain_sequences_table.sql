CREATE TABLE domain_sequences (
    sequence_name VARCHAR(50) PRIMARY KEY,
    next_val BIGINT NOT NULL
);

INSERT INTO domain_sequences (sequence_name, next_val) VALUES
('merchant_seq', 1000),
('customer_seq', 1000),
('subaccount_seq', 1000),
('apikey_seq', 1000),
('virtual_account_seq', 1000);
