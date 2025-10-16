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

    @JsonProperty("productCode")
    private String transactionEntityDTOProductCode;

    @JsonProperty("customerName")
    private String transactionEntityDTOCustomerName;

    @JsonProperty("customerIdentityNumber")
    private String transactionEntityDTOCustomerIdentityNumber;

    @JsonProperty("customerPhoneNumber")
    private String transactionEntityDTOCustomerPhoneNumber;

    @JsonProperty("customerEmail")
    private String transactionEntityDTOCustomerEmail;

    @JsonProperty("customerAddress")
    private String transactionEntityDTOCustomerAddress;

    @JsonProperty("transactionDate")
    private LocalDateTime transactionEntityDTODate;

    @JsonProperty("transactionStatus")
    private String transactionEntityDTOStatus;
}
