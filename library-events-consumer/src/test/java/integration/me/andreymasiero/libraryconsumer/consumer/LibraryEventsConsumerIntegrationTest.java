package me.andreymasiero.libraryconsumer.consumer;

import me.andreymasiero.libraryconsumer.entity.LibraryEvent;
import me.andreymasiero.libraryconsumer.entity.LibraryEventType;
import me.andreymasiero.libraryconsumer.repository.LibraryEventRepository;
import me.andreymasiero.libraryconsumer.service.LibraryEventService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@EmbeddedKafka(topics = {"library-events"}, partitions = 3, count = 3)
@TestPropertySource(properties = {
        "spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.consumer.bootstrap-servers=${spring.embedded.kafka.brokers}"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class LibraryEventsConsumerIntegrationTest {

    @Autowired
    KafkaTemplate<Long, String> kafkaTemplate;

    @SpyBean
    LibraryEventsConsumer libraryEventsConsumerSpy;

    @SpyBean
    LibraryEventService libraryEventServiceSpy;

    @Autowired
    LibraryEventRepository repository;

    @BeforeEach
    void setUp(
            @Autowired EmbeddedKafkaBroker embeddedKafkaBroker,
            @Autowired KafkaListenerEndpointRegistry endpointRegistry
    ) {
        for (MessageListenerContainer container : endpointRegistry.getListenerContainers()) {
            ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic());
        }
    }

    @AfterEach
    void tearDown() {
        repository.deleteAll();
    }

    @Test
    void whenConsumerReceiveAnEvent() throws ExecutionException, InterruptedException {
        // Given
        String json = """
                {
                    "id": 42,
                    "type": CREATE,
                    "book": {
                        "id": 1,
                        "name": "Andrey's book",
                        "author": "Andrey"
                    }
                }
                """;
        kafkaTemplate.send("library-events", json).get();

        // When
        CountDownLatch latch = new CountDownLatch(1);
        boolean messageConsumed = latch.await(3, TimeUnit.SECONDS);

        // Then
        verify(libraryEventsConsumerSpy, times(1)).onMessageReceived(isA(ConsumerRecord.class));
        verify(libraryEventServiceSpy, times(1)).processEvent(isA(ConsumerRecord.class));

        List<LibraryEvent> events = (List<LibraryEvent>) repository.findAll();
        System.out.println(events);
        assertTrue(messageConsumed);
        assertEquals(1, events.size());
        events.forEach(event -> {
            assertNotNull(event.getId());
            assertEquals(LibraryEventType.CREATE, event.getType());
            assertEquals(1, event.getBook().getId());
        });
    }


}