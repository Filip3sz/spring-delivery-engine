package code.filipesz.springdeliveryengine.listeners;

import code.filipesz.springdeliveryengine.config.KafkaTopicConfig;
import code.filipesz.springdeliveryengine.dto.CourierLocationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CourierLocationConsumer {

    private static final String REDIS_KEY = "courier:locations";
    private final StringRedisTemplate redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;

    @KafkaListener(topics = KafkaTopicConfig.COURIER_LOCATION_TOPIC, groupId = "delivery-group")
    public void consumeLocationEvent(CourierLocationEvent event) {
        log.info("Odebrano punkt GPS z Kafki dla kuriera: {}", event.courierId());

        Point point = new Point(event.longitude(), event.latitude());
        redisTemplate.opsForGeo().add(
                REDIS_KEY,
                point,
                event.courierId().toString()
        );

        String destination = "/topic/courier/" + event.courierId() + "/location";
        messagingTemplate.convertAndSend(destination, event);
    }
}