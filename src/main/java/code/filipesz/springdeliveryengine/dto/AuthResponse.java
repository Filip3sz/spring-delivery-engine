package code.filipesz.springdeliveryengine.dto;

import code.filipesz.springdeliveryengine.entities.Courier;

public record AuthResponse(
        Courier courier,
        String token
) {
}
