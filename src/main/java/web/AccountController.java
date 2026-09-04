package web;

import banking.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import web.dto.*;

import java.util.List;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse createAccount(@Valid @RequestBody CreateAccountRequest request) {
        return AccountResponse.from(accountService.createAccount(
                request.customerId(), request.bankType(), request.initialDeposit()));
    }

    @GetMapping("/{accountNumber}")
    public AccountResponse getAccount(@PathVariable String accountNumber) {
        return AccountResponse.from(accountService.getAccount(accountNumber));
    }

    @PostMapping("/{accountNumber}/deposit")
    public AccountResponse deposit(@PathVariable String accountNumber, @Valid @RequestBody AmountRequest request) {
        return AccountResponse.from(accountService.deposit(accountNumber, request.amount()));
    }

    @PostMapping("/{accountNumber}/withdraw")
    public WithdrawalResponse withdraw(@PathVariable String accountNumber, @Valid @RequestBody AmountRequest request) {
        return WithdrawalResponse.from(accountService.withdraw(accountNumber, request.amount()));
    }

    @PostMapping("/transfer")
    public void transfer(@Valid @RequestBody TransferRequest request) {
        accountService.transfer(request.fromAccount(), request.toAccount(), request.amount());
    }

    @GetMapping("/{accountNumber}/transactions")
    public List<TransactionResponse> getTransactions(
            @PathVariable String accountNumber,
            @RequestParam(defaultValue = "20") int limit) {
        return accountService.getTransactionHistory(accountNumber, limit).stream()
                .map(TransactionResponse::from)
                .toList();
    }

    @DeleteMapping("/{accountNumber}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void closeAccount(@PathVariable String accountNumber) {
        accountService.closeAccount(accountNumber);
    }
}
