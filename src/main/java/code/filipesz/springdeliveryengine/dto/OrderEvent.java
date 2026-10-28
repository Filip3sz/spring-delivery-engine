package code.filipesz.springdeliveryengine.dto;

import code.filipesz.springdeliveryengine.entities.DeliveryStatus;
import code.filipesz.springdeliveryengine.entities.Order;
import code.filipesz.springdeliveryengine.entities.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record OrderEvent(
        UUID id,
        String clientEmail,
        UUID courierId,
        OrderStatus status,
        DeliveryStatus deliveryStatus,
        BigDecimal totalAmount,
        Integer discount,
        String notes,
        LocalDateTime createdAt,
        Integer rate,
        String eventType
) {
    public static OrderEvent from(Order order, String eventType) {
        return new OrderEvent(
                order.getId(),
                order.getClient() != null ? order.getClient().getEmail() : null,
                order.getCourier() != null ? order.getCourier().getId() : null,
                order.getStatus(),
                order.getDeliveryStatus(),
                order.getTotalAmount(),
                order.getDiscount(),
                order.getNotes(),
                order.getCreatedAt(),
                order.getRate(),
                eventType
        );
    }
}