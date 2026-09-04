package banking;

import database.DatabaseManager;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;

/**
 * Business layer for customer operations. Thin wrapper over DatabaseManager
 * that adds validation and turns checked SQLException into unchecked
 * exceptions the web layer can translate into HTTP responses.
 */
@Service
public class CustomerService {

    private final DatabaseManager db = new DatabaseManager();

    public DatabaseManager.CustomerData createCustomer(String name, String email, String phone) {
        if (name == null || name.isBlank()) {
            throw new ValidationException("Customer name is required");
        }
        try {
            int customerId = db.createCustomer(name.trim(), email, phone);
            return db.getCustomer(customerId);
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                // Postgres unique_violation - most likely the email column
                throw new ValidationException("A customer with this email already exists");
            }
            throw new RuntimeException("Failed to create customer: " + e.getMessage(), e);
        }
    }

    public DatabaseManager.CustomerData getCustomer(int customerId) {
        try {
            DatabaseManager.CustomerData customer = db.getCustomer(customerId);
            if (customer == null) {
                throw new CustomerNotFoundException(customerId);
            }
            return customer;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load customer: " + e.getMessage(), e);
        }
    }

    public List<DatabaseManager.AccountData> getCustomerAccounts(int customerId) {
        // Confirms the customer exists before listing accounts (throws 404 otherwise)
        getCustomer(customerId);
        try {
            return db.getCustomerAccounts(customerId);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load accounts: " + e.getMessage(), e);
        }
    }
}
