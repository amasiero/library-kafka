package me.amasiero.library.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.amasiero.library.domain.Book;
import me.amasiero.library.domain.LibraryEvent;
import me.amasiero.library.domain.LibraryEventType;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LibraryEventProducerTest {

    private final String TOPIC_NAME = "library-events";

    @Mock
    private KafkaTemplate<Long, String> kafkaTemplate;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private LibraryEventProducer libraryEventProducer;

    @Test
    @DisplayName("When sendLibraryEvent method called and it fails")
    public void whenSendLibraryEventCalled_thenOnFailure() {
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

        CompletableFuture<?> badRequest = new CompletableFuture<>();
        badRequest.completeExceptionally(new RuntimeException("Kafka call failed"));

        when(kafkaTemplate.send(isA(ProducerRecord.class))).thenReturn(badRequest);
        // When
        Assertions.assertThrows(Exception.class, () -> libraryEventProducer.sendLibraryEvent(TOPIC_NAME, event).get());

    }

    @Test
    @DisplayName("When sendLibraryEvent method called and it success")
    public void whenSendLibraryEventCalled_thenOnSuccess()
            throws JsonProcessingException, ExecutionException, InterruptedException {
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

        String record = objectMapper.writeValueAsString(event);
        ProducerRecord<Long, String> producerRecord = new ProducerRecord<>(TOPIC_NAME, event.getId(), record);
        RecordMetadata recordMetadata = new RecordMetadata(new TopicPartition(TOPIC_NAME, 1),
                1, 1, 342, System.currentTimeMillis(), 1, 2);
        SendResult<Long, String> sendResult = new SendResult<>(producerRecord, recordMetadata);

        CompletableFuture<?> goodRequest = CompletableFuture.completedFuture(sendResult);

        when(kafkaTemplate.send(isA(ProducerRecord.class))).thenReturn(goodRequest);
        // When
        SendResult<Long, String> result = libraryEventProducer.sendLibraryEvent(TOPIC_NAME, event).get();
        assertEquals(1, result.getRecordMetadata().partition());

    }
}
