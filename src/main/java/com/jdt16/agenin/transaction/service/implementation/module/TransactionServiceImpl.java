package com.jdt16.agenin.transaction.service.implementation.module;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jdt16.agenin.transaction.configuration.kafka.properties.RequestTopicProperties;
import com.jdt16.agenin.transaction.dto.entity.TransactionEntityDTO;
import com.jdt16.agenin.transaction.dto.entity.TransactionOpenBankAccountEntityDTO;
import com.jdt16.agenin.transaction.dto.entity.UserBalanceEntityDTO;
import com.jdt16.agenin.transaction.dto.entity.UserBalanceHistoricalEntityDTO;
import com.jdt16.agenin.transaction.dto.exception.CoreThrowHandlerException;
import com.jdt16.agenin.transaction.dto.request.LogRequestDTO;
import com.jdt16.agenin.transaction.dto.request.TransactionRequest;
import com.jdt16.agenin.transaction.dto.response.RestApiResponse;
import com.jdt16.agenin.transaction.dto.response.TransactionResponse;
import com.jdt16.agenin.transaction.model.repository.*;
import com.jdt16.agenin.transaction.service.interfacing.module.CommissionValueProjection;
import com.jdt16.agenin.transaction.service.interfacing.module.KafkaConnector;
import com.jdt16.agenin.transaction.service.interfacing.module.ProductCodeProjection;
import com.jdt16.agenin.transaction.service.interfacing.module.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
@Service
public class TransactionServiceImpl implements TransactionService {

    private final TTransactionRepositories tTransactionRepositories;
    private final MProductsRepositories mProductsRepositories;
    private final TUserBalanceRepositories tUserBalanceRepositories;
    private final TUsersBalanceHistoricalRepositories tUsersBalanceHistoricalRepositories;
    private final MCommissionRepositories mCommissionRepositories;
    private final TTransactionOpenBankAccountRepositories tTransactionOpenBankAccountRepositories;
    private final KafkaConnector kafkaConnector;
    private final ObjectMapper objectMapper;
    private final RequestTopicProperties requestTopicProperties;

    private <T> RestApiResponse<T> createRestApiResponse(HttpStatus httpStatus, String message, T result) {
        return RestApiResponse.<T>builder()
                .restApiResponseCode(httpStatus.value())
                .restApiResponseMessage(message)
                .restApiResponseResults(result)
                .build();
    }

    private void sendLogKafka(String description, String name, String type) {
        LogRequestDTO logRequestDTO = new LogRequestDTO();
        logRequestDTO.setLogDescription(description);
        logRequestDTO.setLogName(name);
        logRequestDTO.setLogType(type);

        try {
            kafkaConnector.kafkaAsync(objectMapper.writeValueAsString(logRequestDTO), requestTopicProperties.getCreateLogTopic());
        } catch (JsonProcessingException e) {
            log.error("Failed to send log to Kafka: {}", e.getMessage(), e);
            throw new CoreThrowHandlerException("ERROR: " + e.getMessage());
        }
    }

    private TransactionResponse transactionEntityDTOToTransactionResponse(
            TransactionEntityDTO transactionEntityDTO,
            TransactionOpenBankAccountEntityDTO transactionOpenBankAccountEntityDTO
    ) {
        TransactionResponse transactionResponse = new TransactionResponse();
        transactionResponse.setTransactionEntityDTOId(transactionEntityDTO.getTransactionEntityDTOId());
        transactionResponse.setTransactionEntityDTOCode(transactionEntityDTO.getTransactionEntityDTOCode());
        transactionResponse.setTransactionEntityDTOUserId(transactionEntityDTO.getTransactionEntityDTOUserId());
        transactionResponse.setTransactionEntityDTOProductId(transactionEntityDTO.getTransactionEntityDTOProductId());
        transactionResponse.setTransactionEntityDTOProductCode(transactionEntityDTO.getTransactionEntityDTOProductCode());
        transactionResponse.setTransactionEntityDTOCustomerName(transactionOpenBankAccountEntityDTO.getTransactionOpenBankAccountEntityDTOCustomerName());
        transactionResponse.setTransactionEntityDTOCustomerIdentityNumber(transactionOpenBankAccountEntityDTO.getTransactionOpenBankAccountEntityDTOCustomerIdentityNumber());
        transactionResponse.setTransactionEntityDTOCustomerPhoneNumber(transactionOpenBankAccountEntityDTO.getTransactionOpenBankAccountEntityDTOCustomerPhoneNumber());
        transactionResponse.setTransactionEntityDTOCustomerEmail(transactionOpenBankAccountEntityDTO.getTransactionOpenBankAccountEntityDTOCustomerEmail());
        transactionResponse.setTransactionEntityDTOCustomerAddress(transactionOpenBankAccountEntityDTO.getTransactionOpenBankAccountEntityDTOCustomerAddress());
        transactionResponse.setTransactionEntityDTODate(transactionEntityDTO.getTransactionEntityDTODate());
        transactionResponse.setTransactionEntityDTOStatus(transactionEntityDTO.getTransactionEntityDTOStatus());
        return transactionResponse;
    }

    public String getProductCode(UUID productId) {
        return mProductsRepositories.findByProductEntityDTOId(productId)
                .map(ProductCodeProjection::getProductEntityDTOCode)
                .orElseThrow(() -> new CoreThrowHandlerException("Product not found"));
    }

    public BigDecimal getCommissionValue(UUID productId) {
        return mCommissionRepositories.findByCommissionsEntityDTOProductId(productId)
                .map(CommissionValueProjection::getCommissionsEntityDTOValue)
                .orElseThrow(() -> new CoreThrowHandlerException("Commission value not found"));
    }

    @Transactional
    @Override
    public RestApiResponse<TransactionResponse> inquiry(UUID userId, UUID productId, TransactionRequest transactionRequest) {
        UUID transactionId = UUID.randomUUID();
        LocalDateTime transactionDate = LocalDateTime.now();

        TransactionEntityDTO transactionEntityDTO = new TransactionEntityDTO();
        transactionEntityDTO.setTransactionEntityDTOId(transactionId);
        transactionEntityDTO.setTransactionEntityDTOCode("TRX_" + transactionId + "_" + transactionDate);
        transactionEntityDTO.setTransactionEntityDTOUserId(userId);
        transactionEntityDTO.setTransactionEntityDTOProductId(productId);
        transactionEntityDTO.setTransactionEntityDTOProductCode(getProductCode(productId));
        transactionEntityDTO.setTransactionEntityDTODate(transactionDate);
        transactionEntityDTO.setTransactionEntityDTOStatus("SUCCESS");
        tTransactionRepositories.save(transactionEntityDTO);

        TransactionOpenBankAccountEntityDTO openBankAccountEntityDTO = saveTransactionOpenBankAccount(transactionId, transactionRequest);

        UUID userBalanceId = saveUserBalance(userId, productId);

        saveUserBalanceHistorical(userBalanceId, transactionId, productId);

        log.info("Transaction inquiry success with ID: {}", transactionId);
        sendLogKafka("Transaction inquiry success with ID: " + transactionId, "INQUIRY", "SUCCESS");

        TransactionResponse transactionResponse = transactionEntityDTOToTransactionResponse(transactionEntityDTO, openBankAccountEntityDTO);

        return createRestApiResponse(HttpStatus.OK, "SUCCESS", transactionResponse);
    }

    /**
     * Save transaction open bank account detail
     *
     * @return saved entity untuk response
     */
    private TransactionOpenBankAccountEntityDTO saveTransactionOpenBankAccount(UUID transactionId, TransactionRequest transactionRequest) {
        TransactionOpenBankAccountEntityDTO openBankAccountEntityDTO = new TransactionOpenBankAccountEntityDTO();
        openBankAccountEntityDTO.setTransactionOpenBankAccountEntityDTOId(UUID.randomUUID());
        openBankAccountEntityDTO.setTransactionOpenBankAccountEntityDTOTransactionId(transactionId);
        openBankAccountEntityDTO.setTransactionOpenBankAccountEntityDTOCustomerName(transactionRequest.getTransactionEntityDTOCustomerName());
        openBankAccountEntityDTO.setTransactionOpenBankAccountEntityDTOCustomerIdentityNumber(transactionRequest.getTransactionEntityDTOCustomerIdentityNumber());
        openBankAccountEntityDTO.setTransactionOpenBankAccountEntityDTOCustomerPhoneNumber(transactionRequest.getTransactionEntityDTOCustomerPhoneNumber());
        openBankAccountEntityDTO.setTransactionOpenBankAccountEntityDTOCustomerEmail(transactionRequest.getTransactionEntityDTOCustomerEmail());
        openBankAccountEntityDTO.setTransactionOpenBankAccountEntityDTOCustomerAddress(transactionRequest.getTransactionEntityDTOCustomerAddress());

        return tTransactionOpenBankAccountRepositories.save(openBankAccountEntityDTO);
    }

    /**
     * Save or update user balance
     *
     * @return userBalanceId (UUID) untuk digunakan sebagai foreign key
     */
    private UUID saveUserBalance(UUID userId, UUID productId) {
        BigDecimal commissionValue = getCommissionValue(productId);

        UserBalanceEntityDTO userBalanceEntityDTO = tUserBalanceRepositories.findByUserBalanceEntityDTOUserId(userId)
                .orElseGet(() -> {
                    UserBalanceEntityDTO newBalance = new UserBalanceEntityDTO();
                    newBalance.setUserBalanceEntityDTOUserBalanceId(UUID.randomUUID());
                    newBalance.setUserBalanceEntityDTOUserId(userId);
                    newBalance.setUserBalanceEntityDTOBalanceAmount(BigDecimal.ZERO);
                    newBalance.setUserBalanceEntityDTOBalanceLastUpdate(LocalDateTime.now());
                    return newBalance;
                });

        BigDecimal currentBalance = userBalanceEntityDTO.getUserBalanceEntityDTOBalanceAmount();
        userBalanceEntityDTO.setUserBalanceEntityDTOBalanceAmount(currentBalance.add(commissionValue));
        userBalanceEntityDTO.setUserBalanceEntityDTOBalanceLastUpdate(LocalDateTime.now());

        UserBalanceEntityDTO savedBalance = tUserBalanceRepositories.save(userBalanceEntityDTO);
        return savedBalance.getUserBalanceEntityDTOUserBalanceId();
    }

    /**
     * Save user balance historical (audit trail)
     *
     * @param userBalanceId UUID dari M_USER_BALANCE (foreign key)
     */
    private void saveUserBalanceHistorical(UUID userBalanceId, UUID transactionId, UUID productId) {
        BigDecimal commissionValue = getCommissionValue(productId);

        UserBalanceHistoricalEntityDTO userBalanceHistoricalEntityDTO = new UserBalanceHistoricalEntityDTO();
        userBalanceHistoricalEntityDTO.setUserBalanceHistoricalEntityDTOId(UUID.randomUUID());
        userBalanceHistoricalEntityDTO.setUserBalanceHistoricalEntityDTOUserBalanceId(userBalanceId);
        userBalanceHistoricalEntityDTO.setUserBalanceHistoricalEntityDTOTransactionId(transactionId);
        userBalanceHistoricalEntityDTO.setUserBalanceHistoricalEntityDTOAmount(commissionValue);
        userBalanceHistoricalEntityDTO.setUserBalanceHistoricalEntityDTOCreatedDate(LocalDateTime.now());

        tUsersBalanceHistoricalRepositories.save(userBalanceHistoricalEntityDTO);
    }
}
