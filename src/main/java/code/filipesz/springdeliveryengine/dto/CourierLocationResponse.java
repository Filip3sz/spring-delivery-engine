package code.filipesz.springdeliveryengine.dto;

import java.util.UUID;

public record CourierLocationResponse(
        UUID courierId,
        double latitude,
        double longitude
) {
}
