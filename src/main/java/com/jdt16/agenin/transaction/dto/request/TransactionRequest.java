package com.jdt16.agenin.transaction.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransactionRequest {
    @NotBlank
    @JsonProperty("transactionQuantity")
    private BigDecimal transactionEntityDTOQuantity;

    @NotBlank
    @JsonProperty("transactionTotalAmount")
    private BigDecimal transactionEntityDTOTotalAmount;
}
