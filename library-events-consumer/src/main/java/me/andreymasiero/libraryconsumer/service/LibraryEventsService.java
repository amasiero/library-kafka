package me.andreymasiero.libraryconsumer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.andreymasiero.libraryconsumer.entity.LibraryEvent;
import me.andreymasiero.libraryconsumer.repository.LibraryEventsRepository;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class LibraryEventsService {

    private final ObjectMapper objectMapper;
    private final LibraryEventsRepository libraryEventRepository;

    public void processEvent(ConsumerRecord<Long, String> consumerRecord) {
        try {
            LibraryEvent libraryEvent = objectMapper.readValue(consumerRecord.value(), LibraryEvent.class);
            log.info("Received event: {}", libraryEvent);
            switch (libraryEvent.getType()) {
                case CREATE -> save(libraryEvent);
                case UPDATE -> {
                    validate(libraryEvent);
                    save(libraryEvent);
                }
                default -> log.warn("Unsupported event type: {}", libraryEvent.getType());
            }
        } catch (Exception e) {
            log.error("Error processing event: {}", consumerRecord.value(), e);
        }
    }
    
    private void validate(LibraryEvent libraryEvent) {
        if (libraryEvent.getId() == null) throw new IllegalArgumentException("Event must have an id");
        LibraryEvent existingEvent = libraryEventRepository.findById(libraryEvent.getId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Event %d does not exist".formatted(libraryEvent.getId())));
        log.info("Validating event: {}", existingEvent);
    }

    private void save(LibraryEvent libraryEvent) {
        libraryEvent.getBook().setLibraryEvent(libraryEvent);
        libraryEventRepository.save(libraryEvent);
        log.info("Saved event: {}", libraryEvent);
    }
}
