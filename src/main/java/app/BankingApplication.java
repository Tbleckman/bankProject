package app;

import database.DatabaseConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the banking REST API.
 *
 * Domain, database, service (risk client), and web classes live in flat
 * sibling packages rather than nested under app.*, matching the project's
 * existing package layout - so scanBasePackages lists them explicitly
 * instead of relying on the default "scan everything under my own package"
 * behavior.
 */
@SpringBootApplication(scanBasePackages = {"app", "web", "banking", "database", "service"})
public class BankingApplication {

    public static void main(String[] args) {
        System.out.println("Banking System (REST API) starting...");

        if (!DatabaseConfig.testConnection()) {
            System.out.println("WARNING: database connection failed at startup.");
            System.out.println("The API will still start, but requests touching the database will fail.");
        }

        SpringApplication.run(BankingApplication.class, args);
    }
}
