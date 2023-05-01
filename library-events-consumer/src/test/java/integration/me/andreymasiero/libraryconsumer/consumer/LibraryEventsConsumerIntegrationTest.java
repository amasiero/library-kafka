package me.andreymasiero.libraryconsumer.consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

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
    EmbeddedKafkaBroker embeddedKafkaBroker;
    KafkaListenerEndpointRegistry endpointRegistry;

    @BeforeEach
    public void setUp(
            @Autowired EmbeddedKafkaBroker embeddedKafkaBroker,
            @Autowired KafkaListenerEndpointRegistry endpointRegistry
    ) {
        this.embeddedKafkaBroker = embeddedKafkaBroker;
        this.endpointRegistry = endpointRegistry;
    }

    @Test
    public void whenConsumerReceiveAnEvent() {
        for (MessageListenerContainer container : endpointRegistry.getListenerContainers()) {
            ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic());
        }
    }


}