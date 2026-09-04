package web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record CreateAccountRequest(
        @NotNull(message = "customerId is required") Integer customerId,
        @NotBlank(message = "bankType is required (BNY, CH, or CA)") String bankType,
        @NotNull @PositiveOrZero(message = "initialDeposit cannot be negative") Double initialDeposit
) {}
