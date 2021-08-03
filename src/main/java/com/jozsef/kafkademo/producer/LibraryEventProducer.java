package com.jozsef.kafkademo.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jozsef.kafkademo.domain.LibraryEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
@Slf4j
public class LibraryEventProducer {

    private static final String TOPIC = "library-events";

    @Autowired
    private KafkaTemplate<Integer, String> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    public void sendLibraryEvent(LibraryEvent libraryEvent) throws JsonProcessingException {
        Integer key = libraryEvent.getId();
        String value = objectMapper.writeValueAsString(libraryEvent);
        ListenableFuture<SendResult<Integer, String>> result = kafkaTemplate.sendDefault(key, value);
        result.addCallback(new ListenableFutureCallback<>() {
            @Override
            public void onFailure(Throwable ex) {
                handleFailure(key, value, ex);
            }

            @Override
            public void onSuccess(SendResult<Integer, String> integerStringSendResult) {
                handleSuccess(key, value, integerStringSendResult);
            }
        });
    }

    public ListenableFuture<SendResult<Integer, String>> sendLibraryEventApproach2(LibraryEvent libraryEvent) throws JsonProcessingException {
        Integer key = libraryEvent.getId();
        String value = objectMapper.writeValueAsString(libraryEvent);

        ProducerRecord<Integer, String> producerRecord = buildProducerRecord(TOPIC, key, value);

        ListenableFuture<SendResult<Integer, String>> result = kafkaTemplate.send(producerRecord);
        result.addCallback(new ListenableFutureCallback<>() {
            @Override
            public void onFailure(Throwable ex) {
                handleFailure(key, value, ex);
            }

            @Override
            public void onSuccess(SendResult<Integer, String> integerStringSendResult) {
                handleSuccess(key, value, integerStringSendResult);
            }
        });

        return result;
    }

    private ProducerRecord<Integer, String> buildProducerRecord(String topic, Integer key, String value) {
        return new ProducerRecord<>(topic, null, key, value, List.of(new RecordHeader("event-source", "scanner".getBytes())));
    }

    public SendResult<Integer, String> sendLibraryEventSynchronous(LibraryEvent libraryEvent) throws JsonProcessingException, ExecutionException, InterruptedException, TimeoutException {
        Integer key = libraryEvent.getId();
        String value = objectMapper.writeValueAsString(libraryEvent);
        SendResult<Integer, String> result = null;
        try {
            result = kafkaTemplate.sendDefault(key, value).get(1, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            log.error("Error sending the message with key {}, exception is {}", key, e.getMessage());
            throw e;
        }

        return result;
    }

    private void handleFailure(Integer key, String value, Throwable ex) {
        log.error("Error sending the message with key {}, exception is {}", key, ex.getMessage());
        try {
            throw ex;
        } catch (Throwable throwable) {
            log.error("Error in onFailure: {}", throwable.getMessage());
        }
    }

    private void handleSuccess(Integer key, String value, SendResult<Integer, String> result) {
        log.info("Message sent successfully for key : {} and the value is {} , partition is {}", key, value, result.getRecordMetadata().partition());
    }

}