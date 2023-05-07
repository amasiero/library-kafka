package me.andreymasiero.libraryconsumer.consumer;

import me.andreymasiero.libraryconsumer.entity.LibraryEvent;
import me.andreymasiero.libraryconsumer.repository.LibraryEventsRepository;
import me.andreymasiero.libraryconsumer.service.LibraryEventsService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@EmbeddedKafka(topics = {"library-events"}, partitions = 3)
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
    LibraryEventsService libraryEventsServiceSpy;

    @Autowired
    LibraryEventsRepository libraryEventsRepository;

    @BeforeEach
    void setUp(
            @Autowired EmbeddedKafkaBroker embeddedKafkaBroker,
            @Autowired KafkaListenerEndpointRegistry endpointRegistry
    ) {
        for (MessageListenerContainer container : endpointRegistry.getListenerContainers()) {
            ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic());
        }
    }

    @Test
    void whenConsumerReceiveAnEvent() throws ExecutionException, InterruptedException {
        // Given
        String json = """
                {
                    "id": 1,
                    "type": "CREATE",
                    "book": {
                        "id": 1,
                        "name": "Andrey's book",
                        "author": "Andrey"
                    }
                }
                """;
        kafkaTemplate.sendDefault(json);

        // When
        CountDownLatch latch = new CountDownLatch(1);
        boolean latchResult = latch.await(3, TimeUnit.SECONDS);

        // Then
        verify(libraryEventsConsumerSpy, times(1)).onMessageReceived(isA(ConsumerRecord.class));
        verify(libraryEventsServiceSpy, times(1)).processEvent(isA(ConsumerRecord.class));
        assertFalse(latchResult);

        List<LibraryEvent> eventsRetrieved = (List<LibraryEvent>) libraryEventsRepository.findAll();
        assertEquals(1, eventsRetrieved.size());
        eventsRetrieved.forEach(event -> {
            assertEquals(1, event.getId());
            assertEquals(1, event.getBook().getId());
        });
    }


}