package web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record TransferRequest(
        @NotBlank(message = "fromAccount is required") String fromAccount,
        @NotBlank(message = "toAccount is required") String toAccount,
        @NotNull @Positive(message = "amount must be positive") Double amount
) {}
