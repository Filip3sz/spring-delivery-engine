package code.filipesz.springdeliveryengine.config;

import code.filipesz.springdeliveryengine.entities.Courier;
import code.filipesz.springdeliveryengine.entities.CourierStatus;
import code.filipesz.springdeliveryengine.entities.MenuItem;
import code.filipesz.springdeliveryengine.repositories.CourierRepository;
import code.filipesz.springdeliveryengine.repositories.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final CourierRepository courierRepository;
    private final MenuItemRepository menuItemRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        Courier courier1 = Courier.builder()
                .firstName("Jan")
                .lastName("Kowalski")
                .email("jan.kowalski@example.com")
                .phoneNumber("+48100200300")
                .status(CourierStatus.AVAILABLE)
                .balance(BigDecimal.ZERO)
                .build();

        Courier courier2 = Courier.builder()
                .firstName("Piotr")
                .lastName("Nowak")
                .email("piotr.nowak@example.com")
                .phoneNumber("+48200300400")
                .status(CourierStatus.OFFLINE)
                .balance(BigDecimal.ZERO)
                .build();

        courierRepository.saveAll(List.of(courier1, courier2));

        MenuItem menuItem1 = MenuItem.builder()
                .name("Pizza")
                .price(new BigDecimal("36.99"))
                .build();

        MenuItem menuItem2 = MenuItem.builder()
                .name("Burger")
                .price(new BigDecimal("29.99"))
                .build();

        MenuItem menuItem3 = MenuItem.builder()
                .name("Frytki")
                .price(new BigDecimal("9.99"))
                .build();

        menuItemRepository.saveAll(List.of(menuItem1, menuItem2, menuItem3));
    }
}