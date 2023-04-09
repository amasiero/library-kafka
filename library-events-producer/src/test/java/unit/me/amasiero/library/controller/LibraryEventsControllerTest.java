package me.amasiero.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.amasiero.library.domain.Book;
import me.amasiero.library.domain.LibraryEvent;
import me.amasiero.library.domain.LibraryEventType;
import me.amasiero.library.producer.LibraryEventProducer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(LibraryEventsController.class)
@AutoConfigureMockMvc
public class LibraryEventsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LibraryEventProducer libraryEventProducer;

    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("when library event post is requested then return http status created (201)")
    public void whenLibraryEventPostIsRequested_thenReturnHttpStatusCreated() throws Exception {
        // Given
        Book book = Book.builder()
                .id(123L)
                .author("test")
                .name("test")
                .build();

        LibraryEvent event = LibraryEvent.builder()
                .id(null)
                .book(book)
                .type(LibraryEventType.CREATE)
                .build();

        String json = objectMapper.writeValueAsString(event);

        doNothing().when(libraryEventProducer).sendLibraryEvent(isA(LibraryEvent.class));

        // When
        mockMvc.perform(
                post("/v1/library-event")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
        ).andExpect(status().isCreated());

        // Then
    }
}
