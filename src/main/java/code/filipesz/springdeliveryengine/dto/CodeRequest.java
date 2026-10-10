package code.filipesz.springdeliveryengine.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CodeRequest(
        String code,

        @NotNull(message = "Zniżka nie może być pusta.")
        @Min(value = 1, message = "Minimalna zniżka to 1%.")
        @Max(value = 100, message = "Maksymalna zniżka to 100%.")
        Integer discount,

        @NotNull(message = "Ilość użyć nie może być pusta.")
        @Min(value = 1, message = "Minimalna ilość użyć to 1.")
        Integer usesCount
) {
}