package me.amasiero.library.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import me.amasiero.library.domain.LibraryEvent;
import me.amasiero.library.domain.LibraryEventType;
import me.amasiero.library.producer.LibraryEventProducer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class LibraryEventsController {

    private final LibraryEventProducer libraryEventProducer;

    @PostMapping("/v1/library-event")
    public ResponseEntity<LibraryEvent> create(@RequestBody @Valid LibraryEvent libraryEvent) throws JsonProcessingException {
        libraryEvent.setType(LibraryEventType.CREATE);
        libraryEventProducer.sendLibraryEvent("library-events", libraryEvent);
        return ResponseEntity.status(HttpStatus.CREATED).body(libraryEvent);
    }

    @PutMapping("/v1/library-event")
    public ResponseEntity<?> update(@RequestBody @Valid LibraryEvent libraryEvent) throws JsonProcessingException {
        if (libraryEvent.getId() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Id is mandatory for this operation");
        }
        libraryEvent.setType(LibraryEventType.UPDATE);
        libraryEventProducer.sendLibraryEvent(libraryEvent);
        return ResponseEntity.status(HttpStatus.OK).body(libraryEvent);
    }
}
