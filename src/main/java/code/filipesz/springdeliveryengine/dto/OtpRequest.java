package code.filipesz.springdeliveryengine.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record OtpRequest(
        @NotBlank(message = "Email nie może być pusty.")
        @Email(message = "Niepoprawny format adresu email.")
        String email
) {
}
