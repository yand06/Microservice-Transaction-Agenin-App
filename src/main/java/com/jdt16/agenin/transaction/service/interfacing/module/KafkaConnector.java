package com.jdt16.agenin.transaction.service.interfacing.module;

import com.jdt16.agenin.transaction.dto.exception.CoreThrowHandlerException;
import com.jdt16.agenin.transaction.dto.response.RestApiResponse;

public interface KafkaConnector {
    default RestApiResponse<?> kafkaSync(String request,
                                         String topic,
                                         String replyTopic) throws CoreThrowHandlerException {
        return null;
    }

    default void kafkaAsync(String request, String topic) throws CoreThrowHandlerException {
    }

}