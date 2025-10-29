-- Banking System Database Schema
-- Run this script to create all necessary tables

-- Drop existing tables if they exist (for clean setup)
DROP TABLE IF EXISTS transactions CASCADE;
DROP TABLE IF EXISTS accounts CASCADE;
DROP TABLE IF EXISTS customers CASCADE;

-- Customers table
CREATE TABLE customers (
    customer_id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE,
    phone VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Accounts table
CREATE TABLE accounts (
    account_id SERIAL PRIMARY KEY,
    customer_id INTEGER REFERENCES customers(customer_id) ON DELETE CASCADE,
    account_number VARCHAR(20) UNIQUE NOT NULL,
    account_type VARCHAR(20) NOT NULL CHECK (account_type IN ('CHECKING', 'SAVINGS', 'CREDIT')),
    balance DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    interest_rate DECIMAL(5,2) DEFAULT 0.00,
    credit_limit DECIMAL(15,2),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Transactions table
CREATE TABLE transactions (
    transaction_id SERIAL PRIMARY KEY,
    account_id INTEGER REFERENCES accounts(account_id) ON DELETE CASCADE,
    transaction_type VARCHAR(20) NOT NULL CHECK (transaction_type IN ('DEPOSIT', 'WITHDRAW', 'TRANSFER_IN', 'TRANSFER_OUT', 'INTEREST', 'FEE')),
    amount DECIMAL(15,2) NOT NULL,
    balance_after DECIMAL(15,2) NOT NULL,
    description TEXT,
    related_account VARCHAR(20), -- For transfers, stores the other account number
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better query performance
CREATE INDEX idx_accounts_customer ON accounts(customer_id);
CREATE INDEX idx_accounts_number ON accounts(account_number);
CREATE INDEX idx_transactions_account ON transactions(account_id);
CREATE INDEX idx_transactions_date ON transactions(transaction_date);

-- Insert some sample data for testing
INSERT INTO customers (name, email, phone) VALUES 
    ('John Doe', 'john.doe@email.com', '555-0101'),
    ('Jane Smith', 'jane.smith@email.com', '555-0102'),
    ('Bob Johnson', 'bob.johnson@email.com', '555-0103');

-- Sample accounts
INSERT INTO accounts (customer_id, account_number, account_type, balance, interest_rate, credit_limit) VALUES
    (1, 'CHK001', 'CHECKING', 1000.00, 0.00, NULL),
    (1, 'SAV001', 'SAVINGS', 5000.00, 2.50, NULL),
    (2, 'CHK002', 'CHECKING', 2500.00, 0.00, NULL),
    (2, 'CRD001', 'CREDIT', 0.00, 0.00, 5000.00),
    (3, 'SAV002', 'SAVINGS', 10000.00, 2.50, NULL);

-- Sample transactions
INSERT INTO transactions (account_id, transaction_type, amount, balance_after, description) VALUES
    (1, 'DEPOSIT', 1000.00, 1000.00, 'Initial deposit'),
    (2, 'DEPOSIT', 5000.00, 5000.00, 'Initial deposit'),
    (3, 'DEPOSIT', 2500.00, 2500.00, 'Initial deposit'),
    (5, 'DEPOSIT', 10000.00, 10000.00, 'Initial deposit');

-- Verify the setup
SELECT 'Customers created:' as status, COUNT(*) as count FROM customers
UNION ALL
SELECT 'Accounts created:', COUNT(*) FROM accounts
UNION ALL
SELECT 'Transactions logged:', COUNT(*) FROM transactions;