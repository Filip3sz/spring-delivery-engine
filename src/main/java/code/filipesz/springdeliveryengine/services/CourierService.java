package code.filipesz.springdeliveryengine.services;

import code.filipesz.springdeliveryengine.entities.Courier;
import code.filipesz.springdeliveryengine.entities.CourierStatus;
import code.filipesz.springdeliveryengine.repositories.CourierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourierService {

    private final CourierRepository courierRepository;

    public List<Courier> courierList() {
        return courierRepository.findAll();
    }

    public Courier getCourierById(UUID id) {
        return courierRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono kuriera o podanym ID."));
    }

    @Transactional
    public Courier createCourier(Courier courier) {
        if (courier.getEmail() == null || courier.getEmail().isBlank()) {
            throw new IllegalArgumentException("Adres email jest wymagany.");
        }

        String cleanEmail = courier.getEmail().trim().toLowerCase();

        if (courierRepository.findByEmail(cleanEmail).isPresent()) {
            throw new IllegalArgumentException("Kurier o podanym adresie email już istnieje.");
        }

        courier.setEmail(cleanEmail);
        courier.setStatus(CourierStatus.OFFLINE);

        return courierRepository.save(courier);
    }

    @Transactional
    public Courier changeStatus(UUID id, CourierStatus newStatus) {
        Courier courier = getCourierById(id);
        courier.setStatus(newStatus);

        return courier;
    }

    @Transactional
    public Courier regulateBalance(UUID id, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Kwota rozliczenia musi być większa od zera.");
        }

        Courier courier = getCourierById(id);
        BigDecimal currentBalance = courier.getBalance() != null ? courier.getBalance() : BigDecimal.ZERO;

        if (amount.compareTo(currentBalance) > 0) {
            throw new IllegalStateException("Podana kwota jest większa niż aktualny balans kuriera (" + currentBalance + " PLN).");
        }

        BigDecimal newBalance = currentBalance.subtract(amount);
        courier.setBalance(newBalance);

        return courier;
    }
}