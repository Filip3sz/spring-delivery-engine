package code.filipesz.springdeliveryengine.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OtpVerificationRequest(
        @NotBlank(message = "Email nie może być pusty.")
        @Email(message = "Niepoprawny format adresu email.")
        String email,

        @NotBlank(message = "Kod OTP nie może być pusty.")
        @Size(min = 6, max = 6, message = "Kod OTP musi mieć dokładnie 6 znaków.")
        String code
) {
}