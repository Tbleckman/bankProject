# Banking System with PostgreSQL Integration

A Java-based banking management system featuring persistent storage with PostgreSQL database integration. Manages user accounts across multiple banks with complete transaction logging and session continuity.

## Features

### Core Banking Operations
- **Multi-bank account management** - Support for mock BNY Mellon, Chase, and Capital One accounts
- **Financial transactions** - Deposits, withdrawals with real-time balance updates
- **Session persistence** - Save and resume sessions across program restarts
- **Transaction history** - Complete audit trail of all operations
- **Account management** - Create, switch between, and close bank accounts (max 3 per user)

### Database Integration
- **PostgreSQL persistent storage** - All account data survives program restarts
- **Automatic account recovery** - Load existing accounts when resuming from saved sessions (using the created txt file from prior session)
- **Transaction logging** - Every deposit/withdrawal logged with timestamps
- **Unique account identifiers** - Timestamp-based account numbers prevent conflicts, and can be also used to identify accounts from prior sessions
- **Hybrid persistence** - File I/O for portability, database for data integrity, can use both to pull prior sessions

### Technical Features
- **Custom data structures** - Linked list implementations for transaction management
- **Dynamic polymorphism** - Bank interface with multiple implementations
- **Error handling** - User input validation and exception management
- **JDBC integration** - Professional database connectivity patterns

## Technical Stack

- **Language**: Java 11+
- **Database**: PostgreSQL 14+
- **Build Tool**: Maven 3.6+
- **JDBC Driver**: PostgreSQL 42.7.1
- **Data Structures**: Custom linked lists

## Architecture

```
User Layer
├── User object (pocket money, transaction history)
└── Bank accounts array (max 3)

Bank Layer (Interface-based)
├── BNYMellon implementation
├── Chase implementation  
└── CapitalOne implementation

Database Layer
├── DatabaseConfig (connection management)
├── DatabaseManager (SQL operations)
└── AccountDatabaseAdapter (ORM bridge)

Storage
├── PostgreSQL (primary persistence)
└── Text files (session backup/portability)
```

## Database Schema

### Tables

**customers**
- `customer_id` (PRIMARY KEY)
- `name`, `email`, `phone`
- `created_at` timestamp

**accounts**
- `account_id` (PRIMARY KEY)
- `customer_id` (FOREIGN KEY)
- `account_number` (UNIQUE) - e.g., BNY1730147823456
- `account_type` - CHECKING, SAVINGS, or CREDIT
- `balance`, `interest_rate`, `credit_limit`
- `is_active` (soft delete flag)
- `created_at`, `updated_at` timestamps

**transactions**
- `transaction_id` (PRIMARY KEY)
- `account_id` (FOREIGN KEY)
- `transaction_type` - DEPOSIT, WITHDRAW, TRANSFER_IN, TRANSFER_OUT
- `amount`, `balance_after`
- `description`, `related_account`
- `transaction_date` timestamp

## Setup Instructions

### Prerequisites
```bash
# Check versions
java -version    # Need Java 11+
mvn -version     # Need Maven 3.6+
psql --version   # Need PostgreSQL 14+
```

### 1. Install PostgreSQL

**Ubuntu/WSL:**
```bash
sudo apt update
sudo apt install postgresql postgresql-contrib
sudo service postgresql start
```

**macOS:**
```bash
brew install postgresql@14
brew services start postgresql@14
```

**Windows:**
- Download from [postgresql.org](https://www.postgresql.org/download/windows/)
- Run installer with default settings

### 2. Create Database

```bash
# Connect to PostgreSQL
sudo -u postgres psql

# Create database
CREATE DATABASE banking_system;

# Set password for postgres user
ALTER USER postgres WITH PASSWORD 'your_password';

# Exit
\q
```

### 3. Run Database Schema

```bash
# Run the schema file to create tables
sudo -u postgres psql -d banking_system -f banking_schema.sql

# Verify tables were created
sudo -u postgres psql -d banking_system -c "\dt"
```

### 4. Configure Database Connection

Edit `src/main/java/database/DatabaseConfig.java`:
```java
private static final String DB_PASSWORD = "your_password";
```

### 5. Build and Run

```bash
# Compile project
mvn clean compile

# Run application
mvn exec:java -Dexec.mainClass="RUNME"
```

## Usage Guide

### Starting a Session

**New User:**
- Choose 'N' when asked about previous sessions
- Enter starting funds
- Select a bank and initial deposit amount

**Returning User (from text file):**
- Choose 'Y' when asked about previous sessions
- Program automatically loads accounts from database
- Database balance takes precedence over file balance

### Menu Options

1. **View bank balance** - Check current bank funds
2. **View personal funds** - Check pocket money
3. **Deposit** - Transfer money from pocket to bank
4. **Withdraw** - Transfer money from bank to pocket
5. **View all transactions** - See complete transaction history
6. **Account overview** - Full financial summary
7. **Add bank account** - Create additional account (max 3)
8. **Remove bank account** - Close and delete account
9. **Switch banks** - Change active account
10. **View bank transactions** - See bank-specific history
11. **Wipe transaction history** - Clear records
12. **Wipe transaction history** - Clear records
13. **Adjust pocket money** - Add/remove personal funds
14. **Adjust pocket money** - Add/remove personal funds
15. **End session** - Exit and optionally save

### Session Management

**Saving:**
- Choose option 15 to exit
- Select 'Y' to save session
- Creates `bankProject.txt` with account details
- Database automatically maintains all data

**Loading:**
- Start program and choose 'Y' for previous session
- Program loads from `bankProject.txt`
- Checks database for current account balances
- Merges file and database data seamlessly

## Database Features

### Automatic Account Recovery
When loading a saved session:
1. Program reads account numbers from text file
2. Queries database for each account
3. Uses database balance (current) over file balance (potentially stale)
4. Creates database entries for accounts not yet in database

### Transaction Audit Trail
Every operation is logged:
```sql
-- View recent transactions
SELECT transaction_type, amount, balance_after, transaction_date 
FROM transactions 
ORDER BY transaction_date DESC LIMIT 10;

-- View specific account history
SELECT * FROM transactions 
WHERE account_id = (
    SELECT account_id FROM accounts 
    WHERE account_number = 'BNY1730147823456'
);
```

### Account Management
```sql
-- View all active accounts
SELECT account_number, account_type, balance, created_at 
FROM accounts 
WHERE is_active = true;

-- View deleted accounts (soft delete)
SELECT account_number, balance, updated_at 
FROM accounts 
WHERE is_active = false;
```

## Design Patterns

- **Interface-based polymorphism** - Bank interface with multiple implementations
- **Adapter pattern** - AccountDatabaseAdapter bridges domain objects with database
- **Repository pattern** - DatabaseManager encapsulates data access
- **Singleton-like** - Static database adapter in Interface class
- **Hybrid persistence** - File + database for redundancy

## Current Limitations

- **Need txt file to restore prior sessions** - must use a txt file created by a prior section to retrieve the correct account information
- **Maximum 3 bank accounts** - Architectural constraint
- **Account counter resets** - Uses timestamps to avoid conflicts
- **Terminal UI only** - No graphical interface
- **No authentication** - No login/password system

## Future Enhancements

The database schema supports these planned features:

- **User authentication** - Login system with username/password
- **Multiple account types** - SAVINGS and CREDIT card accounts
- **Interest calculations** - Automatic interest on savings accounts
- **Credit limits** - Overdraft protection and credit card limits
- **Account transfers** - Move money between your own accounts
- **Multi-user support** - Separate sessions for different users
- **Transaction categories** - Tag transactions (bills, groceries, etc.)
- **Reporting** - Monthly statements and spending analytics

## Learning Outcomes

This project demonstrates:

- **Object-oriented design** - Interfaces, inheritance, polymorphism
- **Data structures** - Custom linked list implementations
- **Database integration** - JDBC, SQL, schema design
- **Persistence patterns** - File I/O and database storage
- **Error handling** - Input validation, exception management
- **Build automation** - Maven dependency management
- **Software architecture** - Layered design, separation of concerns

## Files Overview

```
src/main/java/
├── RUNME.java              # Application entry point
├── Interface.java          # Main UI controller and menu system
├── User.java              # User domain model
├── Bank.java              # Bank interface
├── BNYMellon.java         # BNY Mellon bank implementation
├── Chase.java             # Chase bank implementation
├── CapitalOne.java        # Capital One bank implementation
└── database/
    ├── DatabaseConfig.java           # Connection management
    ├── DatabaseManager.java          # SQL operations
    ├── AccountDatabaseAdapter.java   # Object-relational mapping
    └── DatabaseTest.java            # Integration tests

banking_schema.sql         # Database setup script
pom.xml                   # Maven configuration
README.md                 # This file
```

## Testing

**Manual Testing:**
```bash
# Run the application
mvn exec:java -Dexec.mainClass="RUNME"

# Test workflow:
# 1. Create account with $1000, deposit $200 to bank
# 2. Make deposit of $100
# 3. Make withdrawal of $50
# 4. Save session and exit
# 5. Restart and load session
# 6. Verify balance is correct (should be $250)
```

**Database Verification:**
```bash
sudo -u postgres psql -d banking_system

-- Check accounts
SELECT account_number, balance FROM accounts 
WHERE account_number LIKE 'BNY%' OR account_number LIKE 'CHS%' OR account_number LIKE 'CAP%';

-- Check transactions
SELECT transaction_type, amount, balance_after, transaction_date 
FROM transactions 
ORDER BY transaction_date DESC LIMIT 5;

\q
```

**Run Database Tests:**
```bash
mvn exec:java -Dexec.mainClass="database.DatabaseTest"
```

## Demo

See DEMO.md for screenshots and example workflows.

## Contributing

This is an educational project. The code demonstrates:

- Clean architecture principles
- Database integration patterns
- Legacy system migration (file → database)
- Extensible design for future features

## License

Educational project - free to use for learning purposes.

## Author

**Thomas Bleckman**

GitHub: [@Tbleckman](https://github.com/Tbleckman)

---

## Notes

- The file I/O system remains functional for backwards compatibility, as well as the way to retrieve prior banking sessions and their accounts
- Database serves as the source of truth when both exist
- Account numbers use timestamps to ensure global uniqueness
- Schema supports multi-user architecture for future expansion
