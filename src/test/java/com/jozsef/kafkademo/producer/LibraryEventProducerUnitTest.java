package com.jozsef.kafkademo.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jozsef.kafkademo.domain.Book;
import com.jozsef.kafkademo.domain.LibraryEvent;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.SettableListenableFuture;

import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LibraryEventProducerUnitTest {

    @Mock
    private KafkaTemplate<Integer, String> kafkaTemplate;

    @Spy
    private ObjectMapper objectMapper;

    @InjectMocks
    private LibraryEventProducer libraryEventProducer;

    @Test
    void sendLibraryEventApproach2_failure() {
        // given
        LibraryEvent libraryEvent = LibraryEvent.builder()
                .id(null)
                .book(Book.builder()
                        .id(12)
                        .name("Kafka Using Spring Boot")
                        .author("Dilip")
                        .build())
                .build();
        SettableListenableFuture future = new SettableListenableFuture();
        future.setException(new RuntimeException("Exception calling Kafka"));

        // when
        when(kafkaTemplate.send(isA(ProducerRecord.class))).thenReturn(future);

        // then
        assertThrows(Exception.class, () -> libraryEventProducer.sendLibraryEventApproach2(libraryEvent).get());
    }

    @Test
    void sendLibraryEventApproach2_success() throws JsonProcessingException, ExecutionException, InterruptedException {
        // given
        LibraryEvent libraryEvent = LibraryEvent.builder()
                .id(null)
                .book(Book.builder()
                        .id(12)
                        .name("Kafka Using Spring Boot")
                        .author("Dilip")
                        .build())
                .build();
        String record = objectMapper.writeValueAsString(libraryEvent);
        ProducerRecord<Integer, String> producerRecord = new ProducerRecord<>("library-events", libraryEvent.getId(), record);
        RecordMetadata recordMetadata = new RecordMetadata(
                new TopicPartition("library-events", 1), 1, 1,
                342, System.currentTimeMillis(), 1, 2);
        SendResult<Integer, String> sendResult = new SendResult<>(producerRecord, recordMetadata);
        SettableListenableFuture future = new SettableListenableFuture();
        future.set(sendResult);

        // when
        when(kafkaTemplate.send(isA(ProducerRecord.class))).thenReturn(future);

        // then
        ListenableFuture<SendResult<Integer, String>> listenableFuture = libraryEventProducer.sendLibraryEventApproach2(libraryEvent);
        SendResult<Integer, String> result = listenableFuture.get();
        assert result.getRecordMetadata().partition() == 1;
    }

}
