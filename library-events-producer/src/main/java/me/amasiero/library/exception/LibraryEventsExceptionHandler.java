package me.amasiero.library.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import me.amasiero.library.domain.Error;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
@Slf4j
public class LibraryEventsExceptionHandler {

    ObjectMapper objectMapper = new ObjectMapper();

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handlerException(MethodArgumentNotValidException exception)
            throws JsonProcessingException {
        List<Error> errors = exception
                .getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> Error.builder()
                        .field(fieldError.getField())
                        .message(fieldError.getDefaultMessage())
                        .build())
                .sorted(Comparator.comparing(Error::getField))
                .collect(Collectors.toList());
        log.info("errors: {}", objectMapper.writeValueAsString(errors));

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }
}
