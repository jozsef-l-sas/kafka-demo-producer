package com.jozsef.kafkademo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jozsef.kafkademo.domain.Book;
import com.jozsef.kafkademo.domain.LibraryEvent;
import com.jozsef.kafkademo.producer.LibraryEventProducer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LibraryEventsController.class)
@AutoConfigureMockMvc
public class LibraryEventControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @MockBean
    private LibraryEventProducer libraryEventProducer;

    @Test
    public void postLibraryEvent() throws Exception {
        // given
        LibraryEvent libraryEvent = LibraryEvent.builder()
                .id(null)
                .book(Book.builder()
                        .id(12)
                        .name("Kafka Using Spring Boot")
                        .author("Dilip")
                        .build())
                .build();
        String json = objectMapper.writeValueAsString(libraryEvent);

        // when
        when(libraryEventProducer.sendLibraryEventApproach2(isA(LibraryEvent.class))).thenReturn(null);

        // then
        mockMvc.perform(post("/v1/libraryevent")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    public void postLibraryEvent_4xx() throws Exception {
        // given
        LibraryEvent libraryEvent = LibraryEvent.builder()
                .id(null)
                .book(Book.builder()
                        .id(null)
                        .name(null)
                        .author("Dilip")
                        .build())
                .build();
        String json = objectMapper.writeValueAsString(libraryEvent);

        // when
        when(libraryEventProducer.sendLibraryEventApproach2(isA(LibraryEvent.class))).thenReturn(null);

        // then
        mockMvc.perform(post("/v1/libraryevent")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string("book.id - must not be null, book.name - must not be blank"));
    }

    @Test
    public void putLibraryEvent() throws Exception {
        // given
        LibraryEvent libraryEvent = LibraryEvent.builder()
                .id(123)
                .book(Book.builder()
                        .id(12)
                        .name("Kafka Using Spring Boot")
                        .author("Dilip")
                        .build())
                .build();
        String json = objectMapper.writeValueAsString(libraryEvent);

        // when
        when(libraryEventProducer.sendLibraryEventApproach2(isA(LibraryEvent.class))).thenReturn(null);

        // then
        mockMvc.perform(put("/v1/libraryevent")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void putLibraryEvent_4xx() throws Exception {
        // given
        LibraryEvent libraryEvent = LibraryEvent.builder()
                .id(null)
                .book(Book.builder()
                        .id(12)
                        .name("Kafka Using Spring Boot")
                        .author("Dilip")
                        .build())
                .build();
        String json = objectMapper.writeValueAsString(libraryEvent);

        // when
        when(libraryEventProducer.sendLibraryEventApproach2(isA(LibraryEvent.class))).thenReturn(null);

        // then
        mockMvc.perform(put("/v1/libraryevent")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string("Library Event id must not be null"));
    }

}
