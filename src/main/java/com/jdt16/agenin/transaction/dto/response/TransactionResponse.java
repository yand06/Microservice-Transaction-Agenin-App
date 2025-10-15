package com.jdt16.agenin.transaction.dto.response;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class TransactionResponse {
    @JsonProperty("transactionId")
    private UUID transactionEntityDTOId;

    @JsonProperty("transactionCode")
    private String transactionEntityDTOCode;

    @JsonProperty("userId")
    private UUID transactionEntityDTOUserId;

    @JsonProperty("productId")
    private UUID transactionEntityDTOProductId;

    @JsonProperty("transactionQuantity")
    private BigDecimal transactionEntityDTOQuantity;

    @JsonProperty("transactionTotalAmount")
    private BigDecimal transactionEntityDTOTotalAmount;

    @JsonProperty("transactionDate")
    private LocalDateTime transactionEntityDTODate;

    @JsonProperty("transactionStatus")
    private String transactionEntityDTOStatus;
}
