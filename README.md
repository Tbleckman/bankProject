# Banking System — Containerized, Microservice-Backed, PostgreSQL-Integrated

A Java banking management system, modernized into a containerized, multi-service application. What started as a terminal-based banking simulator with PostgreSQL persistence now runs as three coordinated Docker services: the Java core app, a Postgres database, and a Python/FastAPI risk-analysis microservice — all orchestrated with Docker Compose and ready to move to AWS ECS Fargate.

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

### Risk Analysis Microservice
- **Separate Python/FastAPI service** - Decoupled from the Java core, called over HTTP
- **Per-transaction risk scoring** - Flags large amounts, high recent activity, and withdrawal patterns
- **Independently deployable** - Own Dockerfile, own dependency set, communicates only through a defined JSON contract

### Containerization & Cloud Readiness
- **Multi-stage Docker build** - Java app built in a Maven container, run in a slim JRE container
- **Docker Compose orchestration** - Postgres, the Java app, and the risk service start together with proper health-check-based startup ordering
- **Environment-variable configuration** - Both the Java app and the risk service read all connection details from env vars, with local defaults — no code changes needed to move from a laptop to ECS Fargate + RDS
- **AWS-ready** - Designed as the containerization step ahead of an ECS Fargate deployment with ECR and RDS

### Technical Features
- **Custom data structures** - Linked list implementations for transaction management
- **Dynamic polymorphism** - Bank interface with multiple implementations
- **Error handling** - User input validation and exception management
- **JDBC integration** - Professional database connectivity patterns

## Technical Stack

- **Language**: Java 11+ (core app), Python 3.12 (risk microservice)
- **Database**: PostgreSQL 16 (containerized) / 14+ (local)
- **Build Tool**: Maven 3.9 (multi-stage Docker build)
- **JDBC Driver**: PostgreSQL 42.7.1
- **Microservice Framework**: FastAPI + Uvicorn
- **Containerization**: Docker, Docker Compose
- **Data Structures**: Custom linked lists

## Architecture

```
Docker Compose Stack
│
├── app (Java, multi-stage build: Maven → JRE)
│   ├── User Layer
│   │   ├── User object (pocket money, transaction history)
│   │   └── Bank accounts array (max 3)
│   │
│   ├── Bank Layer (Interface-based)
│   │   ├── BNYMellon implementation
│   │   ├── Chase implementation
│   │   └── CapitalOne implementation
│   │
│   ├── Database Layer
│   │   ├── DatabaseConfig (env-var-driven connection management)
│   │   ├── DatabaseManager (SQL operations)
│   │   └── AccountDatabaseAdapter (ORM bridge)
│   │
│   └── Risk Layer
│       └── RiskServiceClient (HTTP client → risk_service)
│
├── db (Postgres 16, healthchecked before app starts)
│   └── banking_schema.sql (auto-loaded on first boot)
│
└── risk_service (Python/FastAPI, independent container)
    └── POST /analyze → { risk_score, flagged, reason }

Storage
├── PostgreSQL (primary persistence, containerized or RDS)
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

### Option A: Docker Compose (recommended)

This is the fastest way to run the full stack — Java app, Postgres, and the risk-analysis microservice — with zero local Java/Postgres/Python setup.

**Prerequisites:**
```bash
docker --version          # Need Docker 24+
docker compose version    # Need Compose v2
```

**Run it:**
```bash
docker compose up --build
```

That single command will:
1. Build the Java app image (multi-stage: Maven build → slim JRE runtime)
2. Build the risk_service image (Python 3.12 + FastAPI/Uvicorn)
3. Start Postgres, wait for it to report healthy, then start the risk service and the Java app
4. Auto-load `banking_schema.sql` into Postgres on first boot
5. Attach to the Java app's terminal UI (compose runs it with `stdin_open`/`tty` so the interactive menu works normally)

The risk service is reachable at `http://localhost:8000` from your host (e.g. `curl http://localhost:8000/health`), and at `http://risk-service:8000` from inside the `app` container.

**Stopping / resetting:**
```bash
docker compose down          # stop containers, keep the Postgres volume (data persists)
docker compose down -v       # stop containers and wipe the Postgres volume (clean slate)
```

No connection strings, passwords, or hostnames need to be edited anywhere — `compose.yaml` injects `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`, `RISK_SERVICE_HOST`, and `RISK_SERVICE_PORT` as environment variables, and both the Java app and the risk service read them at startup with sensible local defaults if they're absent.

> **Note:** the Postgres credentials in `compose.yaml` are plaintext defaults intended for local development only (`postgres`/`postgres`). In the AWS deployment these are replaced by RDS-managed credentials pulled from Secrets Manager / environment injection at the ECS task level — not committed to the repo.

### Option B: Manual local setup (no Docker)

Useful if you want to run the Java app directly against a local Postgres install without containers.

**Prerequisites:**
```bash
# Check versions
java -version    # Need Java 11+
mvn -version     # Need Maven 3.6+
psql --version   # Need PostgreSQL 14+
```

**1. Install PostgreSQL**

*Ubuntu/WSL:*
```bash
sudo apt update
sudo apt install postgresql postgresql-contrib
sudo service postgresql start
```

*macOS:*
```bash
brew install postgresql@14
brew services start postgresql@14
```

*Windows:*
- Download from [postgresql.org](https://www.postgresql.org/download/windows/)
- Run installer with default settings

**2. Create Database**

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

**3. Run Database Schema**

```bash
# Run the schema file to create tables
sudo -u postgres psql -d banking_system -f banking_schema.sql

# Verify tables were created
sudo -u postgres psql -d banking_system -c "\dt"
```

**4. Configure Database Connection**

The app reads `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, and `DB_PASSWORD` from environment variables, falling back to `localhost:5432/banking_system` with user/password `postgres`/`postgres` if unset. Either export the variables to match your local setup, or edit the defaults directly in `src/main/java/database/DatabaseConfig.java`.

**5. (Optional) Run the risk service locally**

```bash
cd risk_service
pip install -r requirements.txt
uvicorn app:app --host 0.0.0.0 --port 8000
```
If skipped, `RiskServiceClient` will fail its HTTP calls to `localhost:8000` — the app still runs, but risk-flagged transactions won't be available.

**6. Build and Run**

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

## Risk Analysis Microservice

The `risk_service/` directory is a standalone FastAPI application, deliberately kept decoupled from the Java app — it has its own Dockerfile, its own dependencies, and communicates only through HTTP/JSON. This mirrors how a real risk/fraud-scoring service would sit alongside a core banking system: separately deployable, separately scalable, and replaceable without touching the Java codebase.

**Endpoints:**

`GET /health`
```json
{ "status": "ok" }
```

`POST /analyze`
```json
// Request
{ "amount": 1500.00, "transaction_type": "withdrawal", "recent_transactions": 6 }

// Response
{ "risk_score": 0.9, "flagged": true, "reason": "large transaction, high recent transaction activity" }
```

Scoring is intentionally simple (amount thresholds, recent-activity thresholds, withdrawal weighting) — the point of this service isn't a production-grade fraud model, it's the integration pattern: a Java monolith calling out to an independently deployed microservice over HTTP, with its own container lifecycle.

On the Java side, `RiskServiceClient` (in `service/`) builds the request, calls `RISK_SERVICE_HOST:RISK_SERVICE_PORT/analyze`, and parses the response into a `RiskAnalysis` object. Like the database config, the host and port are environment-variable-driven with `localhost:8000` as the local-dev default.

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
- **Plaintext local credentials** - `compose.yaml` uses hardcoded Postgres credentials suitable only for local development, not production
- **No CI/CD yet** - Images are built and run manually; GitHub Actions automation is planned (see Roadmap)

## Future Enhancements

### AWS Deployment (in progress)
The containerization work above is the first phase of a larger cloud migration:
- **ECR** - Push the Java app and risk_service images to private ECR repositories
- **RDS** - Replace the containerized Postgres with a managed RDS instance (schema and env-var-driven config are already compatible — no app code changes required)
- **ECS Fargate** - Run both services as Fargate tasks behind the same environment-variable contract used today
- **GitHub Actions CI/CD** - Build, tag, and push images on merge, using OIDC federation for AWS auth (no long-lived credentials)

### Feature Roadmap
The database schema supports these planned features:

- **User authentication** - Login system with username/password
- **Multiple account types** - SAVINGS and CREDIT card accounts
- **Interest calculations** - Automatic interest on savings accounts
- **Credit limits** - Overdraft protection and credit card limits
- **Account transfers** - Move money between your own accounts
- **Multi-user support** - Separate sessions for different users
- **Transaction categories** - Tag transactions (bills, groceries, etc.)
- **Reporting** - Monthly statements and spending analytics
- **Real risk model** - Replace the rule-based risk_service scoring with a trained model or a third-party fraud-detection API

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
├── database/
│   ├── DatabaseConfig.java           # Connection management (env-var-driven)
│   ├── DatabaseManager.java          # SQL operations
│   ├── AccountDatabaseAdapter.java   # Object-relational mapping
│   └── DatabaseTest.java            # Integration tests
└── service/
    ├── RiskServiceClient.java        # HTTP client for risk_service (env-var-driven)
    └── RiskAnalysis.java             # Risk response domain model

risk_service/
├── app.py                  # FastAPI app (/health, /analyze)
├── requirements.txt        # fastapi, uvicorn[standard]
└── Dockerfile              # python:3.12-slim image

Dockerfile                  # Multi-stage build for the Java app
compose.yaml                # Orchestrates app + db + risk_service
.dockerignore                # Excludes target/, .git/, docs from build context
banking_schema.sql          # Database setup script (auto-loaded by Postgres container)
pom.xml                     # Maven configuration
README.md                   # This file
```

## Testing

**Manual Testing (Docker):**
```bash
docker compose up --build

# Test workflow:
# 1. Create account with $1000, deposit $200 to bank
# 2. Make deposit of $100
# 3. Make withdrawal of $50 (triggers a risk_service call)
# 4. Save session and exit
# 5. docker compose up again and load the saved session
# 6. Verify balance is correct (should be $250)
```

**Manual Testing (local, no Docker):**
```bash
# Run the application
mvn exec:java -Dexec.mainClass="RUNME"

# Same workflow as above
```

**Risk Service Verification:**
```bash
# With the stack running via docker compose:
curl http://localhost:8000/health
curl -X POST http://localhost:8000/analyze \
  -H "Content-Type: application/json" \
  -d '{"amount": 1500, "transaction_type": "withdrawal", "recent_transactions": 6}'
```

**Database Verification:**
```bash
# Docker: exec into the db container
docker compose exec db psql -U postgres -d banking_system

# Local: connect directly
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
