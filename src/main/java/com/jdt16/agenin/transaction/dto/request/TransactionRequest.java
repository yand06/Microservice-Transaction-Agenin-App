package com.jdt16.agenin.transaction.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransactionRequest {
    @NotNull
    @Positive
    @JsonProperty("transactionQuantity")
    private BigDecimal transactionEntityDTOQuantity;

    @NotNull
    @Positive
    @JsonProperty("transactionTotalAmount")
    private BigDecimal transactionEntityDTOTotalAmount;
}
