package code.filipesz.springdeliveryengine.dto;

import jakarta.validation.constraints.NotNull;

public record CourierLocationRequest(
        @NotNull(message = "Szerokość geograficzna (latitude) jest wymagana.")
        Double latitude,

        @NotNull(message = "Długość geograficzna (longitude) jest wymagana.")
        Double longitude,

        long timestamp
) {
}
