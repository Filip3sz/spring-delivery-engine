package code.filipesz.springdeliveryengine.controllers;

import code.filipesz.springdeliveryengine.dto.CourierLocationRequest;
import code.filipesz.springdeliveryengine.dto.CourierLocationResponse;
import code.filipesz.springdeliveryengine.entities.Courier;
import code.filipesz.springdeliveryengine.entities.CourierStatus;
import code.filipesz.springdeliveryengine.services.CourierLocationService;
import code.filipesz.springdeliveryengine.services.CourierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/db/couriers")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CourierController {

    private final CourierService courierService;
    private final CourierLocationService courierLocationService;

    @GetMapping
    public ResponseEntity<List<Courier>> courierList() {
        return ResponseEntity.ok(courierService.courierList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Courier> getCourierById(@PathVariable UUID id) {
        return ResponseEntity.ok(courierService.getCourierById(id));
    }

    @PostMapping
    public ResponseEntity<Courier> createCourier(@Valid @RequestBody Courier courier) {
        return new ResponseEntity<>(courierService.createCourier(courier), HttpStatus.CREATED);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Courier> changeStatus(
            @PathVariable UUID id,
            @RequestParam CourierStatus status) {
        return ResponseEntity.ok(courierService.changeStatus(id, status));
    }

    @GetMapping("/{id}/location")
    public ResponseEntity<CourierLocationResponse> getCourierLocation(@PathVariable UUID id) {
        return ResponseEntity.ok(courierLocationService.getCourierLocation(id));
    }

    @PostMapping("/location")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void updateLocation(
            @AuthenticationPrincipal UUID courierId,
            @Valid @RequestBody CourierLocationRequest request) {
        courierLocationService.updateCourierLocation(courierId, request);
    }

    @PostMapping("/{id}/regulate")
    public ResponseEntity<Courier> regulateBalance(
            @PathVariable UUID id,
            @RequestParam BigDecimal amount) {
        return ResponseEntity.ok(courierService.regulateBalance(id, amount));
    }
}