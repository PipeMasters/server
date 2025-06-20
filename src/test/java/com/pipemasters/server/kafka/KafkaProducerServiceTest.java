package com.pipemasters.server.kafka;

import com.pipemasters.server.kafka.KafkaProducerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;
import org.springframework.kafka.core.KafkaTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class KafkaProducerServiceTest {

    private KafkaTemplate<String, String> kafkaTemplate;
    private KafkaProducerService producerService;

    @BeforeEach
    void setUp() {
        kafkaTemplate = mock(KafkaTemplate.class);
        producerService = new KafkaProducerService(kafkaTemplate);
    }

    @Test
    void send_shouldSendMessageToKafka() {
        String topic = "test-topic";
        String message = "test-message";

        producerService.send(topic, message);

        verify(kafkaTemplate, times(1)).send(topic, message);
    }
}