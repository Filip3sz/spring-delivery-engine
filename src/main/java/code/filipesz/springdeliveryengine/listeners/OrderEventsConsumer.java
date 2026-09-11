package code.filipesz.springdeliveryengine.listeners;

import code.filipesz.springdeliveryengine.config.KafkaTopicConfig;
import code.filipesz.springdeliveryengine.dto.OrderEvent;
import code.filipesz.springdeliveryengine.services.CourierLocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventsConsumer {

    private final CourierLocationService courierLocationService;
    // private final JavaMailSender mailSender;

    @KafkaListener(topics = KafkaTopicConfig.ORDER_EVENTS_TOPIC, groupId = "delivery-group")
    public void consumeOrderEvent(OrderEvent event) {
        log.info("Odebrano zdarzenie zamówienia z Kafki [ID: {}, Typ: {}]", event.id(), event.eventType());

        if (event.eventType() == null) {
            return;
        }

        switch (event.eventType()) {
            case "ORDER_PLACED" -> handleOrderPlaced(event);
            case "ORDER_PAID" -> handleOrderPaid(event);
            case "ORDER_PAID_BY_CASH" -> handleOrderPaidByCash(event);
            case "ORDER_ON_THE_WAY" -> handleOrderOnTheWay(event);
            case "ORDER_DELIVERED" -> handleOrderDelivered(event);
            case "ORDER_CANCELLED" -> handleOrderCancelled(event);
            default -> log.warn("Nieznany typ zdarzenia: {}", event.eventType());
        }
    }

    private void handleOrderPlaced(OrderEvent event) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("company@gmail.com");
        message.setTo(event.clientEmail());
        message.setSubject("Rozpocząłeś realizację zamówienia #" + event.id());
        message.setText("Złożyłeś zamówienie, które należy opłacić.");
        // mailSender.send(message);

        log.info("[E-MAIL] Wysyłanie potwierdzenia przyjęcia zamówienia #{} do: {}", event.id(), event.clientEmail());
    }

    private void handleOrderPaid(OrderEvent event) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("company@gmail.com");
        message.setTo(event.clientEmail());
        message.setSubject("Potwierdzenie płatności za zamówienie #" + event.id());
        message.setText("Twoje zamówienie zostało pomyślnie opłacone!");
        // mailSender.send(message);

        log.info("[E-MAIL] Wysyłanie potwierdzenia płatności online #{} do: {}", event.id(), event.clientEmail());
    }

    private void handleOrderPaidByCash(OrderEvent event) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("company@gmail.com");
        message.setTo(event.clientEmail());
        message.setSubject("Potwierdzenie zamówienia z płatnością przy odbiorze #" + event.id());
        message.setText("Twoje zamówienie zostało pomyślnie złożone, należy przygotować najlepiej odliczoną kwotę.");
        // mailSender.send(message);

        log.info("[E-MAIL] Wysyłanie potwierdzenia płatności gotówką #{} do: {}", event.id(), event.clientEmail());
    }

    private void handleOrderOnTheWay(OrderEvent event) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("company@gmail.com");
        message.setTo(event.clientEmail());
        message.setSubject("Twoje zamówienie #" + event.id() + " jest w drodze!");
        message.setText("Kurier odebrał Twoje zamówienie z restauracji i jedzie do Ciebie.");
        // mailSender.send(message);

        log.info("[E-MAIL] Zamówienie #{} jest w drodze! Wysyłanie powiadomienia do: {}", event.id(), event.clientEmail());
    }

    private void handleOrderDelivered(OrderEvent event) {
        LocalDateTime deliveredAt = LocalDateTime.now();
        long minutesTaken = event.createdAt() != null
                ? Duration.between(event.createdAt(), deliveredAt).toMinutes()
                : 0;

        String ratingLink = "http://localhost:8080/api/orders/" + event.id() + "/rate";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("company@gmail.com");
        message.setTo(event.clientEmail());
        message.setSubject("Zamówienie #" + event.id() + " zostało dostarczone!");
        message.setText("Twoje zamówienie zostało pomyślnie doręczone. Smacznego!\n" +
                "Czas realizacji zamówienia: " + minutesTaken + " min\n" +
                "Oceń nasze zamówienie klikając w poniższy link:\n" + ratingLink);
        // mailSender.send(message);

        log.info("[E-MAIL] Zamówienie #{} dostarczone do: {}", event.id(), event.clientEmail());

        if (event.courierId() != null) {
            courierLocationService.removeCourierLocation(event.courierId());
            log.info("Wyczyszczono pozycję GPS w Redisie dla kuriera ID: {}", event.courierId());
        }
    }

    private void handleOrderCancelled(OrderEvent event) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("company@gmail.com");
        message.setTo(event.clientEmail());
        message.setSubject("Zamówienie #" + event.id() + " zostało anulowane");
        message.setText("Twoje zamówienie zostało pomyślnie anulowane. Jeżeli płaciłeś przed dostawą, środki zostaną zwrócone na Twoje konto.");
        // mailSender.send(message);

        log.info("[E-MAIL] Zamówienie #{} anulowane dla: {}", event.id(), event.clientEmail());

        if (event.courierId() != null) {
            courierLocationService.removeCourierLocation(event.courierId());
            log.info("Wyczyszczono pozycję GPS w Redisie dla kuriera ID: {}", event.courierId());
        }
    }
}