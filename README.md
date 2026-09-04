# Banking System — Spring Boot REST API, Containerized, Microservice-Backed

A Java banking management system, evolved in stages: from a terminal-based CLI simulator, to a containerized multi-service application, to a Spring Boot REST API ready for AWS ECS Fargate deployment. The system now runs as three coordinated Docker services — a Spring Boot API, a Postgres database, and a Python/FastAPI risk-analysis microservice — with no interactive terminal or local file dependency standing between it and a cloud deployment.

## Features

### REST API
- **Spring Boot 3.2 / Java 17** - Full HTTP API in front of the banking core: customers, accounts, deposits, withdrawals, transfers, transaction history
- **Bean Validation** - Request DTOs validated at the boundary (`@NotBlank`, `@Positive`, etc.) with clean 400 responses on bad input
- **Centralized exception handling** - `@RestControllerAdvice` maps domain exceptions to proper HTTP status codes (404 for not-found, 400 for validation failures) instead of leaking stack traces
- **Actuator health endpoint** - `/actuator/health` used by Docker Compose's healthcheck today, and by an ALB/ECS target group tomorrow

### Core Banking Operations
- **Multi-bank account management** - Support for mock BNY Mellon, Chase, and Capital One accounts
- **Financial transactions** - Deposits, withdrawals with real-time balance updates, persisted to Postgres on every operation
- **Transaction history** - Complete audit trail of all operations, queryable per account
- **Account limits** - Max 3 accounts per customer, one account per bank type per customer

### Database Integration
- **PostgreSQL persistent storage** - All account and transaction data lives in Postgres; every deposit/withdrawal writes through immediately (not batched or cached in memory)
- **Unique account identifiers** - Timestamp-based account numbers (e.g. `BNY1730147823456`)
- **Repository layer** - `DatabaseManager` provides a clean data-access API (`createCustomer`, `getAccount`, `updateBalance`, `logTransaction`, `transferBetweenAccounts`, etc.), reused by both the REST API and the legacy CLI

### Risk Analysis Microservice
- **Separate Python/FastAPI service** - Decoupled from the Java core, called over HTTP
- **Per-transaction risk scoring** - Flags large amounts, high recent activity, and withdrawal patterns
- **Independently deployable** - Own Dockerfile, own dependency set, communicates only through a defined JSON contract
- **Live in the withdrawal flow** - `POST /accounts/{accountNumber}/withdraw` calls the risk service synchronously and returns the risk verdict (`riskScore`, `flagged`, `reason`) in the response body

### Containerization & Cloud Readiness
- **Multi-stage Docker build** - Java app built in a Maven container, run in a slim JRE container
- **Docker Compose orchestration** - Postgres, the Spring Boot app, and the risk service start together with proper health-check-based startup ordering (db → risk_service → app)
- **Environment-variable configuration** - Both the Java app and the risk service read all connection details from env vars, with local defaults — no code changes needed to move from a laptop to ECS Fargate + RDS
- **Stateless service design** - No local file writes, no interactive terminal dependency — the API can be killed and restarted by ECS at any point without losing anything that matters (all state lives in Postgres)
- **AWS-ready** - Designed as the step directly ahead of an ECS Fargate deployment with ECR and RDS

### Technical Features
- **Custom data structures** - Linked list implementations for transaction management (retained in the domain layer)
- **Dynamic polymorphism** - `Bank` interface with multiple implementations (BNYMellon, Chase, CapitalOne)
- **Layered architecture** - `domain` (business objects) → `banking` (service layer, validation) → `web` (REST controllers, DTOs)
- **JDBC integration** - Professional database connectivity patterns

## Technical Stack

- **Language**: Java 17 (core app), Python 3.12 (risk microservice)
- **Framework**: Spring Boot 3.2.5 (Web, Validation, Actuator)
- **Database**: PostgreSQL 16 (containerized) / 14+ (local)
- **Build Tool**: Maven 3.9 (multi-stage Docker build, `spring-boot-maven-plugin`)
- **JDBC Driver**: PostgreSQL 42.6.2
- **Microservice Framework**: FastAPI + Uvicorn
- **Containerization**: Docker, Docker Compose
- **Data Structures**: Custom linked lists

## Architecture

```
Docker Compose Stack
│
├── app (Spring Boot, multi-stage build: Maven → JRE, port 8080)
│   ├── web/            REST controllers + DTOs
│   │   ├── AccountController      (create, deposit, withdraw, transfer, transactions, close)
│   │   ├── CustomerController     (create, view, list accounts)
│   │   └── GlobalExceptionHandler (domain exceptions -> HTTP status codes)
│   │
│   ├── banking/         Service layer - validation rules, orchestration
│   │   ├── AccountService   (business rules + risk_service integration)
│   │   └── CustomerService
│   │
│   ├── domain/           Business objects (no framework dependency)
│   │   ├── User, Bank interface
│   │   └── BNYMellon, Chase, CapitalOne implementations
│   │
│   ├── database/         Data access layer
│   │   ├── DatabaseConfig (env-var-driven connection management)
│   │   ├── DatabaseManager (SQL operations)
│   │   └── AccountDatabaseAdapter (ORM bridge)
│   │
│   └── service/           External HTTP client
│       └── RiskServiceClient (calls risk_service)
│
├── db (Postgres 16, healthchecked before app starts)
│   └── banking_schema.sql (auto-loaded on first boot)
│
└── risk_service (Python/FastAPI, independent container, port 8000)
    └── POST /analyze → { risk_score, flagged, reason }

legacy-cli/ (not part of the build - see legacy-cli/README.md)
└── Original terminal-based entry point, kept for reference
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
3. Start Postgres, wait for it to report healthy, then start the risk service, wait for *it* to report healthy, then start the Spring Boot app
4. Auto-load `banking_schema.sql` into Postgres on first boot
5. Expose the API on `http://localhost:8080` once its own `/actuator/health` check passes

The risk service is reachable at `http://localhost:8000` from your host (e.g. `curl http://localhost:8000/health`), and at `http://risk-service:8000` from inside the `app` container. See [REST API](#rest-api) below for the full endpoint list.

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
java -version    # Need Java 17+
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

# Run the REST API (Spring Boot dev mode - auto-restarts on code changes)
mvn spring-boot:run

# Or build the jar and run it directly
mvn clean package -DskipTests
java -jar target/bankProject-1.0.0.jar
```

The API starts on `http://localhost:8080` by default (override with the `SERVER_PORT` env var). See [REST API](#rest-api) below for the endpoint list.

## REST API

Base URL: `http://localhost:8080` (Docker Compose) or wherever `SERVER_PORT` points locally.

### Customers

**Create a customer**
```bash
curl -X POST localhost:8080/customers -H "Content-Type: application/json" \
  -d '{"name":"Jane Doe","email":"jane@example.com","phone":"555-0100"}'
```
```json
{ "customerId": 1, "name": "Jane Doe", "email": "jane@example.com", "phone": "555-0100" }
```

**Get a customer** — `GET /customers/{customerId}`

**List a customer's accounts** — `GET /customers/{customerId}/accounts`

### Accounts

**Create an account** (`bankType` is `BNY`, `CH`, or `CA`)
```bash
curl -X POST localhost:8080/accounts -H "Content-Type: application/json" \
  -d '{"customerId":1,"bankType":"BNY","initialDeposit":500}'
```
```json
{ "accountNumber": "BNY1730147823456", "bankName": "BNY Mellon", "accountType": "CHECKING", "balance": 500.0, "active": true }
```
Enforces: max 3 accounts per customer, one account per bank type per customer, non-negative initial deposit.

**Get an account** — `GET /accounts/{accountNumber}`

**Deposit**
```bash
curl -X POST localhost:8080/accounts/{accountNumber}/deposit \
  -H "Content-Type: application/json" -d '{"amount":100}'
```

**Withdraw** — returns the risk_service verdict alongside the updated account. The withdrawal still processes even if flagged or if risk_service is unreachable (mirrors the graceful degradation the original CLI had) — the caller decides what to do with the flag.
```bash
curl -X POST localhost:8080/accounts/{accountNumber}/withdraw \
  -H "Content-Type: application/json" -d '{"amount":50}'
```
```json
{
  "account": { "accountNumber": "BNY1730147823456", "bankName": "BNY Mellon", "accountType": "CHECKING", "balance": 550.0, "active": true },
  "riskScore": 0.1, "flagged": false, "riskReason": "no unusual activity", "riskServiceAvailable": true
}
```

**Transfer between accounts**
```bash
curl -X POST localhost:8080/accounts/transfer -H "Content-Type: application/json" \
  -d '{"fromAccount":"BNY...","toAccount":"CHS...","amount":50}'
```

**Transaction history** — `GET /accounts/{accountNumber}/transactions?limit=20`

**Close an account** — `DELETE /accounts/{accountNumber}`

### Errors

Validation failures and not-found lookups return a clean JSON body instead of a stack trace:
```json
{ "error": "No active account found with number: BNY123" }
```
- `404` — account or customer not found
- `400` — validation failure (negative amount, insufficient funds, account limit reached, duplicate email, etc.)
- `500` — unexpected server error

## Database Features

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
- **Layered architecture** - domain (business objects) → banking (service layer) → web (REST controllers/DTOs)
- **Adapter pattern** - AccountDatabaseAdapter bridges domain objects with database
- **Repository pattern** - DatabaseManager encapsulates data access
- **DTO pattern** - Request/response records in `web/dto` decouple the HTTP contract from domain objects
- **Centralized exception translation** - `@RestControllerAdvice` maps business exceptions to HTTP status codes in one place

## Current Limitations

- **Maximum 3 bank accounts** - Architectural constraint
- **Account counter resets** - Uses timestamps to avoid conflicts
- **No web UI** - API only, no frontend
- **No authentication** - No login/password system, no API auth (anyone who can reach the API can call it)
- **Plaintext local credentials** - `compose.yaml` uses hardcoded Postgres credentials suitable only for local development, not production
- **No CI/CD yet** - Images are built and run manually; GitHub Actions automation is planned (see Roadmap)

## Future Enhancements

### AWS Deployment (in progress)
The REST API refactor above was the prerequisite for this - Fargate has no attached terminal and no persistent local disk, so the app needed to be a real stateless HTTP service before it could deploy there. Next:
- **ECR** - Push the Java app and risk_service images to private ECR repositories
- **RDS** - Replace the containerized Postgres with a managed RDS instance (schema and env-var-driven config are already compatible — no app code changes required)
- **ECS Fargate** - Run both services as Fargate tasks behind an ALB, using the same environment-variable contract used today and the existing `/actuator/health` endpoint for target group health checks
- **GitHub Actions CI/CD** - Build, tag, and push images on merge, using OIDC federation for AWS auth (no long-lived credentials)

### Feature Roadmap
The database schema supports these planned features:

- **User authentication** - Login system with username/password
- **Multiple account types** - SAVINGS and CREDIT card accounts
- **Interest calculations** - Automatic interest on savings accounts
- **Credit limits** - Overdraft protection and credit card limits
- **Multi-user support** - Separate sessions for different users
- **Transaction categories** - Tag transactions (bills, groceries, etc.)
- **Reporting** - Monthly statements and spending analytics
- **Real risk model** - Replace the rule-based risk_service scoring with a trained model or a third-party fraud-detection API

## Learning Outcomes

This project demonstrates:

- **Object-oriented design** - Interfaces, inheritance, polymorphism
- **Data structures** - Custom linked list implementations
- **Database integration** - JDBC, SQL, schema design
- **REST API design** - Spring Boot, Bean Validation, centralized exception handling
- **Error handling** - Input validation, exception management, proper HTTP status codes
- **Build automation** - Maven dependency management
- **Software architecture** - Layered design, separation of concerns
- **Legacy migration** - Refactoring an interactive CLI into a stateless, cloud-deployable REST service

## Files Overview

```
src/main/java/
├── app/
│   └── BankingApplication.java       # Spring Boot entry point
├── web/
│   ├── AccountController.java        # REST endpoints: accounts, deposit, withdraw, transfer
│   ├── CustomerController.java       # REST endpoints: customers
│   ├── GlobalExceptionHandler.java   # Maps business exceptions to HTTP status codes
│   └── dto/                          # Request/response records
├── banking/
│   ├── AccountService.java           # Business logic, validation, risk_service integration
│   ├── CustomerService.java
│   └── *Exception.java               # AccountNotFoundException, CustomerNotFoundException, ValidationException
├── domain/
│   ├── User.java                     # User domain model
│   ├── Bank.java                     # Bank interface
│   ├── BNYMellon.java                # BNY Mellon bank implementation
│   ├── Chase.java                    # Chase bank implementation
│   └── CapitalOne.java               # Capital One bank implementation
├── database/
│   ├── DatabaseConfig.java           # Connection management (env-var-driven)
│   ├── DatabaseManager.java          # SQL operations
│   ├── AccountDatabaseAdapter.java   # Object-relational mapping
│   └── DatabaseTest.java             # Integration tests
└── service/
    ├── RiskServiceClient.java        # HTTP client for risk_service (env-var-driven)
    └── RiskAnalysis.java             # Risk response domain model

src/main/resources/
└── application.properties            # Server port, actuator config

legacy-cli/                           # Original terminal entry point - not part of the build, see legacy-cli/README.md
├── src/Interface.java
└── src/RUNME.java

risk_service/
├── app.py                  # FastAPI app (/health, /analyze)
├── requirements.txt        # fastapi, uvicorn[standard]
└── Dockerfile              # python:3.12-slim image

Dockerfile                  # Multi-stage build for the Java app
compose.yaml                # Orchestrates app + db + risk_service
.dockerignore                # Excludes target/, .git/, legacy-cli/, docs from build context
banking_schema.sql          # Database setup script (auto-loaded by Postgres container)
pom.xml                     # Maven configuration (Spring Boot 3.2.5, Java 17)
README.md                   # This file
```

## Testing

**Manual Testing (Docker):**
```bash
docker compose up --build

# In another terminal, once all three containers report healthy:
curl -X POST localhost:8080/customers -H "Content-Type: application/json" \
  -d '{"name":"Jane Doe","email":"jane@example.com"}'
# note the customerId from the response

curl -X POST localhost:8080/accounts -H "Content-Type: application/json" \
  -d '{"customerId":1,"bankType":"BNY","initialDeposit":500}'
# note the accountNumber from the response

curl -X POST localhost:8080/accounts/{accountNumber}/deposit \
  -H "Content-Type: application/json" -d '{"amount":100}'   # -> balance 600

curl -X POST localhost:8080/accounts/{accountNumber}/withdraw \
  -H "Content-Type: application/json" -d '{"amount":50}'    # -> balance 550, includes risk verdict

curl localhost:8080/accounts/{accountNumber}/transactions   # -> both transactions, newest first
```

**Manual Testing (local, no Docker):**
```bash
mvn spring-boot:run
# same curl workflow as above
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
# The exec-maven-plugin isn't part of the Spring Boot pom, so run
# DatabaseTest via your IDE, or on the classpath directly:
mvn clean compile
java -cp target/classes:$(find ~/.m2 -name 'postgresql-*.jar' | head -1) database.DatabaseTest
```

## Contributing

This is an educational project. The code demonstrates:

- Clean architecture principles
- Database integration patterns
- Legacy system migration (interactive CLI → stateless REST API)
- Extensible design for future features

## License

Educational project - free to use for learning purposes.

## Author

**Thomas Bleckman**

GitHub: [@Tbleckman](https://github.com/Tbleckman)

---

## Notes

- The original file-based session I/O (`bankProject.txt`) only exists in `legacy-cli/` now - the REST API is entirely database-backed, with no local file dependency, since Fargate containers don't have persistent local disk
- Account numbers use timestamps to ensure global uniqueness
- Schema supports multi-user architecture for future expansion
- See `legacy-cli/README.md` for why the CLI was split out and how to run it standalone if you want to see where this project started
