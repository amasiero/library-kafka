package me.andreymasiero.libraryconsumer.repository;

import me.andreymasiero.libraryconsumer.entity.LibraryEvent;
import org.springframework.data.repository.CrudRepository;

public interface LibraryEventsRepository extends CrudRepository<LibraryEvent, Long> {
}
