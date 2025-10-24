package com.jdt16.agenin.transaction.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class OpenBankAccountCustomerInfoResponse {
    @JsonProperty("transactionId")
    private UUID openBankAccountCustomerInfoEntityDTOTransactionId;

    @JsonProperty("productName")
    private String openBankAccountCustomerInfoEntityDTOProductName;

    @JsonProperty("customerName")
    private String openBankAccountCustomerInfoEntityDTOCustomerName;

    @JsonProperty("customerPhoneNumber")
    private String openBankAccountCustomerInfoEntityDTOPhoneNumber;

    @JsonProperty("customerEmail")
    private String openBankAccountCustomerInfoEntityDTOEmail;

    @JsonProperty("customerAddress")
    private String openBankAccountCustomerInfoEntityDTOAddress;

    @JsonProperty("transactionDate")
    private LocalDateTime openBankAccountCustomerInfoEntityDTOTransactionDate;

    @JsonProperty("transactionStatus")
    private String openBankAccountCustomerInfoEntityDTOTransactionStatus;
}
