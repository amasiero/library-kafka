package me.amasiero.library.controller;

import me.amasiero.library.domain.Book;
import me.amasiero.library.domain.LibraryEvent;
import me.amasiero.library.domain.LibraryEventType;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.TestPropertySource;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(topics = {"library-events"}, partitions = 3)
@TestPropertySource(properties = {
        "spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.admin.properties.bootstrap.servers=${spring.embedded.kafka.brokers}"
})
public class LibraryEventsControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private Consumer<Long, String> consumer;

    @BeforeEach
    public void setUp(@Autowired EmbeddedKafkaBroker embeddedKafkaBroker) {
        Map<String, Object> configs = new HashMap<>(
                KafkaTestUtils.consumerProps("group1", "true", embeddedKafkaBroker)
        );
        consumer = new DefaultKafkaConsumerFactory<>(
                configs,
                new LongDeserializer(),
                new StringDeserializer()
        ).createConsumer();
        embeddedKafkaBroker.consumeFromAllEmbeddedTopics(consumer);
    }

    @AfterEach
    public void tearDown() {
        consumer.close();
    }

    @Test
    @Timeout(5)
    @DisplayName("When POST is requested returns http status created (201)")
    public void whenPostIsRequested_returnsHttpStatusCreated() {
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

        HttpHeaders headers = new HttpHeaders();
        headers.add("content-type", MediaType.APPLICATION_JSON_VALUE);

        HttpEntity<LibraryEvent> request = new HttpEntity<>(event, headers);

        String expectedValue =
                "{\"id\":null,\"type\":\"CREATE\",\"book\":{\"id\":123,\"name\":\"test\",\"author\":\"test\"}}";

        // When
        ResponseEntity<LibraryEvent> responseEntity = restTemplate.exchange(
                "/v1/library-event",
                HttpMethod.POST,
                request,
                LibraryEvent.class
        );

        ConsumerRecord<Long, String> result = KafkaTestUtils.getSingleRecord(consumer, "library-events");

        // Then
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        assertEquals(expectedValue, result.value());
    }
}
