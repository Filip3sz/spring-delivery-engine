package code.filipesz.springdeliveryengine.services;

import code.filipesz.springdeliveryengine.config.KafkaTopicConfig;
import code.filipesz.springdeliveryengine.dto.CourierLocationEvent;
import code.filipesz.springdeliveryengine.dto.CourierLocationRequest;
import code.filipesz.springdeliveryengine.dto.CourierLocationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CourierLocationService {

    private static final String REDIS_KEY = "courier:locations";
    private final StringRedisTemplate redisTemplate;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public CourierLocationResponse getCourierLocation(UUID courierId) {
        List<Point> points = redisTemplate.opsForGeo().position(
                REDIS_KEY,
                courierId.toString()
        );

        if (points == null || points.isEmpty() || points.get(0) == null) {
            throw new IllegalArgumentException("Brak danych GPS dla kuriera o ID: " + courierId);
        }

        Point point = points.get(0);
        return new CourierLocationResponse(courierId, point.getY(), point.getX());
    }

    public void updateCourierLocation(UUID courierId, CourierLocationRequest request) {
        CourierLocationEvent event = CourierLocationEvent.from(courierId, request.latitude(), request.longitude());
        kafkaTemplate.send(KafkaTopicConfig.COURIER_LOCATION_TOPIC, courierId.toString(), event);
    }

    public void removeCourierLocation(UUID courierId) {
        redisTemplate.opsForGeo().remove(REDIS_KEY, courierId.toString());
    }
}