package com.jdt16.agenin.transaction.service.interfacing.module;

import com.jdt16.agenin.transaction.dto.entity.TransactionOpenBankAccountEntityDTO;
import com.jdt16.agenin.transaction.dto.request.CommissionToWalletRequest;
import com.jdt16.agenin.transaction.dto.request.TransactionRequest;
import com.jdt16.agenin.transaction.dto.response.*;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface TransactionService {
    @Transactional
    RestApiResponse<TransactionResponse> inquiry(UUID userId, UUID productId, TransactionRequest transactionRequest);

    RestApiResponse<List<ProductsResponse>> getListProducts();

    RestApiResponse<List<CustomerOpenBankAccountResponse>> getAllTransactionsByUser(UUID userId);

    RestApiResponse<UserBalanceResponse> transactionCommissionToWallet(UUID userId, CommissionToWalletRequest commissionToWalletRequest);

    RestApiResponse<UserBalanceAndWalletResponse> getUserBalanceAndWallet(UUID userId);
}
