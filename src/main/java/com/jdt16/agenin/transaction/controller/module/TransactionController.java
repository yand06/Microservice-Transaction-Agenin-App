package com.jdt16.agenin.transaction.controller.module;

import com.jdt16.agenin.transaction.dto.request.CommissionToWalletRequest;
import com.jdt16.agenin.transaction.dto.request.TransactionRequest;
import com.jdt16.agenin.transaction.dto.response.*;
import com.jdt16.agenin.transaction.service.interfacing.module.TransactionService;
import com.jdt16.agenin.transaction.utility.RestApiPathUtility;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping(RestApiPathUtility.API_PATH + RestApiPathUtility.API_VERSION + RestApiPathUtility.API_PATH_TRANSACTION)
public class TransactionController {
    private final TransactionService transactionService;

    @PostMapping(RestApiPathUtility.API_PATH_MOCK_TRANSACTION_OPEN_BANK_ACCOUNT)
    public ResponseEntity<RestApiResponse<?>> inquiry(
            @RequestHeader("X-USER-ID") UUID userId,
            @RequestHeader("X-PRODUCT-ID") UUID productId,
            @Valid @RequestBody TransactionRequest transactionRequest) {
        return ResponseEntity.ok(transactionService.inquiry(userId, productId, transactionRequest));
    }

    @GetMapping(RestApiPathUtility.API_PATH_GET_CUSTOMER_LIST)
    public ResponseEntity<RestApiResponse<?>> getCustomerList(@RequestHeader("X-USER-ID") UUID userId) {
        return ResponseEntity.ok(transactionService.getAllTransactionsByUser(userId));
    }

    @GetMapping(RestApiPathUtility.API_PATH_GET_PRODUCTS)
    public ResponseEntity<RestApiResponse<?>> getProductsList() {
        return ResponseEntity.ok(transactionService.getListProducts());
    }

    @PatchMapping(RestApiPathUtility.API_PATH_MODULE_TRANSFER_TO_WALLET)
    public ResponseEntity<RestApiResponse<?>> patchTransferCommissionToWallet(
            @RequestHeader("X-USER-ID") UUID userId,
            @Valid @RequestBody CommissionToWalletRequest commissionToWalletRequest
    ) {
        return ResponseEntity.ok(transactionService.transactionCommissionToWallet(userId, commissionToWalletRequest));
    }

    @GetMapping(RestApiPathUtility.API_PATH_GET_BALANCE_AND_WALLET)
    public ResponseEntity<RestApiResponse<?>> getBalanceAndWallet(@RequestHeader("X-USER-ID") UUID userId) {
        return ResponseEntity.ok(transactionService.getUserBalanceAndWallet(userId));
    }
}
