package web;

import banking.CustomerService;
import database.DatabaseManager;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import web.dto.AccountResponse;
import web.dto.CreateCustomerRequest;
import web.dto.CustomerResponse;

import java.util.List;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerResponse createCustomer(@Valid @RequestBody CreateCustomerRequest request) {
        DatabaseManager.CustomerData customer = customerService.createCustomer(
                request.name(), request.email(), request.phone());
        return CustomerResponse.from(customer);
    }

    @GetMapping("/{customerId}")
    public CustomerResponse getCustomer(@PathVariable int customerId) {
        return CustomerResponse.from(customerService.getCustomer(customerId));
    }

    @GetMapping("/{customerId}/accounts")
    public List<AccountResponse> getCustomerAccounts(@PathVariable int customerId) {
        return customerService.getCustomerAccounts(customerId).stream()
                .map(AccountResponse::from)
                .toList();
    }
}
