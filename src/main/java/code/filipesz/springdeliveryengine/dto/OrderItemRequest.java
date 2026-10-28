package code.filipesz.springdeliveryengine.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderItemRequest(
        @NotNull(message = "ID produktu jest wymagane.")
        Long productId,

        @NotNull(message = "Ilość jest wymagana.")
        @Min(value = 1, message = "Ilość musi wynosić co najmniej 1.")
        Integer quantity,

        String notes
) {
}
