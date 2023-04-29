package me.andreymasiero.libraryconsumer.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.listener.AcknowledgingMessageListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.lang.NonNullApi;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LibraryEventsConsumerManualOffset implements AcknowledgingMessageListener<Long, String> {

    @Override
    @KafkaListener(topics = "library-events")
    public void onMessage(ConsumerRecord<Long, String> data, Acknowledgment acknowledgment) {
        log.info("Message received: {}", data);
        acknowledgment.acknowledge();
    }
}
