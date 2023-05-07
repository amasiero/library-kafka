package me.andreymasiero.libraryconsumer.consumer;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.andreymasiero.libraryconsumer.service.LibraryEventsService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@AllArgsConstructor
public class LibraryEventsConsumer {

    private final LibraryEventsService service;

    @KafkaListener(topics = "library-events")
    public void onMessageReceived(ConsumerRecord<Long, String> consumerRecord) {
        log.info("Message received: {}", consumerRecord);
        service.processEvent(consumerRecord);
    }
}
