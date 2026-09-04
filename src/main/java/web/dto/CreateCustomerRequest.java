package web.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateCustomerRequest(
        @NotBlank(message = "name is required") String name,
        String email,
        String phone
) {}
