package code.filipesz.springdeliveryengine.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ClientRequest(
        @NotBlank(message = "Email jest wymagany.")
        @Email(message = "Niepoprawny format email.")
        String email,

        @NotBlank(message = "Telefon jest wymagany.")
        String phoneNumber,

        @NotBlank(message = "Adres jest wymagany.")
        String address,

        @NotBlank(message = "Imię jest wymagane.")
        String firstName,

        @NotBlank(message = "Nazwisko jest wymagane.")
        String lastName
) {
}
