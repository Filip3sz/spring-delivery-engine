package code.filipesz.springdeliveryengine.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record OrderRequest(
        @NotEmpty(message = "Koszyk nie może być pusty.")
        @Valid
        List<OrderItemRequest> items,

        String code,

        String notes,

        @NotNull(message = "Dane klienta są wymagane.")
        @Valid
        ClientRequest client
) {
}
