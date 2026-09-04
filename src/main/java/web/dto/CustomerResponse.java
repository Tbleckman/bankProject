package web.dto;

import database.DatabaseManager.CustomerData;

public record CustomerResponse(int customerId, String name, String email, String phone) {

    public static CustomerResponse from(CustomerData c) {
        return new CustomerResponse(c.customerId, c.name, c.email, c.phone);
    }
}
