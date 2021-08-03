package com.jozsef.kafkademo.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jozsef.kafkademo.domain.LibraryEvent;
import com.jozsef.kafkademo.domain.LibraryEventType;
import com.jozsef.kafkademo.producer.LibraryEventProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@Slf4j
public class LibraryEventsController {

    @Autowired
    private LibraryEventProducer libraryEventProducer;

    @PostMapping("/v1/libraryevent")
    public ResponseEntity<LibraryEvent> postLibraryEvent(@RequestBody @Valid LibraryEvent libraryEvent) throws JsonProcessingException {
        // invoke the Kafka producer
        libraryEvent.setType(LibraryEventType.NEW);
        libraryEventProducer.sendLibraryEventApproach2(libraryEvent);

        return ResponseEntity.status(HttpStatus.CREATED).body(libraryEvent);
    }

    @PutMapping("/v1/libraryevent")
    public ResponseEntity<?> putLibraryEvent(@RequestBody @Valid LibraryEvent libraryEvent) throws JsonProcessingException {
        if (libraryEvent.getId() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Library Event id must not be null");
        }

        // invoke the Kafka producer
        libraryEvent.setType(LibraryEventType.UPDATE);
        libraryEventProducer.sendLibraryEventApproach2(libraryEvent);

        return ResponseEntity.status(HttpStatus.OK).body(libraryEvent);
    }

}
