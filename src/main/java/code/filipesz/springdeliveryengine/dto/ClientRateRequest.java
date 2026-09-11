package code.filipesz.springdeliveryengine.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ClientRateRequest(
        @NotNull(message = "Ocena nie może być pusta.")
        @Min(value = 1, message = "Minimalna ocena to 1.")
        @Max(value = 5, message = "Maksymalna ocena to 5.")
        Integer rate

        // String review
) {
}
