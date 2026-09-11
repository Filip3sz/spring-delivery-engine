package code.filipesz.springdeliveryengine.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record MenuItemRequest(
        @NotBlank(message = "Nazwa nie może być pusta.")
        String name,

        @Positive @NotNull(message = "Cena nie może być pusta oraz <= 0.")
        BigDecimal price
) {
}
