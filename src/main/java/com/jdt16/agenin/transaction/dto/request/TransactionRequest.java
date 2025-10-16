package com.jdt16.agenin.transaction.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransactionRequest {
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
}
