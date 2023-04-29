package me.andreymasiero.libraryconsumer.config;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.kafka.annotation.EnableKafka;

@Configurable
@EnableKafka
public class LibraryEventsConsumerConfig {
}
