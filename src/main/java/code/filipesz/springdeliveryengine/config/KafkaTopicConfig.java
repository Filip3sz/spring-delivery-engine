package code.filipesz.springdeliveryengine.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    public static final String COURIER_LOCATION_TOPIC = "courier-locations";
    public static final String ORDER_EVENTS_TOPIC = "order-events";

    @Bean
    public NewTopic courierLocationTopic() {
        return TopicBuilder.name(COURIER_LOCATION_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic orderEventsTopic() {
        return TopicBuilder.name(ORDER_EVENTS_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
