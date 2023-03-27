package me.amasiero.library.controller;

import me.amasiero.library.domain.LibraryEvent;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LibraryEventsController {
    @PostMapping("/v1/library-event")
    public ResponseEntity<LibraryEvent> create(@RequestBody LibraryEvent libraryEvent) {
        return ResponseEntity.status(HttpStatus.CREATED).body(libraryEvent);
    }
}
