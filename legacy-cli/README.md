# Legacy CLI

This is the original terminal-based entry point for the banking system
(`Interface.java` + `RUNME.java`). It's kept here, out of the Maven build,
as a record of how the project evolved: it started as an interactive
`Scanner`-driven CLI, and was refactored into the Spring Boot REST API
that now lives in `src/main/java`.

**This code is not part of the deployed application.** The `AccountService`
and `CustomerService` classes in `src/main/java/banking/` are where the
business logic and validation rules that used to live inline in
`Interface.java`'s prompt loops now live - ported to be callable from HTTP
requests instead of a terminal session.

## Why it isn't in the build

- It reads from `System.in` and writes session state to a local
  `bankProject.txt` file - both of which are fundamentally incompatible with
  running as an ECS Fargate service (no attached terminal, no persistent
  local disk across task restarts).
- The domain classes it depends on (`User`, `Bank`, `BNYMellon`, `Chase`,
  `CapitalOne`) moved from the default package into `domain.*` as part of
  the refactor, so this code no longer compiles by just dropping it back
  into `src/main/java` without adjustment.

## Running it anyway (for reference only)

If you want to see the original CLI in action, compile it manually
against the current domain/database/service classes:

```bash
javac -d /tmp/legacy-build \
  -cp "$(find ~/.m2 -name 'postgresql-*.jar' | head -1)" \
  src/main/java/domain/*.java \
  src/main/java/database/*.java \
  src/main/java/service/*.java \
  legacy-cli/src/*.java

java -cp "/tmp/legacy-build:$(find ~/.m2 -name 'postgresql-*.jar' | head -1)" RUNME
```

You'll need a local Postgres instance reachable at `localhost:5432` (or the
usual `DB_HOST`/`DB_PORT`/etc environment variables set) since this path
doesn't go through Docker Compose.
