package code.filipesz.springdeliveryengine.controllers;

import code.filipesz.springdeliveryengine.dto.ClientRateRequest;
import code.filipesz.springdeliveryengine.dto.CourierLocationResponse;
import code.filipesz.springdeliveryengine.dto.OrderRequest;
import code.filipesz.springdeliveryengine.entities.Order;
import code.filipesz.springdeliveryengine.services.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<List<Order>> orderList() {
        return ResponseEntity.ok(orderService.orderList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable UUID id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @PostMapping
    public ResponseEntity<Order> placeOrder(@Valid @RequestBody OrderRequest request) {
        return new ResponseEntity<>(orderService.placeOrder(request), HttpStatus.CREATED);
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<Order> payOrder(@PathVariable UUID id) {
        return ResponseEntity.ok(orderService.payOrder(id));
    }

    @PostMapping("/{id}/paybycash")
    public ResponseEntity<Order> payOrderByCash(@PathVariable UUID id) {
        return ResponseEntity.ok(orderService.payOrderByCash(id));
    }

    @PutMapping("/{id}/assign-courier")
    public ResponseEntity<Order> assignCourierToOrder(
            @PathVariable UUID id,
            @RequestParam UUID courierId) {
        return ResponseEntity.ok(orderService.assignCourierToOrder(id, courierId));
    }

    @GetMapping("/{id}/track")
    public ResponseEntity<CourierLocationResponse> trackOrder(@PathVariable UUID id) {
        return ResponseEntity.ok(orderService.trackOrder(id));
    }

    @PostMapping("/{id}/on-the-way")
    public ResponseEntity<Order> orderOnTheWay(@PathVariable UUID id) {
        return ResponseEntity.ok(orderService.orderOnTheWay(id));
    }

    @PostMapping("/{id}/delivered")
    public ResponseEntity<Order> orderDelivered(@PathVariable UUID id) {
        return ResponseEntity.ok(orderService.orderDelivered(id));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Order> cancelOrder(@PathVariable UUID id) {
        return ResponseEntity.ok(orderService.orderCancelled(id));
    }

    @PostMapping("/{id}/rate")
    public ResponseEntity<Order> rateOrder(
            @PathVariable UUID id,
            @Valid @RequestBody ClientRateRequest request) {
        return ResponseEntity.ok(orderService.setOrderRate(id, request));
    }
}