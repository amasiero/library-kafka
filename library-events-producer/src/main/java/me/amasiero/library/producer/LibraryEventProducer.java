package me.amasiero.library.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.amasiero.library.domain.LibraryEvent;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
@AllArgsConstructor
public class LibraryEventProducer {
    private final KafkaTemplate<Long, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    /**
     * It publishes the topic defined in spring.kafka.template.default-topic
     * @param libraryEvent - Object which represents the message to be published
     * @throws JsonProcessingException
     */
    public void sendLibraryEvent(LibraryEvent libraryEvent) throws JsonProcessingException {
        Long key = libraryEvent.getId();
        String value = objectMapper.writeValueAsString(libraryEvent);

        CompletableFuture<SendResult<Long, String>> future = kafkaTemplate.sendDefault(key, value);
        future.whenComplete((result, ex) -> {
           if (ex == null) {
               handlerSuccess(key, value, result);
           } else {
               handlerFailure(key, value, ex);
           }
        });
    }

    public CompletableFuture<SendResult<Long, String>> sendLibraryEvent(String topic, LibraryEvent libraryEvent) throws JsonProcessingException {
        Long key = libraryEvent.getId();
        String value = objectMapper.writeValueAsString(libraryEvent);

        ProducerRecord<Long, String> producerRecord = buildProducerRecord(topic, key, value);
        CompletableFuture<SendResult<Long, String>> future = kafkaTemplate.send(producerRecord);
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                handlerSuccess(key, value, result);
            } else {
                handlerFailure(key, value, ex);
            }
        });

        return future;
    }

    private ProducerRecord<Long, String> buildProducerRecord(String topic, Long key, String value) {
        List<Header> headers = List.of(new RecordHeader("event-source", "manual-scanner".getBytes()));
        return new ProducerRecord<Long, String>(topic, null, key, value, headers);
    }

    private Void handlerFailure(Long key, String value, Throwable ex) {
        log.error("Error sending the message: {}", ex.getMessage());
        try {
            throw ex;
        } catch (Throwable throwable) {
            log.error("Error in OnFailure: {}", throwable.getMessage());
        }
        return null;
    }

    private void handlerSuccess(Long key, String value, SendResult<Long, String> result) {
        log.info("Message sent successfully for the key: {} and the value: {} at partition: {}",
                key, value, result.getRecordMetadata().partition());
    }
}
