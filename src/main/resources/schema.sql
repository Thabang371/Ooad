-- Database schema for the Banking System. It mirrors the class diagram:
--   Customer (1) ---- (1..*) Account (1) ---- (*) Transaction
--   Account subclasses (Savings / Investment / Cheque) are stored in ONE table with an account_type
--   column (single-table inheritance). Cheque-only fields (employer) are nullable columns.
--   ON DELETE CASCADE enforces composition: deleting a customer deletes their accounts.

CREATE TABLE IF NOT EXISTS customers (
    customer_id    VARCHAR(10)  PRIMARY KEY,
    first_name     VARCHAR(50)  NOT NULL,
    surname        VARCHAR(50)  NOT NULL,
    address        VARCHAR(200),
    username       VARCHAR(50)  NOT NULL UNIQUE,
    password_hash  CHAR(64)     NOT NULL
);

CREATE TABLE IF NOT EXISTS accounts (
    account_number   VARCHAR(20)   PRIMARY KEY,
    customer_id      VARCHAR(10)   NOT NULL,
    account_type     VARCHAR(12)   NOT NULL,
    branch           VARCHAR(50),
    balance          DECIMAL(15,2) NOT NULL,
    employer_name    VARCHAR(100),
    employer_address VARCHAR(200),
    CONSTRAINT fk_account_customer FOREIGN KEY (customer_id)
        REFERENCES customers(customer_id) ON DELETE CASCADE,
    CONSTRAINT chk_account_type CHECK (account_type IN ('SAVINGS', 'INVESTMENT', 'CHEQUE')),
    CONSTRAINT chk_balance CHECK (balance >= 0),
    CONSTRAINT chk_cheque_employer CHECK (account_type <> 'CHEQUE' OR employer_name IS NOT NULL)
);

CREATE TABLE IF NOT EXISTS transactions (
    transaction_id  INT AUTO_INCREMENT PRIMARY KEY,
    account_number  VARCHAR(20)   NOT NULL,
    txn_type        VARCHAR(12)   NOT NULL,
    amount          DECIMAL(15,2) NOT NULL,
    balance_after   DECIMAL(15,2) NOT NULL,
    created_at      TIMESTAMP     NOT NULL,
    CONSTRAINT fk_txn_account FOREIGN KEY (account_number)
        REFERENCES accounts(account_number) ON DELETE CASCADE,
    CONSTRAINT chk_txn_type CHECK (txn_type IN ('DEPOSIT', 'WITHDRAWAL', 'INTEREST'))
);
