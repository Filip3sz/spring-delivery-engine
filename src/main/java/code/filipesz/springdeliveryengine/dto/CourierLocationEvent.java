package code.filipesz.springdeliveryengine.dto;

import java.util.UUID;

public record CourierLocationEvent(
        UUID courierId,
        double latitude,
        double longitude
) {
    public static CourierLocationEvent from(UUID courierId, double latitude, double longitude) {
        return new CourierLocationEvent(courierId, latitude, longitude);
    }
}