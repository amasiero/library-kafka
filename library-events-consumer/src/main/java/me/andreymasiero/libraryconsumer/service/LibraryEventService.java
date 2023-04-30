package me.andreymasiero.libraryconsumer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.andreymasiero.libraryconsumer.entity.LibraryEvent;
import me.andreymasiero.libraryconsumer.repository.LibraryEventRepository;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class LibraryEventService {

    private final ObjectMapper objectMapper;
    private final LibraryEventRepository libraryEventRepository;

    public void processEvent(ConsumerRecord<Long, String> consumerRecord) {
        try {
            LibraryEvent libraryEvent = objectMapper.readValue(consumerRecord.value(), LibraryEvent.class);
            log.info("Received event: {}", libraryEvent);
            switch (libraryEvent.getType()) {
                case CREATE -> save(libraryEvent);
                default -> log.warn("Unsupported event type: {}", libraryEvent.getType());
            }
        } catch (Exception e) {
            log.error("Error processing event: {}", consumerRecord.value(), e);
        }
    }

    private void save(LibraryEvent libraryEvent) {
        libraryEvent.getBook().setLibraryEvent(libraryEvent);
        libraryEventRepository.save(libraryEvent);
        log.info("Saved event: {}", libraryEvent);
    }
}
