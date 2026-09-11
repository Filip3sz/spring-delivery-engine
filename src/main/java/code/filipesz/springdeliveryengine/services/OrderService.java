package code.filipesz.springdeliveryengine.services;

import code.filipesz.springdeliveryengine.config.KafkaTopicConfig;
import code.filipesz.springdeliveryengine.dto.ClientRateRequest;
import code.filipesz.springdeliveryengine.dto.CourierLocationResponse;
import code.filipesz.springdeliveryengine.dto.OrderEvent;
import code.filipesz.springdeliveryengine.dto.OrderItemRequest;
import code.filipesz.springdeliveryengine.dto.OrderRequest;
import code.filipesz.springdeliveryengine.entities.*;
import code.filipesz.springdeliveryengine.repositories.CodeRepository;
import code.filipesz.springdeliveryengine.repositories.CourierRepository;
import code.filipesz.springdeliveryengine.repositories.MenuItemRepository;
import code.filipesz.springdeliveryengine.repositories.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final MenuItemRepository menuItemRepository;
    private final OrderRepository orderRepository;
    private final CourierRepository courierRepository;
    private final CourierLocationService courierLocationService;
    private final CodeRepository codeRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public List<Order> orderList() {
        return orderRepository.findAll();
    }

    public Order getOrderById(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono zamówienia o takim id."));
    }

    @Transactional
    public Order placeOrder(OrderRequest request) {
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal basePriceTotal = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : request.items()) {
            MenuItem menuItem = menuItemRepository.findById(itemRequest.productId())
                    .orElseThrow(() -> new IllegalArgumentException("Produkt o ID " + itemRequest.productId() + " nie istnieje."));

            BigDecimal itemTotal = menuItem.getPrice().multiply(BigDecimal.valueOf(itemRequest.quantity()));
            basePriceTotal = basePriceTotal.add(itemTotal);

            OrderItem orderItem = OrderItem.builder()
                    .menuItem(menuItem)
                    .quantity(itemRequest.quantity())
                    .notes(itemRequest.notes())
                    .price(menuItem.getPrice())
                    .build();

            orderItems.add(orderItem);
        }

        BigDecimal finalAmount = basePriceTotal;
        Integer appliedDiscount = 0;

        if (request.code() != null && !request.code().isBlank()) {
            String cleanCode = request.code().trim().toUpperCase();

            Code promoCode = codeRepository.findById(cleanCode)
                    .orElseThrow(() -> new IllegalArgumentException("Podany kod rabatowy nie istnieje lub został wykorzystany."));

            appliedDiscount = promoCode.getDiscount();
            BigDecimal percentageLeft = BigDecimal.valueOf(100 - appliedDiscount);

            finalAmount = basePriceTotal.multiply(percentageLeft)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

            if (promoCode.getUsesCount() <= 1) {
                codeRepository.delete(promoCode);
            } else {
                promoCode.setUsesCount(promoCode.getUsesCount() - 1);
                codeRepository.save(promoCode);
            }
        }

        Client client = Client.builder()
                .email(request.client().email())
                .phoneNumber(request.client().phoneNumber())
                .address(request.client().address())
                .firstName(request.client().firstName())
                .lastName(request.client().lastName())
                .build();

        Order order = Order.builder()
                .client(client)
                .items(orderItems)
                .status(OrderStatus.PENDING)
                .discount(appliedDiscount)
                .totalAmount(finalAmount)
                .notes(request.notes())
                .createdAt(LocalDateTime.now())
                .build();

        Order savedOrder = orderRepository.save(order);

        sendOrderEvent(savedOrder, "ORDER_PLACED");
        return savedOrder;
    }

    @Transactional
    public Order payOrder(UUID orderId) {
        Order order = getOrderById(orderId);

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Zamówienie nie jest w stanie PENDING.");
        }

        order.setStatus(OrderStatus.PAID);
        order.setDeliveryStatus(DeliveryStatus.PREPARING);

        sendOrderEvent(order, "ORDER_PAID");
        return order;
    }

    @Transactional
    public Order payOrderByCash(UUID orderId) {
        Order order = getOrderById(orderId);

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Zamówienie nie jest w stanie PENDING.");
        }

        order.setStatus(OrderStatus.PAY_BY_CASH);
        order.setDeliveryStatus(DeliveryStatus.PREPARING);

        sendOrderEvent(order, "ORDER_PAID_BY_CASH");
        return order;
    }

    @Transactional
    public Order assignCourierToOrder(UUID orderId, UUID courierId) {
        Order order = getOrderById(orderId);

        Courier courier = courierRepository.findById(courierId)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono kuriera o takim id."));

        if (courier.getStatus() != CourierStatus.AVAILABLE) {
            throw new IllegalStateException("Kurier nie jest dostępny (jest offline lub wiezie inne zamówienie).");
        }

        order.setCourier(courier);
        courier.setStatus(CourierStatus.BUSY);

        return order;
    }

    public CourierLocationResponse trackOrder(UUID orderId) {
        Order order = getOrderById(orderId);

        if (order.getCourier() == null) {
            throw new IllegalStateException("Do tego zamówienia nie przypisano jeszcze kuriera.");
        }

        if (order.getDeliveryStatus() != DeliveryStatus.ON_THE_WAY) {
            throw new IllegalStateException("Zamówienie nie wyjechało jeszcze z lokalu (aktualny status: " + order.getDeliveryStatus() + ").");
        }

        return courierLocationService.getCourierLocation(order.getCourier().getId());
    }

    @Transactional
    public Order orderOnTheWay(UUID orderId) {
        Order order = getOrderById(orderId);

        if ((order.getStatus() != OrderStatus.PAID && order.getStatus() != OrderStatus.PAY_BY_CASH)
                || order.getDeliveryStatus() != DeliveryStatus.PREPARING) {
            throw new IllegalStateException("Zamówienie musi być opłacone/gotówkowe oraz w stanie PREPARING.");
        }

        order.setDeliveryStatus(DeliveryStatus.ON_THE_WAY);

        sendOrderEvent(order, "ORDER_ON_THE_WAY");
        return order;
    }

    @Transactional
    public Order orderDelivered(UUID orderId) {
        Order order = getOrderById(orderId);

        if (order.getDeliveryStatus() != DeliveryStatus.ON_THE_WAY) {
            throw new IllegalStateException("Nie można oznaczyć jako dostarczone zamówienia, które nie jest w drodze.");
        }

        if (order.getStatus() == OrderStatus.PAY_BY_CASH) {
            order.setStatus(OrderStatus.PAID);
            Courier courier = order.getCourier();

            if (courier != null) {
                BigDecimal currentBalance = courier.getBalance() != null ? courier.getBalance() : BigDecimal.ZERO;
                BigDecimal newBalance = currentBalance.add(order.getTotalAmount());
                courier.setBalance(newBalance);
            }
        }

        order.setDeliveryStatus(DeliveryStatus.DELIVERED);

        if (order.getCourier() != null) {
            order.getCourier().setStatus(CourierStatus.AVAILABLE);
        }

        sendOrderEvent(order, "ORDER_DELIVERED");
        return order;
    }

    @Transactional
    public Order orderCancelled(UUID orderId) {
        Order order = getOrderById(orderId);

        if (order.getStatus() != OrderStatus.PAID && order.getStatus() != OrderStatus.PAY_BY_CASH) {
            throw new IllegalStateException("Nie można anulować zamówienia, gdy nie ma wybranej formy płatności przez klienta.");
        }

        if (order.getDeliveryStatus() == DeliveryStatus.DELIVERED) {
            throw new IllegalStateException("Nie możesz anulować dostarczonego zamówienia.");
        }

        order.setDeliveryStatus(DeliveryStatus.CANCELLED);

        if (order.getCourier() != null) {
            order.getCourier().setStatus(CourierStatus.AVAILABLE);
        }

        sendOrderEvent(order, "ORDER_CANCELLED");
        return order;
    }

    @Transactional
    public Order setOrderRate(UUID orderId, ClientRateRequest request) {
        Order order = getOrderById(orderId);

        if (order.getDeliveryStatus() != DeliveryStatus.DELIVERED) {
            throw new IllegalStateException("Nie można ocenić zamówienia, gdy nie zostało ono dostarczone.");
        }

        if (order.getRate() != null) {
            throw new IllegalStateException("Oceniłeś już to zamówienie.");
        }

        order.setRate(request.rate());

        return order;
    }

    private void sendOrderEvent(Order order, String eventType) {
        OrderEvent event = OrderEvent.from(order, eventType);
        kafkaTemplate.send(KafkaTopicConfig.ORDER_EVENTS_TOPIC, order.getId().toString(), event);
    }
}