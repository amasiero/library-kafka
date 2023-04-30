package me.andreymasiero.libraryconsumer.repository;

import me.andreymasiero.libraryconsumer.entity.LibraryEvent;
import org.springframework.data.repository.CrudRepository;

public interface LibraryEventRepository extends CrudRepository<LibraryEvent, Long> {
}
