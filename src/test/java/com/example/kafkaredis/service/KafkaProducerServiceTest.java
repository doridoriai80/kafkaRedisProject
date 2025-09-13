package com.example.kafkaredis.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KafkaProducerServiceTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private KafkaProducerService kafkaProducerService;

    private CompletableFuture<SendResult<String, String>> future;

    @BeforeEach
    void setUp() {
        future = new CompletableFuture<>();
        when(kafkaTemplate.send(anyString(), anyString())).thenReturn(future);
        when(kafkaTemplate.send(anyString(), anyString(), anyString())).thenReturn(future);
    }

    @Test
    void testSendStringMessage() throws Exception {
        String topic = "test-topic";
        String message = "test message";

        kafkaProducerService.sendStringMessage(topic, message);

        verify(kafkaTemplate).send(topic, message);
    }

    @Test
    void testSendMessageWithKey() throws Exception {
        String topic = "test-topic";
        String key = "test-key";
        String message = "test message";

        when(objectMapper.writeValueAsString(message)).thenReturn(message);

        kafkaProducerService.sendMessage(topic, key, message);

        verify(kafkaTemplate).send(topic, key, message);
        verify(objectMapper).writeValueAsString(message);
    }

    @Test
    void testSendMessageWithoutKey() throws Exception {
        String topic = "test-topic";
        String message = "test message";

        when(objectMapper.writeValueAsString(message)).thenReturn(message);

        kafkaProducerService.sendMessage(topic, message);

        verify(kafkaTemplate).send(topic, null, message);
        verify(objectMapper).writeValueAsString(message);
    }
}