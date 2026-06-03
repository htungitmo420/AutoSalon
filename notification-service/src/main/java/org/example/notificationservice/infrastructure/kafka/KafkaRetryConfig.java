package org.example.notificationservice.infrastructure.kafka;

import org.apache.kafka.common.TopicPartition;
import org.example.commoncontracts.kafka.KafkaTopics;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaRetryConfig {
    @Bean
    DefaultErrorHandler kafkaErrorHandler(
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${kafka.retry.max-attempts:3}") long maxAttempts,
            @Value("${kafka.retry.backoff-ms:1000}") long backoffMs) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, exception) -> new TopicPartition(KafkaTopics.deadLetter(record.topic()), record.partition()));
        return new DefaultErrorHandler(recoverer, new FixedBackOff(backoffMs, Math.max(0, maxAttempts - 1)));
    }
}
