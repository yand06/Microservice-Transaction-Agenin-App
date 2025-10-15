package com.jdt16.agenin.transaction.service.interfacing.module;

import com.jdt16.agenin.transaction.dto.request.TransactionRequest;
import com.jdt16.agenin.transaction.dto.response.RestApiResponse;
import com.jdt16.agenin.transaction.dto.response.TransactionResponse;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public interface TransactionService {
    @Transactional
    RestApiResponse<TransactionResponse> inquiry(UUID userId, UUID productId, TransactionRequest transactionRequest);
}
