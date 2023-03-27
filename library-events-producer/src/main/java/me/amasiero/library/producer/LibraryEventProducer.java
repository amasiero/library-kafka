package me.amasiero.library.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.amasiero.library.domain.LibraryEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
@AllArgsConstructor
public class LibraryEventProducer {
    private final KafkaTemplate<Long, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

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
